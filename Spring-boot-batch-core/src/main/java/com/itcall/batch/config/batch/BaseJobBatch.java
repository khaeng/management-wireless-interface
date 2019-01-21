package com.itcall.batch.config.batch;

import java.io.IOException;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.FlowBuilderException;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.builder.JobBuilderException;
import org.springframework.batch.core.job.flow.support.SimpleFlow;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.builder.StepBuilderException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;

import com.itcall.batch.common.controller.rest.RestServerController;
import com.itcall.batch.common.exception.BatchBizException;
import com.itcall.batch.common.service.batchInfo.BatchInfoService;
import com.itcall.batch.common.vo.batchInfo.BatchInfoVo;
import com.itcall.batch.config.batch.listener.BaseJobExecutionListener;

/**
 * 관리대상 배치공통
 *
 */
public abstract class BaseJobBatch implements BaseBatch{

	protected Logger LOG = LoggerFactory.getLogger(getClass());

	private boolean isKillJob;

	private String jobName;

	@Resource
	private BatchInfoService batchInfoService;

//	@Autowired
//	private CmdJobLaucher cmdJobLaucher; //  = (CmdJobLaucher) ctx.getBean("cmdJobLaucher");
	@Resource(name="batchJobLaucher")
	private BatchJobLaucher batchJobLaucher;

	@Value("#{batch['batch.app.war.path']?:'/app/wars/MY-BOOT-BATCH-1.0.war'}")
	private String warPath;

	@Value("${spring.profiles.active:prod}")
	private String profile;

	@Autowired
	protected TaskExecutor batchTaskExecutor;

	@Autowired
	protected JobBuilderFactory jobBuilderFactory;

	@Autowired
	protected StepBuilderFactory stepBuilderFactory;

	@Autowired
	protected JobLauncher jobLauncher;

	@Autowired
	protected JobExplorer jobExplorer;

	@Autowired
	protected JobRegistry jobRegistry;

	@Autowired
	protected JobOperator jobOperator;

	@Autowired
	protected JobRepository jobRepository;

	public abstract JobExecution scheduledJobs() throws JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException;

	protected JobBuilder getJobBuilder() throws JobBuilderException {
		for (StackTraceElement element : new Throwable().getStackTrace()) {
			if(element.getClassName().endsWith(element.getMethodName().substring(1)+JOB_CONF_POST_FIX)) {
				this.jobName = element.getMethodName();
				LOG.debug("Find and Registered Job [{}]", this.jobName);
				return jobBuilderFactory.get(this.jobName/*+BaseBatch.JOB_NAME_CON_FIX+new Date().getTime()*/).listener(
						new BaseJobExecutionListener(batchInfoService, jobRegistry, jobRepository)
						)/*.incrementer(new BaseJobParametersIncrementer(batchInfoService, this.jobName))*/;
			}
		}
		throw new JobBuilderException(new Exception("Dose not found Job-Name - Check your Job-Method Name(bean)"));
	}

	protected FlowBuilder<SimpleFlow> getFlowBuilder() throws FlowBuilderException{
		for (StackTraceElement element : new Throwable().getStackTrace()) {
			if(element.getClassName().endsWith(this.jobName.substring(1)+JOB_CONF_POST_FIX)) {
				LOG.debug("Find and Registered Job-Flow [{}]-[{}]", this.jobName, element.getMethodName());
				return new FlowBuilder<SimpleFlow>(this.jobName+BaseBatch.JOB_NAME_CON_FIX+element.getMethodName()/*+BaseBatch.JOB_NAME_CON_FIX+new Date().getTime()*/);
				// return stepBuilderFactory.get(this.jobName+BaseBatch.JOB_NAME_CON_FIX+element.getMethodName()/*+BaseBatch.JOB_NAME_CON_FIX+new Date().getTime()*/);
			}
		}
		throw new FlowBuilderException(new Exception("Dose not found Flow-Name - Check your Flow-Method Name(bean)"));
	}

	protected StepBuilder getStepBuilder() throws StepBuilderException {
		for (StackTraceElement element : new Throwable().getStackTrace()) {
			if(element.getClassName().endsWith(this.jobName.substring(1)+JOB_CONF_POST_FIX)) {
				LOG.debug("Find and Registered Job-Step [{}]-[{}]", this.jobName, element.getMethodName());
				return stepBuilderFactory.get(this.jobName+BaseBatch.JOB_NAME_CON_FIX+element.getMethodName()/*+BaseBatch.JOB_NAME_CON_FIX+new Date().getTime()*/);
			}
		}
		throw new StepBuilderException(new Exception("Dose not found Step-Name - Check your Step-Method Name(bean)"));
	}

	// /app/wars
//	public JobExecution laucher(Job job, JobParameters jobParameters) throws JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException, NoSuchJobException, JobInstanceAlreadyExistsException {
////		ReferenceJobFactory jobFactory = new ReferenceJobFactory(job);
////		jobRegistry.register(jobFactory);
////		SimpleJobOperator operator = new SimpleJobOperator();
////		operator.
////		jobOperator.start("jobName", "JobParameters");
////		jobRegistry.
//		return scheduledJobs();
//	}

	protected JobExecution doLaucher(Job job, String... jobArgs) throws JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException {
		/**************************************************************************************************************************************************************************************
		 * 배치 중복실행을 막기 위해서 선행배치 체크 및 배치 실행/등록 시점을 동기화한다. 동기화부분이 2군데 임으로 job Object로 맞춰서 동일 job에 대해 synchronized를 수행한다.
		 **************************************************************************************************************************************************************************************/
		synchronized (job) {
			BatchInfoVo batchInfo = new BatchInfoVo(this.jobName);
			
			if(RestServerController.isRunningJob(batchInfoService, this.jobName)) {
				return null;
			};

			try {
				batchInfo = batchInfoService.getBatchInfo(batchInfo);
				if(batchInfo!=null&&batchInfo.getUseYn()!=null) {
					if(batchInfo.getUseYn().trim().equalsIgnoreCase("Y")) {
						return this.batchJobLaucher.startBatchJob(job, jobArgs);
					}else {
						LOG.info("Job[{}] schedule is not used. useYn[{}], BatchInfo[{}]", this.jobName, batchInfo.getUseYn(), batchInfo);
					}
				}
				throw new BatchBizException("Job["+this.jobName+"] Does not found runnable job or useYn is not set...");
			} catch (Exception e) {
				LOG.error("Job[{}] laucher faild[{}], BatchInfo[{}], {}",this.jobName, e.getMessage(), batchInfo, e);
				return null;//			throw new BatchBizException(e.getMessage());
			}
		}
	}

	protected int shellLaucher(String... jobArgs) throws InterruptedException, IOException {
		StringBuffer cmdSb = new StringBuffer();
		cmdSb.append("java -Dspring.profiles.active=").append(this.profile).append(" -jar ").append(this.warPath).append(" ").append(this.jobName);
		for (String arg : jobArgs) {
			cmdSb.append(" ").append(arg.contains(" ")?"\""+arg+"\"":arg);
		}
		final Runtime runtime = Runtime.getRuntime();
		Process process = runtime.exec(cmdSb.toString());

		// jobPid을 insert한다.

		process.waitFor();
		process.destroy();

		return 0;
	}

	@Override
	public boolean isKillJob() {
		return isKillJob;
	}

	@Override
	public void setKillJob(boolean isKillJob) {
		this.isKillJob = isKillJob;
	}
}