package com.itcall.batch.jobs.sample.sampleBatch;

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
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;

import com.itcall.batch.config.batch.BaseJobBatch;
import com.itcall.batch.config.support.ScheduledJob;
import com.itcall.batch.jobs.sample.sampleBatch.processor.SampleBatchJobProcessor;
import com.itcall.batch.jobs.sample.sampleBatch.reader.SampleBatchJobReader;
import com.itcall.batch.jobs.sample.sampleBatch.writer.SampleBatchJobWriter;

@ScheduledJob
public class SampleBatchJobConf extends BaseJobBatch {

	@Override
	public JobExecution scheduledJobs() throws JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException {
		LOG.debug("Job Started at :", new Date());

		String[] jobArgs = {"scheduledJobs의 전달하고픈 첫번째 인수"
				, new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date())
				, "scheduledJobs의 전달하고픈 세번째 인수"};

		return doLaucher(sampleBatchJob(), jobArgs);

//		JobParameters params = JobParamCode.makeJobParameters(jobArgs);
//
//		JobExecution execution = jobLauncher.run(sampleBatchJob(), params);
//
//		LOG.debug("Job finished with status :",execution.getStatus().getBatchStatus());
//
//		return execution;
	}

	@Bean
	public Job sampleBatchJob() {
		return getJobBuilder()
				.incrementer(new RunIdIncrementer())
				.flow(sampleBatchStep01())
				.end()
				.build();
	}

	@Bean
	public Step sampleBatchStep01() {
		return getStepBuilder()
				.<String,String> chunk(10)
				.faultTolerant()
				.skip(Exception.class)
				.skipLimit(10)
				.reader(sampleBatchJobReader())
				.processor(sampleBatchJobProcessor())
				.writer(sampleBatchJobWriter())
				.build();
	}
	
	@Bean
	public ItemReader<String> sampleBatchJobReader(){
		return new SampleBatchJobReader();
	}

	@Bean
	public ItemProcessor<String,String> sampleBatchJobProcessor() {
		return new SampleBatchJobProcessor();
	}

	@Bean
	public ItemWriter<? super String> sampleBatchJobWriter() {
		return new SampleBatchJobWriter();
	}

}
