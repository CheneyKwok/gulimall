package com.guo.gulimall.cart.controller;

import com.guo.gulimall.cart.service.CartService;
import com.guo.gulimall.cart.vo.Cart;
import com.guo.gulimall.cart.vo.CartItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
public class CartController {


    @Autowired
    private CartService cartService;

    @GetMapping("/cartList")
    public String cartListPage(Model model) {
        Cart cart = cartService.getCart();
        model.addAttribute("cart", cart);
        return "cartList";
    }


    @PostMapping("/addToCart")
    public String addToCart(Long skuId, Integer num, RedirectAttributes ra) {
        cartService.addToCart(skuId, num);
        ra.addAttribute("skuId", skuId);
        return "redirect:Http://cart.gulimall.com/addToCartSuccessPage";
    }

    @GetMapping("/addToCartSuccessPage")
    public String addToCartSuccessPage(@RequestParam("skuId") Long skuId, Model model) {
        CartItem cartItem = cartService.getCartItem(skuId);
        model.addAttribute("item", cartItem);
        return "success";
    }
}
