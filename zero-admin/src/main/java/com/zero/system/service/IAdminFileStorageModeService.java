package com.zero.system.service;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 决定管理后台通用上传和头像上传的存储位置。
 */
public interface IAdminFileStorageModeService
{
    /** 系统参数键，读取通用及头像上传的存储方式。 */
    String CONFIG_KEY = "sys.file.storage.mode";

    /** 使用独立文件服务的约定配置值，也是默认存储方式。 */
    String MODE_ZERO_FILE = "zero-file";

    /** 使用管理后台本地磁盘的配置值，比较时忽略大小写及首尾空白。 */
    String MODE_LOCAL = "local";

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 判断通用上传和头像上传是否走独立文件服务。
     * @return
     * 返回值: boolean
     * 返回值说明: 为 true 时走文件服务，为 false 时写入本地磁盘
     */
    boolean useZeroFile();
}
