package com.itcall.batch.common.service.rest.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.launch.JobExecutionNotRunningException;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.ScheduledMethodRunnable;
import org.springframework.stereotype.Service;

import com.itcall.batch.common.exception.BaseException;
import com.itcall.batch.common.mapper.batchInfo.BatchInfoMapper;
import com.itcall.batch.common.service.rest.BatchMngService;
import com.itcall.batch.config.batch.BaseBatch;
import com.itcall.batch.config.batch.BatchJobLaucher;

@Service
@EnableAsync
public class BatchMngServiceImpl implements BatchMngService {

	private static final Logger LOG = LoggerFactory.getLogger(BatchMngServiceImpl.class);

	@Resource
	private ApplicationContext ctx;

	@Resource
	private TaskScheduler taskScheduler;

//	@Resource
//	private CmdJobLaucher cmdJobLaucher;
	@Resource(name="batchJobLaucher")
	private BatchJobLaucher batchJobLaucher;

	@Resource
	private JobOperator jobOperator;

	@Resource
	private BatchInfoMapper batchInfoMapper;

	private static final Map<String, ScheduledFuture<?>> SCHEDULER_HISTORY_MAP = new HashMap<String, ScheduledFuture<?>>();
	private static final List<String> JOB_KILLING_LIST = new ArrayList<String>();
	private static final int MAX_TRY_COUNT = 10;

	public Map<String, Object> getBatchInfoList(){
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("SCHEDULED_INFO", SCHEDULER_HISTORY_MAP);
		result.put("jobNames", jobOperator.getJobNames());
		result.putAll(getRunningJobList(jobOperator.getJobNames()));
		return result;
	}

	@Override
	public Map<String, Object> getRunningJobList() throws Exception {
		return getRunningJobList(jobOperator.getJobNames());
	}

	private Map<String, Object> getRunningJobList(Set<String> jobNames) {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("jobNames", jobNames);
		for (String jobName : jobNames) {
			try {
				result.put(jobName, jobOperator.getRunningExecutions(jobName));
			} catch (NoSuchJobException e) {
				result.put(jobName, e.getMessage());
				LOG.error("{}",e);
			}
		}
		return result;
	}

	@Async
	public void jobAsyncStart(String jobName, String[] params) {
		jobStart(jobName, params);
	}

	public int jobStart(String jobName, String[] params) {
		return batchJobLaucher.startBatchJob(jobName, params);
	}

	/**
	 * 실행중인 Job을 정지한다.
	 * @param jobOperator
	 * @param jobName
	 * @throws NoSuchJobException
	 * @throws NoSuchJobExecutionException
	 * @throws JobExecutionNotRunningException
	 */
	public int jobStop(String jobName) throws NoSuchJobException, NoSuchJobExecutionException, JobExecutionNotRunningException {
		Set<Long> jobExecutionIds = jobOperator.getRunningExecutions(jobName);
		if(jobExecutionIds==null) return 0;
		for (Long jobExecutionId : jobExecutionIds) {
			int limitCount = 0;
			while (!jobOperator.stop(jobExecutionId)) {
				if(++limitCount>=MAX_TRY_COUNT) {
					LOG.error("Job[{}], ExecutionId[{}] Stop operation failure... jobExecution summary : [{}]",jobName, jobExecutionId, jobOperator.getSummary(jobExecutionId));
					return -1;
				}
				LOG.warn("Job[{}], ExecutionId[{}] Stop operation retrying to[{}/{}]... jobExecution summary : [{}]",jobName, jobExecutionId, limitCount, MAX_TRY_COUNT, jobOperator.getSummary(jobExecutionId));
				sleep(500);
			}
		}
		return jobExecutionIds.size();
	}

	/**
	 * JOB의 클론정책을 등록한다. 이미 등록되어 있으면 삭제한다.
	 * 
	 * @param jobName
	 * @param cronCmd
	 * @return
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	public ScheduledFuture<?> setScheduler(String jobName, String cronCmd) throws NoSuchMethodException, SecurityException, Exception {
		try {
			Method scheduledJobs=null;
			BaseBatch baseBatch = (BaseBatch) ctx.getBean(jobName.endsWith(BaseBatch.JOB_CONF_POST_FIX)?jobName:jobName+BaseBatch.JOB_CONF_POST_FIX);
			scheduledJobs = baseBatch.getClass().getMethod(BaseBatch.JOB_SCHEDULED_METHOD_NAME);
			ScheduledMethodRunnable scheduledMethodRunnable = new ScheduledMethodRunnable(baseBatch, scheduledJobs) ;
			ScheduledFuture<?> scheduledFuture = null;
			if(removeScheduler(jobName)) {
//				Thread.sleep(1000);
				if(cronCmd.matches("^[0-9]*$")) {
					/*** FixedDelay : 종료시각으로부터 동일한 간격으로 실행 ***/
					scheduledFuture = taskScheduler.scheduleWithFixedDelay(scheduledMethodRunnable, new Date(System.currentTimeMillis()+BaseBatch.JOB_START_DELAY_MS), Long.parseLong(cronCmd));
					/*** FixedRate : 시작시각으로부터 동일한 간격으로 실행 ***/
					// scheduledFuture = taskScheduler.scheduleAtFixedRate   (scheduledMethodRunnable, new Date(System.currentTimeMillis()+10000), Long.parseLong(cronCmd));
				}else {
					CronTrigger cronTrigger = new CronTrigger(cronCmd); // ("0/10 * * * * *");
					scheduledFuture = taskScheduler.schedule(scheduledMethodRunnable, cronTrigger);
				}
				SCHEDULER_HISTORY_MAP.put(jobName, scheduledFuture);
				LOG.info("Job[{}], Scheduler operation done... cron info[{}]", jobName, cronCmd);
				return scheduledFuture;
			}else {
				LOG.error("Job[{}], Scheduler operation failure... cron info[{}]", jobName, cronCmd);
				throw new BaseException("Job[" + jobName + "] : Remove before scheduler operation failure...");
			}
		} catch (Exception e) {
			throw new BaseException(e);
		}
	}

