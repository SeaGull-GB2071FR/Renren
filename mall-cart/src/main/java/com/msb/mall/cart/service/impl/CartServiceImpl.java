package com.msb.mall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.msb.common.constant.CartConstant;
import com.msb.common.utils.R;
import com.msb.common.vo.MemberVO;
import com.msb.mall.cart.feign.ProductFeignService;
import com.msb.mall.cart.interceptor.AuthInterceptor;
import com.msb.mall.cart.service.CartService;
import com.msb.mall.cart.vo.Cart;
import com.msb.mall.cart.vo.CartItem;
import com.msb.mall.cart.vo.SkuInfoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    ThreadPoolExecutor executor;


    //todo

    /**
     * BoundHashOperations 是 Spring Data Redis 中的一种数据操作类，
     * 用于操作 Redis 中的 Hash 类型数据结构。
     * Hash 类型数据结构在 Redis 中是一个键值对的集合，
     * 其中每个键都对应一个值，每个键值对被称为一个 field-value 对。
     * 在 Spring Data Redis 中，BoundHashOperations 提供了一组方法来对 Hash 类型数据进行操作，
     * 包括设置和获取值、删除字段、检查字段是否存在等。
     */
    @Override
    public CartItem addCart(Long skuId, Integer num) throws Exception {
        BoundHashOperations hashOperations = getCartKeyOperation();
        // 如果Redis存储在商品的信息，那么我们只需要修改商品的数量就可以了
        Object o = hashOperations.get(skuId.toString());
        if (o != null) {
            // 说明已经存在了这个商品那么修改商品的数量即可
            String json = (String) o;
            CartItem item = JSON.parseObject(json, CartItem.class);
            item.setCount(item.getCount() + num);
            hashOperations.put(skuId.toString(), JSON.toJSONString(item));
            return item;
        }

        CartItem item = new CartItem();
        CompletableFuture future1 = CompletableFuture.runAsync(() -> {
            // 1.远程调用获取 商品信息
            R r = productFeignService.info(skuId);
            String skuInfoJSON = (String) r.get("skuInfoJSON");
            SkuInfoVo vo = JSON.parseObject(skuInfoJSON, SkuInfoVo.class);
            item.setCheck(true);
            item.setCount(num);
            item.setPrice(vo.getPrice());
            item.setImage(vo.getSkuDefaultImg());
            item.setSkuId(skuId);
            item.setTitle(vo.getSkuTitle());
            item.setSpuId(vo.getSpuId());
        }, executor);

        CompletableFuture future2 = CompletableFuture.runAsync(() -> {
            // 2.获取商品的销售属性
            List<String> skuSaleAttrs = productFeignService.getSkuSaleAttrs(skuId);
            item.setSkuAttr(skuSaleAttrs);
        }, executor);

        CompletableFuture.allOf(future1, future2).get();
        // 3.把数据存储在Redis中
        String json = JSON.toJSONString(item);
        hashOperations.put(skuId.toString(), json);

        return item;
    }

    /**
     * 查询出当前登录用户的所有的购物车信息
     * @return
     */
    @Override
    public Cart getCartList() {
        BoundHashOperations<String, Object, Object> operations = getCartKeyOperation();
        Set<Object> keys = operations.keys();
        Cart cart = new Cart();
        List<CartItem> list = new ArrayList<>();
        for (Object k : keys) {
            String key = (String) k;
            Object o = operations.get(key);
            String json = (String) o;
            CartItem item = JSON.parseObject(json, CartItem.class);
            list.add(item);
        }
        cart.setItems(list);
        return cart;
    }


    /**
     * 获取当前登录用户选中的商品信息 购物车中
     * @return
     */
    @Override
    public List<CartItem> getUserCartItems() {
        BoundHashOperations<String, Object, Object> operations = getCartKeyOperation();
        List<Object> values = operations.values();
        List<CartItem> list = values.stream().map(item -> {
            String json = (String) item;
            CartItem cartItem = JSON.parseObject(json, CartItem.class);
            return cartItem;
        }).filter(item -> {
            return item.isCheck();
        }).collect(Collectors.toList());
        return list;
    }

    private BoundHashOperations<String, Object, Object> getCartKeyOperation() {
// hash key: cart:1   skuId:cartItem
        MemberVO memberVO = AuthInterceptor.threadLocal.get();
        String cartKey = CartConstant.CART_PREFIX + memberVO.getId();
        BoundHashOperations<String, Object, Object> hashOperations = redisTemplate.boundHashOps(cartKey);
        return hashOperations;

    }
}
