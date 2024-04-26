package com.msb.mall.order.feign;

import com.msb.mall.order.vo.MemberAddressVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@FeignClient(name = "mall-member")
public interface MemberFeignService {

    @GetMapping("/member/umsmemberreceiveaddress/{memberId}/address")
    List<MemberAddressVo> getAddress(@PathVariable("memberId") Long memberId);

    @RequestMapping("/member/umsmemberreceiveaddress/getAddressById/{id}")
    MemberAddressVo getAddressById(@PathVariable("id") Long id);

}