//	public ScheduledFuture<?> setScheduler(BaseBatch baseBatch, String cronCmd){
//		try {
//			Method scheduledJobs=null;
//			scheduledJobs = baseBatch.getClass().getMethod(BaseBatch.JOB_SCHEDULED_METHOD_NAME);
//			ScheduledMethodRunnable scheduledMethodRunnable = new ScheduledMethodRunnable(baseBatch, scheduledJobs) ;
//			CronTrigger cronTrigger = new CronTrigger(cronCmd); // ("0/10 * * * * *");
//			if(removeScheduler(jobName)) {
//				ScheduledFuture<?> scheduledFuture = taskScheduler.schedule(scheduledMethodRunnable, cronTrigger);
//				SCHEDULER_HISTORY_MAP.put(jobName, scheduledFuture);
//				LOG.info("Job[{}], Scheduler operation done... cron info[{}]", jobName, cronCmd);
//				return scheduledFuture;
//			}else {
//				LOG.error("Job[{}], Scheduler operation failure... cron info[{}]", jobName, cronCmd);
//				throw new Exception("Job[" + jobName + "] : Remove before scheduler operation failure...");
//			}
//		} catch (Exception e) {
//			throw new Exception(e);
//		}
//	}

	/**
	 * 스케줄링된 Job을 제거한다. // 실행중인 Job도 정지한다.
	 * @param jobName
	 * @return
	 */
	public boolean removeScheduler(String jobName) {
		try { // 등록전 실행중인 Job은 정지한다.
			jobStop(jobName);
		} catch (NoSuchJobException | NoSuchJobExecutionException | JobExecutionNotRunningException e) {
			// e.printStackTrace();
		}
		ScheduledFuture<?> scheduledFuture = SCHEDULER_HISTORY_MAP.get(jobName);
		if(scheduledFuture!=null) {
			int limitCount = 0;
//			LOG.debug("scheduledFuture.isCancelled() : [{}]", scheduledFuture.isCancelled());
//			LOG.debug("scheduledFuture.isDone() : [{}]", scheduledFuture.isDone());
			while (!scheduledFuture.cancel(true)) {
//				LOG.debug("scheduledFuture.isCancelled() : [{}]", scheduledFuture.isCancelled());
//				LOG.debug("scheduledFuture.isDone() : [{}]", scheduledFuture.isDone());
//				scheduledFuture.cancel(false);
//				LOG.debug("scheduledFuture.isCancelled() : [{}]", scheduledFuture.isCancelled());
//				LOG.debug("scheduledFuture.isDone() : [{}]", scheduledFuture.isDone());
				if(++limitCount>=MAX_TRY_COUNT) {
					LOG.error("Job[{}], Remove scheduler operation failure... ScheduledFuture summary : [{}]",jobName, scheduledFuture);
					return false;
				}
				LOG.warn("Job[{}], Remove scheduler operation retrying to[{}/{}]... ScheduledFuture summary : [{}]",jobName, limitCount, MAX_TRY_COUNT, scheduledFuture);
				sleep(500);
			}
			SCHEDULER_HISTORY_MAP.remove(jobName);
		}
		return true;
	}

	public boolean killJob(JobOperator jobOperator, String jobName) throws NoSuchJobException {
		ScheduledFuture<?> scheduledFuture = SCHEDULER_HISTORY_MAP.get(jobName);
		ScheduledExecutorService scheduledExecutorService = ((ThreadPoolTaskScheduler)taskScheduler).getScheduledExecutor();
		ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = ((ThreadPoolTaskScheduler)taskScheduler).getScheduledThreadPoolExecutor();
		Set<Long> jobExecutionIds = jobOperator.getRunningExecutions(jobName);
//		for (Long jobExecutionId : jobExecutionIds) {
//			//jobOperator.
//		}
		
		return true;
	}

	public static boolean isKillJob(String jobName) {
		for (String killJobName : JOB_KILLING_LIST) {
			if(killJobName.equals(jobName)) {
				return JOB_KILLING_LIST.remove(killJobName);
			}
		}
		return false;
	}

	private static void sleep(final int millseconds) {
		try {Thread.sleep(millseconds<=0?500:millseconds);} catch (InterruptedException e) {e.printStackTrace();}
	}

}
