package com.msb.mall.order.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.msb.common.constant.OrderConstant;
import com.msb.common.exception.NoStockExecption;
import com.msb.common.utils.R;
import com.msb.common.vo.MemberVO;
import com.msb.mall.order.dto.OrderCreateTO;
import com.msb.mall.order.entity.OmsOrderItemEntity;
import com.msb.mall.order.feign.MemberFeignService;
import com.msb.mall.order.feign.CartFeignService;
import com.msb.mall.order.feign.ProductFeignService;
import com.msb.mall.order.feign.WareFeignService;
import com.msb.mall.order.interceptor.AuthInterceptor;
import com.msb.mall.order.service.OmsOrderItemService;
import com.msb.mall.order.utils.OrderMsgProducer;
import com.msb.mall.order.vo.*;
import com.msb.mall.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.msb.common.utils.PageUtils;
import com.msb.common.utils.Query;

import com.msb.mall.order.dao.OmsOrderDao;
import com.msb.mall.order.entity.OmsOrderEntity;
import com.msb.mall.order.service.OmsOrderService;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;


@Service("omsOrderService")
public class OmsOrderServiceImpl extends ServiceImpl<OmsOrderDao, OmsOrderEntity> implements OmsOrderService {

    @Autowired
    private CartFeignService cartFeignService;

    @Autowired
    ThreadPoolExecutor executor;

    @Autowired
    private MemberFeignService memberFeignService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private WareFeignService wareFeignService;

    @Autowired
    private OmsOrderService orderService;

    @Autowired
    private OmsOrderItemService orderItemService;

    @Autowired
    OrderMsgProducer orderMsgProducer;



    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OmsOrderEntity> page = this.page(
                new Query<OmsOrderEntity>().getPage(params),
                new QueryWrapper<OmsOrderEntity>()
        );

        return new PageUtils(page);
    }

    // 确认订单
    @Override
    public OrderConfirmVo confirmOrder() {
        OrderConfirmVo vo = new OrderConfirmVo();
        MemberVO memberVO = (MemberVO) AuthInterceptor.threadLocal.get();

        // 获取到 RequestContextHolder 的相关信息
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        CompletableFuture<Void> future1 = CompletableFuture.runAsync(() -> {
            // 同步主线程中的 RequestContextHolder
            //在Feign调用远程服务的时候会出现请求Header丢失的问题。
            //首先我们创建 `RequestInterceptor`的实现来绑定Header信息，同时在异步处理的时候我们需要从主线程中获取Request信息，然后绑定在子线程中。
            RequestContextHolder.setRequestAttributes(requestAttributes);
            // 1.查询当前登录用户对应的会员的地址信息
            Long id = memberVO.getId();
            List<MemberAddressVo> addresses = memberFeignService.getAddress(id);
            vo.setAddress(addresses);
        }, executor);

        CompletableFuture<Void> future2 = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            // 2.查询购物车中选中的商品信息
            List<OrderItemVo> userCartItems = cartFeignService.getUserCartItems();
            vo.setItems(userCartItems);
        }, executor);

        try {
            CompletableFuture.allOf(future1, future2).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 3.计算订单的总金额和需要支付的总金额 VO自动计算

        // 4.生成防重的token
        String token = UUID.randomUUID().toString().replaceAll("_", "");
        // 我们需要把这个token信息存储在redis中
        // order:token:用户编号
        redisTemplate.opsForValue().set(OrderConstant.ORDER_TOKEN_PREFIX + ":" + memberVO.getId(), token);
        // 然后我们需要将这个token绑定在响应的数据对象中
        vo.setOrderToken(token);
        return vo;
    }

    @Override
    public OrderResponseVO submitOrder(OrderSubmitVO vo) {

        // 需要返回响应的对象
        OrderResponseVO responseVO = new OrderResponseVO();
        // 获取当前登录的用户信息;
        MemberVO memberVO = (MemberVO) AuthInterceptor.threadLocal.get();
        // 1.验证是否重复提交  保证Redis中的token 的查询和删除是一个原子性操作
        String key = OrderConstant.ORDER_TOKEN_PREFIX + ":" + memberVO.getId();
        String script = "if redis.call('get',KEYS[1])==ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";

        Long result = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class)
                , Arrays.asList(key)
                , vo.getOrderToken());
        if (result == 0) {
            // 表示验证失败 说明是重复提交
            responseVO.setCode(1);
            return responseVO;
        }

        // 2.创建订单和订单项信息
        OrderCreateTO orderCreateTO = createOrder(vo);
        responseVO.setOrderEntity(orderCreateTO.getOrderEntity());
        // 3.保存订单信息
        saveOrder(orderCreateTO);

        // 4.锁定库存信息
        // 订单号  SKU_ID  SKU_NAME 商品数量
        // 封装 WareSkuLockVO 对象
        lockWareSkuStock(responseVO, orderCreateTO);
