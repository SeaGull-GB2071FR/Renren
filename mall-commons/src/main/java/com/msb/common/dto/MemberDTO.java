package com.msb.common.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 会员DTO
 * 
 * @author dpb
 * @email dengpbs@163.com
 * @date 2022-10-08 23:01:44
 */
@Data
public class MemberDTO implements Serializable {

	private Long id;
	private Long levelId;
	private String username;
	private String password;
	private String nickname;
	private String mobile;
	private String email;
	private String header;
	private Integer gender;
	private Date birth;
	private String city;
	private String job;
	private String sign;
	private Integer sourceType;
	private Integer integration;
	private Integer growth;
	private Integer status;
	private Date createTime;
	private String accessToken;
	private String uid;
	private Long expiresIn;
}
