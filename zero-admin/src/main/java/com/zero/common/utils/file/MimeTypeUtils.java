package com.zero.common.utils.file;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 媒体类型工具类
 */
public class MimeTypeUtils
{
    /**
     * IMAGEPNG字段
     */
    public static final String IMAGE_PNG = "image/png";
    /**
     * IMAGEJPG字段
     */

    public static final String IMAGE_JPG = "image/jpg";
    /**
     * IMAGEJPEG字段
     */

    public static final String IMAGE_JPEG = "image/jpeg";
    /**
     * IMAGEBMP字段
     */

    public static final String IMAGE_BMP = "image/bmp";
    /**
     * IMAGEGIF字段
     */

    public static final String IMAGE_GIF = "image/gif";
    /**
     * IMAGEEXTENSION字段
     */

    public static final String[] IMAGE_EXTENSION = { "bmp", "gif", "jpg", "jpeg", "png" };
    /**
     * FLASHEXTENSION字段
     */

    public static final String[] FLASH_EXTENSION = { "swf", "flv" };

    public static final String[] MEDIA_EXTENSION = { "swf", "flv", "mp3", "wav", "wma", "wmv", "mid", "avi", "mpg",
            "asf", "rm", "rmvb" };
    /**
     * VIDEOEXTENSION字段
     */

    public static final String[] VIDEO_EXTENSION = { "mp4", "avi", "rmvb" };

    public static final String[] DEFAULT_ALLOWED_EXTENSION = {
            // 图片
            "bmp", "gif", "jpg", "jpeg", "png",
            // word excel powerpoint
            "doc", "docx", "xls", "xlsx", "ppt", "pptx", "html", "htm", "txt",
            // 压缩文件
            "rar", "zip", "gz", "bz2",
            // 视频格式
            "mp4", "avi", "rmvb",
            // pdf
            "pdf" };
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Extension
     * @param prefix prefix参数
     * @return
     * 返回值: String
     * 返回值说明：获取Extension
     */

    public static String getExtension(String prefix)
    {
        switch (prefix)
        {
            case IMAGE_PNG:
                return "png";
            case IMAGE_JPG:
                return "jpg";
            case IMAGE_JPEG:
                return "jpeg";
            case IMAGE_BMP:
                return "bmp";
            case IMAGE_GIF:
                return "gif";
            default:
                return "";
        }
    }
}
