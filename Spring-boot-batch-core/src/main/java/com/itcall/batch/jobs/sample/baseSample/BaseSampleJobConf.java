package com.itcall.batch.jobs.sample.baseSample;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.context.annotation.Bean;

import com.itcall.batch.biz.vo.sample.bizSam.SampleInfoVo;
import com.itcall.batch.config.batch.BaseJobBatch;
import com.itcall.batch.jobs.sample.baseSample.listener.BaseSampleJobListener;
import com.itcall.batch.jobs.sample.baseSample.processor.BaseSampleJobProcessor;
import com.itcall.batch.jobs.sample.baseSample.reader.BaseSampleJobReader;
import com.itcall.batch.jobs.sample.baseSample.writer.BaseSampleJobWriter;

//@ScheduledJob
public class BaseSampleJobConf extends BaseJobBatch{

	private static final Logger LOG = LoggerFactory.getLogger(BaseSampleJobConf.class);

	public JobExecution scheduledJobs() throws JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException {

		LOG.info("Job Started at :" + new Date());

		String[] jobArgs = {"전달하고픈 첫번째 인수"
				, new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date())
				, "전달하고픈 세번째 인수"};

		return doLaucher(baseSampleJob(), jobArgs);

	}

	@Bean
	public Job baseSampleJob() {
		return getJobBuilder()
				.incrementer(new RunIdIncrementer())
				.listener(baseSampleJobListener())
				.start(baseSampleStep01()).build();
	}

	@Bean
	public Step baseSampleStep01() {
		return getStepBuilder()
				.<List<SampleInfoVo>, List<SampleInfoVo>>chunk(20)
				.reader(baseSampleReader())
				.processor(baseSampleProcessor())
				.writer(baseSampleWriter())

				/********* MultiProcessConfiguration *********/
				.taskExecutor(batchTaskExecutor)
				.throttleLimit(8)

				/********* 모든 종류의 리스너 추가예제 ********/
				// .listener(new BaseSampleJobChunkListener())
				// .listener(new BaseSampleJobReadListener())
				// .listener(new BaseSampleJobProcessListener())
				// .listener(BaseSampleJobWriteListener())
				// .listener(new BaseSampleJobStepExecutionListener())
				// .exceptionHandler(new BaseSampleJobExceptionHandler())

				.build();
	}



	@Bean
	public BaseSampleJobReader baseSampleReader() {
		return new BaseSampleJobReader();
	}


	@Bean
	public BaseSampleJobProcessor baseSampleProcessor() {
		return new BaseSampleJobProcessor();
	}

	@Bean
	public BaseSampleJobWriter baseSampleWriter() {
		return new BaseSampleJobWriter();
	}

	@Bean
	public JobExecutionListener baseSampleJobListener() {
		return new BaseSampleJobListener();
	}

}
