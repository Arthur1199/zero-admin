package com.zero.system.domain;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/17 21:20
 * 说明: 备忘录列表查询条件；不接受所属用户ID，可见范围由服务端登录态确定
 */
@Getter
@Setter
public class SysMemoQuery
{
    /** 标题关键字，可空。 */
    @Size(max = 200, message = "标题查询条件不能超过200个字符")
    private String title;
    /** 资源地址关键字，可空。 */
    @Size(max = 2000, message = "地址查询条件不能超过2000个字符")
    private String resourceUrl;
    /** 可见范围，可空表示全部可见数据；0私有、1公开。 */
    @Pattern(regexp = "[01]?", message = "可见范围无效")
    private String visibility;
}

