package com.zero.service.impl;

import com.zero.domain.FileDetail;
import com.zero.vo.FileInfo;
import com.zero.vo.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.support.SimpleTransactionStatus;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

/**
 * 验证数据库提交与外部存储清理之间的执行顺序和失败边界。
 */
class FileDetailDeleteTest
{
    private static final int FILE_ID = 1;
    private final PlatformTransactionManager transactions = mock(PlatformTransactionManager.class);
    private final SimpleTransactionStatus status = new SimpleTransactionStatus();
    private final FileStorageService storage = mock(FileStorageService.class);
    private final FileDetailServiceImpl service = spy(new FileDetailServiceImpl(transactions));

    /** 使用模拟的事务和存储平台验证删除，无需连接文件服务。 */
    @BeforeEach
    void setUp()
    {
        FileDetail detail = new FileDetail();
        detail.setId(FILE_ID);
        detail.setPlatform("mock-store");
        detail.setFilename("original.png");
        detail.setThFilename("thumbnail.png");
        when(transactions.getTransaction(any())).thenReturn(status);
        ReflectionTestUtils.setField(service, "fileStorageService", storage);
        doReturn(detail).when(service).selectFileById(FILE_ID);
        doReturn(true).when(service).removeById(FILE_ID);
    }

    /** 数据库事务提交后才使用快照清理原文件和缩略图。 */
    @Test
    void commitsRecordDeletionBeforeStorageCleanup()
    {
        when(storage.deleteStorageObjects(any(FileInfo.class))).thenReturn(true);
        assertTrue(service.deleteFileById(FILE_ID));
        InOrder order = inOrder(transactions, service, storage);
        order.verify(transactions).getTransaction(argThat(
                definition -> definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        order.verify(service).removeById(FILE_ID);
        order.verify(transactions).commit(status);
        order.verify(storage).deleteStorageObjects(argThat(info ->
                "mock-store".equals(info.getPlatform()) && "original.png".equals(info.getFilename())
                        && "thumbnail.png".equals(info.getThFilename())));
    }

    /** 存储异常只影响清理结果，数据库提交后无需回滚。 */
    @Test
    void keepsCommittedDeletionWhenStorageFails()
    {
        when(storage.deleteStorageObjects(any(FileInfo.class))).thenThrow(new IllegalStateException("mock failure"));
        assertFalse(service.deleteFileById(FILE_ID));
        InOrder order = inOrder(transactions, storage);
        order.verify(transactions).commit(status);
        order.verify(storage).deleteStorageObjects(any(FileInfo.class));
        verify(transactions, never()).rollback(any());
    }

    /** 记录没有删除时，存储对象必须保留。 */
    @Test
    void skipsStorageWhenRowDeletionFails()
    {
        doReturn(false).when(service).removeById(FILE_ID);
        assertThrows(IllegalArgumentException.class, () -> service.deleteFileById(FILE_ID));
        verify(transactions).rollback(status);
        verifyNoInteractions(storage);
    }

    /** 数据库提交失败时不能继续清理存储对象。 */
    @Test
    void skipsStorageWhenCommitFails()
    {
        doThrow(new TransactionSystemException("mock commit failure")).when(transactions).commit(status);
        assertThrows(TransactionSystemException.class, () -> service.deleteFileById(FILE_ID));
        verifyNoInteractions(storage);
    }
}
