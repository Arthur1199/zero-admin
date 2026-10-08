package com.zero.system.service;

import com.zero.common.core.domain.AjaxResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IAdminFileUploadService
{
    AjaxResult uploadCommonFile(MultipartFile file) throws Exception;

    AjaxResult uploadCommonFiles(List<MultipartFile> files) throws Exception;

    String uploadAvatar(MultipartFile file) throws Exception;

    AjaxResult uploadManagementFile(MultipartFile file) throws Exception;
}
