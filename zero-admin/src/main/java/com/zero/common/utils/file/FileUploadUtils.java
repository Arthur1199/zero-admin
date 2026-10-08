package com.zero.common.utils.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Objects;
import org.apache.commons.io.FilenameUtils;
import org.springframework.web.multipart.MultipartFile;
import com.zero.common.config.ZeroConfig;
import com.zero.common.constant.Constants;
import com.zero.common.exception.file.FileNameLengthLimitExceededException;
import com.zero.common.exception.file.FileSizeLimitExceededException;
import com.zero.common.exception.file.InvalidExtensionException;
import com.zero.common.utils.DateUtils;
import com.zero.common.utils.StringUtils;
import com.zero.common.utils.uuid.IdUtils;
import com.zero.common.utils.uuid.Seq;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 文件上传工具类
 */
public class FileUploadUtils
{
    /**
     * 默认大小 50M
     */
    public static final long DEFAULT_MAX_SIZE = 50 * 1024 * 1024L;

    /**
     * 默认的文件名最大长度 100
     */
    public static final int DEFAULT_FILE_NAME_LENGTH = 100;

    /**
     * 默认上传的地址
     */
    private static String defaultBaseDir = ZeroConfig.getProfile();
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置DefaultBaseDir
     * @param defaultBaseDir defaultBaseDir参数
     * @return
     * 返回值: void
     * 返回值说明：设置DefaultBaseDir
     */

