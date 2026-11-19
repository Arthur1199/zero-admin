package com.zero.jobadmin.business.service;

import com.zero.jobadmin.business.model.XxlJobInfo;
import com.xxl.sso.core.model.LoginInfo;
import com.xxl.tool.response.PageModel;
import com.xxl.tool.response.Response;

import java.util.Date;
import java.util.Map;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 定义调度任务列表、维护、启停、手动触发及报表统计接口。
 */
public interface XxlJobService {

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 按执行器组、状态和任务条件查询调度任务。
	 * @param offset 分页偏移量，从 0 开始
	 * @param pagesize 每页最多记录数
	 * @param jobGroup 执行器组 ID，小于等于 0 时不限
	 * @param triggerStatus 触发状态，小于 0 时不限
	 * @param name 任务名称筛选
	 * @param executorHandler 执行器处理器筛选
	 * @param author 任务负责人筛选
	 * @return
	 * 返回值: Response<PageModel<XxlJobInfo>>
	 * 返回值说明: 任务分页结果及符合条件的总数
	 */
	public Response<PageModel<XxlJobInfo>> pageList(int offset, int pagesize, int jobGroup, int triggerStatus, String name, String executorHandler, String author);

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 校验执行器组权限及任务配置，保存调度任务。
	 * @param jobInfo 新任务配置
	 * @param loginInfo 调度控制台登录信息
	 * @return
	 * 返回值: Response<String>
	 * 返回值说明: 创建结果或校验失败原因
	 */
	public Response<String> add(XxlJobInfo jobInfo, LoginInfo loginInfo);

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 校验执行器组权限，更新任务定义及与之相关的调度配置。
	 * @param jobInfo 含主键的修改参数
	 * @param loginInfo 调度控制台登录信息
	 * @return
	 * 返回值: Response<String>
	 * 返回值说明: 修改结果或校验失败原因
	 */
	public Response<String> update(XxlJobInfo jobInfo, LoginInfo loginInfo);

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 删除任务及其相关记录，已不存在的任务按操作成功处理。
	 * @param id 任务主键
	 * @param loginInfo 调度控制台登录信息
	 * @return
	 * 返回值: Response<String>
	 * 返回值说明: 删除结果或执行器组权限错误
	 */
	public Response<String> remove(int id, LoginInfo loginInfo);

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 校验任务和执行器组权限，启动自动调度并计算首次触发时间。
	 * @param id 任务主键
	 * @param loginInfo 调度控制台登录信息
	 * @return
	 * 返回值: Response<String>
	 * 返回值说明: 启动结果或失败原因
	 */
	public Response<String> start(int id, LoginInfo loginInfo);

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 校验任务和执行器组权限，停止后续自动调度。
	 * @param id 任务主键
	 * @param loginInfo 调度控制台登录信息
	 * @return
	 * 返回值: Response<String>
	 * 返回值说明: 停止结果或失败原因
	 */
	public Response<String> stop(int id, LoginInfo loginInfo);

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 校验执行器组权限后提交一次人工触发，可为本次覆盖参数或地址。
	 * @param loginInfo 调度控制台登录信息
	 * @param jobId 任务主键
	 * @param executorParam 本次运行参数
	 * @param addressList 本次运行指定的执行器地址列表
	 * @return
	 * 返回值: Response<String>
	 * 返回值说明: 触发请求提交结果
	 */
	public Response<String> trigger(LoginInfo loginInfo, int jobId, String executorParam, String addressList);

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 汇总任务数、调度统计和在线执行器地址数。
	 * @return
	 * 返回值: Map<String, Object>
	 * 返回值说明: 调度控制台首页概览数据
	 */
	public Map<String,Object> dashboardInfo();

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 查询日期区间内的调度次数趋势及区间合计。
	 * @param startDate 起始统计日期
	 * @param endDate 结束统计日期
	 * @return
	 * 返回值: Response<Map<String, Object>>
	 * 返回值说明: 运行中、成功和失败的按日数据及合计
	 */
	public Response<Map<String,Object>> chartInfo(Date startDate, Date endDate);

}
