package com.msb.mall.auth.feign;

import com.msb.common.utils.R;
import com.msb.mall.auth.vo.LoginVo;
import com.msb.mall.auth.vo.SocialUser;
import com.msb.mall.auth.vo.UserRegisterVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("mall-member")
public interface MemberFeignService {

    @PostMapping("/member/umsmember/register")
    public R register(@RequestBody UserRegisterVo vo) ;

    @RequestMapping("/member/umsmember/login")
    public R login(@RequestBody LoginVo vo);

    @RequestMapping("/member/umsmember/oauth2/login")
    public R socialLogin(@RequestBody SocialUser vo);
}
