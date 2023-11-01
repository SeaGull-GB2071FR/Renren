package com.msb.mall.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

import com.msb.common.valid.groups.AddGroupsInterface;
import com.msb.common.valid.groups.UpdateGroupsInterface;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.*;

/**
 * 品牌
 *
 * @author dpb
 * @email dengpbs@163.com
 * @date 2023-10-30 17:10:05
 */
@Data
@TableName("pms_brand")
public class BrandEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 品牌id
     */
    @NotNull(message = "添加品牌信息品牌ID必须不为空",groups = {UpdateGroupsInterface.class})
    @Null(message = "添加品牌信息品牌ID必须为空",groups = {AddGroupsInterface.class})
    @TableId
    private Long brandId;
    /**
     * 品牌名
     */

    //在需要校验的Bean的字段头部添加对应的注解
    @NotBlank(message = "品牌的名称不能为空",groups = {AddGroupsInterface.class})
    private String name;
    /**
     * 品牌logo地址
     */
    @NotBlank(message = "logo不能为空")
    @URL(message = "logo必须是一个合法URL地址")
    private String logo;
    /**
     * 介绍
     */
    private String descript;
    /**
     * 显示状态[0-不显示；1-显示]
     */
    private Integer showStatus;
    /**
     * 检索首字母
     */
    @NotBlank(message = "检索首字母不能为空")
    @Pattern(regexp = "/^[a-zA-Z]$/", message = "检索首字母必须是单个的字母")
    private String firstLetter;
    /**
     * 排序
     */
    @NotNull(message = "排序不能为null")
    @Min(value = 0, message = "排序不能小于0")
    private Integer sort;

}
