package com.zero.framework.manager.factory;

import lombok.extern.slf4j.Slf4j;
import java.util.TimerTask;
import com.zero.common.constant.Constants;
import com.zero.common.utils.LogUtils;
import com.zero.common.utils.ServletUtils;
import com.zero.common.utils.StringUtils;
import com.zero.common.utils.ip.AddressUtils;
import com.zero.common.utils.ip.IpUtils;
import com.zero.common.utils.spring.SpringUtils;
import com.zero.system.domain.SysLogininfor;
import com.zero.system.domain.SysOperLog;
import com.zero.system.service.ISysLogininforService;
import com.zero.system.service.ISysOperLogService;
import eu.bitwalker.useragentutils.UserAgent;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 异步工厂（产生任务用）
 */
@Slf4j(topic = "sys-user")
public class AsyncFactory
{
    /**
     * sys_user_logger字段
     */
    /**
     * 记录登录信息
     *
     * @param username 用户名
     * @param status 状态
     * @param message 消息
     * @param args 列表
     * @return 任务task
     */
    public static TimerTask recordLogininfor(final String username, final String status, final String message,
            final Object... args)
    {
        final UserAgent userAgent = UserAgent.parseUserAgentString(ServletUtils.getRequest().getHeader("User-Agent"));
        final String ip = IpUtils.getIpAddr();
        return new TimerTask()
        {
            /**
             *
             * 作者: Arthur 亚瑟
             * 时间: 2026/5/14 00:58
             * 说明: 执行
             * @return
             * 返回值: void
             * 返回值说明：执行
             */
            @Override
            public void run()
            {
                String address = AddressUtils.getRealAddressByIP(ip);
                StringBuilder s = new StringBuilder();
                s.append(LogUtils.getBlock(ip));
                s.append(address);
                s.append(LogUtils.getBlock(username));
                s.append(LogUtils.getBlock(status));
                s.append(LogUtils.getBlock(message));
                // 打印信息到日志
                log.info(s.toString(), args);
                // 获取客户端操作系统
                String os = userAgent.getOperatingSystem().getName();
                // 获取客户端浏览器
                String browser = userAgent.getBrowser().getName();
                // 封装对象
                SysLogininfor logininfor = new SysLogininfor();
                logininfor.setUserName(username);
                logininfor.setIpaddr(ip);
                logininfor.setLoginLocation(address);
                logininfor.setBrowser(browser);
                logininfor.setOs(os);
                logininfor.setMsg(message);
                // 日志状态
                if (StringUtils.equalsAny(status, Constants.LOGIN_SUCCESS, Constants.LOGOUT))
                {
                    logininfor.setStatus(Constants.SUCCESS);
                }
                else if (Constants.LOGIN_FAIL.equals(status))
                {
                    logininfor.setStatus(Constants.FAIL);
                }
                // 插入数据
                SpringUtils.getBean(ISysLogininforService.class).insertLogininfor(logininfor);
            }
        };
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 操作日志记录
     * @param operLog 操作日志信息
     * @return
     * 返回值: 任务task
     * 返回值说明：操作日志记录
     */
    public static TimerTask recordOper(final SysOperLog operLog)
    {
        return new TimerTask()
        {
            /**
             *
             * 作者: Arthur 亚瑟
             * 时间: 2026/5/14 00:58
             * 说明: 执行
             * @return
             * 返回值: void
             * 返回值说明：执行
             */
            @Override
            public void run()
            {
                // 远程查询操作地点
                operLog.setOperLocation(AddressUtils.getRealAddressByIP(operLog.getOperIp()));
                SpringUtils.getBean(ISysOperLogService.class).insertOperlog(operLog);
            }
        };
    }
}
