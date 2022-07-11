package com.guo.gulimall.order.web;


import com.guo.gulimall.order.service.OrderService;
import com.guo.gulimall.order.vo.OrderConfirmVO;
import com.guo.gulimall.order.vo.OrderSubmitVO;
import com.guo.gulimall.order.vo.SubmitOrderResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class OrderWebController {

    @Autowired
    OrderService orderService;

    @GetMapping("/toTrade")
    public String toTrade(Model model) {

        OrderConfirmVO confirmVO = orderService.confirmOrder();

        model.addAttribute("orderCofirm", confirmVO);
        return "confirm";
    }


    /**
     * 订单提交
     *
     * 前端无需传递购物车参数，服务端自己获取
     */
    @PostMapping("/submitOrder")
    public String submitOrder(@RequestBody OrderSubmitVO orderSubmitVO) {


        SubmitOrderResponseVO responseVO = orderService.submitOrder(orderSubmitVO);
        // 下单成功跳转支付页
        if (responseVO.getCode() == 0) {
            return "pay";
        }
        // 下单失败回到订单页重新确认订单信息
        return "redirect://http://order.gulimall.com/toTrade";
    }
}
