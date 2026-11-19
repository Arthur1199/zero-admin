package com.zero.listener;

import lombok.extern.slf4j.Slf4j;
import com.alibaba.fastjson.JSON;
import com.zero.util.EnvironmentUtil;
import com.zero.util.TelRobotUtil;
import com.zero.vo.MicroservicesMsgVo;
import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceStatusChangedEvent;
import de.codecentric.boot.admin.server.notify.AbstractStatusChangeNotifier;
import org.apache.commons.lang3.time.DateFormatUtils;
import reactor.core.publisher.Mono;

import java.util.Date;

/**
 * 作者: Jack
 * 时间: 2023年4月21日下午3:26:40
 * 说明: 自定义监控
 *
 */
//@Component
@Slf4j
public class CustomNotifier extends AbstractStatusChangeNotifier {
	
//
//    @Value("${client.notify.mail.subject}")
//    private String subject;
//
//    @Autowired
//    EmailUtil emailUtil;

    //邮件模板内容信息
    private String content="亲爱的运维人员,您的运维管理的服务程序${服务状态},服务名称:${服务名称},服务部署ip是:${服务器ip地址},请查看及时服务运行状态！";

    public CustomNotifier(InstanceRepository repository) {
        super(repository);
    }

    @Override
    protected Mono<Void> doNotify(InstanceEvent event, Instance instance) {
        log.info("event:{}",JSON.toJSONString(event));
        log.info("instance:{}",JSON.toJSONString(instance));
        return Mono.fromRunnable(() -> {
            if (event instanceof InstanceStatusChangedEvent) {
                log.info("Instance {} ({}) is {}", instance.getRegistration().getName(), event.getInstance(),
                        ((InstanceStatusChangedEvent) event).getStatusInfo().getStatus());
                //获取服务状态 status  UP上线,OFFLINE离职
                String status = ((InstanceStatusChangedEvent) event).getStatusInfo().getStatus();
                //服务名称,
                String clientName = instance.getRegistration().getName().toLowerCase();
                //服务所在的服务器ip地址
                String clientServerUrl = instance.getRegistration().getServiceUrl();
                String statusName="OFFLINE".equals(status)?"已经宕机了":"已经重新上线";
                if ("OFFLINE".equals(status) || "UP".equals(status)) {
//                    emailUtil.sendEmail(subject + "-" + clientName + "-" + status, repelateMessage(content,statusName,clientName,clientServerUrl));
                    String dateTime = DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");
                	String errorMsg = "OFFLINE".equals(status) ? "请运维开发检查微服务" : "已正常启动运行";
                	boolean dev = EnvironmentUtil.isDev();
                	boolean test = EnvironmentUtil.isTest();
                	boolean prod = EnvironmentUtil.isProd();
                	String runEnv = "";
                	if(dev) {
                		runEnv = "本地环境";
                	}else if(test) {
                		runEnv = "测试环境";
                	}else if(prod) {
                		runEnv = "生成环境";
                	}
                	MicroservicesMsgVo msg = new MicroservicesMsgVo(runEnv,"微服务状态提醒：", statusName, clientName, clientServerUrl, dateTime, errorMsg);
//                	TelRobotUtil.sendMessage(repelateMessage(content,statusName,clientName,clientServerUrl));
                	TelRobotUtil.sendMessage(msg.toString());
                }
            } else {
                log.info("Instance {} ({}) {}", instance.getRegistration().getName(), event.getInstance(),
                        event.getType());
            }

        });
    }

    /**
     * 替换邮件模板内占位符
     */
    private String repelateMessage(String msg, String state, String appName, String serverIp) {
        msg = msg.replace("${服务状态}", state);
        msg = msg.replace( "${服务名称}", appName);
        msg =msg.replace("${服务器ip地址}", serverIp);
        return msg;
    }
    
    
}
