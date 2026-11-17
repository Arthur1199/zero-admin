package com.zero.system.domain;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.*;
import com.zero.framework.websocket.WebSocketDisplayOptions;
import com.zero.common.enums.webSocket.*;
/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/17
 * 说明: 系统通知发送及预检查参数；目标由服务端解析，不接受任意事件或来源身份
 */

@Getter @Setter
public class SystemNoticeRequest {
    /** 接收范围，四种方式互斥。 */
    @NotNull private TargetType targetType;
    /** 指定在线账号ID，单次显式选择最多1000个。 */
    @Size(max=1000) private Set<@NotNull @Positive Long> userIds = Set.of();
    /** 部门ID，多选取并集。 */
    @Size(max=1000) private Set<@NotNull @Positive Long> deptIds = Set.of();
    /** 角色ID，多选任意命中。 */
    @Size(max=1000) private Set<@NotNull @Positive Long> roleIds = Set.of();
    /** 部门范围默认包含下级。 */
    private boolean includeChildren = true;
    /** 纯文本标题，最多100字。 */
    @NotBlank @Size(max=100) private String title;
    /** 正文，TEXT最多2000字；HTML最多20000字符、32KB，可见文字最多2000字，不记录操作日志。 */
    @NotBlank @Size(max=20000) private String content;
    /** 正文格式，旧调用默认TEXT；编辑器使用HTML，服务端和浏览器分别过滤。 */
    @NotNull private ContentFormat contentFormat = ContentFormat.TEXT;
    /** 三种可展示形式，NONE拒绝。 */
    @NotNull private WebSocketDisplayType displayType = WebSocketDisplayType.NOTIFICATION;
    /** 普通、警告或错误。 */
    @NotNull private WebSocketMessageLevel messageLevel = WebSocketMessageLevel.NORMAL;
    /** 毫秒，默认0手动关闭；自动关闭使用整数秒，最大1小时，页面预填8000。 */
    @Min(0) @Max(3600000) private long duration = 0;
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 接收方式，仅管理后台通知使用
     */

    public enum TargetType { ALL, USERS, DEPT, ROLE }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 系统通知正文格式，禁止对未明确声明的旧文本按HTML解释
     */
    public enum ContentFormat { TEXT, HTML }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 验证目标互斥，空指定目标绝不退化为广播
     * @return
     * 返回值: void
     * 返回值说明: 拒绝非法参数或完成操作
     */

    public void validate() {
        if (targetType == null || userIds == null || deptIds == null || roleIds == null)
            throw new IllegalArgumentException("通知接收范围不能为空");
        for (var ids : java.util.List.of(userIds, deptIds, roleIds)) {
            if (ids.size() > 1000 || ids.stream().anyMatch(id -> id == null || id <= 0))
                throw new IllegalArgumentException("通知目标ID无效或超过1000个");
        }
        boolean valid = switch (targetType) {
            case ALL -> userIds.isEmpty() && deptIds.isEmpty() && roleIds.isEmpty();
            case USERS -> !userIds.isEmpty() && deptIds.isEmpty() && roleIds.isEmpty();
            case DEPT -> !deptIds.isEmpty() && userIds.isEmpty() && roleIds.isEmpty();
            case ROLE -> !roleIds.isEmpty() && userIds.isEmpty() && deptIds.isEmpty();
        };
        if (!valid) throw new IllegalArgumentException("通知接收范围无效，请只填写当前方式的目标");
        if (title == null || title.isBlank() || title.length() > 100 || content == null || content.isBlank() || contentFormat == null)
            throw new IllegalArgumentException("通知标题或内容无效");
        if (contentFormat == ContentFormat.HTML) com.zero.system.service.SystemNoticeContent.clean(content);
        else if (content.length() > 2000) throw new IllegalArgumentException("通知正文最多2000字");
        new WebSocketDisplayOptions(displayType, messageLevel, duration).validate();
    }
}
