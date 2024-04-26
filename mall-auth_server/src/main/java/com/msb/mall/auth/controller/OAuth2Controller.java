package com.msb.mall.auth.controller;

import com.alibaba.fastjson.JSON;
import com.msb.common.utils.R;
import com.msb.mall.auth.feign.MemberFeignService;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.apache.http.HttpResponse;
import com.msb.mall.auth.vo.SocialUser;
import com.msb.common.utils.HttpUtils;

import java.util.HashMap;

@Controller
public class OAuth2Controller {

    @Autowired
    private MemberFeignService memberFeignService;

    //code = 783321aa9780fa4a0c6b4de75a30f8bd
//    d0ed73b5375a4c87660f3c866efd68ec

    @RequestMapping("/oauth/weibo/success")
    public String weiboOAuth(@RequestParam("code") String code) throws Exception {

        HashMap<String, String> body = new HashMap<>();
        body.put("client_id", "2325411584");
        body.put("client_secret", "d0ed73b5375a4c87660f3c866efd68ec");
        body.put("grant_type", "authorization_code");
        body.put("redirect_uri", "http://msb.auth.com/oauth/weibo/success");
        body.put("code", code);

        // 根据Code获取对应得Token信息
        HttpResponse post = HttpUtils.doPost("https://api.weibo.com"
                , "/oauth2/access_token"
                , "post"
                , new HashMap<>()
                , null
                , body
        );

        int statusCode = post.getStatusLine().getStatusCode();
        if (statusCode != 200) {
            // 说明获取Token失败,就调回到登录页面
            return "redirect:http://auth.msb.com/login.html";
        }
        // 说明获取Token信息成功
        String json = EntityUtils.toString(post.getEntity());
        SocialUser socialUser = JSON.parseObject(json, SocialUser.class);

        R r = memberFeignService.socialLogin(socialUser);
        if (r.getCode() != 0) {
            // 登录错误
            return "redirect:http://auth.msb.com/login.html";
        }
        String entityJson = (String) r.get("entity");
        System.out.println("----------------->" + entityJson);
        // 注册成功就需要调整到商城的首页

        return "redirect:http://mall.msb.com/home.html";
    }


}
