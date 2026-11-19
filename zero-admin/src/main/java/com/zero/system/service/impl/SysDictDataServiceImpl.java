package com.zero.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.zero.common.core.domain.entity.SysDictData;
import com.zero.common.utils.DictUtils;
import com.zero.system.mapper.SysDictDataMapper;
import com.zero.system.service.ISysDictDataService;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 字典 业务层处理
 */
@Service
public class SysDictDataServiceImpl implements ISysDictDataService
{
    /**
     * 字典数据数据访问字段
     */
    @Autowired
    private SysDictDataMapper dictDataMapper;

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
    @Override
    public List<SysDictData> selectDictDataList(SysDictData dictData)
    {
        return dictDataMapper.selectDictDataList(dictData);
    }

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
    @Override
    public String selectDictLabel(String dictType, String dictValue)
    {
        return dictDataMapper.selectDictLabel(dictType, dictValue);
    }

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
    @Override
    public SysDictData selectDictDataById(Long dictCode)
    {
        return dictDataMapper.selectDictDataById(dictCode);
    }

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
    @Override
    public void deleteDictDataByIds(Long[] dictCodes)
    {
        for (Long dictCode : dictCodes)
        {
            SysDictData data = selectDictDataById(dictCode);
            dictDataMapper.deleteDictDataById(dictCode);
            List<SysDictData> dictDatas = dictDataMapper.selectDictDataByType(data.getDictType());
            DictUtils.setDictCache(data.getDictType(), dictDatas);
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 新增保存字典数据信息
     * @param data 字典数据信息
     * @return
     * 返回值: int
     * 返回值说明：新增保存字典数据信息
     */
    @Override
    public int insertDictData(SysDictData data)
    {
        int row = dictDataMapper.insertDictData(data);
        if (row > 0)
        {
            List<SysDictData> dictDatas = dictDataMapper.selectDictDataByType(data.getDictType());
            DictUtils.setDictCache(data.getDictType(), dictDatas);
        }
        return row;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 修改保存字典数据信息
     * @param data 字典数据信息
     * @return
     * 返回值: int
     * 返回值说明：修改保存字典数据信息
     */
    @Override
    public int updateDictData(SysDictData data)
    {
        int row = dictDataMapper.updateDictData(data);
        if (row > 0)
        {
            List<SysDictData> dictDatas = dictDataMapper.selectDictDataByType(data.getDictType());
            DictUtils.setDictCache(data.getDictType(), dictDatas);
        }
        return row;
    }
}
