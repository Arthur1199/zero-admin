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
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 将 Boot Admin 实例的上线和离线事件转换成 Telegram 监控通知。供需要 Telegram 通知的调用方手动装配。
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
                // 当前通知只处理上线和离线状态，其他变化仅保留事件日志。
                String status = ((InstanceStatusChangedEvent) event).getStatusInfo().getStatus();
                //服务名称,
                String clientName = instance.getRegistration().getName().toLowerCase();
                // 注册信息提供的服务地址，可能包含协议、域名及端口。
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
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 用监控事件的数据替换通知模板中的状态、服务名及服务地址占位符。
     * @param msg 待渲染的通知模板正文
     * @param state 服务状态的展示文本
     * @param appName 服务注册名称
     * @param serverIp 模板中所需的服务地址
     * @return
     * 返回值: String
     * 返回值说明: 替换后可用于通知的文本
     */
    private String repelateMessage(String msg, String state, String appName, String serverIp) {
        msg = msg.replace("${服务状态}", state);
        msg = msg.replace( "${服务名称}", appName);
        msg =msg.replace("${服务器ip地址}", serverIp);
        return msg;
    }
    
    
}
