package com.zero.platform;

import java.io.InputStream;
import java.util.function.Consumer;

import com.zero.vo.FileInfo;
import com.zero.vo.UploadPretreatment;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 文件存储操作接口，由各存储平台实现。
 */
public interface FileStorage {

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 获取存储平台。
     * @return
     * 返回值: String
     * 返回值说明: 按上述规则读取或解析得到的文本
     */
    String getPlatform();

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 为存储实例设置用于查找的逻辑平台名称。
     * @param platform 存储配置中的平台标识
     * @return
     * 返回值: void
     * 返回值说明: 更新当前实例的存储平台标识
     */
    void setPlatform(String platform);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 在当前平台保存原文件及可选缩略图，并补齐存储位置。
     * @param fileInfo 待补齐存储信息的文件元数据
     * @param pre 上传文件和预处理结果
     * @return
     * 返回值: boolean
     * 返回值说明: 平台保存结果
     */
    boolean save(FileInfo fileInfo,UploadPretreatment pre);


    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 在当前平台删除文件信息关联的存储对象。
     * @param fileInfo 携带平台和对象位置的文件信息
     * @return
     * 返回值: boolean
     * 返回值说明: 平台删除操作结果
     */
    boolean delete(FileInfo fileInfo);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 在当前平台检查原文件是否存在。
     * @param fileInfo 携带对象位置的文件信息
     * @return
     * 返回值: boolean
     * 返回值说明: 原文件存在时返回 true
     */
    boolean exists(FileInfo fileInfo);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 读取原文件并将输入流交给调用方消费。
     * @param fileInfo 携带原文件位置的文件信息
     * @param consumer 消费原文件输入流的回调
     * @return
     * 返回值: void
     * 返回值说明: 由回调在流可用期间消费内容
     */
    void download(FileInfo fileInfo,Consumer<InputStream> consumer);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 读取缩略图并将输入流交给调用方消费。
     * @param fileInfo 携带缩略图位置的文件信息
     * @param consumer 消费缩略图输入流的回调
     * @return
     * 返回值: void
     * 返回值说明: 由回调在流可用期间消费内容
     */
    void downloadTh(FileInfo fileInfo,Consumer<InputStream> consumer);
}

