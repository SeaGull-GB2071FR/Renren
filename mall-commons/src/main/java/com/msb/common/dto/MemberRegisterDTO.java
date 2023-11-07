package com.msb.common.dto;

import lombok.Data;

@Data
public class MemberRegisterDTO {

    // 账号
    private String userName;
    // 密码
    private String password;
    // 手机号
    private String phone;

}
