package com.zero.system.service;

import com.zero.common.core.domain.AjaxResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 统一处理管理后台通用文件、头像和文件管理上传，保留各入口的响应约定。
 */
public interface IAdminFileUploadService
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 按配置上传单个通用文件。
     * @param file 要上传的非空文件
     * @return
     * 返回值: AjaxResult
     * 返回值说明: 旧版上传组件读取的顶层字段，走文件服务时 data 同时包含文件信息
     */
    AjaxResult uploadCommonFile(MultipartFile file) throws Exception;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 按配置上传多个通用文件。
     * @param files 要上传的非空文件列表
     * @return
     * 返回值: AjaxResult
     * 返回值说明: 上传结果及旧版上传组件读取的逗号分隔字段
     */
    AjaxResult uploadCommonFiles(List<MultipartFile> files) throws Exception;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 按配置上传用户头像，仅接受允许的图片扩展名。
     * @param file 用户头像图片
     * @return
     * 返回值: String
     * 返回值说明: 远程存储返回访问地址，本地存储返回相对于资源映射的路径
     */
    String uploadAvatar(MultipartFile file) throws Exception;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 将文件管理上传交给独立文件服务。
     * @param file 要写入独立文件服务的文件
     * @return
     * 返回值: AjaxResult
     * 返回值说明: 上传结果，成功时在 data 中返回文件记录和访问地址
     */
    AjaxResult uploadManagementFile(MultipartFile file) throws Exception;
}
