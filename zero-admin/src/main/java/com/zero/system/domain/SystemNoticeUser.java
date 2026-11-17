package com.zero.system.domain;
import lombok.Getter;
import lombok.Setter;
/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/17
 * 说明: 系统通知在线候选账号，严格限制返回字段，不包含Token、会话编号或联系方式
 */

@Getter @Setter
public class SystemNoticeUser {
    /** 管理账号ID。 */ private Long userId;
    /** 登录账号。 */ private String userName;
    /** 用户昵称。 */ private String nickName;
    /** 当前归属部门ID。 */ private Long deptId;
    /** 当前部门名称。 */ private String deptName;
}
