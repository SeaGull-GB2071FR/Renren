package com.msb.mall.order.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.msb.common.utils.PageUtils;
import com.msb.common.utils.Query;

import com.msb.mall.order.dao.OmsPaymentInfoDao;
import com.msb.mall.order.entity.OmsPaymentInfoEntity;
import com.msb.mall.order.service.OmsPaymentInfoService;


@Service("omsPaymentInfoService")
public class OmsPaymentInfoServiceImpl extends ServiceImpl<OmsPaymentInfoDao, OmsPaymentInfoEntity> implements OmsPaymentInfoService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OmsPaymentInfoEntity> page = this.page(
                new Query<OmsPaymentInfoEntity>().getPage(params),
                new QueryWrapper<OmsPaymentInfoEntity>()
        );

        return new PageUtils(page);
    }

}