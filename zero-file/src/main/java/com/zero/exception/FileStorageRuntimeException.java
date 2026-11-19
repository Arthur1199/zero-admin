package com.zero.exception;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: FileStorage 运行时异常。
 */
@SuppressWarnings("serial")
public class FileStorageRuntimeException extends RuntimeException {
    public FileStorageRuntimeException() {
    }

    public FileStorageRuntimeException(String message) {
        super(message);
    }

    public FileStorageRuntimeException(String message,Throwable cause) {
        super(message,cause);
    }

    public FileStorageRuntimeException(Throwable cause) {
        super(cause);
    }

    public FileStorageRuntimeException(String message,Throwable cause,boolean enableSuppression,boolean writableStackTrace) {
        super(message,cause,enableSuppression,writableStackTrace);
    }
}

