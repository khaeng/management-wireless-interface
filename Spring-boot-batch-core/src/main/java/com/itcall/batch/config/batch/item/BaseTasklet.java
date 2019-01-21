package com.itcall.batch.config.batch.item;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;

import com.itcall.batch.common.service.batchInfo.BatchInfoService;
import com.itcall.batch.common.vo.batchInfo.BatchInfoHstVo;
import com.itcall.batch.config.batch.JobParamCode;

public abstract class BaseTasklet implements Tasklet, InitializingBean {

	protected Logger LOG = LoggerFactory.getLogger(getClass());

	private StepExecution stepExecution;

	private String[] jobParameters;

	@Resource
	private BatchInfoService batchInfoService;

	public abstract RepeatStatus execTasklet(StepContribution contribution, ChunkContext chunkContext) throws Exception;

	@Override
	public void afterPropertiesSet() throws Exception {
		return;
	}

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		this.stepExecution = chunkContext.getStepContext().getStepExecution();
		String[] params = JobParamCode.getParameters(chunkContext.getStepContext().getStepExecution().getJobParameters());
		this.jobParameters = params;
		return execTasklet(contribution, chunkContext);
	}

	protected RepeatStatus addBizMsgAndContinue(int resultCnt, String resultMsg) throws Exception {
		addBizMsgAndClose(resultCnt, resultMsg);
		return RepeatStatus.CONTINUABLE;
	}
	protected RepeatStatus addBizMsgAndClose(int resultCnt, String resultMsg) throws Exception {
		JobExecution jobExecution = this.stepExecution.getJobExecution();
		BatchInfoHstVo batchInfoHst = new BatchInfoHstVo(jobExecution);
		batchInfoHst.setRestCnt(resultCnt);
		batchInfoHst.setRestMsg(resultMsg);
		batchInfoService.setBatchInfoHst(batchInfoHst);
		return RepeatStatus.FINISHED;
	}


//	@Override
//	public void beforeJob(JobExecution jobExecution) {
//		this.jobExecution = jobExecution;
////		new Thread(new Runnable() {
////			@Override public void run() {
////				BaseTasklet.this.jobExecution.getStatus().name();
////				//COMPLETED, STARTING, STARTED, STOPPING, STOPPED, FAILED, ABANDONED, UNKNOWN
////				BaseTasklet.this.jobExecution.stop();
//////				Thread.sleep(30000);
//////				throw new JobExecutionException("");
////			}
////		}).start();
//	}

//	public void killer() {
//		Thread killer = new Thread(new Runnable() {
//			@Override public void run() {
//				while (BatchJobConfig.isKillJob(stepExecution.getJobExecution().getJobConfigurationName())) {
//					switch (stepExecution.getStatus()) {
//					case STARTED:case STARTING:case UNKNOWN:
//						break;
//					default:
//						return;
//					}
//					try {Thread.sleep(1000);} catch (InterruptedException e) {}
//					stepExecution.setTerminateOnly();
//					stepExecution.getJobExecution().stop();
//					// throw new JobExecutionException("User killed job.");
//				}
//			}});
//		killer.start();
//		/*killer.join();*/
//	}

	public StepExecution getStepExecution() {
		return stepExecution;
	}

	public void setStepExecution(StepExecution stepExecution) {
		this.stepExecution = stepExecution;
	}

	public String[] getJobParameters() {
		return jobParameters;
	}

	public void setJobParameters(String[] jobParameters) {
		this.jobParameters = jobParameters;
	}

}
