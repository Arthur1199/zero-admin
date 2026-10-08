package com.zero.system.service;

import java.util.List;
import com.zero.common.core.domain.entity.SysDictData;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 字典 业务层
 */
public interface ISysDictDataService
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 根据条件分页查询字典数据
     * @param dictData 字典数据信息
     * @return
     * 返回值: 字典数据集合信息
     * 返回值说明：根据条件分页查询字典数据
     */
    public List<SysDictData> selectDictDataList(SysDictData dictData);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 根据字典类型和字典键值查询字典数据信息
     * @param dictType 字典类型
     * @param dictValue 字典键值
     * @return
     * 返回值: 字典标签
     * 返回值说明：根据字典类型和字典键值查询字典数据信息
     */
    public String selectDictLabel(String dictType, String dictValue);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 根据字典数据ID查询信息
     * @param dictCode 字典数据ID
     * @return
     * 返回值: 字典数据
     * 返回值说明：根据字典数据ID查询信息
     */
    public SysDictData selectDictDataById(Long dictCode);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 批量删除字典数据信息
     * @param dictCodes 需要删除的字典数据ID
     * @return
     * 返回值: void
     * 返回值说明：批量删除字典数据信息
     */
    public void deleteDictDataByIds(Long[] dictCodes);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 新增保存字典数据信息
     * @param dictData 字典数据信息
     * @return
     * 返回值: 结果
     * 返回值说明：新增保存字典数据信息
     */
    public int insertDictData(SysDictData dictData);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 修改保存字典数据信息
     * @param dictData 字典数据信息
     * @return
     * 返回值: 结果
     * 返回值说明：修改保存字典数据信息
     */
    public int updateDictData(SysDictData dictData);
}
