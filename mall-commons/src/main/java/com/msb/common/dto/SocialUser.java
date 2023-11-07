package com.msb.common.dto;

import lombok.Data;

/**
 * 第三方授权，返回数据封装
 */
@Data
public class SocialUser {

    private String accessToken;
    private Long remindIn;
    private Long expiresIn; //过期时间
    private String uid;
    private Boolean isRealName;

}
