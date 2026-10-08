package com.zero.common.exception.file;

import java.util.Arrays;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 文件上传 误异常类
 */
public class InvalidExtensionException extends FileUploadException
{
    private static final long serialVersionUID = 1L;
    /**
     * allowedExtension字段
     */

    private String[] allowedExtension;
    /**
     * extension字段
     */
    private String extension;
    /**
     * filename字段
     */
    private String filename;
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: InvalidExtension异常
     * @param allowedExtension allowedExtension参数
     * @param extension extension参数
     * @param filename filename参数
     */

    public InvalidExtensionException(String[] allowedExtension, String extension, String filename)
    {
        super("文件[" + filename + "]后缀[" + extension + "]不正确，请上传" + Arrays.toString(allowedExtension) + "格式");
        this.allowedExtension = allowedExtension;
        this.extension = extension;
        this.filename = filename;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取AllowedExtension
     * @return
     * 返回值: String[]
     * 返回值说明：获取AllowedExtension
     */

    public String[] getAllowedExtension()
    {
        return allowedExtension;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Extension
     * @return
     * 返回值: String
     * 返回值说明：获取Extension
     */

    public String getExtension()
    {
        return extension;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Filename
     * @return
     * 返回值: String
     * 返回值说明：获取Filename
     */

    public String getFilename()
    {
        return filename;
    }

    public static class InvalidImageExtensionException extends InvalidExtensionException
    {
        private static final long serialVersionUID = 1L;

        /**
         *
         * 作者: Arthur 亚瑟
         * 时间: 2026/5/16 07:16
         * 说明: 图片文件扩展名非法异常构造方法
         * @param allowedExtension 允许上传的图片扩展名
         * @param extension 当前文件扩展名
         * @param filename 当前文件名
         * @return
         * 返回值: InvalidImageExtensionException
         * 返回值说明：创建图片文件扩展名非法异常
         */
        public InvalidImageExtensionException(String[] allowedExtension, String extension, String filename)
        {
            super(allowedExtension, extension, filename);
        }
    }

    public static class InvalidFlashExtensionException extends InvalidExtensionException
    {
        private static final long serialVersionUID = 1L;

        /**
         *
         * 作者: Arthur 亚瑟
         * 时间: 2026/5/16 07:16
         * 说明: Flash文件扩展名非法异常构造方法
         * @param allowedExtension 允许上传的Flash扩展名
         * @param extension 当前文件扩展名
         * @param filename 当前文件名
         * @return
         * 返回值: InvalidFlashExtensionException
         * 返回值说明：创建Flash文件扩展名非法异常
         */
        public InvalidFlashExtensionException(String[] allowedExtension, String extension, String filename)
        {
            super(allowedExtension, extension, filename);
        }
    }

    public static class InvalidMediaExtensionException extends InvalidExtensionException
    {
        private static final long serialVersionUID = 1L;

        /**
         *
         * 作者: Arthur 亚瑟
         * 时间: 2026/5/16 07:16
         * 说明: 媒体文件扩展名非法异常构造方法
         * @param allowedExtension 允许上传的媒体扩展名
         * @param extension 当前文件扩展名
         * @param filename 当前文件名
         * @return
         * 返回值: InvalidMediaExtensionException
         * 返回值说明：创建媒体文件扩展名非法异常
         */
        public InvalidMediaExtensionException(String[] allowedExtension, String extension, String filename)
        {
            super(allowedExtension, extension, filename);
        }
    }

    public static class InvalidVideoExtensionException extends InvalidExtensionException
    {
        private static final long serialVersionUID = 1L;

        /**
         *
         * 作者: Arthur 亚瑟
         * 时间: 2026/5/16 07:16
         * 说明: 视频文件扩展名非法异常构造方法
         * @param allowedExtension 允许上传的视频扩展名
         * @param extension 当前文件扩展名
         * @param filename 当前文件名
         * @return
         * 返回值: InvalidVideoExtensionException
         * 返回值说明：创建视频文件扩展名非法异常
         */
        public InvalidVideoExtensionException(String[] allowedExtension, String extension, String filename)
        {
            super(allowedExtension, extension, filename);
        }
    }
}
