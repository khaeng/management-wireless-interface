package com.itcall.batch.laucher;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.FilterType;

import com.itcall.batch.common.service.batchInfo.BatchInfoService;
import com.itcall.batch.config.batch.BatchJobLaucher;
import com.itcall.batch.config.support.EnvironmentSupport;

@SpringBootApplication
@EnableAutoConfiguration(exclude = { DataSourceTransactionManagerAutoConfiguration.class, DataSourceAutoConfiguration.class})
@ComponentScan(basePackages = {EnvironmentSupport.MAPPER_BASE_PACKAGE}, excludeFilters= {
		@ComponentScan.Filter(type=FilterType.REGEX, pattern= {"com.itcall.batch.boots.*","com.itcall.batch.rest.*"})
})
@DependsOn(value={"batchJobConfig"})
public class CmdJobLaucher implements CommandLineRunner{

	private static final Logger LOG = LoggerFactory.getLogger(CmdJobLaucher.class);

	@Resource
	private ApplicationContext ctx;

	@Resource(name="jobLauncher")
	private JobLauncher jobLauncher;

	@Resource(name="batchJobLaucher")
	private BatchJobLaucher batchJobLaucher;

	@Resource
	private BatchInfoService batchInfoService;

	public static void main(String[] args) throws JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException, InterruptedException, IOException {

		String[] jobParams = CmdBatchLaucher.checkUserInputData(args);

		ConfigurableApplicationContext ctx = EnvironmentSupport.runSpringApp(CmdJobLaucher.class, false, jobParams);

//		if(ctx!=null)
//			return;
//		Job job = null;
//		try {
//			BatchJobLaucher batchJobLaucher = (BatchJobLaucher) ctx.getBean("batchJobLaucher");
//			System.exit(batchJobLaucher.startBatchJob(args[0], Arrays.copyOfRange(args, 1, args.length)));
//		}catch (Exception e) {
//			LOG.error("Job[{}] failure at : {} <== with your arguments :[{}]. Need arguments [JobName, JobParameters...]", job, new Date(), args, e);
//		}
//		System.exit(-1);
	}

	@Override
	public void run(String... args) throws Exception {

		LOG.debug("CmdJobLaucher for CommandLineRunner.run [{}]", Arrays.toString(args));

		// 환경초기화...
		EnvironmentSupport.initFirstSet(this.ctx);

		Job job = null;
		try {
			// BatchJobLaucher batchJobLaucher = (BatchJobLaucher) ctx.getBean("batchJobLaucher");
			System.exit(batchJobLaucher.startBatchJob(args[0], Arrays.copyOfRange(args, 1, args.length)));
		}catch (Exception e) {
			LOG.error("Job[{}] failure at : {} <== with your arguments :[{}]. Need arguments [JobName, JobParameters...]", job, new Date(), args, e);
		}
		System.exit(-1);

	}

}
