package com.zero.jobadmin.business.controller;

import com.zero.jobadmin.framework.constant.Consts;
import com.zero.jobadmin.business.model.XxlJobGroup;
import com.zero.jobadmin.business.model.XxlJobRegistry;
import com.zero.jobadmin.business.util.ExecutorAddressUtil;
import com.zero.jobadmin.framework.util.I18nUtil;
import com.zero.jobadmin.business.mapper.XxlJobGroupMapper;
import com.zero.jobadmin.business.mapper.XxlJobInfoMapper;
import com.zero.jobadmin.business.mapper.XxlJobRegistryMapper;
import com.zero.jobadmin.framework.util.JobGroupPermissionUtil;
import com.zero.jobadmin.framework.util.XssUtil;
import com.xxl.job.core.constant.Const;
import com.xxl.job.core.constant.RegistTypeEnum;
import com.xxl.sso.core.annotation.XxlSso;
import com.xxl.tool.core.CollectionTool;
import com.xxl.tool.core.StringTool;
import com.xxl.tool.response.PageModel;
import com.xxl.tool.response.Response;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

/**
 * job group controller
 *
 * @author xuxueli 2016-10-02 20:52:56
 */
@Controller
@RequestMapping("/jobgroup")
public class JobGroupController {

	/** 执行器地址由心跳注册维护。 */
	private static final int AUTO_REGISTER = 0;
	/** 执行器地址由管理员录入。 */
	private static final int MANUAL_REGISTER = 1;

	@Resource
	public XxlJobInfoMapper xxlJobInfoMapper;
	@Resource
	public XxlJobGroupMapper xxlJobGroupMapper;
	@Resource
	private XxlJobRegistryMapper xxlJobRegistryMapper;

	@RequestMapping
	@XxlSso(role = Consts.ADMIN_ROLE)
	public String index(Model model) {
		return "business/group.list";
	}

