package com.zero.listener;



import lombok.extern.slf4j.Slf4j;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;

import com.zero.util.SlackRobotUtil;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.zero.util.EnvironmentUtil;
import com.zero.vo.MicroservicesMsgVo;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceStatusChangedEvent;
import de.codecentric.boot.admin.server.notify.AbstractStatusChangeNotifier;
import reactor.core.publisher.Mono;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 按实例状态筛选 Boot Admin 事件，生成包含环境、实例及状态说明的 Slack 通知。
 */
@Slf4j
@Component
public class SlackRobotNotifier extends AbstractStatusChangeNotifier {

	
  /** 状态过滤表，格式为来源状态:目标状态，过滤代码同时尝试匹配通配状态。 */
  private String[] ignoreChanges = new String[]{"UNKNOWN:UP","DOWN:UP","OFFLINE:UP"};

  public SlackRobotNotifier(InstanceRepository repository) {
    super(repository);
  }

  /**
   *
   * 作者: Arthur 亚瑟
   * 时间: 2026/11/20 01:36
   * 说明: 仅对状态变更事件执行过滤，依次检查完整状态和含通配符的规则。
   * @param event Boot Admin 实例事件
   * @param instance 事件所属的实例
   * @return
   * 返回值: boolean
   * 返回值说明: 当前事件是否进入通知处理
   */
  @Override
  protected boolean shouldNotify(InstanceEvent event, Instance instance) {
    if (!(event instanceof InstanceStatusChangedEvent)) {
      return false;
    } else {
      InstanceStatusChangedEvent statusChange = (InstanceStatusChangedEvent)event;
      String from = this.getLastStatus(event.getInstance());
      String to = statusChange.getStatusInfo().getStatus();
      return Arrays.binarySearch(this.ignoreChanges, from + ":" + to) < 0 && Arrays.binarySearch(this.ignoreChanges, "*:" + to) < 0 && Arrays.binarySearch(this.ignoreChanges, from + ":*") < 0;
    }
  }

  /**
   *
   * 作者: Arthur 亚瑟
   * 时间: 2026/11/20 01:36
   * 说明: 在响应式任务执行时将实例状态、环境和服务地址组装成 Slack 提醒。
   * @param event Boot Admin 实例事件
   * @param instance 提供服务名称、地址和状态详情的实例
   * @return
   * 返回值: Mono<Void>
   * 返回值说明: 执行通知发送的任务，完成后无返回体
   */
  @Override
  protected Mono<Void> doNotify(InstanceEvent event, Instance instance) {
    return Mono.fromRunnable(() -> {
//      if (!nacosConfigService.getIsopen()){
//        return;
//      }
//      String watchapplications = nacosConfigService.getWatchapplications();
//      Boolean flag=watchapplications.contains(instance.getRegistration().getName());
//      if (!flag){
//        return;
//      }
      if (event instanceof InstanceStatusChangedEvent) {
        log.info("Instance {} ({}) is {}", instance.getRegistration().getName(),
            event.getInstance(),
            ((InstanceStatusChangedEvent) event).getStatusInfo().getStatus());

        String status = ((InstanceStatusChangedEvent) event).getStatusInfo().getStatus();
        //服务名称,
        String clientName = instance.getRegistration().getName().toLowerCase();
//        String statusName="OFFLINE".equals(status)?"已经宕机了":"已经重新上线";
      //服务所在的服务器ip地址
        String clientServerUrl = instance.getRegistration().getServiceUrl();
//        String dateTime = DateUtil.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss");
        String dateTime = getDataStr(new Date());
        boolean dev = EnvironmentUtil.isDev();
    	boolean test = EnvironmentUtil.isTest();
    	boolean prod = EnvironmentUtil.isProd();
    	//详情
    	String details = JSONObject.toJSONString(instance.getStatusInfo().getDetails());
    	log.info("微服务：{},状态详情：{}",clientName,details);
    	 String runEnv = "";
    	 String statusName="";
    	 String errorMsg="";
    	if(dev) {
    		runEnv = "本地环境";
    	}else if(test) {
    		runEnv = "测试环境";
    	}else if(prod) {
    		runEnv = "生产环境";
    	}
        switch (status) {
          // 健康检查没通过
          case "DOWN":
            log.info("发送健康检查没通过的通知！");
            statusName="健康检查未通过";
        	errorMsg = "请运维开发检查微服务";
            break;
          // 服务离线
          case "OFFLINE":
            log.info("发送服务离线的通知！");
            statusName="已经宕机了";
        	errorMsg = "请运维开发检查微服务";
            break;
          //服务上线
          case "UP":
            log.info("发送服务上线的通知！");
            statusName="已经重新上线";
        	errorMsg = "已正常启动运行";
            break;
          // 服务未知异常
          case "UNKNOWN":
            log.info("发送服务未知异常的通知！");
            statusName="微服务状态未知";
        	errorMsg = "请运维开发检查微服务";
            break;
          default:
            break;
        }
        MicroservicesMsgVo msg = new MicroservicesMsgVo(runEnv,"微服务状态提醒：", statusName, clientName, clientServerUrl, dateTime, errorMsg);
//       	TelRobotUtil.sendMessage(msg.toString());
          SlackRobotUtil.sendMsg(msg.toString());
      } else {
        log.info("Instance {} ({}) {}", instance.getRegistration().getName(), event.getInstance(),
            event.getType());
      }
    });
  }

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 将通知时间转换为上海时区的展示文本。
	 * @param date 实例通知使用的时间
	 * @return
	 * 返回值: String
	 * 返回值说明: yyyy-MM-dd HH:mm:ss 格式的时间字符串
	 */
	private String getDataStr(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
		String formattedDate = sdf.format(date);
		return formattedDate;
	}


}