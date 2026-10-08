package com.zero.listener;



import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;

import com.zero.util.SlackRobotUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * 作者: Jack
 * 时间: 2024年9月7日12:29:41
 * 说明: telegram机器人通知，自定义监控
 *
 */
@Component
public class SlackRobotNotifier extends AbstractStatusChangeNotifier {

   private static final Logger log = LoggerFactory.getLogger(SlackRobotNotifier.class);
	
  private String[] ignoreChanges = new String[]{"UNKNOWN:UP","DOWN:UP","OFFLINE:UP"};

  public SlackRobotNotifier(InstanceRepository repository) {
    super(repository);
  }

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
	 * 作者:Jack 日期:2025年4月22日20:05:55
	 * 
	 * @param date
	 * @return 返回值:String 返回值说明:时间格式化，北京时间
	 */
	private String getDataStr(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
		String formattedDate = sdf.format(date);
		return formattedDate;
	}


}