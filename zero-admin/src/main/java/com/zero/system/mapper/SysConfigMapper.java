package com.zero.system.mapper;

import java.util.List;
import com.zero.system.domain.SysConfig;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 参数配置 数据层
 */
public interface SysConfigMapper
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 查询参数配置信息
     * @param config 参数配置信息
     * @return
     * 返回值: 参数配置信息
     * 返回值说明：查询参数配置信息
     */
    public SysConfig selectConfig(SysConfig config);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 通过ID查询配置
     * @param configId 参数ID
     * @return
     * 返回值: 参数配置信息
     * 返回值说明：通过ID查询配置
     */
    public SysConfig selectConfigById(Long configId);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 查询参数配置列表
     * @param config 参数配置信息
     * @return
     * 返回值: 参数配置集合
     * 返回值说明：查询参数配置列表
     */
    public List<SysConfig> selectConfigList(SysConfig config);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 根据键名查询参数配置信息
     * @param configKey 参数键名
     * @return
     * 返回值: 参数配置信息
     * 返回值说明：根据键名查询参数配置信息
     */
    public SysConfig checkConfigKeyUnique(String configKey);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 新增参数配置
     * @param config 参数配置信息
     * @return
     * 返回值: int
     * 返回值说明：新增参数配置
     */
    public int insertConfig(SysConfig config);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 修改参数配置
     * @param config 参数配置信息
     * @return
     * 返回值: int
     * 返回值说明：修改参数配置
     */
    public int updateConfig(SysConfig config);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 删除参数配置
     * @param configId 参数ID
     * @return
     * 返回值: int
     * 返回值说明：删除参数配置
     */
    public int deleteConfigById(Long configId);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 批量删除参数信息
     * @param configIds 需要删除的参数ID
     * @return
     * 返回值: int
     * 返回值说明：批量删除参数信息
     */
    public int deleteConfigByIds(Long[] configIds);
}
