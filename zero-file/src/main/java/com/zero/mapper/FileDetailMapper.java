package com.zero.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zero.domain.FileDetail;
import org.apache.ibatis.annotations.Mapper;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 使用 MyBatis-Plus 访问 biz_file_detail 文件记录。
 */
@Mapper
public interface FileDetailMapper extends BaseMapper<FileDetail>
{
}
