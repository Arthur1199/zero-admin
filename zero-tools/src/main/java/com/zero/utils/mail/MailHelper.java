package com.zero.utils.mail;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import jakarta.activation.DataHandler;
import jakarta.activation.FileDataSource;
import jakarta.activation.URLDataSource;
import jakarta.mail.Authenticator;
import jakarta.mail.Message.RecipientType;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.internet.MimeUtility;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/16 23:58
 * 说明: 邮件发送工具类，基于Jakarta Mail提供普通邮件、HTML邮件和附件邮件发送能力
 */
public class MailHelper
{
    private static final String TEXT_PLAIN_GBK = "text/plain;charset=GBK";
    private static final String TEXT_HTML_UTF8 = "text/html;charset=UTF-8";
    private static final String SMTP_HOST = "mail.smtp.host";
    private static final String SMTP_PORT = "mail.smtp.port";
    private static final String SMTP_AUTH = "mail.smtp.auth";
    private static final String SMTP_STARTTLS = "mail.smtp.starttls.enable";

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 23:58
     * 说明: 创建普通SMTP邮件会话
     * @param host SMTP服务器地址
     * @param username SMTP账号名称
     * @param password SMTP账号密码，调用方不得写入日志
     * @return
     * 返回值: Session
     * 返回值说明: 已配置账号认证的邮件会话
     */
    public static Session createSession(String host, final String username, final String password) {
        Properties prop = new Properties();
        prop.setProperty(SMTP_HOST, host);
        prop.setProperty(SMTP_AUTH, "true");
        return Session.getInstance(prop, createAuthenticator(username, password));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 23:58
     * 说明: 创建启用TLS的SMTP邮件会话，默认使用587端口
     * @param host SMTP服务器地址
     * @param username SMTP账号名称
     * @param password SMTP账号密码，调用方不得写入日志
     * @return
     * 返回值: Session
     * 返回值说明: 已配置TLS和账号认证的邮件会话
     */
    public static Session createTlsSession(String host, final String username, final String password) {
        Properties prop = new Properties();
        prop.setProperty(SMTP_HOST, host);
        prop.setProperty(SMTP_PORT, "587");
        prop.setProperty(SMTP_AUTH, "true");
        prop.setProperty(SMTP_STARTTLS, "true");
        return Session.getInstance(prop, createAuthenticator(username, password));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 23:58
     * 说明: 发送Mail对象邮件，支持HTML正文、收件人、抄送、密送和本地附件
     * @param session 邮件会话，可通过createSession或createTlsSession创建
     * @param mail 邮件内容对象，必须包含发件人、收件人、主题和正文
     * @return
     * 返回值: void
     * 返回值说明: 邮件发送成功后无返回，发送失败时抛出异常
     * @throws MessagingException 邮件地址、内容或发送过程异常时抛出
     * @throws IOException 附件读取失败时抛出
     */
    public static void send(Session session, final Mail mail) throws MessagingException, IOException {
        MimeMessage msg = createBaseMessage(session, mail.getFrom(), mail.getToAddress(), mail.getSubject());
        addRecipients(msg, RecipientType.CC, mail.getCcAddress());
        addRecipients(msg, RecipientType.BCC, mail.getBccAddress());
        msg.setContent(createMultipart(mail.getContent(), TEXT_HTML_UTF8, mail.getAttachs()));
        Transport.send(msg);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 23:58
     * 说明: 发送普通文本邮件，支持单个收件人
     * @param host SMTP服务器地址
     * @param username SMTP账号名称
     * @param password SMTP账号密码，调用方不得写入日志
     * @param subject 邮件主题
     * @param contents 邮件正文
     * @param toEmailAddress 收件人邮箱地址
     * @param fromEmailAddress 发件人邮箱地址
     * @return
     * 返回值: void
     * 返回值说明: 邮件发送成功后无返回，发送失败时抛出异常
     * @throws MessagingException 邮件地址、内容或发送过程异常时抛出
     */
    public static void sendSimpleEmail(String host, String username, String password, String subject, String contents,
                                       String toEmailAddress, String fromEmailAddress) throws MessagingException
    {
        sendMessage(createSession(host, username, password), fromEmailAddress, toEmailAddress, subject, contents,
                TEXT_PLAIN_GBK, null);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 23:58
     * 说明: 发送普通文本邮件，支持多个收件人
     * @param host SMTP服务器地址
     * @param username SMTP账号名称
     * @param password SMTP账号密码，调用方不得写入日志
     * @param subject 邮件主题
     * @param contents 邮件正文
     * @param toEmailAddress 收件人邮箱地址数组
     * @param fromEmailAddress 发件人邮箱地址
     * @return
     * 返回值: void
     * 返回值说明: 邮件发送成功后无返回，发送失败时抛出异常
     * @throws MessagingException 邮件地址、内容或发送过程异常时抛出
     */
    public static void sendSimpleEmail(String host, String username, String password, String subject, String contents,
                                       String[] toEmailAddress, String fromEmailAddress) throws MessagingException
    {
        sendMessage(createSession(host, username, password), fromEmailAddress, joinAddress(toEmailAddress), subject,
                contents, TEXT_PLAIN_GBK, null);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 23:58
     * 说明: 发送带附件的文本邮件，附件支持本地文件路径或HTTP/HTTPS地址
     * @param host SMTP服务器地址
     * @param username SMTP账号名称
     * @param password SMTP账号密码，调用方不得写入日志
     * @param subject 邮件主题
     * @param contents 邮件正文
     * @param toEmailAddress 收件人邮箱地址
     * @param fromEmailAddress 发件人邮箱地址
     * @param multiPaths 附件路径数组，可为空
     * @return
     * 返回值: void
     * 返回值说明: 邮件发送成功后无返回，发送失败时抛出异常
     * @throws MessagingException 邮件地址、内容或发送过程异常时抛出
     * @throws IOException 附件地址解析或读取失败时抛出
     */
    public static void sendMultiPartEmail(String host, String username, String password, String subject,
                                          String contents, String toEmailAddress, String fromEmailAddress,
                                          String[] multiPaths) throws MessagingException, IOException
    {
        sendMessage(createSession(host, username, password), fromEmailAddress, toEmailAddress, subject, contents,
                TEXT_PLAIN_GBK, multiPaths);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 23:58
     * 说明: 发送带附件的文本邮件，支持多个收件人，附件支持本地文件路径或HTTP/HTTPS地址
     * @param host SMTP服务器地址
     * @param username SMTP账号名称
     * @param password SMTP账号密码，调用方不得写入日志
     * @param subject 邮件主题
     * @param contents 邮件正文
     * @param toEmailAddress 收件人邮箱地址数组
     * @param fromEmailAddress 发件人邮箱地址
     * @param multiPaths 附件路径数组，可为空
     * @return
     * 返回值: void
     * 返回值说明: 邮件发送成功后无返回，发送失败时抛出异常
     * @throws MessagingException 邮件地址、内容或发送过程异常时抛出
     * @throws IOException 附件地址解析或读取失败时抛出
     */
    public static void sendMultiPartEmail(String host, String username, String password, String subject,
                                          String contents, String[] toEmailAddress, String fromEmailAddress,
                                          String[] multiPaths) throws MessagingException, IOException
    {
        sendMessage(createSession(host, username, password), fromEmailAddress, joinAddress(toEmailAddress), subject,
                contents, TEXT_PLAIN_GBK, multiPaths);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 23:58
     * 说明: 发送HTML格式邮件，支持单个收件人
     * @param host SMTP服务器地址
     * @param username SMTP账号名称
     * @param password SMTP账号密码，调用方不得写入日志
     * @param subject 邮件主题
     * @param contents HTML邮件正文
     * @param toEmailAddress 收件人邮箱地址
     * @param fromEmailAddress 发件人邮箱地址
     * @return
     * 返回值: void
     * 返回值说明: 邮件发送成功后无返回，发送失败时抛出异常
     * @throws MessagingException 邮件地址、内容或发送过程异常时抛出
     */
    public static void sendHtmlEmail(String host, String username, String password, String subject, String contents,
                                     String toEmailAddress, String fromEmailAddress) throws MessagingException
    {
        sendMessage(createSession(host, username, password), fromEmailAddress, toEmailAddress, subject, contents,
                TEXT_HTML_UTF8, null);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 23:58
     * 说明: 发送HTML格式邮件，支持多个收件人
     * @param host SMTP服务器地址
     * @param username SMTP账号名称
     * @param password SMTP账号密码，调用方不得写入日志
     * @param subject 邮件主题
     * @param contents HTML邮件正文
     * @param toEmailAddress 收件人邮箱地址数组
     * @param fromEmailAddress 发件人邮箱地址
     * @return
     * 返回值: void
     * 返回值说明: 邮件发送成功后无返回，发送失败时抛出异常
     * @throws MessagingException 邮件地址、内容或发送过程异常时抛出
     */
    public static void sendHtmlEmail(String host, String username, String password, String subject, String contents,
                                     String[] toEmailAddress, String fromEmailAddress) throws MessagingException
    {
        sendMessage(createSession(host, username, password), fromEmailAddress, joinAddress(toEmailAddress), subject,
                contents, TEXT_HTML_UTF8, null);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 23:58
     * 说明: 创建SMTP账号认证器
     * @param username SMTP账号名称
     * @param password SMTP账号密码，调用方不得写入日志
     * @return
     * 返回值: Authenticator
     * 返回值说明: Jakarta Mail使用的账号认证器
     */
    private static Authenticator createAuthenticator(final String username, final String password) {
        return new Authenticator() {
            @Override
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        };
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 23:58
     * 说明: 组装并发送邮件
     * @param session 邮件会话
     * @param fromEmailAddress 发件人邮箱地址
     * @param toEmailAddress 收件人邮箱地址，多个地址使用英文逗号分隔
     * @param subject 邮件主题
     * @param contents 邮件正文
     * @param contentType 正文类型和字符集
     * @param multiPaths 附件路径数组，可为空
     * @return
     * 返回值: void
     * 返回值说明: 邮件发送成功后无返回，发送失败时抛出异常
     * @throws MessagingException 邮件地址、内容或发送过程异常时抛出
     * @throws MessagingException 邮件内容组装或附件读取失败时抛出
     */
    private static void sendMessage(Session session, String fromEmailAddress, String toEmailAddress, String subject,
                                    String contents, String contentType, String[] multiPaths)
            throws MessagingException {
        MimeMessage msg = createBaseMessage(session, fromEmailAddress, toEmailAddress, subject);
        try {
            msg.setContent(createMultipart(contents, contentType, multiPaths));
        } catch (IOException e) {
            throw new MessagingException("附件读取失败", e);
        }
        Transport.send(msg);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 23:58
     * 说明: 创建邮件基础信息，统一设置发件人、收件人、主题和发送时间
     * @param session 邮件会话
     * @param fromEmailAddress 发件人邮箱地址
     * @param toEmailAddress 收件人邮箱地址，多个地址使用英文逗号分隔
     * @param subject 邮件主题
     * @return
     * 返回值: MimeMessage
     * 返回值说明: 已填充基础头信息的邮件对象
     * @throws MessagingException 邮件地址或会话异常时抛出
     */
    private static MimeMessage createBaseMessage(Session session, String fromEmailAddress, String toEmailAddress,
                                                 String subject) throws MessagingException {
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(fromEmailAddress));
        addRecipients(msg, RecipientType.TO, toEmailAddress);
        msg.setSubject(defaultString(subject), "UTF-8");
        msg.setSentDate(new Date());
        return msg;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 23:58
     * 说明: 给邮件添加收件人、抄送人或密送人
     * @param msg 邮件对象
     * @param recipientType 收件类型
     * @param address 邮箱地址，多个地址使用英文逗号分隔
     * @return
     * 返回值: void
     * 返回值说明: 地址为空时不添加，避免空地址导致发送失败
     * @throws MessagingException 邮件地址格式异常时抛出
     */
    private static void addRecipients(MimeMessage msg, RecipientType recipientType, String address)
            throws MessagingException {
        if (isBlank(address)) {
            return;
        }
        msg.addRecipients(recipientType, InternetAddress.parse(address));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 23:58
     * 说明: 创建正文和本地附件组成的多段邮件内容
     * @param contents 邮件正文
     * @param contentType 正文类型和字符集
     * @param attachBeanList 本地附件对象列表，可为空
     * @return
     * 返回值: MimeMultipart
     * 返回值说明: 可直接写入MimeMessage的多段邮件内容
     * @throws MessagingException 邮件内容组装异常时抛出
     * @throws IOException 附件读取失败时抛出
     */
    private static MimeMultipart createMultipart(String contents, String contentType, List<AttachBean> attachBeanList)
            throws MessagingException, IOException {
        MimeMultipart parts = createBodyMultipart(contents, contentType);
        if (attachBeanList != null) {
            for (AttachBean attach : attachBeanList) {
                if (attach == null || attach.getFile() == null) {
                    continue;
                }
                addFileAttachment(parts, attach.getFile(), attach.getFileName(), attach.getCid());
            }
        }
        return parts;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 23:58
     * 说明: 创建正文和附件路径组成的多段邮件内容
     * @param contents 邮件正文
     * @param contentType 正文类型和字符集
     * @param multiPaths 附件路径数组，可为空
     * @return
     * 返回值: MimeMultipart
     * 返回值说明: 可直接写入MimeMessage的多段邮件内容
     * @throws MessagingException 邮件内容组装异常时抛出
     * @throws IOException 附件地址解析或读取失败时抛出
     */
    private static MimeMultipart createMultipart(String contents, String contentType, String[] multiPaths)
            throws MessagingException, IOException {
        MimeMultipart parts = createBodyMultipart(contents, contentType);
        if (multiPaths != null) {
            for (String path : multiPaths) {
                if (isBlank(path)) {
                    continue;
                }
                addPathAttachment(parts, path);
            }
        }
        return parts;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 23:58
     * 说明: 创建邮件正文部分
     * @param contents 邮件正文
     * @param contentType 正文类型和字符集
     * @return
     * 返回值: MimeMultipart
     * 返回值说明: 已包含正文段的多段邮件内容
     * @throws MessagingException 邮件正文组装异常时抛出
     */
    private static MimeMultipart createBodyMultipart(String contents, String contentType) throws MessagingException {
        MimeMultipart parts = new MimeMultipart();
        MimeBodyPart part = new MimeBodyPart();
        part.setContent(defaultString(contents), contentType);
        parts.addBodyPart(part);
        return parts;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 23:58
     * 说明: 添加本地文件附件
     * @param parts 邮件多段内容
     * @param file 本地附件文件
     * @param fileName 邮件中展示的附件名称，为空时使用文件名
     * @param cid 内嵌资源ID，可为空
     * @return
     * 返回值: void
     * 返回值说明: 附件被追加到邮件内容中
     * @throws MessagingException 附件组装异常时抛出
     */
    private static void addFileAttachment(MimeMultipart parts, File file, String fileName, String cid)
            throws MessagingException {
        MimeBodyPart attachPart = new MimeBodyPart();
        attachPart.setDataHandler(new DataHandler(new FileDataSource(file)));
        attachPart.setFileName(encodeFileName(isBlank(fileName) ? file.getName() : fileName));
        if (!isBlank(cid)) {
            attachPart.setContentID(cid);
        }
        parts.addBodyPart(attachPart);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 23:58
     * 说明: 根据路径添加附件，本地路径直接读取，HTTP/HTTPS地址按远程资源处理
     * @param parts 邮件多段内容
     * @param path 附件路径或URL
     * @return
     * 返回值: void
     * 返回值说明: 附件被追加到邮件内容中
     * @throws MessagingException 附件组装异常时抛出
     * @throws IOException URL解析或附件读取失败时抛出
     */
    private static void addPathAttachment(MimeMultipart parts, String path) throws MessagingException, IOException {
        if (isHttpUrl(path)) {
            URL url = new URL(path);
            MimeBodyPart attachPart = new MimeBodyPart();
            attachPart.setDataHandler(new DataHandler(new URLDataSource(url)));
            attachPart.setFileName(encodeFileName(resolveUrlFileName(url)));
            parts.addBodyPart(attachPart);
            return;
        }
        File file = new File(path);
        addFileAttachment(parts, file, file.getName(), null);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 23:58
     * 说明: 合并多个收件人邮箱地址
     * @param addresses 收件人邮箱地址数组
     * @return
     * 返回值: String
     * 返回值说明: 使用英文逗号分隔的收件人地址，空数组返回空字符串
     */
    private static String joinAddress(String[] addresses) {
        if (addresses == null || addresses.length == 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (String address : addresses) {
            if (isBlank(address)) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(",");
            }
            builder.append(address.trim());
        }
        return builder.toString();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 23:58
     * 说明: 判断附件路径是否为HTTP或HTTPS地址
     * @param path 附件路径
     * @return
     * 返回值: boolean
     * 返回值说明: true-远程URL，false-本地文件路径
     */
    private static boolean isHttpUrl(String path) {
        String lowerPath = path.toLowerCase();
        return lowerPath.startsWith("http://") || lowerPath.startsWith("https://");
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 23:58
     * 说明: 从URL中解析附件文件名
     * @param url 附件URL
     * @return
     * 返回值: String
     * 返回值说明: URL路径末段文件名，无法解析时返回attachment
     */
    private static String resolveUrlFileName(URL url) {
        String path = url.getPath();
        if (isBlank(path)) {
            return "attachment";
        }
        int index = path.lastIndexOf('/');
        String fileName = index >= 0 ? path.substring(index + 1) : path;
        return isBlank(fileName) ? "attachment" : fileName;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 23:58
     * 说明: 对附件名称进行邮件头编码，避免中文文件名乱码
     * @param fileName 附件名称
     * @return
     * 返回值: String
     * 返回值说明: 已按UTF-8编码的附件名称
     * @throws MessagingException 附件名称编码失败时抛出
     */
    private static String encodeFileName(String fileName) throws MessagingException {
        try {
            return MimeUtility.encodeText(fileName, "UTF-8", null);
        } catch (Exception e) {
            throw new MessagingException("附件名称编码失败", e);
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 23:58
     * 说明: 字符串空值兜底
     * @param value 原始字符串
     * @return
     * 返回值: String
     * 返回值说明: 原始值为空时返回空字符串
     */
    private static String defaultString(String value) {
        return value == null ? "" : value;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 23:58
     * 说明: 判断字符串是否为空白
     * @param value 待判断字符串
     * @return
     * 返回值: boolean
     * 返回值说明: true-为空或只包含空白字符，false-存在有效内容
     */
    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
