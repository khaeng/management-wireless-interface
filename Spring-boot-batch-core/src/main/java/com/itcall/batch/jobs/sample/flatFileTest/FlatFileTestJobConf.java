package com.itcall.batch.jobs.sample.flatFileTest;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.context.annotation.Bean;

import com.itcall.batch.biz.vo.sample.flatFileTest.SvcAreaInfoVO;
import com.itcall.batch.config.batch.BaseJobBatch;
import com.itcall.batch.jobs.sample.flatFileTest.reader.FlatFileTestJobReader;
import com.itcall.batch.jobs.sample.flatFileTest.writer.FlatFileTestJobWriter;

//@ScheduledJob
public class FlatFileTestJobConf extends BaseJobBatch {

	@Override
	public JobExecution scheduledJobs() throws JobExecutionAlreadyRunningException, JobRestartException,
			JobInstanceAlreadyCompleteException, JobParametersInvalidException {
		return doLaucher(flatFileTestJob(), new String[]{"",""});
	}

	@Bean
	public Job flatFileTestJob() {
		return getJobBuilder()
				.incrementer(new RunIdIncrementer())
				.flow(flatFileTestStep01())
				.end()
				.build();
	}

	@Bean
	public Step flatFileTestStep01() {
		return getStepBuilder()
				.<SvcAreaInfoVO,SvcAreaInfoVO> chunk(3)
				.reader(flatFileTestJobReader())
//				.processor(flatFileTestJobProcessor())
				.writer(flatFileTestJobWriter())
				.build();
	}

	@Bean
	public FlatFileItemReader<SvcAreaInfoVO> flatFileTestJobReader(){
		
		return new FlatFileTestJobReader();
		// return svcAreaInfoReader(null);
	}

//	@Bean
//	public ItemProcessor<SvcAreaInfoVO,SvcAreaInfoVO> flatFileTestJobProcessor() {
//		return new flatFileTestJobProcessor();
//	}

	@Bean
	public ItemWriter<SvcAreaInfoVO> flatFileTestJobWriter() {
		return new FlatFileTestJobWriter();
	}

}
