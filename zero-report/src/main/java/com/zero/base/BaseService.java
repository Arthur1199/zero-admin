package com.zero.base;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zero.dto.ReportAdminContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * 说明: 报表服务 Service 层基础父类，统一承载 MyBatis-Plus 通用 CRUD、请求上下文读取、Redis 和日志辅助能力
 */
public abstract class BaseService<M extends BaseMapper<T>, T> extends ServiceImpl<M, T> {
    /**
     * 子类业务日志对象，使用真实子类类型作为日志分类
     */
    protected final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Redis模板，未启用Redis或测试环境未创建Bean时允许为空，调用方需按业务需要判空
     */
    @Getter
    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

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

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 02:05
     * 说明: 获取指定类型的日志对象，兼容需要按工具类或辅助类分类输出日志的场景
     *
     * @param clazz 日志分类类型，不能为空
     * @return 返回值: Logger
     * 返回值说明: 指定类型对应的日志对象
     */
    public Logger getLogger(Class<?> clazz) {
        return clazz == null ? log : LoggerFactory.getLogger(clazz);
    }
}
