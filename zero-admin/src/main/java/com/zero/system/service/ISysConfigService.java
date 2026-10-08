package com.zero.system.service;

import java.util.List;
import com.zero.system.domain.SysConfig;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 参数配置 服务层
 */
public interface ISysConfigService
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 查询参数配置信息
     * @param configId 参数配置ID
     * @return
     * 返回值: 参数配置信息
     * 返回值说明：查询参数配置信息
     */
    public SysConfig selectConfigById(Long configId);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 根据键名查询参数配置信息
     * @param configKey 参数键名
     * @return
     * 返回值: 参数键值
     * 返回值说明：根据键名查询参数配置信息
     */
    public String selectConfigByKey(String configKey);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取验证码开关
     * @return
     * 返回值: true开启，false关闭
     * 返回值说明：获取验证码开关
     */
    public boolean selectCaptchaEnabled();

    /**
     * 获取登录锁定保护开关
     *
     * @return true开启，false关闭
     */
    public boolean selectLoginLockEnabled();

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
     * 说明: 新增参数配置
     * @param config 参数配置信息
     * @return
     * 返回值: 结果
     * 返回值说明：新增参数配置
     */
    public int insertConfig(SysConfig config);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 修改参数配置
     * @param config 参数配置信息
     * @return
     * 返回值: 结果
     * 返回值说明：修改参数配置
     */
    public int updateConfig(SysConfig config);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 批量删除参数信息
     * @param configIds 需要删除的参数ID
     * @return
     * 返回值: void
     * 返回值说明：批量删除参数信息
     */
    public void deleteConfigByIds(Long[] configIds);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 加载参数缓存数据
     * @return
     * 返回值: void
     * 返回值说明：加载参数缓存数据
     */
    public void loadingConfigCache();

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 清空参数缓存数据
     * @return
     * 返回值: void
     * 返回值说明：清空参数缓存数据
     */
    public void clearConfigCache();

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 重置参数缓存数据
     * @return
     * 返回值: void
     * 返回值说明：重置参数缓存数据
     */
    public void resetConfigCache();

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 校验参数键名是否唯一
     * @param config 参数信息
     * @return
     * 返回值: 结果
     * 返回值说明：校验参数键名是否唯一
     */
    public boolean checkConfigKeyUnique(SysConfig config);
}