    public static void setDefaultBaseDir(String defaultBaseDir)
    {
        FileUploadUtils.defaultBaseDir = defaultBaseDir;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取DefaultBaseDir
     * @return
     * 返回值: String
     * 返回值说明：获取DefaultBaseDir
     */

    public static String getDefaultBaseDir()
    {
        return defaultBaseDir;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 以默认配置进行文件上传
     * @param file 上传的文件
     * @return
     * 返回值: 文件名称
     * 返回值说明：以默认配置进行文件上传
     */
    public static final String upload(MultipartFile file) throws IOException
    {
        try
        {
            return upload(getDefaultBaseDir(), file, MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION);
        }
        catch (Exception e)
        {
            throw new IOException(e.getMessage(), e);
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 根据文件路径上传
     * @param baseDir 相对应用的基目录
     * @param file 上传的文件
     * @return
     * 返回值: 文件名称
     * 返回值说明：根据文件路径上传
     */
    public static final String upload(String baseDir, MultipartFile file) throws IOException
    {
        try
        {
            return upload(baseDir, file, MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION);
        }
        catch (Exception e)
        {
            throw new IOException(e.getMessage(), e);
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 文件上传
     * @param baseDir 相对应用的基目录
     * @param file 上传的文件
     * @param allowedExtension 上传文件类型
     * @return
     * 返回值: 返回上传成功的文件名
     * 返回值说明：文件上传
     */
    public static final String upload(String baseDir, MultipartFile file, String[] allowedExtension)
            throws FileSizeLimitExceededException, IOException, FileNameLengthLimitExceededException,
            InvalidExtensionException
    {
        return upload(baseDir, file, allowedExtension, false);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 文件上传
     * @param baseDir 相对应用的基目录
     * @param file 上传的文件
     * @param allowedExtension 上传文件类型
     * @param useCustomNaming 系统自定义文件名
     * @return
     * 返回值: 返回上传成功的文件名
     * 返回值说明：文件上传
     */
    public static final String upload(String baseDir, MultipartFile file, String[] allowedExtension, boolean useCustomNaming)
            throws FileSizeLimitExceededException, IOException, FileNameLengthLimitExceededException,
            InvalidExtensionException
    {
        int fileNameLength = Objects.requireNonNull(file.getOriginalFilename()).length();
        if (fileNameLength > FileUploadUtils.DEFAULT_FILE_NAME_LENGTH)
        {
            throw new FileNameLengthLimitExceededException(FileUploadUtils.DEFAULT_FILE_NAME_LENGTH);
        }

        assertAllowed(file, allowedExtension);

        String fileName = useCustomNaming ? uuidFilename(file) : extractFilename(file);

        String absPath = getAbsoluteFile(baseDir, fileName).getAbsolutePath();
        file.transferTo(Paths.get(absPath));
        return getPathFileName(baseDir, fileName);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 编码文件名(日期格式目录 + 原文件名 + 序列值 + 后缀)
     * @param file 文件参数
     * @return
     * 返回值: String
     * 返回值说明：编码文件名(日期格式目录 + 原文件名 + 序列值 + 后缀)
     */
    public static final String extractFilename(MultipartFile file)
    {
        return StringUtils.format("{}/{}_{}.{}", DateUtils.datePath(), FilenameUtils.getBaseName(file.getOriginalFilename()), Seq.getId(Seq.uploadSeqType), getExtension(file));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 编编码文件名(日期格式目录 + UUID + 后缀)
     * @param file 文件参数
     * @return
     * 返回值: String
     * 返回值说明：编编码文件名(日期格式目录 + UUID + 后缀)
     */
    public static final String uuidFilename(MultipartFile file)
    {
        return StringUtils.format("{}/{}.{}", DateUtils.datePath(), IdUtils.fastSimpleUUID(), getExtension(file));
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Absolute文件
     * @param uploadDir 上传Dir参数
     * @param fileName 文件名称参数
     * @return
     * 返回值: File
     * 返回值说明：获取Absolute文件
     */

    public static final File getAbsoluteFile(String uploadDir, String fileName) throws IOException
    {
        File desc = new File(uploadDir + File.separator + fileName);

        if (!desc.exists())
        {
            if (!desc.getParentFile().exists())
            {
                desc.getParentFile().mkdirs();
            }
        }
        return desc;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取路径文件名称
     * @param uploadDir 上传Dir参数
     * @param fileName 文件名称参数
     * @return
     * 返回值: String
     * 返回值说明：获取路径文件名称
     */

    public static final String getPathFileName(String uploadDir, String fileName) throws IOException
    {
        int dirLastIndex = ZeroConfig.getProfile().length() + 1;
        String currentDir = StringUtils.substring(uploadDir, dirLastIndex);
        return Constants.RESOURCE_PREFIX + "/" + currentDir + "/" + fileName;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 文件大小校验
     * @param file 上传的文件
     * @param allowedExtension allowedExtension参数
     * @return
     * 返回值: void
     * 返回值说明：文件大小校验
     */
    public static final void assertAllowed(MultipartFile file, String[] allowedExtension)
            throws FileSizeLimitExceededException, InvalidExtensionException
    {
        long size = file.getSize();
        if (size > DEFAULT_MAX_SIZE)
        {
            throw new FileSizeLimitExceededException(DEFAULT_MAX_SIZE / 1024 / 1024);
        }

        String fileName = file.getOriginalFilename();
        String extension = getExtension(file);
        if (allowedExtension != null && !isAllowedExtension(extension, allowedExtension))
        {
            if (allowedExtension == MimeTypeUtils.IMAGE_EXTENSION)
            {
                throw new InvalidExtensionException.InvalidImageExtensionException(allowedExtension, extension,
                        fileName);
            }
            else if (allowedExtension == MimeTypeUtils.FLASH_EXTENSION)
            {
                throw new InvalidExtensionException.InvalidFlashExtensionException(allowedExtension, extension,
                        fileName);
            }
            else if (allowedExtension == MimeTypeUtils.MEDIA_EXTENSION)
            {
                throw new InvalidExtensionException.InvalidMediaExtensionException(allowedExtension, extension,
                        fileName);
            }
            else if (allowedExtension == MimeTypeUtils.VIDEO_EXTENSION)
            {
                throw new InvalidExtensionException.InvalidVideoExtensionException(allowedExtension, extension,
                        fileName);
            }
            else
            {
                throw new InvalidExtensionException(allowedExtension, extension, fileName);
            }
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 判断MIME类型是否是允许的MIME类型
     * @param extension extension参数
     * @param allowedExtension allowedExtension参数
     * @return
     * 返回值: boolean
     * 返回值说明：判断MIME类型是否是允许的MIME类型
     */
    public static final boolean isAllowedExtension(String extension, String[] allowedExtension)
    {
        for (String str : allowedExtension)
        {
            if (str.equalsIgnoreCase(extension))
            {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取文件名的后缀
     * @param file 表单文件
     * @return
     * 返回值: 后缀名
     * 返回值说明：获取文件名的后缀
     */
    public static final String getExtension(MultipartFile file)
    {
        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        if (StringUtils.isEmpty(extension))
        {
            extension = MimeTypeUtils.getExtension(Objects.requireNonNull(file.getContentType()));
        }
        return extension;
    }
}
