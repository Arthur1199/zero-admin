package com.zero.jobadmin.business.scheduler.exception;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 表示调度中心业务处理失败的运行时异常。
 */
public class XxlJobException extends RuntimeException {

    public XxlJobException() {
    }
    public XxlJobException(String message) {
        super(message);
    }

}
