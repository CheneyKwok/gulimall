package com.guo.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guo.common.utils.PageUtils;
import com.guo.common.utils.Query;
import com.guo.common.utils.R;
import com.guo.gulimall.ware.dao.WareInfoDao;
import com.guo.gulimall.ware.entity.WareInfoEntity;
import com.guo.gulimall.ware.feign.MemberFeignService;
import com.guo.gulimall.ware.service.WareInfoService;
import com.guo.gulimall.ware.vo.MemberAddressVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Map;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {


    @Autowired
    MemberFeignService memberFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareInfoEntity> queryWrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.eq("id", key)
                    .or().like("name", key)
                    .or().like("areacode", key);
        }
        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public BigDecimal getFare(Long addressId) {

        R r = memberFeignService.infoAddress(addressId);
        if (r != null) {
            MemberAddressVO address = r.getData("memberReceiveAddress", new TypeReference<MemberAddressVO>() {
            });
            return new BigDecimal(address.getPhone().substring(0, 2));
        }
        return null;
    }

}