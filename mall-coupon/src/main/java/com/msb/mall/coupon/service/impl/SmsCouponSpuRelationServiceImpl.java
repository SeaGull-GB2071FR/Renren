package com.msb.mall.coupon.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.msb.common.utils.PageUtils;
import com.msb.common.utils.Query;

import com.msb.mall.coupon.dao.SmsCouponSpuRelationDao;
import com.msb.mall.coupon.entity.SmsCouponSpuRelationEntity;
import com.msb.mall.coupon.service.SmsCouponSpuRelationService;


@Service("smsCouponSpuRelationService")
public class SmsCouponSpuRelationServiceImpl extends ServiceImpl<SmsCouponSpuRelationDao, SmsCouponSpuRelationEntity> implements SmsCouponSpuRelationService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SmsCouponSpuRelationEntity> page = this.page(
                new Query<SmsCouponSpuRelationEntity>().getPage(params),
                new QueryWrapper<SmsCouponSpuRelationEntity>()
        );

        return new PageUtils(page);
    }

}