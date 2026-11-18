package com.zero.system.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.zero.common.core.domain.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/17 21:20
 * 说明: 备忘录表sys_memo；列表不读取正文，正文只在校验可见范围后解密，不生成包含正文的toString
 */
@Getter
@Setter
public class SysMemo extends BaseEntity
{
    /** 序列化版本。 */
    private static final long serialVersionUID = 1L;
    /** 备忘录ID。 */
    private Long memoId;
    /** 备忘录标题。 */
    private String title;
    /** 资源地址，仅作为文本展示，不由后端主动访问。 */
    private String resourceUrl;
    /** 数据库存储密文，禁止作为JSON输入或输出。 */
    @JsonIgnore
    private String contentCipher;
    /** 详情中的解密正文，列表中不返回。 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String memoContent;
    /** 可见范围：0私有，1公开。 */
    private String visibility;
    /** 创建用户ID，创建后不允许转移。 */
    private Long ownerUserId;
    /** 并发修改版本，每次保存递增。 */
    private Long version;
    /** 当前登录人是否为创建人或超级管理员；仅用于页面操作展示，不作为写入授权依据。 */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private boolean canManage;
}

