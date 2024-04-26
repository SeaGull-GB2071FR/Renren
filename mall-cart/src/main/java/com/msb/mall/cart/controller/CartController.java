package com.msb.mall.cart.controller;

import com.msb.mall.cart.service.CartService;
import com.msb.mall.cart.vo.Cart;
import com.msb.mall.cart.vo.CartItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping("/cartList")
    public String queryCartList(Model model) {
        Cart cart = cartService.getCartList();
        model.addAttribute("cart", cart);
        return "cartList";
    }


    @GetMapping("getUserCartItems")
    public List<CartItem> getCartItems() {
        return cartService.getUserCartItems();
    }

}
