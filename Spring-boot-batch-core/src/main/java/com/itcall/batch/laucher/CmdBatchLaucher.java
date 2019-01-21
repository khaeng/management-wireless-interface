package com.itcall.batch.laucher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersInvalidException;
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

import com.itcall.batch.config.batch.BaseBatch;
import com.itcall.batch.config.support.EnvironmentSupport;

@SpringBootApplication
@EnableAutoConfiguration(exclude = { DataSourceTransactionManagerAutoConfiguration.class, DataSourceAutoConfiguration.class})
@ComponentScan(basePackages = {EnvironmentSupport.MAPPER_BASE_PACKAGE}, excludeFilters= {
		@ComponentScan.Filter(type=FilterType.REGEX, pattern= {"com.itcall.batch.boots.*","com.itcall.batch.rest.*"})
})
@DependsOn(value={"batchJobConfig"})
public class CmdBatchLaucher implements CommandLineRunner{

	private static final Logger LOG = LoggerFactory.getLogger(CmdBatchLaucher.class);

	/**
	 * 요청 JOB명으로 Configuration Bean을 찾는다.
	 */

	@Resource
	private ApplicationContext ctx;

	public static void main(String[] args) throws JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException, InterruptedException, IOException {

		args = checkUserInputData(args);

		ConfigurableApplicationContext ctx = 
				EnvironmentSupport.runSpringApp(CmdBatchLaucher.class, false, args);
				// new SpringApplicationBuilder(CmdBatchLaucher.class).web(false).run(args);

//		BaseBatch batch = null;
//		try {
//			String jobName = args[0];
//			batch = (BaseBatch) ctx.getBean(jobName.endsWith(BaseBatch.JOB_CONF_POST_FIX)?jobName:jobName+BaseBatch.JOB_CONF_POST_FIX);
//			CmdBatchLaucher cmdBatchLaucher = (CmdBatchLaucher) ctx.getBean("cmdBatchLaucher");
//			cmdBatchLaucher.startBatchJob(batch, args);
//		}catch (Exception e) {
//			LOG.error("Batch[{}] failure at : {} <== with your arguments :[{}]. Need arguments [JobName, JobParameters...]", batch, new Date(), Arrays.toString(args), e);
//		}
//		System.exit(-1);
	}

//	@Bean
//	@DependsOn(value = { "jobLauncher", "masterDatabaseConfig" })
	public void startBatchJob(BaseBatch batch, String[] args) {

		JobExecution execution = null;

		try {
		
			// JobParameters params = JobParamCode.makeJobParameters(Arrays.copyOfRange(args, 1, args.length));
	
			LOG.debug("Batch[{}] Started at : {}", batch, new Date());
	
			execution = batch.scheduledJobs();
	
			while (execution.getExitStatus().isRunning()) {
				Thread.sleep(1000);
				LOG.debug("Batch[{}] Chack batch each one second - Batch is still running ==> with status :[{}]", batch, execution.getStatus());
			}
			
			if(execution.getExitStatus().getExitCode().contains(ExitStatus.FAILED.getExitCode())) {
				LOG.error("Batch[{}] finished at : {} <== with status :[{}], detail[{}]", batch, new Date(), execution.getStatus(), execution);
			}else if(!execution.getExitStatus().getExitCode().contains(ExitStatus.COMPLETED.getExitCode())) {
				LOG.warn("Batch[{}] finished at : {} <== with status :[{}], detail[{}]", batch, new Date(), execution.getStatus(), execution);
			}else {
				LOG.info("Batch[{}] finished at : {} <== with status :[{}], detail[{}]", batch, new Date(), execution.getStatus(), execution);
			}

		} catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException | JobParametersInvalidException | InterruptedException e) {
			LOG.error("Batch[{}] failure at : {} <== with status :[{}], detail[{}]\n===========================[Exception({})===========================\n{}", batch, new Date(), execution==null?execution:execution.getStatus(), execution, e.getClass().getSimpleName(), e);
			System.exit(-2);
		}

		System.exit(0);

	}

	public static String[] checkUserInputData(String...args) throws IOException {
		List<String> result = new ArrayList<String>();
		if(args!=null&&args.length>0)
			return args;
		System.out.print("\n실행할JOB명칭과 파라메터를 순서대로 입력하세요...(스케줄테스트는 job명만 넣어주세요) : sampleCheckJob 파라메터1번 \"2번째 파라메터 데이터 스트링\" ...\n Start Target Job : ==> ");
		BufferedReader br = null;
		br = new BufferedReader(new InputStreamReader(System.in));
		String[] arrData = br.readLine().trim().split(" ");

		result.add(arrData[0]);
		boolean isQuartro = false;
		StringBuffer sb = new StringBuffer();
		for (int i = 1; i < arrData.length; i++) {
			if(isQuartro) {
				sb.append(" " + arrData[i]);
				if(arrData[i].endsWith("\"")) {
					isQuartro=false;
					result.add(sb.substring(1, sb.length()-1));
					sb.setLength(0);
				}
			}else{
				if(arrData[i].startsWith("\"") && !arrData[i].endsWith("\"")) {
					isQuartro = true;
					sb = new StringBuffer(arrData[i]);
				}else{
					result.add(arrData[i]);
				}
			}
		}
		if(sb.length()>0) {
			result.add(sb.toString());
		}
		return result.toArray(new String[result.size()]);
	}

	@Override
	public void run(String... args) throws Exception {

		LOG.debug("CmdBatchLaucher for CommandLineRunner.run [{}]", Arrays.toString(args));

		// 환경초기화...
		EnvironmentSupport.initFirstSet(this.ctx);

		BaseBatch batch = null;
		try {
			String jobName = args[0];
			batch = (BaseBatch) ctx.getBean(jobName.endsWith(BaseBatch.JOB_CONF_POST_FIX)?jobName:jobName+BaseBatch.JOB_CONF_POST_FIX);
			// CmdBatchLaucher cmdBatchLaucher = (CmdBatchLaucher) ctx.getBean("cmdBatchLaucher");
			this.startBatchJob(batch, args);
		}catch (Exception e) {
			LOG.error("Batch[{}] failure at : {} <== with your arguments :[{}]. Need arguments [JobName, JobParameters...]", batch, new Date(), Arrays.toString(args), e);
		}
		System.exit(-1);

	}
}
