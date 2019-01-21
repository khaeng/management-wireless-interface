package com.itcall.batch.jobs.sample.taskletSample;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.context.annotation.Bean;

import com.itcall.batch.config.batch.BaseJobBatch;
import com.itcall.batch.jobs.sample.taskletSample.tasklet.TaskletSampleJobTasklet;

//@ScheduledJob
public class TaskletSampleJobConf extends BaseJobBatch{

	@Override
	public JobExecution scheduledJobs() throws JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException {
		LOG.info("Job Started at :" + new Date());

		String[] jobArgs = {"taskletSampleJob의 전달하고픈 첫번째 인수"
				, new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date())
				, "taskletSampleJob의 전달하고픈 세번째 인수"};

		return doLaucher(taskletSampleJob(), jobArgs);

//		JobParameters params = JobParamCode.makeJobParameters(jobArgs);
//
//		JobExecution execution = jobLauncher.run(taskletSampleJob(), params);
//
//		LOG.info("Job finished with status :" + execution.getStatus());
//
//		return execution;
	}

	@Bean
	public Job taskletSampleJob() {
		return getJobBuilder()
				.incrementer(new RunIdIncrementer())
				.start(taskletSampleStep01())
				.build();
	}

	@Bean
	public Step taskletSampleStep01() {
		return getStepBuilder()
				.tasklet(taskletSampleTasklet())
				.build();
	}

	@Bean
	public Tasklet taskletSampleTasklet() {
		return new TaskletSampleJobTasklet();
	}

}