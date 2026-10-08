package com.zero.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zero.domain.DictItem;
import com.zero.queryDto.DictItemQuery;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/6/28 04:33
 * 说明: 业务字典项服务接口，封装 biz_dict_item 的分页查询、详情、新增、修改、软删除和恢复业务规则
 */
public interface IDictItemService extends IService<DictItem>
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 04:33
     * 说明: 分页查询业务字典项，包含未删除和已删除记录，支持按字典组、编码、名称、值和状态筛选
     * @param query 查询条件和分页参数
     * @return
     * 返回值: IPage<DictItem>
     * 返回值说明: 分页后的业务字典项列表，delFlag用于页面区分删除状态
     */
    IPage<DictItem> selectItemPage(DictItemQuery query);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 04:33
     * 说明: 根据ID查询业务字典项详情，用于管理后台修改弹窗回显
     * @param id 字典项ID
     * @return
     * 返回值: DictItem
     * 返回值说明: 未删除的业务字典项；不存在时返回null
     */
    DictItem selectItemById(Long id);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 04:33
     * 说明: 新增业务字典项，写入前校验字典组存在、编码和值在组内唯一，并补齐审计字段
     * @param dictItem 字典项信息
     * @return
     * 返回值: boolean
     * 返回值说明: true-新增成功，false-新增失败
     */
    boolean insertItem(DictItem dictItem);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 04:33
     * 说明: 修改业务字典项，写入前校验记录存在、字典组存在、编码和值在组内唯一，并补齐审计字段
     * @param dictItem 字典项信息
     * @return
     * 返回值: boolean
     * 返回值说明: true-修改成功，false-修改失败
     */
    boolean updateItem(DictItem dictItem);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 04:33
     * 说明: 批量软删除业务字典项，删除后不再出现在业务参数设置页面
     * @param ids 字典项ID集合
     * @return
     * 返回值: boolean
     * 返回值说明: true-删除成功，false-删除失败
     */
    boolean deleteItemByIds(Long[] ids);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 06:42
     * 说明: 批量恢复已软删除的业务字典项，所属字典组必须处于未删除状态
     * @param ids 字典项ID集合
     * @return
     * 返回值: boolean
     * 返回值说明: true-恢复成功，false-恢复失败
     */
    boolean restoreItemByIds(Long[] ids);
}
