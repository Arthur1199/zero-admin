package com.zero.jobadmin.business.scheduler.thread;

import com.zero.jobadmin.business.constant.TriggerStatus;
import com.zero.jobadmin.business.model.XxlJobInfo;
import com.zero.jobadmin.business.model.dto.JobScheduleUpdate;
import com.zero.jobadmin.business.scheduler.config.XxlJobAdminBootstrap;
import com.zero.jobadmin.business.scheduler.misfire.MisfireStrategyEnum;
import com.zero.jobadmin.business.scheduler.trigger.TriggerTypeEnum;
import com.zero.jobadmin.business.scheduler.type.ScheduleTypeEnum;
import com.xxl.tool.core.CollectionTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author xuxueli 2019-05-21
 */
public class JobScheduleHelper {
    private static final Logger logger = LoggerFactory.getLogger(JobScheduleHelper.class);


    /**
     * pre-read time for scheduler, increase efficiency
     */
    public static final long PRE_READ_MS = 5000;
    /**
     * 关停扫描后，允许时间轮派发已预读任务的最长时间。
     */
    private static final long ELEGANT_SHUTDOWN_WAITING_SECONDS = 10;
    /** 关停期间检查时间轮是否已排空的间隔，单位毫秒。 */
    private static final long SHUTDOWN_DRAIN_CHECK_MS = 100;

    private Thread scheduleThread;
    private Thread ringThread;
    private volatile boolean scheduleThreadToStop = false;
    private volatile boolean ringThreadToStop = false;
    private final Map<Long, List<Integer>> ringData = new ConcurrentHashMap<>();

