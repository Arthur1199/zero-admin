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
 * 文件存储操作日志切面。
 */
@Slf4j
@Component
public class LogFileStorageAspect implements FileStorageAspect
{
    @Override
    public FileInfo uploadAround(UploadAspectChain chain, FileInfo fileInfo, UploadPretreatment pre,
                                 FileStorage fileStorage, FileRecorder fileRecorder)
    {
        log.info("开始上传文件，platform={}, filename={}", fileInfo.getPlatform(), fileInfo.getOriginalFilename());
        FileInfo result = chain.next(fileInfo, pre, fileStorage, fileRecorder);
        log.info("文件上传完成，success={}, url={}", result != null, result == null ? null : result.getUrl());
        return result;
    }

    @Override
    public boolean deleteAround(DeleteAspectChain chain, FileInfo fileInfo, FileStorage fileStorage,
                                FileRecorder fileRecorder)
    {
        log.info("开始删除文件，platform={}, url={}", fileInfo.getPlatform(), fileInfo.getUrl());
        boolean result = chain.next(fileInfo, fileStorage, fileRecorder);
        log.info("文件删除完成，success={}", result);
        return result;
    }

    @Override
    public boolean existsAround(ExistsAspectChain chain, FileInfo fileInfo, FileStorage fileStorage)
    {
        return chain.next(fileInfo, fileStorage);
    }

    @Override
    public void downloadAround(DownloadAspectChain chain, FileInfo fileInfo, FileStorage fileStorage,
                               Consumer<InputStream> consumer)
    {
        log.info("开始下载文件，platform={}, url={}", fileInfo.getPlatform(), fileInfo.getUrl());
        chain.next(fileInfo, fileStorage, consumer);
    }

    @Override
    public void downloadThAround(DownloadThAspectChain chain, FileInfo fileInfo, FileStorage fileStorage,
                                 Consumer<InputStream> consumer)
    {
        log.info("开始下载缩略图，platform={}, url={}", fileInfo.getPlatform(), fileInfo.getThUrl());
        chain.next(fileInfo, fileStorage, consumer);
    }
}