//        // 5.同步更新用户的会员积分
//        // int i = 1 / 0;
//        // 订单成功后需要给 消息中间件发送延迟30分钟的关单消息
//        orderMsgProducer.sendOrderMessage(orderCreateTO.getOrderEntity().getOrderSn());
        return responseVO;
    }

    @Override
    public OrderCreateTO createOrder(OrderSubmitVO vo) {
        OrderCreateTO createTO = new OrderCreateTO();
        // 创建订单
        OmsOrderEntity orderEntity = buildOrder(vo);
        createTO.setOrderEntity(orderEntity);
        // 创建OrderItemEntity 订单项
        List<OmsOrderItemEntity> omsOrderItemEntities = buildOrderItems(orderEntity.getOrderSn());
        createTO.setOrderItemEntitys(omsOrderItemEntities);
        return createTO;
    }

    private OmsOrderItemEntity buildOrderItem(OrderItemVo userCartItem, OrderItemSpuInfoVO spuInfo) {

        OmsOrderItemEntity entity = new OmsOrderItemEntity();
        // SKU信息
        entity.setSkuId(userCartItem.getSkuId());
        entity.setSkuName(userCartItem.getTitle());
        entity.setSkuPic(userCartItem.getImage());
        entity.setSkuQuantity(userCartItem.getCount());
        // 商品的销售属性
        List<String> skuAttr = userCartItem.getSkuAttr();
        String skuAttrStr = StringUtils.collectionToDelimitedString(skuAttr, ";");
        entity.setSkuAttrsVals(skuAttrStr);
        // SPU信息
        entity.setSpuId(spuInfo.getId());
        entity.setSpuBrand(spuInfo.getBrandName());
        entity.setCategoryId(spuInfo.getCatalogId());
        entity.setSpuPic(spuInfo.getImg());
        // 优惠信息 忽略
        // 积分信息
        entity.setGiftGrowth(userCartItem.getPrice().intValue());
        entity.setGiftIntegration(userCartItem.getPrice().intValue());
        return entity;
    }

    private List<OmsOrderItemEntity> buildOrderItems(String orderSn) {
        List<OmsOrderItemEntity> omsOrderItemEntities = new ArrayList<>();
        // 获取购物车中的商品信息 选中的
        List<OrderItemVo> userCartItems = cartFeignService.getUserCartItems();
        if (userCartItems != null && userCartItems.size() > 0) {
            // 统一根据SKUID查询出对应的SPU的信息
            List<Long> spuIds = new ArrayList<>();
            for (OrderItemVo orderItemVo : userCartItems) {
                if (!spuIds.contains(orderItemVo.getSpuId())) {
                    spuIds.add(orderItemVo.getSpuId());
                }
            }
            Long[] spuIdsArray = new Long[spuIds.size()];
            spuIdsArray = spuIds.toArray(spuIdsArray);
            System.out.println("---->" + spuIdsArray.length);
            // 远程调用商品服务获取到对应的SPU信息
            List<OrderItemSpuInfoVO> spuInfos = productFeignService.getOrderItemSpuInfoBySpuId(spuIdsArray);
            Map<Long, OrderItemSpuInfoVO> map = spuInfos.stream().collect(Collectors.toMap(OrderItemSpuInfoVO::getId, item -> item));
            for (OrderItemVo userCartItem : userCartItems) {
                // 获取到商品信息对应的 SPU信息
                OrderItemSpuInfoVO spuInfo = map.get(userCartItem.getSpuId());
                OmsOrderItemEntity orderItemEntity = buildOrderItem(userCartItem, spuInfo);
                // 绑定对应的订单编号
                orderItemEntity.setOrderSn(orderSn);
                omsOrderItemEntities.add(orderItemEntity);
            }
        }

        return omsOrderItemEntities;
    }

    private OmsOrderEntity buildOrder(OrderSubmitVO vo) {

        MemberVO memberVO = (MemberVO) AuthInterceptor.threadLocal.get();
        OmsOrderEntity entity = new OmsOrderEntity();

        //mybatis-plus雪花算法增强:idworker
        String orderSn = IdWorker.getTimeId();
        entity.setOrderSn(orderSn);

        entity.setMemberId(memberVO.getId());
        entity.setMemberUsername(memberVO.getUsername());

        // 根据收获地址ID获取收获地址的详细信息
        MemberAddressVo memberAddressVo = memberFeignService.getAddressById(vo.getAddrId());
        entity.setReceiverCity(memberAddressVo.getCity());
        entity.setReceiverDetailAddress(memberAddressVo.getDetailAddress());
        entity.setReceiverName(memberAddressVo.getName());
        entity.setReceiverPhone(memberAddressVo.getPhone());
        entity.setReceiverPostCode(memberAddressVo.getPostCode());
        entity.setReceiverRegion(memberAddressVo.getRegion());
        entity.setReceiverProvince(memberAddressVo.getProvince());
        // 设置订单的状态
        entity.setStatus(OrderConstant.OrderStateEnum.FOR_THE_PAYMENT.getCode());

        return entity;


    }

    /**
     * 生成订单数据
     *
     * @param orderCreateTO
     */
    private void saveOrder(OrderCreateTO orderCreateTO) {
        // 1.订单数据
        OmsOrderEntity orderEntity = orderCreateTO.getOrderEntity();
        orderService.save(orderEntity);
        // 2.订单项数据
        List<OmsOrderItemEntity> orderItemEntities = orderCreateTO.getOrderItemEntitys();
        orderItemService.saveBatch(orderItemEntities);
    }


    /**
     * 锁定库存的方法
     */
    private void lockWareSkuStock(OrderResponseVO responseVO, OrderCreateTO orderCreateTO) throws NoStockExecption {
        WareSkuLockVO wareSkuLockVO = new WareSkuLockVO();
        wareSkuLockVO.setOrderSN(orderCreateTO.getOrderEntity().getOrderSn());
        List<OrderItemVo> orderItemVos = orderCreateTO.getOrderItemEntitys().stream().map(item -> {
            OrderItemVo itemVo = new OrderItemVo();
            itemVo.setSkuId(item.getSkuId());
            itemVo.setTitle(item.getSkuName());
            itemVo.setCount(item.getSkuQuantity());
            return itemVo;
        }).collect(Collectors.toList());
        wareSkuLockVO.setItems(orderItemVos);
        // 远程锁库存的操作
        R r = wareFeignService.orderLockStock(wareSkuLockVO);
        if (r.getCode() == 0) {
            // 表示锁定库存成功
            responseVO.setCode(0); // 表示 创建订单成功
        } else {
            // 表示锁定库存失败
            responseVO.setCode(2); // 表示库存不足，锁定失败
            throw new NoStockExecption(10000L);
        }
    }

}