	@RequestMapping("/pageList")
	@ResponseBody
	@XxlSso(role = Consts.ADMIN_ROLE)
	public Response<PageModel<XxlJobGroup>> pageList(@RequestParam(required = false, defaultValue = "0") int offset,
													 @RequestParam(required = false, defaultValue = "10") int pagesize,
													 String appname,
													 String name) {

		// page query
		List<XxlJobGroup> list = xxlJobGroupMapper.pageList(offset, pagesize, appname, name);
		int list_count = xxlJobGroupMapper.pageListCount(offset, pagesize, appname, name);

		// package result
		PageModel<XxlJobGroup> pageModel = new PageModel<>();
		pageModel.setData(list);
		pageModel.setTotal(list_count);

		return Response.ofSuccess(pageModel);
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/18 20:00
	 * 说明: 管理员新增执行器组，自动注册模式的地址由心跳维护。
	 * @param xxlJobGroup 执行器组配置
	 * @return
	 * 返回值: Response<String>
	 * 返回值说明: 新增结果或输入校验提示
	 */
	@RequestMapping("/insert")
	@ResponseBody
	@XxlSso(role = Consts.ADMIN_ROLE)
	public Response<String> insert(XxlJobGroup xxlJobGroup){

		// valid appname
		if (StringTool.isBlank(xxlJobGroup.getAppname())) {
			return Response.ofFail((I18nUtil.getString("system_please_input")+"AppName") );
		}
		if (xxlJobGroup.getAppname().length()<4 || xxlJobGroup.getAppname().length()>64) {
			return Response.ofFail( "AppName " + I18nUtil.getString("system_length_limit") + "4~64");
		}
		if (XssUtil.hasXss(xxlJobGroup.getAppname())) {
			return Response.ofFail( "AppName"+I18nUtil.getString("system_invalid") );
		}

		// valid title
		if (StringTool.isBlank(xxlJobGroup.getName())) {
			return Response.ofFail((I18nUtil.getString("system_please_input") + I18nUtil.getString("jobgroup_field_title")) );
		}
		if (XssUtil.hasXss(xxlJobGroup.getName())) {
			return Response.ofFail(I18nUtil.getString("jobgroup_field_title") + I18nUtil.getString("system_invalid"));
		}
		Response<String> addressCheck = validateAddresses(xxlJobGroup);
		if (!addressCheck.isSuccess()) {
			return addressCheck;
		}

		// valid appname duplicate
		if (xxlJobGroupMapper.loadByAppname(xxlJobGroup.getAppname()) != null) {
			return Response.ofFail("AppName "+I18nUtil.getString("system_repeat"));
		}

		// process
		xxlJobGroup.setUpdateTime(new Date());

		try {
			return xxlJobGroupMapper.save(xxlJobGroup) > 0 ? Response.ofSuccess() : Response.ofFail();
		} catch (DuplicateKeyException e) {
			return Response.ofFail("AppName " + I18nUtil.getString("system_repeat"));
		}
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/18 20:00
	 * 说明: 管理员修改执行器组配置，注册方式发生并发变更时拒绝过期保存。
	 * @param xxlJobGroup 执行器组配置，AppName 由数据库记录确定
	 * @return
	 * 返回值: Response<String>
	 * 返回值说明: 保存结果或输入校验提示
	 */
	@RequestMapping("/update")
	@ResponseBody
	@XxlSso(role = Consts.ADMIN_ROLE)
	public Response<String> update(XxlJobGroup xxlJobGroup){

		// load existing, appname not allowed to change
		XxlJobGroup exists = xxlJobGroupMapper.load(xxlJobGroup.getId());
		if (exists == null) {
			return Response.ofFail(I18nUtil.getString("system_opt_error"));
		}

		// valid title
		if (StringTool.isBlank(xxlJobGroup.getName())) {
			return Response.ofFail((I18nUtil.getString("system_please_input") + I18nUtil.getString("jobgroup_field_title")) );
		}
		if (XssUtil.hasXss(xxlJobGroup.getName())) {
			return Response.ofFail(I18nUtil.getString("jobgroup_field_title") + I18nUtil.getString("system_invalid"));
		}

		Response<String> addressCheck = validateAddresses(xxlJobGroup);
		if (!addressCheck.isSuccess()) {
			return addressCheck;
		}
		if (xxlJobGroup.getAddressType() == AUTO_REGISTER && exists.getAddressType() != AUTO_REGISTER) {
			xxlJobGroup.setAddressList(findRegistryByAppName(exists.getAppname()));
		}

		// process
		xxlJobGroup.setUpdateTime(new Date());

		int ret = xxlJobGroupMapper.update(xxlJobGroup, exists.getAddressType());
		return ret > 0 ? Response.ofSuccess() : Response.ofFail(I18nUtil.getString("system_opt_fail"));
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/18 20:00
	 * 说明: 切换为自动注册时，读取该应用仍有效的心跳地址。
	 * @param appname 数据库中的执行器 AppName
	 * @return
	 * 返回值: String
	 * 返回值说明: 排序去重后的地址列表，无在线节点时为空
	 */
	private String findRegistryByAppName(String appname) {
		List<XxlJobRegistry> list = xxlJobRegistryMapper.findAll(Const.DEAD_TIMEOUT, new Date());
		if (CollectionTool.isEmpty(list)) {
			return null;
		}
		List<String> addresses = list.stream()
				.filter(node -> RegistTypeEnum.EXECUTOR.name().equals(node.getRegistryGroup())
						&& Objects.equals(appname, node.getRegistryKey()))
				.map(XxlJobRegistry::getRegistryValue)
				.filter(StringTool::isNotBlank)
				.distinct().sorted().toList();
		return addresses.isEmpty() ? null : String.join(",", addresses);
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/18 20:00
	 * 说明: 校验注册方式和手动地址，保留地址顺序以维持路由优先级。
	 * @param group 要持久化的执行器组配置
	 * @return
	 * 返回值: Response<String>
	 * 返回值说明: 校验成功时地址已整理，错误时返回提示
	 */
	private Response<String> validateAddresses(XxlJobGroup group) {
		if (group.getAddressType() == AUTO_REGISTER) {
			group.setAddressList(null);
			return Response.ofSuccess();
		}
		if (group.getAddressType() != MANUAL_REGISTER) {
			return Response.ofFail(I18nUtil.getString("jobgroup_field_addressType") + I18nUtil.getString("system_invalid"));
		}
		String addressList = group.getAddressList();
		if (StringTool.isBlank(addressList)) {
			return Response.ofFail(I18nUtil.getString("jobgroup_field_addressType_limit"));
		}
		try {
			group.setAddressList(ExecutorAddressUtil.normalize(addressList));
			return Response.ofSuccess();
		} catch (IllegalArgumentException e) {
			return Response.ofFail(I18nUtil.getString("jobgroup_field_registryList_invalid"));
		}
	}

	@RequestMapping("/delete")
	@ResponseBody
	@XxlSso(role = Consts.ADMIN_ROLE)
	public Response<String> delete(@RequestParam("ids[]") List<Integer> ids){

		// parse id
		if (CollectionTool.isEmpty(ids) || ids.size()!=1) {
			return Response.ofFail(I18nUtil.getString("system_please_choose") + I18nUtil.getString("system_one") + I18nUtil.getString("system_data"));
		}
		int id = ids.get(0);

        // valid repeat operation
        XxlJobGroup xxlJobGroup = xxlJobGroupMapper.load(id);
        if (xxlJobGroup == null) {
            return Response.ofSuccess();
        }

		// whether exists job
		int count = xxlJobInfoMapper.pageListCount(0, 10, id, -1,  null, null, null);
		if (count > 0) {
			return Response.ofFail( I18nUtil.getString("jobgroup_del_limit_0") );
		}

        // whether only exists one group
		List<XxlJobGroup> allList = xxlJobGroupMapper.findAll();
		if (allList.size() == 1) {
			return Response.ofFail( I18nUtil.getString("jobgroup_del_limit_1") );
		}

        // remove group
		int ret = xxlJobGroupMapper.remove(id);
        // remove registry-data
        xxlJobRegistryMapper.removeByRegistryGroupAndKey(RegistTypeEnum.EXECUTOR.name(), xxlJobGroup.getAppname());
		return (ret>0)?Response.ofSuccess():Response.ofFail();
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/18 19:54
	 * 说明: 任务管理查看已授权执行器组的注册节点，仅返回节点列表。
	 * @param request 当前登录请求
	 * @param id 执行器组 ID
	 * @return
	 * 返回值: Response<Map<String, List<String>>>
	 * 返回值说明: registryList 是节点列表，无节点时返回空列表
	 */
	@RequestMapping("/loadById")
	@ResponseBody
	public Response<Map<String, List<String>>> loadById(HttpServletRequest request, @RequestParam("id") int id) {
		JobGroupPermissionUtil.validJobGroupPermission(request, id);
		XxlJobGroup jobGroup = xxlJobGroupMapper.load(id);
		if (jobGroup == null) {
			return Response.ofFail(I18nUtil.getString("system_opt_error"));
		}
		List<String> addresses = jobGroup.getRegistryList();
		return Response.ofSuccess(Map.of("registryList", addresses == null ? List.of() : addresses));
	}

}
