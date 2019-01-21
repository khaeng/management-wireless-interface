package com.itcall.batch.jobs.sample.baseSample.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

public class BaseSampleJobListener implements JobExecutionListener {

	private static final Logger LOG = LoggerFactory.getLogger(BaseSampleJobListener.class);

	@Override
	public void beforeJob(JobExecution jobExecution) {
		LOG.debug("batch Listener beforeJob ::: {}", jobExecution);
	}

	@Override
	public void afterJob(JobExecution jobExecution) {
		LOG.debug("batch Listener afterJob ::: {}", jobExecution);
	}

}
