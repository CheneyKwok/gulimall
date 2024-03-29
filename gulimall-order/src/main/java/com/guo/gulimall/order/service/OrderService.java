package com.guo.gulimall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.guo.common.to.SecKillOrderTO;
import com.guo.common.utils.PageUtils;
import com.guo.gulimall.order.entity.OrderEntity;
import com.guo.gulimall.order.vo.*;

import java.util.Map;

/**
 * 订单
 *
 * @author guozhicheng
 * @email guozhicheng@gmail.com
 * @date 2020-12-26 17:21:34
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    OrderConfirmVO confirmOrder();

    SubmitOrderResponseVO submitOrder(OrderSubmitVO orderSubmitVO);

    void closeOrder(OrderEntity entity);

    PayVo getOrderPay(String orderSn);

    PageUtils queryPageWithItem(Map<String, Object> params);

    String handlePayResult(PayAsyncVo payAsyncVo);

    void createSecKillOrder(SecKillOrderTO secKillOrderTO);
}

