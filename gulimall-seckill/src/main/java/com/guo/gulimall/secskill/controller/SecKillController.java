package com.guo.gulimall.secskill.controller;

import com.guo.common.utils.R;
import com.guo.gulimall.secskill.service.SecKillService;
import com.guo.gulimall.secskill.to.SecKillSkuRedisTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class SecKillController {


    @Autowired
    SecKillService secKillService;


    @GetMapping("/currentSecKillSkus")
    @ResponseBody
    public R getCurrentSecKill() {

        List<SecKillSkuRedisTO> list =  secKillService.getCurrentSecKill();
        return R.ok().setData(list);
    }


    @GetMapping("/sku/secKill/{skuId}")
    @ResponseBody
    public R getSkuSecKillInfo(@PathVariable("skuId") Long skuId) {
        SecKillSkuRedisTO to = secKillService.getSkuSecKillInfo(skuId);
        return R.ok().setData(to);
    }

    @GetMapping("/kill")
    public String kill(@RequestParam("killId") String killId,
                       @RequestParam("key")String key,
                       @RequestParam("num")Integer num,
                       Model model) {
        String orderSn= null;
        try {
            orderSn = secKillService.kill(killId, key, num);
            model.addAttribute("orderSn", orderSn);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "success";
    }
}
