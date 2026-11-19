package com.zero.utils.mail;

import java.io.File;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:42
 * 说明: 保存邮件附件文件及其展示名称，供邮件工具构造附件部分。
 */
public class AttachBean {

    private String cid;
    private File   file;
    private String fileName;

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public AttachBean() {

    }

    public AttachBean(File file, String fileName) {
        super();
        this.file = file;
        this.fileName = fileName;
    }

}
