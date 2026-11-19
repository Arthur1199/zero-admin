package com.zero.system.service;

import java.util.List;
import com.zero.common.core.domain.entity.SysDictData;
import com.zero.common.core.domain.entity.SysDictType;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 字典 业务层
 */
public interface ISysDictTypeService
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 根据条件分页查询字典类型
     * @param dictType 字典类型信息
     * @return
     * 返回值: 字典类型集合信息
     * 返回值说明：根据条件分页查询字典类型
     */
    public List<SysDictType> selectDictTypeList(SysDictType dictType);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 根据所有字典类型
     * @return
     * 返回值: 字典类型集合信息
     * 返回值说明：根据所有字典类型
     */
    public List<SysDictType> selectDictTypeAll();

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 根据字典类型查询字典数据
     * @param dictType 字典类型
     * @return
     * 返回值: 字典数据集合信息
     * 返回值说明：根据字典类型查询字典数据
     */
    public List<SysDictData> selectDictDataByType(String dictType);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 根据字典类型ID查询信息
     * @param dictId 字典类型ID
     * @return
     * 返回值: 字典类型
     * 返回值说明：根据字典类型ID查询信息
     */
    public SysDictType selectDictTypeById(Long dictId);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 根据字典类型查询信息
     * @param dictType 字典类型
     * @return
     * 返回值: 字典类型
     * 返回值说明：根据字典类型查询信息
     */
    public SysDictType selectDictTypeByType(String dictType);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 批量删除字典信息
     * @param dictIds 需要删除的字典ID
     * @return
     * 返回值: void
     * 返回值说明：批量删除字典信息
     */
    public void deleteDictTypeByIds(Long[] dictIds);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 加载字典缓存数据
     * @return
     * 返回值: void
     * 返回值说明：加载字典缓存数据
     */
    public void loadingDictCache();

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 清空字典缓存数据
     * @return
     * 返回值: void
     * 返回值说明：清空字典缓存数据
     */
    public void clearDictCache();

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 重置字典缓存数据
     * @return
     * 返回值: void
     * 返回值说明：重置字典缓存数据
     */
    public void resetDictCache();

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 新增保存字典类型信息
     * @param dictType 字典类型信息
     * @return
     * 返回值: int
     * 返回值说明：新增保存字典类型信息
     */
    public int insertDictType(SysDictType dictType);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 修改保存字典类型信息
     * @param dictType 字典类型信息
     * @return
     * 返回值: int
     * 返回值说明：修改保存字典类型信息
     */
    public int updateDictType(SysDictType dictType);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 校验字典类型称是否唯一
     * @param dictType 字典类型
     * @return
     * 返回值: boolean
     * 返回值说明：校验字典类型称是否唯一
     */
    public boolean checkDictTypeUnique(SysDictType dictType);
}
