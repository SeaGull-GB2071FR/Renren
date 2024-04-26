package com.msb.mall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.msb.common.utils.PageUtils;
import com.msb.mall.member.entity.UmsMemberEntity;
import com.msb.mall.member.exception.PhoneExsitExecption;
import com.msb.mall.member.exception.UsernameExsitException;
import com.msb.mall.member.vo.MemberLoginVO;
import com.msb.mall.member.vo.MemberReigerVO;
import com.msb.mall.member.vo.SocialUser;

import java.util.Map;

/**
 * 会员
 *
 * @author dpb
 * @email dengpbs@163.com
 * @date 2023-10-30 17:30:45
 */
public interface UmsMemberService extends IService<UmsMemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void register(MemberReigerVO vo) throws PhoneExsitExecption, UsernameExsitException;

    UmsMemberEntity login(MemberLoginVO vo);

    UmsMemberEntity login(SocialUser vo);
}

