package com.msb.mall.coupon.service.impl;

import com.msb.mall.coupon.entity.SeckillSkuRelationEntity;
import com.msb.mall.coupon.service.SeckillSkuRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.msb.common.utils.PageUtils;
import com.msb.common.utils.Query;

import com.msb.mall.coupon.dao.SeckillSessionDao;
import com.msb.mall.coupon.entity.SeckillSessionEntity;
import com.msb.mall.coupon.service.SeckillSessionService;

import javax.management.relation.RelationService;


@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {

    @Autowired
    SeckillSkuRelationService relationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                new QueryWrapper<SeckillSessionEntity>()
        );

        return new PageUtils(page);
    }

    //    未来三天的秒杀活动的商品
    @Override
    public List<SeckillSessionEntity> getLates3DaysSession() {
        // 计算未来3天的时间
        List<SeckillSessionEntity> list
                = this.list(new QueryWrapper<SeckillSessionEntity>().between("start_time", startTime(), endTime()));
        // 根据对应的sessionId活动编号查询出对应的活动商品信息

        return list.stream().map(item -> {
            List<SeckillSkuRelationEntity> relationEntities
                    = relationService.list(new QueryWrapper<SeckillSkuRelationEntity>().eq("promotion_session_id", item.getId()));

            item.setRelationEntities(relationEntities);

            return item;
        }).collect(Collectors.toList());

    }


    private String startTime() {
        LocalDate now = LocalDate.now();
        LocalDate startDay = now.plusDays(0);
        LocalTime min = LocalTime.MIN;
        LocalDateTime start = LocalDateTime.of(startDay, min);
        return start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private String endTime() {
        LocalDate now = LocalDate.now();
        LocalDate endDay = now.plusDays(2);
        LocalTime max = LocalTime.MAX;
        LocalDateTime end = LocalDateTime.of(endDay, max);
        return end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}