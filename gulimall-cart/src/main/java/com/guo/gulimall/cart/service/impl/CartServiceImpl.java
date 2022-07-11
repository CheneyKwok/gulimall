package com.guo.gulimall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.guo.common.constant.CartConstant;
import com.guo.common.utils.R;
import com.guo.gulimall.cart.feign.ProductFeignService;
import com.guo.gulimall.cart.interceptor.CartInterceptor;
import com.guo.gulimall.cart.service.CartService;
import com.guo.gulimall.cart.vo.Cart;
import com.guo.gulimall.cart.vo.CartItem;
import com.guo.gulimall.cart.vo.SkuInfoVO;
import com.guo.gulimall.cart.vo.UserInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private ThreadPoolExecutor executor;


    @Override
    public CartItem addToCart(Long skuId, Integer num) {

        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String res = (String) cartOps.get(skuId.toString());
        if (StringUtils.hasText(res)) {
            // 已存在
            CartItem cartItem = JSON.parseObject(res, CartItem.class);
            cartItem.setCount(cartItem.getCount() + num);
            cartOps.put(skuId.toString(), JSON.toJSONString(cartItem));
            return cartItem;
        } else {
            CartItem cartItem = new CartItem();
            // 远程查询 skuInfo
            CompletableFuture<Void> skuInfoFuture = CompletableFuture.runAsync(() -> {
                R r = productFeignService.getSkuInfo(skuId);
                SkuInfoVO skuInfo = r.getData("skuInfo", new TypeReference<SkuInfoVO>() {
                });
                cartItem.setSkuId(skuId);
                cartItem.setTitle(skuInfo.getSkuTitle());
                cartItem.setPrice(skuInfo.getPrice());
                cartItem.setImage(skuInfo.getSkuDefaultImg());
                cartItem.setCount(num);
                cartItem.setCheck(true);
            }, executor);
            // 远程查询 sku 组合信息
            CompletableFuture<Void> skuSaleAttrFuture = CompletableFuture.runAsync(() -> {
                List<String> skuSaleAttrValues = productFeignService.getSkuSaleAttrValues(skuId);
                cartItem.setSkuAttr(skuSaleAttrValues);
            }, executor);

            // 等待异步任务完成
            try {
                CompletableFuture.allOf(skuInfoFuture, skuSaleAttrFuture).get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            // 放入 redis
            cartOps.put(skuId.toString(), JSON.toJSONString(cartItem));
            return cartItem;
        }
    }

    @Override
    public CartItem getCartItem(Long skuId) {
        Object o = getCartOps().get(skuId.toString());
        if (o == null) {
            return null;
        }
        return JSON.parseObject(o.toString(), CartItem.class);
    }

    @Override
    public Cart getCart() {

        Cart cart = new Cart();
        UserInfoVO userInfoVO = CartInterceptor.threadLocal.get();
        String tempCartKey = CartConstant.CART_PREFIX + userInfoVO.getUserKey();
        List<CartItem> tempCartItems = getCartItems(tempCartKey);
        if (userInfoVO.getUserId() == null) {
            cart.setItems(tempCartItems);
        } else {
            if (tempCartItems != null) {
                tempCartItems.forEach(e -> addToCart(e.getSkuId(), e.getCount()));
            }
            String cartKey = CartConstant.CART_PREFIX + userInfoVO.getUserId();
            List<CartItem> cartItems = getCartItems(cartKey);
            cart.setItems(cartItems);
            clearCart(tempCartKey);
        }
        return cart;
    }

    @Override
    public void clearCart(String cartKey) {
        redisTemplate.delete(cartKey);
    }

    @Override
    public void checkCart(Long skuId, Integer isChecked) {
        BoundHashOperations<String, Object, Object> ops = getCartOps();
        String cartJson = (String) ops.get(skuId.toString());
        if (cartJson != null) {
            CartItem cartItem = JSON.parseObject(cartJson, CartItem.class);
            cartItem.setCheck(isChecked == 1);
            ops.put(skuId.toString(), JSON.toJSONString(cartItem));
        }
    }

    @Override
    public void changeItemCount(Long skuId, Integer num) {
        BoundHashOperations<String, Object, Object> ops = getCartOps();
        String cartJson = (String) ops.get(skuId.toString());
        if (cartJson != null) {
            CartItem cartItem = JSON.parseObject(cartJson, CartItem.class);
            cartItem.setCount(num);
            ops.put(skuId.toString(), JSON.toJSONString(cartItem));
        }
    }

    @Override
    public void deleteItem(Long skuId) {
        BoundHashOperations<String, Object, Object> ops = getCartOps();
        ops.delete(skuId.toString());
    }

    @Override
    public List<CartItem> getCurrentUserCartItems() {

        UserInfoVO userInfoVO = CartInterceptor.threadLocal.get();
        if (userInfoVO.getUserId() == null) {
            return null;
        }
        String cartKey = CartConstant.CART_PREFIX + userInfoVO.getUserId();
        List<CartItem> cartItems = getCartItems(cartKey);
        if (cartItems == null) {
            return null;
        }
        return cartItems
                .stream()
                .filter(CartItem::getCheck)
                .peek(e -> {

                })
                .collect(Collectors.toList());
    }

    private BoundHashOperations<String, Object, Object> getCartOps() {
        UserInfoVO userInfoVO = CartInterceptor.threadLocal.get();
        String cartKey = CartConstant.CART_PREFIX;
        cartKey += userInfoVO.getUserId() == null ? userInfoVO.getUserKey() : userInfoVO.getUserId();
        return redisTemplate.boundHashOps(cartKey);
    }

    private List<CartItem> getCartItems(String cartKey) {
        BoundHashOperations<String, Object, Object> cartOps = redisTemplate.boundHashOps(cartKey);
        List<Object> values = cartOps.values();
        if (!CollectionUtils.isEmpty(values)) {
            return values
                    .stream()
                    .map(e -> JSON.parseObject(e.toString(), CartItem.class))
                    .collect(Collectors.toList());
        }
        return null;
    }
}
