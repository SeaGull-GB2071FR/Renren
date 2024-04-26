package com.msb.mall.member.controller;

import java.util.Arrays;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.msb.common.exception.BizCodeEnume;
import com.msb.mall.member.exception.PhoneExsitExecption;
import com.msb.mall.member.exception.UsernameExsitException;
import com.msb.mall.member.vo.MemberLoginVO;
import com.msb.mall.member.vo.MemberReigerVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.msb.mall.member.entity.UmsMemberEntity;
import com.msb.mall.member.service.UmsMemberService;
import com.msb.common.utils.PageUtils;
import com.msb.common.utils.R;



/**
 * 会员
 *
 * @author dpb
 * @email dengpbs@163.com
 * @date 2023-10-30 17:30:45
 */
@RestController
@RequestMapping("member/umsmember")
public class UmsMemberController {
    @Autowired
    private UmsMemberService umsMemberService;

    /**
     * 会员注册
     * @return
     */
    @PostMapping("/register")
    public R register(@RequestBody MemberReigerVO vo){
        try {
            umsMemberService.register(vo);
        }catch (UsernameExsitException exception){
            return R.error(BizCodeEnume.USERNAME_EXSIT_EXCEPTION.getCode(),
                    BizCodeEnume.USERNAME_EXSIT_EXCEPTION.getMsg());
        }catch (PhoneExsitExecption exsitExecption) {
            return R.error(BizCodeEnume.PHONE_EXSIT_EXCEPTION.getCode(),
                    BizCodeEnume.PHONE_EXSIT_EXCEPTION.getMsg());
        }catch (Exception e){
            return R.error(BizCodeEnume.UNKNOW_EXCEPTION.getCode(),
                    BizCodeEnume.UNKNOW_EXCEPTION.getMsg());
        }

        return R.ok();
    }

    @RequestMapping("/login")
    public R login(@RequestBody MemberLoginVO vo){
        UmsMemberEntity entity = umsMemberService.login(vo);
        if(entity != null){
            return R.ok().put("entity", JSON.toJSONString(entity));
        }

        return R.error(BizCodeEnume.LOGIN_CHECK_EXCEPTION.getCode(),
                BizCodeEnume.LOGIN_CHECK_EXCEPTION.getMsg());
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("member:umsmember:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = umsMemberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("member:umsmember:info")
    public R info(@PathVariable("id") Long id){
		UmsMemberEntity umsMember = umsMemberService.getById(id);

        return R.ok().put("umsMember", umsMember);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("member:umsmember:save")
    public R save(@RequestBody UmsMemberEntity umsMember){
		umsMemberService.save(umsMember);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("member:umsmember:update")
    public R update(@RequestBody UmsMemberEntity umsMember){
		umsMemberService.updateById(umsMember);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("member:umsmember:delete")
    public R delete(@RequestBody Long[] ids){
		umsMemberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
