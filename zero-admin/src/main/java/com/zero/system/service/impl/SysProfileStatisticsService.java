package com.zero.system.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import eu.bitwalker.useragentutils.UserAgent;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.zero.common.core.domain.entity.SysUser;
import com.zero.common.core.domain.model.LoginUser;
import com.zero.common.utils.StringUtils;
import com.zero.common.utils.ip.AddressUtils;
import com.zero.common.utils.ip.IpUtils;
import com.zero.system.domain.SysProfileStatistics.Memos;
import com.zero.system.domain.SysProfileStatistics.Operation;
import com.zero.system.domain.SysProfileStatistics.OperationDay;
import com.zero.system.domain.SysProfileStatistics.Operations;
import com.zero.system.domain.SysProfileStatistics.Session;
import com.zero.system.mapper.SysProfileStatisticsMapper;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/21 19:16
 * 说明: 按登录账号汇总个人中心的操作日志和备忘录
 */
@Service
public class SysProfileStatisticsService
{
    /** 统计沿用后台的上海时区。 */
    private static final ZoneId STATISTICS_ZONE = ZoneId.of("Asia/Shanghai");

    /** 统计范围包含今天在内的七个自然日。 */
    private static final int STATISTICS_DAYS = 7;

    /** 个人中心只读查询。 */
    private final SysProfileStatisticsMapper mapper;

    public SysProfileStatisticsService(SysProfileStatisticsMapper mapper)
    {
        this.mapper = mapper;
    }

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/21 20:12
     * 说明: 查询当前请求的操作环境，使用首次签发时间统计在线时长
     * @param loginUser 经服务端验证的当前登录会话
     * @param request 当前 HTTP 请求，用于解析客户端 IP 和设备信息
     * @return 当前会话展示信息，旧会话的在线秒数为空
     */
    public Session getSession(LoginUser loginUser, HttpServletRequest request)
    {
        String ip = StringUtils.trimToEmpty(IpUtils.getIpAddr(request));
        if ("::1".equals(ip))
        {
            ip = "127.0.0.1";
        }
        String location = AddressUtils.getRealAddressByIP(ip);
        UserAgent agent = UserAgent.parseUserAgentString(request.getHeader("User-Agent"));
        Long startedAt = loginUser.getSessionStartedAt();
        Long onlineSeconds = startedAt == null || startedAt <= 0 ? null
                : TimeUnit.MILLISECONDS.toSeconds(Math.max(0, System.currentTimeMillis() - startedAt));
        return new Session(onlineSeconds, IpUtils.isUnknown(ip) ? null : ip,
                AddressUtils.UNKNOWN.equals(location) ? null : location,
                agent.getBrowser().getName(), agent.getOperatingSystem().getName());
    }

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/21 19:16
     * 说明: 汇总当前账号七日内的操作并补齐没有操作的日期
     * @param user 由服务端认证上下文取得的当前账号
     * @return 七日汇总、每日计数和最近操作
     */
    @Transactional(readOnly = true)
    public Operations getOperations(SysUser user)
    {
        LocalDate today = LocalDate.now(STATISTICS_ZONE);
        LocalDate firstDay = today.minusDays(STATISTICS_DAYS - 1);
        Map<LocalDate, OperationDay> daysByDate = new HashMap<>();
        List<Operation> recent = List.of();
        // 操作日志没有用户 ID，按创建时间排除账号名被重用前的历史。
        if (StringUtils.isNotBlank(user.getUserName()) && user.getCreateTime() != null)
        {
            LocalDateTime createdAt = LocalDateTime.ofInstant(user.getCreateTime().toInstant(), STATISTICS_ZONE);
            LocalDateTime start = firstDay.atStartOfDay();
            if (createdAt.isAfter(start))
            {
                start = createdAt;
            }
            LocalDateTime end = today.plusDays(1).atStartOfDay();
            for (OperationDay day : mapper.selectOperationDays(user.getUserName(), start, end))
            {
                daysByDate.put(day.day(), day);
            }
            recent = mapper.selectRecentOperations(user.getUserName(), start, end);
        }
        List<OperationDay> days = IntStream.range(0, STATISTICS_DAYS)
                .mapToObj(firstDay::plusDays)
                .map(day -> daysByDate.getOrDefault(day, new OperationDay(day, 0, 0, 0)))
                .toList();
        return new Operations(days.stream().mapToLong(OperationDay::total).sum(),
                days.stream().mapToLong(OperationDay::succeeded).sum(),
                days.stream().mapToLong(OperationDay::failed).sum(), days, recent);
    }

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/21 19:16
     * 说明: 读取本人创建的备忘录统计，调用方需先验证备忘录菜单权限
     * @param userId 由服务端认证上下文取得的用户 ID
     * @return 备忘录数量及最近更新的标题和时间
     */
    @Transactional(readOnly = true)
    public Memos getMemos(Long userId)
    {
        LocalDate today = LocalDate.now(STATISTICS_ZONE);
        return new Memos(mapper.selectMemoCounts(userId, today.minusDays(STATISTICS_DAYS - 1).atStartOfDay(),
                today.plusDays(1).atStartOfDay()), mapper.selectRecentMemos(userId));
    }
}
