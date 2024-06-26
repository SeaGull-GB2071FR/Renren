package com.msb.mall.member.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.msb.common.utils.HttpUtils;
import com.msb.mall.member.entity.UmsMemberLevelEntity;
import com.msb.mall.member.exception.PhoneExsitExecption;
import com.msb.mall.member.exception.UsernameExsitException;
import com.msb.mall.member.service.UmsMemberLevelService;
import com.msb.mall.member.vo.MemberLoginVO;
import com.msb.mall.member.vo.MemberReigerVO;
import com.msb.mall.member.vo.SocialUser;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.msb.common.utils.PageUtils;
import com.msb.common.utils.Query;

import com.msb.mall.member.dao.UmsMemberDao;
import com.msb.mall.member.entity.UmsMemberEntity;
import com.msb.mall.member.service.UmsMemberService;


@Service("umsMemberService")
public class UmsMemberServiceImpl extends ServiceImpl<UmsMemberDao, UmsMemberEntity> implements UmsMemberService {

    @Autowired
    private UmsMemberLevelService memberLevelService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<UmsMemberEntity> page = this.page(
                new Query<UmsMemberEntity>().getPage(params),
                new QueryWrapper<UmsMemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void register(MemberReigerVO vo) throws PhoneExsitExecption, UsernameExsitException {
        UmsMemberEntity entity = new UmsMemberEntity();
        // 设置会员等级 默认值
        UmsMemberLevelEntity memberLevelEntity = memberLevelService.queryMemberLevelDefault();
        entity.setLevelId(memberLevelEntity.getId()); // 设置默认的会员等级

        // 添加对应的账号和手机号是不能重复的
        checkUsernameUnique(vo.getUserName());
        checkPhoneUnique(vo.getPhone());

        entity.setUsername(vo.getUserName());
        entity.setMobile(vo.getPhone());

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encode = encoder.encode(vo.getPassword());
        // 需要对密码做加密处理
        entity.setPassword(encode);
        // 设置其他的默认值
        this.save(entity);
    }

    @Override
    public UmsMemberEntity login(MemberLoginVO vo) {
        // 1.根据账号或者手机号来查询会员信息
        UmsMemberEntity entity = this.getOne(new QueryWrapper<UmsMemberEntity>()
                .eq("username", vo.getUserName())
                .or()
                .eq("mobile", vo.getUserName()));
        if (entity != null) {
            // 2.如果账号或者手机号存在 然后根据密码加密后的校验来判断是否登录成功
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            boolean matches = encoder.matches(vo.getPassword(), entity.getPassword());
            if (matches) {
                // 表明登录成功
                return entity;
            }
        }
        return null;
    }

    /**
     * 社交登录
     *
     * @param vo
     * @return
     */
    @Override
    public UmsMemberEntity login(SocialUser vo) {
        String uid = vo.getUid();
        // 如果该用户是第一次社交登录，那么需要注册
        // 如果不是第一次社交登录 那么就更新相关信息 登录功能
        UmsMemberEntity entity = this.getOne(new QueryWrapper<UmsMemberEntity>().eq("social_uid", uid));
        if (entity != null) {
            // 说明当前用户已经注册过了 更新token和过期时间
            UmsMemberEntity umsMemberEntity = new UmsMemberEntity();
            umsMemberEntity.setId(entity.getId());
            entity.setAccessToken(vo.getAccessToken());
            entity.setExpiresId(vo.getExpiresId());
            this.updateById(entity);
            // 在返回的登录用户信息中我们同步的也保存 token和过期时间

            entity.setAccessToken(vo.getAccessToken());
            entity.setExpiresId(vo.getExpiresId());
            return entity;
        }

        // 表示用户是第一提交，那么我们就需要对应的来注册
        UmsMemberEntity umsMemberEntity = new UmsMemberEntity();
        umsMemberEntity.setSocialUid(vo.getUid());
        umsMemberEntity.setAccessToken(vo.getAccessToken());
        umsMemberEntity.setExpiresId(vo.getExpiresId());

        // 通过token调用微博开发的接口来获取用户的相关信息
        try {
            Map<String,String> querys = new HashMap<>();
            querys.put("access_token",vo.getAccessToken());
            querys.put("uid",vo.getUid());
            HttpResponse response = HttpUtils.doGet("https://api.weibo.com"
                    , "/2/users/show.json"
                    , "get"
                    , new HashMap<>()
                    , querys
            );
            if(response.getStatusLine().getStatusCode() == 200){
                String json = EntityUtils.toString(response.getEntity());
                JSONObject jsonObject = JSON.parseObject(json);
                String nickName = jsonObject.getString("screen_name");
                String gender = jsonObject.getString("gender");
                entity.setNickname(nickName);
                entity.setGender("m".equals(gender)?1:0);
            }
        }catch (Exception e){

        }
        // 注册用户信息
        this.save(entity);
        return entity;
    }

    /**
     * 校验账号是否存在
     *
     * @param userName
     * @throws UsernameExsitException
     */
    private void checkUsernameUnique(String userName) throws UsernameExsitException {
        int username = this.count(new QueryWrapper<UmsMemberEntity>().eq("username", userName));
        if (username > 0) {
            throw new UsernameExsitException();
        }
    }

    /**
     * 校验手机号是否存在
     *
     * @param phone
     * @throws PhoneExsitExecption
     */
    private void checkPhoneUnique(String phone) throws PhoneExsitExecption {
        int mobile = this.count(new QueryWrapper<UmsMemberEntity>().eq("mobile", phone));
        if (mobile > 0) {
            // 说明手机号是存在的
            throw new PhoneExsitExecption();
        }
    }

}