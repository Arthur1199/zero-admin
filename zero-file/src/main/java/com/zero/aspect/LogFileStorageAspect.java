package com.zero.aspect;

import com.zero.platform.FileStorage;
import com.zero.recorder.FileRecorder;
import com.zero.vo.FileInfo;
import com.zero.vo.UploadPretreatment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.function.Consumer;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 文件存储操作日志切面。
 */
@Slf4j
@Component
public class LogFileStorageAspect implements FileStorageAspect
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 在上传前后记录存储平台、文件名和返回结果。
     * @param chain 上传切面链
     * @param fileInfo 当前文件的元数据
     * @param pre 上传预处理结果
     * @param fileStorage 本次选择的存储平台
     * @param fileRecorder 文件记录器
     * @return
     * 返回值: FileInfo
     * 返回值说明: 后续链返回的上传文件信息，失败时为 null
     */
    @Override
    public FileInfo uploadAround(UploadAspectChain chain, FileInfo fileInfo, UploadPretreatment pre,
                                 FileStorage fileStorage, FileRecorder fileRecorder)
    {
        log.info("开始上传文件，platform={}, filename={}", fileInfo.getPlatform(), fileInfo.getOriginalFilename());
        FileInfo result = chain.next(fileInfo, pre, fileStorage, fileRecorder);
        log.info("文件上传完成，success={}, url={}", result != null, result == null ? null : result.getUrl());
        return result;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 记录文件对象的删除开始时间点及处理结果。
     * @param chain 删除切面链
     * @param fileInfo 待删除文件信息
     * @param fileStorage 对象所在平台
     * @param fileRecorder 文件记录器
     * @return
     * 返回值: boolean
     * 返回值说明: 后续删除链的处理结果
     */
    @Override
    public boolean deleteAround(DeleteAspectChain chain, FileInfo fileInfo, FileStorage fileStorage,
                                FileRecorder fileRecorder)
    {
        log.info("开始删除文件，platform={}, url={}", fileInfo.getPlatform(), fileInfo.getUrl());
        boolean result = chain.next(fileInfo, fileStorage, fileRecorder);
        log.info("文件删除完成，success={}", result);
        return result;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 沿原调用链执行文件存在性检查。
     * @param chain 存在性检查切面链
     * @param fileInfo 待检查的文件信息
     * @param fileStorage 对象所在平台
     * @return
     * 返回值: boolean
     * 返回值说明: 后续检查链的结果
     */
    @Override
    public boolean existsAround(ExistsAspectChain chain, FileInfo fileInfo, FileStorage fileStorage)
    {
        return chain.next(fileInfo, fileStorage);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 记录原文件下载的存储平台和访问地址。
     * @param chain 原文件下载切面链
     * @param fileInfo 当前文件信息
     * @param fileStorage 文件所在平台
     * @param consumer 读取下载流的回调
     * @return
     * 返回值: void
     * 返回值说明: 随后执行原文件下载链
     */
    @Override
    public void downloadAround(DownloadAspectChain chain, FileInfo fileInfo, FileStorage fileStorage,
                               Consumer<InputStream> consumer)
    {
        log.info("开始下载文件，platform={}, url={}", fileInfo.getPlatform(), fileInfo.getUrl());
        chain.next(fileInfo, fileStorage, consumer);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 记录缩略图下载的存储平台和访问地址。
     * @param chain 缩略图下载切面链
     * @param fileInfo 当前文件信息
     * @param fileStorage 文件所在平台
     * @param consumer 读取下载流的回调
     * @return
     * 返回值: void
     * 返回值说明: 随后执行缩略图下载链
     */
    @Override
    public void downloadThAround(DownloadThAspectChain chain, FileInfo fileInfo, FileStorage fileStorage,
                                 Consumer<InputStream> consumer)
    {
        log.info("开始下载缩略图，platform={}, url={}", fileInfo.getPlatform(), fileInfo.getThUrl());
        chain.next(fileInfo, fileStorage, consumer);
    }
}
