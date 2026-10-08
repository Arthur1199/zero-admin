package com.zero.system.service;

public interface IAdminFileStorageModeService
{
    String CONFIG_KEY = "sys.file.storage.mode";

    String MODE_ZERO_FILE = "zero-file";

    String MODE_LOCAL = "local";

    boolean useZeroFile();
}
