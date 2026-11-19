package com.zero.utils.mail;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:42
 * 说明: 保存邮件发件人、收件人、主题、正文和可选附件，供邮件工具组装发送内容。
 */
public class Mail {

    /** 发件人*/
    private String           from;

    /** 收件人*/
    private StringBuilder    toAddress  = new StringBuilder();

    /** 抄送*/
    private StringBuilder    ccAddress  = new StringBuilder();

    /** 暗送*/
    private StringBuilder    bccAddress = new StringBuilder();

    /** 主题*/
    private String           subject;

    /** 正文*/
    private String           content;

    // 附件列表
    private List<AttachBean> attachList = new ArrayList<AttachBean>();

    public Mail() {
    }

    public Mail(String from, String to) {
        this(from, to, null, null);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 用发件人、收件人、主题和正文创建邮件对象。
     * @param from 发件人地址
     * @param to 收件人地址
     * @param subject 邮件主题
     * @param content 邮件正文
     */
    public Mail(String from, String to, String subject, String content) {
        this.from = from;
        this.toAddress.append(to);
        this.subject = subject;
        this.content = content;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 更新邮件使用的发件人地址。
     * @param from 发件人地址
     * @return
     * 返回值: void
     * 返回值说明: 保存传入的发件人地址
     */
    public void setFrom(String from) {
        this.from = from;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 返回发件人
     * @return
     * 返回值: String
     * 返回值说明: 按上述规则读取或解析得到的文本
     */
    public String getFrom() {
        return from;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 返回主题
     * @return
     * 返回值: String
     * 返回值说明: 按上述规则读取或解析得到的文本
     */
    public String getSubject() {
        return subject;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 设置主题
     * @param subject 邮件主题
     * @return
     * 返回值: void
     * 返回值说明: 设置主题
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 获取主题内容
     * @return
     * 返回值: String
     * 返回值说明: 按上述规则读取或解析得到的文本
     */
    public String getContent() {
        return content;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 设置主题内容
     * @param content 待处理的正文
     * @return
     * 返回值: void
     * 返回值说明: 设置主题内容
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 获取收件人
     * @return
     * 返回值: String
     * 返回值说明: 按上述规则读取或解析得到的文本
     */
    public String getToAddress() {
        return toAddress.toString();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 获取抄送
     * @return
     * 返回值: String
     * 返回值说明: 按上述规则读取或解析得到的文本
     */
    public String getCcAddress() {
        return ccAddress.toString();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 获取暗送
     * @return
     * 返回值: String
     * 返回值说明: 按上述规则读取或解析得到的文本
     */
    public String getBccAddress() {
        return bccAddress.toString();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 添加收件人,可以是多个收件人
     * @param to 收件人地址或地址集合
     * @return
     * 返回值: void
     * 返回值说明: 添加收件人,可以是多个收件人
     */
    public void addToAddress(String to) {
        if (this.toAddress.length() > 0) {
            this.toAddress.append(",");
        }
        this.toAddress.append(to);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 添加抄送人，可以是多个抄送人
     * @param cc 抄送地址或地址集合
     * @return
     * 返回值: void
     * 返回值说明: 添加抄送人，可以是多个抄送人
     */
    public void addCcAddress(String cc) {
        if (this.ccAddress.length() > 0) {
            this.ccAddress.append(",");
        }
        this.ccAddress.append(cc);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 添加暗送人，可以是多个暗送人
     * @param bcc 暗送地址或地址集合
     * @return
     * 返回值: void
     * 返回值说明: 添加暗送人，可以是多个暗送人
     */
    public void addBccAddress(String bcc) {
        if (this.bccAddress.length() > 0) {
            this.bccAddress.append(",");
        }
        this.bccAddress.append(bcc);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 添加附件，可以添加多个附件
     * @param attachBean 待添加的邮件附件
     * @return
     * 返回值: void
     * 返回值说明: 添加附件，可以添加多个附件
     */
    public void addAttach(AttachBean attachBean) {
        this.attachList.add(attachBean);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 获取所有附件
     * @return
     * 返回值: List<AttachBean>
     * 返回值说明: 符合上述查询条件的记录或值集合
     */
    public List<AttachBean> getAttachs() {
        return this.attachList;
    }

}
