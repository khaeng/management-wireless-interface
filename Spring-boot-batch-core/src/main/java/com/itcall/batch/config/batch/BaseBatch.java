package com.itcall.batch.config.batch;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;

public interface BaseBatch {

	public static final String JOB_SCHEDULED_METHOD_NAME = "scheduledJobs";
	public static final String JOB_CONF_POST_FIX = "Conf";
	public static final String JOB_NAME_CON_FIX = "_";
	public static final int    LOG_UPDATE_TIME_SECONDS = 30 * 1000; // 실행중일 경우 30초당 한번씩 job execution을 DB에 업데이트한다.
	public static final long JOB_START_DELAY_MS = 60 * 1000; // CRON.CMD가 아닌 주기적인 실행인 경우. 최초 실행은 WAS시작 후 1분후부터 시작한다.

	public JobExecution scheduledJobs() throws JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException;

	public boolean isKillJob();

	public void setKillJob(boolean isKillJob);

}
