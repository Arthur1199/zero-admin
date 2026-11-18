package com.zero.jobadmin.business.mapper;

import com.zero.jobadmin.business.constant.TriggerStatus;
import com.zero.jobadmin.business.model.XxlJobInfo;
import com.zero.jobadmin.business.model.dto.JobScheduleUpdate;
import com.zero.jobadmin.business.scheduler.config.XxlJobAdminBootstrap;
import com.zero.jobadmin.business.scheduler.misfire.MisfireStrategyEnum;
import com.zero.jobadmin.business.scheduler.type.ScheduleTypeEnum;
import com.xxl.tool.core.CollectionTool;
import com.xxl.tool.core.DateTool;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// 官方集成用例可能操作数据库或执行器，普通打包按标签排除。
@org.junit.jupiter.api.Tag("job-integration")
public class XxlJobInfoMapperTest {
	private static Logger logger = LoggerFactory.getLogger(XxlJobInfoMapperTest.class);
	
	@Resource
	private XxlJobInfoMapper xxlJobInfoMapper;
	
	@Test
	public void pageList(){
		List<XxlJobInfo> list = xxlJobInfoMapper.pageList(0, 20, 0, -1, null, null, null);
		int list_count = xxlJobInfoMapper.pageListCount(0, 20, 0, -1, null, null, null);

		logger.info("", list);
		logger.info("", list_count);

		List<XxlJobInfo> list2 = xxlJobInfoMapper.getJobsByGroup(1);
	}
	
	@Test
	public void save_load(){
		XxlJobInfo info = new XxlJobInfo();
		info.setJobGroup(1);
		info.setName("desc");
		info.setAuthor("setAuthor");
		info.setAlarmEmail("setAlarmEmail");
		info.setScheduleType(ScheduleTypeEnum.FIX_RATE.name());
		info.setScheduleConf(String.valueOf(33));
		info.setMisfireStrategy(MisfireStrategyEnum.DO_NOTHING.name());
		info.setExecutorRouteStrategy("setExecutorRouteStrategy");
		info.setExecutorHandler("setExecutorHandler");
		info.setExecutorParam("setExecutorParam");
		info.setExecutorBlockStrategy("setExecutorBlockStrategy");
		info.setGlueType("setGlueType");
		info.setGlueSource("setGlueSource");
		info.setGlueRemark("setGlueRemark");
		info.setChildJobId("1");

		info.setAddTime(new Date());
		info.setUpdateTime(new Date());
		info.setGlueUpdatetime(new Date());

		int count = xxlJobInfoMapper.save(info);

		XxlJobInfo info2 = xxlJobInfoMapper.loadById(info.getId());
		info.setScheduleType(ScheduleTypeEnum.FIX_RATE.name());
		info.setScheduleConf(String.valueOf(44));
		info.setMisfireStrategy(MisfireStrategyEnum.FIRE_ONCE_NOW.name());
		info2.setName("desc2");
		info2.setAuthor("setAuthor2");
		info2.setAlarmEmail("setAlarmEmail2");
		info2.setExecutorRouteStrategy("setExecutorRouteStrategy2");
		info2.setExecutorHandler("setExecutorHandler2");
		info2.setExecutorParam("setExecutorParam2");
		info2.setExecutorBlockStrategy("setExecutorBlockStrategy2");
		info2.setGlueType("setGlueType2");
		info2.setGlueSource("setGlueSource2");
		info2.setGlueRemark("setGlueRemark2");
		info2.setGlueUpdatetime(new Date());
		info2.setChildJobId("1");

		info2.setUpdateTime(new Date());
		int item2 = xxlJobInfoMapper.update(info2);

		xxlJobInfoMapper.delete(info2.getId());

		List<XxlJobInfo> list2 = xxlJobInfoMapper.getJobsByGroup(1);

		int ret3 = xxlJobInfoMapper.findAllCount();

	}

	/**
	 *
	 * 作者: xuxueli
	 * 时间: 2016/1/12
	 * 说明: 使用初始任务快照验证调度批量游标写入和停止状态。
	 * @return
	 * 返回值: void
	 * 返回值说明: 在隔离集成环境调用 Mapper 并记录更新数量
	 */
	@Test
	public void scheduleBatchUpdateTest(){

		List<XxlJobInfo> list1 = xxlJobInfoMapper.pageList(0, 20, 0, -1, null, null, null);
		int batchSize = 5;

		// update
		List<XxlJobInfo> list2 = list1.stream().filter(item -> (item.getId()>=4 && item.getId()<=14)).toList();
		List<JobScheduleUpdate> updates = list2.stream().map(JobScheduleUpdate::capture).toList();
		list2.forEach(item -> {
			item.setTriggerLastTime(DateTool.addHours(new Date(), -1).getTime());
			item.setTriggerNextTime(DateTool.addHours(new Date(), 1).getTime());
			if (item.getId() == 5) {
				item.setTriggerStatus(TriggerStatus.STOPPED.getValue());
			}
		});

		// batch update
		List<List<JobScheduleUpdate>> scheduleListBatches = CollectionTool.split(updates, batchSize);
		for (List<JobScheduleUpdate> scheduleListBatch : scheduleListBatches) {
			int totalAffected = XxlJobAdminBootstrap.getInstance().getXxlJobInfoMapper().scheduleBatchUpdate(scheduleListBatch);
			logger.info("scheduleBatchUpdate records:" + totalAffected);
		}
	}

}
