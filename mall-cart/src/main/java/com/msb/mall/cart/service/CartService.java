package com.msb.mall.cart.service;

import com.msb.mall.cart.vo.Cart;
import com.msb.mall.cart.vo.CartItem;
import org.springframework.stereotype.Service;

import java.util.List;

public interface CartService {

    CartItem addCart(Long skuId, Integer num) throws Exception;

    Cart getCartList();

    List<CartItem> getUserCartItems();
}
