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
 * 作者: Jack
 * 时间: 2024年9月7日12:46:08
 * 说明: 重写nacoswatch方法
 *
 */
@Slf4j(topic = "com.zero.listener.CustomNotifier")
public class NacosWatch implements ApplicationEventPublisherAware, SmartLifecycle {

	
  /**
 * watch delay,duration to pull new service from nacos server.
   */
  private long watchDelay = 30000;

  private final NacosDiscoveryProperties properties;
  private final TaskScheduler taskScheduler;
  private final AtomicLong nacosWatchIndex;
  private final AtomicBoolean running;
  private ApplicationEventPublisher publisher;
  private ScheduledFuture<?> watchFuture;
  private Set<String> cacheServices;
  private HashMap<String, EventListener> subscribeListeners;

  public NacosWatch(NacosDiscoveryProperties properties) {
    this(properties, getTaskScheduler());
  }

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

  @Override
  public void setApplicationEventPublisher(ApplicationEventPublisher publisher) {
    this.publisher = publisher;
  }

  @Override
  public boolean isAutoStartup() {
    return true;
  }

  @Override
  public void stop(Runnable callback) {
    this.stop();
    callback.run();
  }

  @Override
  public void start() {
    if (this.running.compareAndSet(false, true)) {
      this.watchFuture =
          this.taskScheduler.scheduleWithFixedDelay(this::nacosServicesWatch, watchDelay);
    }

  }

  @Override
  public void stop() {
    if (this.running.compareAndSet(true, false) && this.watchFuture != null) {
      this.watchFuture.cancel(true);
    }

  }

  @Override
  public boolean isRunning() {
    return false;
  }

  @Override
  public int getPhase() {
    return 0;
  }

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