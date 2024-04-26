package com.msb.mall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.msb.common.utils.PageUtils;
import com.msb.mall.member.entity.UmsMemberLevelEntity;

import java.util.Map;

/**
 * 会员等级
 *
 * @author dpb
 * @email dengpbs@163.com
 * @date 2023-10-30 17:30:45
 */
public interface UmsMemberLevelService extends IService<UmsMemberLevelEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 查询默认的会员等级
     *
     * @return
     */
    UmsMemberLevelEntity queryMemberLevelDefault() ;
}

