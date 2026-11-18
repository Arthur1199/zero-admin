package com.zero.system.domain;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/17 21:20
 * 说明: 备忘录新增及修改参数；身份和审计字段由服务端设置，正文不进入操作日志
 */
@Getter
@Setter
public class SysMemoRequest
{
    /** 修改时必填，新增时不使用。 */
    @Positive(message = "备忘录ID无效")
    private Long memoId;
    /** 标题，最多200个字符。 */
    @NotBlank(message = "备忘录标题不能为空")
    @Size(max = 200, message = "备忘录标题不能超过200个字符")
    private String title;
    /** 资源地址，可空，最多2000个字符，不应包含密码或Token。 */
    @Size(max = 2000, message = "资源地址不能超过2000个字符")
    private String resourceUrl;
    /** 备忘录正文，可空，最多100000个字符，服务端整体加密。 */
    @Size(max = 100000, message = "备忘录内容不能超过100000个字符")
    private String memoContent;
    /** 普通备注，可空，最多1000个字符，不存储敏感信息。 */
    @Size(max = 1000, message = "备注说明不能超过1000个字符")
    private String remark;
    /** 可见范围，默认私有；只接受0私有、1公开。 */
    @NotNull(message = "请选择可见范围")
    @Pattern(regexp = "[01]", message = "可见范围无效")
    private String visibility = "0";
    /** 修改时必填，必须与读取详情时的版本一致。 */
    @Min(value = 0, message = "备忘录版本无效")
    private Long version;
}

