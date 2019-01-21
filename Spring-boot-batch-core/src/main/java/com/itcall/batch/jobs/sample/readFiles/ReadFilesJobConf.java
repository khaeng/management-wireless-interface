package com.itcall.batch.jobs.sample.readFiles;

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
import org.springframework.context.annotation.Bean;

import com.itcall.batch.config.batch.BaseJobBatch;
import com.itcall.batch.config.support.ScheduledJob;
import com.itcall.batch.jobs.sample.readFiles.tasklet.ReadFilesJobTasklet;

@ScheduledJob
public class ReadFilesJobConf extends BaseJobBatch{

	@Override
	public JobExecution scheduledJobs() throws JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException {

		LOG.debug("Job Started at :", new Date());

		String[] jobArgs = {"file:C:\\Test\\"
				, new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date())
				, "전달하고픈 세번째 인수"};

		return doLaucher(readFilesJob(), jobArgs);

//		JobParameters params = JobParamCode.makeJobParameters(jobArgs);
//
//		JobExecution execution = jobLauncher.run(readFilesJob(), params);
//
//		LOG.debug("Job finished with status :",execution.getStatus());
//
//		return execution;
	}

	@Bean
	public Job readFilesJob() {
		return getJobBuilder()
				.incrementer(new RunIdIncrementer())
				.flow(readFilesStep01())
				.end()
				.build();
	}

	@Bean
	public Step readFilesStep01() {
		return getStepBuilder()
				.tasklet(readFilesJobTasklet())
				.build();
	}

	@Bean
	public ReadFilesJobTasklet readFilesJobTasklet() {
		ReadFilesJobTasklet tasklet = new ReadFilesJobTasklet();
		return tasklet;
	}

}