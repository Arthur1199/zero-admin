package com.zero.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/6/28 04:32
 * 说明: 业务字典分组实体，对应 zero_biz.biz_dict_group，用于维护业务参数设置页面的父级字典分类
 */
@Getter
@Setter
@ToString
@TableName("biz_dict_group")
public class DictGroup implements Serializable
{

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID，新增时由数据库自增生成
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 字典组编码，例如 order_status、pay_type，创建后作为业务识别码使用
     */
    @NotBlank(message = "字典组编码不能为空")
    @Size(max = 100, message = "字典组编码不能超过100个字符")
    @TableField("group_code")
    private String groupCode;

    /**
     * 字典组名称，例如订单状态、支付方式
     */
    @NotBlank(message = "字典组名称不能为空")
    @Size(max = 100, message = "字典组名称不能超过100个字符")
    @TableField("group_name")
    private String groupName;

    /**
     * 状态：1:正常，0:停用
     */
    @TableField("status")
    private String status;

    /**
     * 删除标志：0未删除，1:删除
     */
    @TableField("del_flag")
    private String delFlag;

    /**
     * 备注，说明该字典组的业务用途
     */
    @Size(max = 500, message = "备注不能超过500个字符")
    @TableField("remark")
    private String remark;

    /**
     * 创建者，来自管理后台透传的登录账号
     */
    @TableField("create_by")
    private String createBy;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 更新者，来自管理后台透传的登录账号
     */
    @TableField("update_by")
    private String updateBy;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private Date updateTime;
}
