package com.zero.queryDto;

import com.zero.base.BaseQuery;
import lombok.Getter;
import lombok.Setter;

/**
 * 文件记录查询参数。
 */
@Getter
@Setter
public class FileDetailQuery extends BaseQuery
{
    private String fileName;

    private String platform;

    private String ext;

    private String objectId;

    private String objectType;
}
