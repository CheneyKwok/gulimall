package com.guo.gulimall.order.web;


import com.guo.common.excepiton.NoStockException;
import com.guo.gulimall.order.service.OrderService;
import com.guo.gulimall.order.vo.OrderConfirmVO;
import com.guo.gulimall.order.vo.OrderSubmitVO;
import com.guo.gulimall.order.vo.SubmitOrderResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Slf4j
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
    public String submitOrder(OrderSubmitVO orderSubmitVO, Model model, RedirectAttributes attributes) {

        try {
            SubmitOrderResponseVO responseVO = orderService.submitOrder(orderSubmitVO);
            // 下单成功跳转支付页
            if (responseVO.getCode() == 0) {
                model.addAttribute("submitOrderResp", responseVO);
                return "pay";
            }
            // 下单失败回到订单页重新确认订单信息
            String msg = "下单失败";
            switch (responseVO.getCode()) {
                case 1: msg+="订单信息过期，请刷新再次提交";
                    break;
                case 2:
                    msg += "库存锁定失败，商品库存不足";
                    break;
            }
            attributes.addFlashAttribute("msg", msg);
            return "redirect:http://order.gulimall.com/toTrade";
        } catch (Exception e) {
            log.error(e.getMessage());
            if (e instanceof NoStockException) {
                String msg = e.getMessage();
                attributes.addFlashAttribute("msg", msg);
            }
            return "redirect:http://order.gulimall.com/toTrade";
        }
    }
}
