package com.zero.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/6/28 04:32
 * 说明: 业务字典项实体，对应 zero_biz.biz_dict_item，用于任务服务读取有效字典项
 */
@Getter
@Setter
@ToString
@TableName("biz_dict_item")
public class DictItem implements Serializable
{

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID，新增时由数据库自增生成
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 字典组ID，必须指向未删除的 biz_dict_group 记录
     */
    @TableField("group_id")
    private Long groupId;

    /** 联查取得的原始分组编码，仅用于校验缓存键可按库中编码清理。 */
    @JsonIgnore
    @TableField(exist = false)
    private String lookupGroupCode;

    /**
     * 字典编码，可用于程序识别；同一字典组下不允许重复，也不允许为空
     */
    @TableField("item_code")
    private String itemCode;

    /**
     * 字典项名称，例如待支付、已支付
     */
    @TableField("item_name")
    private String itemName;

    /**
     * 字典值，业务表实际存储值
     */
    @TableField("item_value")
    private String itemValue;

    /**
     * 扩展配置JSON，用于少量业务附加配置，不在列表中解析执行
     */
    @TableField("extra_json")
    private String extraJson;

    /**
     * 排序，值越小越靠前
     */
    @TableField("item_sort")
    private Integer itemSort;

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
     * 备注，说明该字典项的业务含义或使用限制
     */
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
