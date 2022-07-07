package com.guo.gulimall.cart.service;

import com.guo.gulimall.cart.vo.Cart;
import com.guo.gulimall.cart.vo.CartItem;

public interface CartService {
    CartItem addToCart(Long skuId, Integer num);

    CartItem getCartItem(Long skuId);

    Cart getCart();

    void clearCart(String cartKey);
}
