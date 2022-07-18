package com.guo.gulimall.secskill.controller;

import com.guo.common.utils.R;
import com.guo.gulimall.secskill.service.SecKillService;
import com.guo.gulimall.secskill.to.SecKillSkuRedisTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SecKillController {


    @Autowired
    SecKillService secKillService;


    @GetMapping("/currentSecKillSkus")
    public R getCurrentSecKill() {

        List<SecKillSkuRedisTO> list =  secKillService.getCurrentSecKill();
        return R.ok().setData(list);
    }
}
