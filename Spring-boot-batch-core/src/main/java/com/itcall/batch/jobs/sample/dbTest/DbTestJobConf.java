package com.itcall.batch.jobs.sample.dbTest;

import java.util.List;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.context.annotation.Bean;

import com.itcall.batch.common.vo.batchInfo.BatchInfoVo;
import com.itcall.batch.config.batch.BaseJobBatch;

//@ScheduledJob
public class DbTestJobConf extends BaseJobBatch {

	@Override
	public JobExecution scheduledJobs() throws JobExecutionAlreadyRunningException, JobRestartException,
			JobInstanceAlreadyCompleteException, JobParametersInvalidException {
		return doLaucher(dbTestJob(), new String[]{"111", "222"});
	}

	@Bean
	public Job dbTestJob() {
		return getJobBuilder().incrementer(new RunIdIncrementer())
		.start(dbTestStep01())
		.build();
	}

	@Bean
	public Step dbTestStep01() {
		int chunkSize = 10;
		return getStepBuilder().<List<BatchInfoVo>,BatchInfoVo>chunk(chunkSize)
				.reader(dbTestJobReader(chunkSize))
				.writer(dbTestJobWriter(chunkSize))
				.taskExecutor(batchTaskExecutor)
				.build();
	}

	@Bean
	public DbTestJobReader dbTestJobReader(int chunkSize) {
		return new DbTestJobReader(chunkSize);
	}

	@Bean
	public DbTestJobWriter dbTestJobWriter(int chunkSize) {
		return new DbTestJobWriter();
	}

}
