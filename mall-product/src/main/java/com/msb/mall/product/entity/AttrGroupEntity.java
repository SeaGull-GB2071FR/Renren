package com.msb.mall.product.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 属性分组
 *
 * @author GB2071FR
 * @email 1184800897@qq.com
 * @date 2023-10-30 17:10:05
 */
@Data
@TableName("pms_attr_group")
public class AttrGroupEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 分组id
	 */
	@TableId
	private Long attrGroupId;
	/**
	 * 组名
	 */
	private String attrGroupName;
	/**
	 * 排序
	 */
	private Integer sort;
	/**
	 * 描述
	 */
	private String descript;
	/**
	 * 组图标
	 */
	private String icon;
	/**
	 * 所属分类id
	 */
	private Long catelogId;

	/**
	 * 修改数据的时候记录类别信息
	 */
	@TableField(exist = false)
	private Long[] catelogPath;

}
