package com.zero.base;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zero.domain.DictItem;
import com.zero.dto.ReportAdminContext;
import com.zero.util.DictConfigUtil;
import com.zero.util.RedissonUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/6/28 02:05
 * 说明: 报表服务基础父类，提供通用 CRUD、请求上下文、Redisson 缓存和业务字典查询。
 */
public abstract class ReportBaseService<M extends BaseMapper<T>, T> extends ServiceImpl<M, T> {

    /**
     * 兼容已有 RedisTemplate 调用；连接由 Redisson 提供，未配置缓存的测试环境可为空。
     */
    @Getter
    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    /** 子类使用的 Redisson 缓存工具，未装配 Redis 的隔离测试环境可为空。 */
    @Getter
    @Autowired(required = false)
    private RedissonUtil redissonUtil;

    /** 独立读取业务字典，确保子类所在的数据源和事务不影响查询。 */
    @Autowired
    private DictConfigUtil dictConfigUtil;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 14:34
     * 说明: 按分组编码和字典项编码读取已启用、未删除的业务字典项。
     * @param dictKey 分组编码.字典项编码，允许去除两段周围的空白
     * @return
     * 返回值: DictItem
     * 返回值说明: 完整字典项，格式非法或无有效记录时返回 null
     */
    protected DictItem getDictItem(String dictKey) {
        if (dictKey == null) {
            return null;
        }
        int separator = dictKey.indexOf('.');
        if (separator < 0 || separator != dictKey.lastIndexOf('.')) {
            return null;
        }
        String groupCode = dictKey.substring(0, separator).strip();
        String itemCode = dictKey.substring(separator + 1).strip();
        if (groupCode.isEmpty() || itemCode.isEmpty()) {
            return null;
        }
        return dictConfigUtil.findActiveItem(groupCode, itemCode);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 14:41
     * 说明: 查询启用且未删除的业务字典分组下所有有效字典项。
     * @param groupCode 字典分组编码，允许两侧有空白
     * @return
     * 返回值: List<DictItem>
     * 返回值说明: 按排序值和主键升序的字典项，分组编码为空或查无有效项时返回空集合
     */
    protected List<DictItem> getDictItems(String groupCode) {
        if (groupCode == null || groupCode.isBlank()) {
            return List.of();
        }
        return dictConfigUtil.findActiveItems(groupCode.strip());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 14:34
     * 说明: 获取有效业务字典项的原始字符串值。
     * @param dictKey 分组编码.字典项编码，允许去除两段周围的空白
     * @return
     * 返回值: String
     * 返回值说明: item_value 字段原值，格式非法或无有效记录时返回 null
     */
    protected String getDictValue(String dictKey) {
        DictItem item = getDictItem(dictKey);
        return item == null ? null : item.getItemValue();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 03:10
     * 说明: 获取后台操作管理员ID；无 Web 请求或请求头不存在时返回空字符串
     *
     * @return 返回值: String
     * 返回值说明: 后台操作管理员ID，未传入时为空字符串
     */
    public String getAdminId() {
        return getAdminContext().getAdminId();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 03:10
     * 说明: 获取后台操作管理员账号；无 Web 请求或请求头不存在时返回空字符串
     *
     * @return 返回值: String
     * 返回值说明: 后台操作管理员账号，未传入时为空字符串
     */
    public String getAdminName() {
        return getAdminContext().getAdminName();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 03:10
     * 说明: 获取后台操作管理员所属公司名称；请求头可能包含中文，由上下文对象统一解码
     *
     * @return 返回值: String
     * 返回值说明: 后台操作管理员所属公司名称，未传入时为空字符串
     */
    public String getAdminCompanyName() {
        return getAdminContext().getAdminCompanyName();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 03:10
     * 说明: 获取后台操作管理员全部角色ID，供报表业务做角色上下文判断
     *
     * @return 返回值: List<Long>
     * 返回值说明: 后台操作管理员角色ID列表，未传入时为空列表
     */
    public List<Long> getAdminRoleIds() {
        return getAdminContext().getAdminRoleIds();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 02:05
     * 说明: 判断当前后台操作用户是否为管理员；只信任上游显式传入的 true 或 1，不做本地环境自动放权
     *
     * @return 返回值: boolean
     * 返回值说明: true-管理员，false-非管理员或未传入管理员标识
     */
    public boolean isAdmin() {
        return getAdminContext().isAdmin();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 02:05
     * 说明: 获取超级管理员昵称；请求头可能包含中文，统一按 UTF-8 解码
     *
     * @return 返回值: String
     * 返回值说明: 超级管理员昵称，未传入或解码失败时返回安全兜底值
     */
    public String getAdminNickName() {
        return getAdminContext().getAdminNickName();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 02:25
     * 说明: 获取后台操作用户首个角色ID，与 ReportAdminContext 的 getFirstAdminRoleId 保持一致
     *
     * @return 返回值: String
     * 返回值说明: 首个角色ID，未传入时为空字符串
     */
    public String getFirstAdminRoleId() {
        return getAdminContext().getFirstAdminRoleId();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 02:25
     * 说明: 获取后台用户客户端IP，用于报表服务记录操作来源
     *
     * @return 返回值: String
     * 返回值说明: 客户端IP，未传入时为空字符串
     */
    public String getClientIp() {
        return getAdminContext().getClientIp();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 02:05
     * 说明: 获取当前请求登录令牌；仅供需要继续识别上游登录态的业务逻辑读取，不在日志中输出明文
     *
     * @return 返回值: String
     * 返回值说明: Authorization 请求头原始值，未传入时为空字符串
     */
    public String getAuthorization() {
        return getAdminContext().getAuthorization();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 02:05
     * 说明: 获取上游服务来源标识，用于定位请求进入 report 前来自哪个服务
     *
     * @return 返回值: String
     * 返回值说明: 上游服务来源标识，未传入时为空字符串
     */
    public String getSourceService() {
        return getAdminContext().getSourceService();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 02:55
     * 说明: 获取 report 当前请求接收到的后台管理员上下文，统一封装请求头解析和中文解码
     *
     * @return 返回值: ReportAdminContext
     * 返回值说明: 当前请求的后台管理员上下文，无请求时返回空上下文
     */
    public ReportAdminContext getAdminContext() {
        return ReportAdminContext.fromRequest(getRequest());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 02:05
     * 说明: 获取当前线程绑定的 HTTP 请求；兼容定时任务、异步线程、单元测试等没有 Web 请求的场景
     *
     * @return 返回值: HttpServletRequest
     * 返回值说明: 当前 HTTP 请求对象，不存在时返回 null
     */
    public HttpServletRequest getRequest() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes servletRequestAttributes)) {
            return null;
        }
        return servletRequestAttributes.getRequest();
    }


}