    /**
     *
     * 作者: xuxueli
     * 时间: 2019/5/21
     * 说明: 启动调度扫描和时间轮线程，游标事务提交后才派发任务。
     * @return
     * 返回值: void
     * 返回值说明: 启动两个后台线程
     */
    public void start(){

        // init thread flag
        scheduleThreadToStop = false;
        ringThreadToStop = false;

        // 1、schedule thread
        scheduleThread = new Thread(new Runnable() {
            @Override
            public void run() {

                // align time
                try {
                    TimeUnit.MILLISECONDS.sleep(5000 - System.currentTimeMillis()%1000 );
                } catch (Throwable e) {
                    if (!scheduleThreadToStop) {
                        logger.error(e.getMessage(), e);
                    }
                }
                logger.info(">>>>>>>>> init xxl-job admin scheduler success.");

                // pre-read count: treadpool-size * 10 (trigger-qps: 1000ms / 100ms each trigger cost)
                int preReadCount = (XxlJobAdminBootstrap.getInstance().getTriggerPoolFastMax() + XxlJobAdminBootstrap.getInstance().getTriggerPoolSlowMax()) * 10;

                // do schedule
                while (!scheduleThreadToStop) {

                    // param
                    long start = System.currentTimeMillis();
                    boolean preReadSuc = false;
                    try {
                        preReadSuc = runScheduleScan(XxlJobAdminBootstrap.getInstance(), preReadCount);
                    } catch (OptimisticLockingFailureException e) {
                        // 配置变动只影响本轮快照，继续按秒扫描，避免其余任务跨过过期窗口。
                        preReadSuc = true;
                        logger.debug("Schedule changed during scan; retrying on the next tick", e);
                    } catch (Throwable e) {
                        if (!scheduleThreadToStop) {
                            logger.error(">>>>>>>>>>> xxl-job, JobScheduleHelper#scheduleThread error:{}", e.getMessage(), e);
                        }
                    }
                    long cost = System.currentTimeMillis()-start;


                    // Wait seconds, align second
                    if (cost < 1000) {  // scan-overtime, not wait
                        try {
                            // pre-read period: success > scan each second; fail > skip this period;
                            TimeUnit.MILLISECONDS.sleep((preReadSuc?1000:PRE_READ_MS) - System.currentTimeMillis()%1000);
                        } catch (Throwable e) {
                            if (!scheduleThreadToStop) {
                                logger.error(e.getMessage(), e);
                            }
                        }
                    }

                }

                logger.info(">>>>>>>>>>> xxl-job, JobScheduleHelper#scheduleThread stop");
            }
        });
        scheduleThread.setDaemon(true);
        scheduleThread.setName("xxl-job, admin JobScheduleHelper#scheduleThread");
        scheduleThread.start();

        // 2、ring thread
        ringThread = new Thread(new Runnable() {
            @Override
            public void run() {

                while (!ringThreadToStop) {

                    // align second
                    try {
                        TimeUnit.MILLISECONDS.sleep(1000 - System.currentTimeMillis() % 1000);
                    } catch (Throwable e) {
                        if (!ringThreadToStop) {
                            logger.error(e.getMessage(), e);
                        }
                    }

                    try {
                        List<Integer> dueJobs = drainTimeRing(System.currentTimeMillis() / 1000);
                        for (int jobId : dueJobs) {
                            try {
                                XxlJobAdminBootstrap.getInstance().getJobTriggerPoolHelper()
                                        .trigger(jobId, TriggerTypeEnum.CRON, -1, null, null, null);
                            } catch (RuntimeException e) {
                                logger.error("Failed to submit job from time ring, jobId={}", jobId, e);
                            }
                        }
                    } catch (Throwable e) {
                        if (!ringThreadToStop) {
                            logger.error(">>>>>>>>>>> xxl-job, JobScheduleHelper#ringThread error:{}", e.getMessage(), e);
                        }
                    }
                }
                logger.info(">>>>>>>>>>> xxl-job, JobScheduleHelper#ringThread stop");
            }
        });
        ringThread.setDaemon(true);
        ringThread.setName("xxl-job, admin JobScheduleHelper#ringThread");
        ringThread.start();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 20:46
     * 说明: 持有调度锁时计算并批量写入游标，事务成功提交后才触发或加入时间轮。
     * @param bootstrap 调度中心组件入口
     * @param preReadCount 本轮预读任务的数量上限
     * @return
     * 返回值: boolean
     * 返回值说明: 本轮成功推进至少一个任务的游标时返回 true
     */
    private boolean runScheduleScan(XxlJobAdminBootstrap bootstrap, int preReadCount) {
        List<Runnable> dispatches = new ArrayList<>();
        Boolean updated = new TransactionTemplate(bootstrap.getTransactionManager()).execute(status -> {
            List<XxlJobInfo> jobs = readScheduleBatch(bootstrap, preReadCount);
            if (!CollectionTool.isNotEmpty(jobs)) {
                return false;
            }
            JobTriggerPoolHelper triggerPool = bootstrap.getJobTriggerPoolHelper();
            List<JobScheduleUpdate> updates = new ArrayList<>(jobs.size());
            for (XxlJobInfo job : jobs) {
                updates.add(JobScheduleUpdate.capture(job));
                scheduleJob(job, System.currentTimeMillis(), triggerPool, dispatches);
            }
            for (List<JobScheduleUpdate> batch : CollectionTool.split(updates, bootstrap.getScheduleBatchSize())) {
                int affected = bootstrap.getXxlJobInfoMapper().scheduleBatchUpdate(batch);
                if (affected != batch.size()) {
                    throw new OptimisticLockingFailureException("Scheduler batch changed before update, expected="
                            + batch.size() + ", updated=" + affected);
                }
                logger.debug("Scheduler cursor batch updated, records={}", affected);
            }
            return true;
        });
        for (Runnable dispatch : dispatches) {
            try {
                dispatch.run();
            } catch (RuntimeException e) {
                logger.error("Failed to dispatch a job after scheduler commit", e);
            }
        }
        return Boolean.TRUE.equals(updated);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 20:44
     * 说明: 在当前调度事务内锁定唯一的调度记录，再按取得锁后的时间预读任务。
     * @param bootstrap 调度中心组件入口
     * @param preReadCount 本轮预读任务的数量上限
     * @return
     * 返回值: List<XxlJobInfo>
     * 返回值说明: 预读任务列表，缺少锁记录时返回空列表并跳过本轮
     */
    private List<XxlJobInfo> readScheduleBatch(XxlJobAdminBootstrap bootstrap, int preReadCount) {
        if (bootstrap.getXxlJobLockMapper().scheduleLock() == null) {
            logger.warn("Scheduler scan skipped: xxl_job_lock is missing schedule_lock");
            return Collections.emptyList();
        }
        return bootstrap.getXxlJobInfoMapper().scheduleJobQuery(System.currentTimeMillis() + PRE_READ_MS, preReadCount);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 20:36
     * 说明: 根据扫描时间处理单个任务的过期、立即触发及预读。
     * @param jobInfo 本轮扫描到的任务
     * @param nowTime 扫描基准时间戳，单位毫秒
     * @param triggerPool 调度提交线程池
     * @param dispatches 事务提交后执行的派发操作，按扫描顺序追加
     * @return
     * 返回值: void
     * 返回值说明: 记录待派发操作并更新内存中的调度游标
     */
    private void scheduleJob(XxlJobInfo jobInfo, long nowTime, JobTriggerPoolHelper triggerPool, List<Runnable> dispatches) {
        int jobId = jobInfo.getId();
        long nextTime = jobInfo.getTriggerNextTime();
        if (nowTime > nextTime + PRE_READ_MS) {
            MisfireStrategyEnum strategy = MisfireStrategyEnum.match(jobInfo.getMisfireStrategy(), MisfireStrategyEnum.DO_NOTHING);
            dispatches.add(() -> strategy.getMisfireHandler().handle(jobId));
            refreshNextTriggerTime(jobInfo, new Date(nowTime));
            return;
        }
        if (nowTime >= nextTime) {
            dispatches.add(() -> triggerPool.trigger(jobId, TriggerTypeEnum.CRON, -1, null, null, null));
            if (!refreshNextTriggerTime(jobInfo, new Date(nowTime)) || nowTime + PRE_READ_MS <= jobInfo.getTriggerNextTime()) {
                return;
            }
            nextTime = jobInfo.getTriggerNextTime();
        }
        long scheduledSecond = nextTime / 1000;
        dispatches.add(() -> pushTimeRing(scheduledSecond, jobId));
        refreshNextTriggerTime(jobInfo, new Date(nextTime));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 20:36
     * 说明: 计算下一个有效时间点，无未来时间或配置异常时停止该任务。
     * @param jobInfo 待更新游标的任务
     * @param fromTime 计算起点
     * @return
     * 返回值: boolean
     * 返回值说明: 得到严格晚于起点的时间时返回 true
     */
    private boolean refreshNextTriggerTime(XxlJobInfo jobInfo, Date fromTime) {
        try {
            // generate next trigger time
            ScheduleTypeEnum scheduleTypeEnum = ScheduleTypeEnum.match(jobInfo.getScheduleType(), ScheduleTypeEnum.NONE);
            Date nextTriggerTime = scheduleTypeEnum.getScheduleType().generateNextTriggerTime(jobInfo, fromTime);

            // refresh next trigger-time + status
            if (nextTriggerTime != null && nextTriggerTime.after(fromTime)) {
                // generate success
                jobInfo.setTriggerStatus(-1);                               // pass, may be Inaccurate
                jobInfo.setTriggerLastTime(jobInfo.getTriggerNextTime());
                jobInfo.setTriggerNextTime(nextTriggerTime.getTime());
                return true;
            } else {
                // generate fail, stop job
                jobInfo.setTriggerStatus(TriggerStatus.STOPPED.getValue());
                jobInfo.setTriggerLastTime(0);
                jobInfo.setTriggerNextTime(0);
                logger.error(">>>>>>>>>>> xxl-job, refreshNextValidTime fail for job: jobId={}, scheduleType={}, scheduleConf={}",
                        jobInfo.getId(), jobInfo.getScheduleType(), jobInfo.getScheduleConf());
            }
        } catch (Exception e) {
            // generate error, stop job
            jobInfo.setTriggerStatus(TriggerStatus.STOPPED.getValue());
            jobInfo.setTriggerLastTime(0);
            jobInfo.setTriggerNextTime(0);

            logger.error(">>>>>>>>>>> xxl-job, refreshNextValidTime error for job: jobId={}, scheduleType={}, scheduleConf={}",
                    jobInfo.getId(), jobInfo.getScheduleType(), jobInfo.getScheduleConf(), e);
        }
        return false;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 20:36
     * 说明: 按绝对秒入队，避免延迟提交的任务等待到下一分钟。
     * @param ringSecond 任务计划触发时间，Unix 时间戳秒
     * @param jobId 任务 ID
     * @return
     * 返回值: void
     * 返回值说明: 将任务加入对应槽位
     */
    private void pushTimeRing(long ringSecond, int jobId) {
        ringData.compute(ringSecond, (second, jobs) -> {
            List<Integer> bucket = jobs == null ? new ArrayList<>() : jobs;
            bucket.add(jobId);
            return bucket;
        });
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 20:46
     * 说明: 从时间轮取出所有已到时间的槽位，同一槽位内按任务去重。
     * @param nowSecond 当前 Unix 时间戳秒
     * @return
     * 返回值: List<Integer>
     * 返回值说明: 待派发的任务 ID，按计划时间升序
     */
    private List<Integer> drainTimeRing(long nowSecond) {
        List<Integer> jobs = new ArrayList<>();
        List<Long> dueSeconds = ringData.keySet().stream().filter(second -> second <= nowSecond).sorted().toList();
        for (long second : dueSeconds) {
            List<Integer> bucket = ringData.remove(second);
            if (CollectionTool.isNotEmpty(bucket)) {
                jobs.addAll(new LinkedHashSet<>(bucket));
            }
        }
        return jobs;
    }

    /**
     *
     * 作者: xuxueli
     * 时间: 2019/5/21
     * 说明: 停止调度扫描，在宽限时间内派发已预读任务，再关闭时间轮并记录未派发任务。
     * @return
     * 返回值: void
     * 返回值说明: 已初始化的工作线程结束后恢复调用线程的中断状态
     */
    public void stop(){
        boolean interrupted = Thread.interrupted();
        try {
            scheduleThreadToStop = true;
            interrupted |= stopWorker(scheduleThread);

            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(ELEGANT_SHUTDOWN_WAITING_SECONDS);
            while (!ringData.isEmpty() && ringThread != null && ringThread.isAlive() && System.nanoTime() < deadline) {
                try {
                    TimeUnit.MILLISECONDS.sleep(SHUTDOWN_DRAIN_CHECK_MS);
                } catch (InterruptedException e) {
                    interrupted = true;
                }
            }

            ringThreadToStop = true;
            interrupted |= stopWorker(ringThread);
            for (int jobId : drainTimeRing(Long.MAX_VALUE)) {
                try {
                    XxlJobAdminBootstrap.getInstance().getJobTrigger()
                            .recordUndispatchedTrigger(jobId, TriggerTypeEnum.CRON, -1, null, null, true);
                } catch (RuntimeException e) {
                    logger.error("Failed to record time ring job cancelled at shutdown, jobId={}", jobId, e);
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
        logger.info(">>>>>>>>>>> xxl-job, JobScheduleHelper stop");
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 22:44
     * 说明: 唤醒工作线程并等待其退出，避免派发线程池关闭后仍有调度提交。
     * @param worker 待停止的调度或时间轮线程，可能尚未创建
     * @return
     * 返回值: boolean
     * 返回值说明: 等待过程中调用线程被中断时返回 true
     */
    private boolean stopWorker(Thread worker) {
        if (worker == null || !worker.isAlive()) {
            return false;
        }
        boolean interrupted = false;
        worker.interrupt();
        while (worker.isAlive()) {
            try {
                worker.join();
            } catch (InterruptedException e) {
                interrupted = true;
            }
        }
        return interrupted;
    }

}
