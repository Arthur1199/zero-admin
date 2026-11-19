package com.zero.config;

import lombok.extern.slf4j.Slf4j;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.cloud.client.discovery.event.HeartbeatEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.SmartLifecycle;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.pojo.ListView;


/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 为 Boot Admin 定期同步 Nacos 服务清单并维护服务订阅，清单或实例发生变化时发布 Spring 心跳事件。
 */
@Slf4j(topic = "com.zero.listener.CustomNotifier")
public class NacosWatch implements ApplicationEventPublisherAware, SmartLifecycle {

	
  /** 两次服务清单轮询之间的固定延时，单位为毫秒。 */
  private long watchDelay = 30000;

  private final NacosDiscoveryProperties properties;
  private final TaskScheduler taskScheduler;
  /** 发布 Spring 心跳时使用的递增标记。 */
  private final AtomicLong nacosWatchIndex;
  /** 用于防止重复创建或重复取消轮询任务的内部状态。 */
  private final AtomicBoolean running;
  private ApplicationEventPublisher publisher;
  private ScheduledFuture<?> watchFuture;
  /** 上一轮轮询保留的服务名称集合，用于识别服务变更。 */
  private Set<String> cacheServices;
  /** 按服务名称记录已注册的监听器，服务移除时用于取消订阅。 */
  private HashMap<String, EventListener> subscribeListeners;

  /**
   *
   * 作者: Arthur 亚瑟
   * 时间: 2026/11/20 01:36
   * 说明: 使用内部创建的任务调度器构造 Nacos 观察器。
   * @param properties Nacos 服务发现配置
   */
  public NacosWatch(NacosDiscoveryProperties properties) {
    this(properties, getTaskScheduler());
  }

  /**
   *
   * 作者: Arthur 亚瑟
   * 时间: 2026/11/20 01:36
   * 说明: 使用调用方提供的任务调度器构造 Nacos 观察器。
   * @param properties Nacos 服务发现配置
   * @param taskScheduler 用于安排周期轮询的调度器
   */
  public NacosWatch(NacosDiscoveryProperties properties, TaskScheduler taskScheduler) {
    this.nacosWatchIndex = new AtomicLong(0L);
    this.running = new AtomicBoolean(false);
    this.cacheServices = new HashSet();
    this.subscribeListeners = new HashMap();
    this.properties = properties;
    this.taskScheduler = taskScheduler;
  }

  private static ThreadPoolTaskScheduler getTaskScheduler() {
    ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
    taskScheduler.initialize();
    return taskScheduler;
  }

  /**
   *
   * 作者: Arthur 亚瑟
   * 时间: 2026/11/20 01:36
   * 说明: 接收 Spring 事件发布器，后续用于通知服务发现变化。
   * @param publisher Spring 应用事件发布器
   * @return
   * 返回值: void
   * 返回值说明: 保存 Spring 提供的事件发布器
   */
  @Override
  public void setApplicationEventPublisher(ApplicationEventPublisher publisher) {
    this.publisher = publisher;
  }

  @Override
  public boolean isAutoStartup() {
    return true;
  }

  /**
   *
   * 作者: Arthur 亚瑟
   * 时间: 2026/11/20 01:36
   * 说明: 取消轮询并通知 Spring 生命周期处理已完成。
   * @param callback 取消轮询后运行的回调
   * @return
   * 返回值: void
   * 返回值说明: 调用停止完成回调
   */
  @Override
  public void stop(Runnable callback) {
    this.stop();
    callback.run();
  }

  /**
   *
   * 作者: Arthur 亚瑟
   * 时间: 2026/11/20 01:36
   * 说明: 通过内部状态控制轮询任务，同一运行周期只安排一次固定延时调度。
   * @return
   * 返回值: void
   * 返回值说明: 开始轮询 Nacos 服务清单
   */
  @Override
  public void start() {
    if (this.running.compareAndSet(false, true)) {
      this.watchFuture =
          this.taskScheduler.scheduleWithFixedDelay(this::nacosServicesWatch, watchDelay);
    }

  }

  /**
   *
   * 作者: Arthur 亚瑟
   * 时间: 2026/11/20 01:36
   * 说明: 切换内部运行状态，取消已创建的周期轮询任务。
   * @return
   * 返回值: void
   * 返回值说明: 可中断正在执行的轮询
   */
  @Override
  public void stop() {
    if (this.running.compareAndSet(true, false) && this.watchFuture != null) {
      this.watchFuture.cancel(true);
    }

  }

  /**
   *
   * 作者: Arthur 亚瑟
   * 时间: 2026/11/20 01:36
   * 说明: 返回当前 Spring 生命周期状态接口使用的固定值。
   * @return
   * 返回值: boolean
   * 返回值说明: 固定返回 false，轮询的实际启停由内部 running 字段控制
   */
  @Override
  public boolean isRunning() {
    return false;
  }

  @Override
  public int getPhase() {
    return 0;
  }

  /**
   *
   * 作者: Arthur 亚瑟
   * 时间: 2026/11/20 01:36
   * 说明: 轮询服务清单并维护订阅，发现清单变化或接收命名事件时发布 Spring 心跳。
   * @return
   * 返回值: void
   * 返回值说明: 单轮轮询异常记录日志，等待下轮继续轮询
   */
  public void nacosServicesWatch() {
    try {
      boolean changed = false;
      NamingService namingService = this.properties.namingServiceInstance();
      ListView<String>
          listView = this.properties.namingServiceInstance().getServicesOfServer(1, 2147483647);
      List<String> serviceList = listView.getData();
      Set<String> currentServices = new HashSet(serviceList);
      currentServices.removeAll(this.cacheServices);
      if (currentServices.size() > 0) {
        changed = true;
      }

      Iterator var6;
      String serviceName;
      if (this.cacheServices.removeAll(new HashSet(serviceList)) && this.cacheServices.size() > 0) {
        changed = true;
        var6 = this.cacheServices.iterator();

        while (var6.hasNext()) {
          serviceName = (String) var6.next();
          namingService
              .unsubscribe(serviceName, (EventListener) this.subscribeListeners.get(serviceName));
          this.subscribeListeners.remove(serviceName);
        }
      }

      this.cacheServices = new HashSet(serviceList);
      var6 = this.cacheServices.iterator();

      while (var6.hasNext()) {
        serviceName = (String) var6.next();
        if (!this.subscribeListeners.containsKey(serviceName)) {
          EventListener eventListener = (event) -> {
            this.publisher
                .publishEvent(new HeartbeatEvent(this, this.nacosWatchIndex.getAndIncrement()));
          };
          this.subscribeListeners.put(serviceName, eventListener);
          namingService.subscribe(serviceName, eventListener);
        }
      }

      if (changed) {
        this.publisher
            .publishEvent(new HeartbeatEvent(this, this.nacosWatchIndex.getAndIncrement()));
      }
    } catch (Exception var9) {
      log.error("Error watching Nacos Service change", var9);
    }

  }
}