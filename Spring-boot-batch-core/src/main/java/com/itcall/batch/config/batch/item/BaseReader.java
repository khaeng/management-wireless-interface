package com.itcall.batch.config.batch.item;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import com.itcall.batch.common.service.batchInfo.BatchInfoService;
import com.itcall.batch.common.vo.batchInfo.BatchInfoHstVo;
import com.itcall.batch.config.batch.JobParamCode;

public abstract class BaseReader<I> implements ItemReader<I> {

	protected Logger LOG = LoggerFactory.getLogger(getClass());

	private StepExecution stepExecution;

	private String[] jobParameters;

	@Resource
	private BatchInfoService batchInfoService;

	private boolean isBizMsgWrited;

	protected abstract void beforeStep(StepExecution stepExecution);
	protected abstract I reader() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException ;
	protected abstract void afterStep(StepExecution stepExecution);

	@BeforeStep
	public void readerBeforeStep(StepExecution stepExecution) {
		this.stepExecution = stepExecution;
		String[] params = JobParamCode.getParameters(stepExecution.getJobParameters());
		this.jobParameters = params;
		beforeStep(stepExecution);
	}

	@Override
	public I read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
		return reader();
	}

	@AfterStep
	public void readerAfterStep(StepExecution stepExecution) {
		afterStep(stepExecution);
		try {
			if(!this.isBizMsgWrited && this.stepExecution!=null && this.stepExecution.getReadCount()>0)
				addBizMsg(this.stepExecution.getReadCount(), new StringBuffer().append("ReadCount[").append(this.stepExecution.getReadCount()).append("], WriteCount[").append(this.stepExecution.getWriteCount()).append("], SkipRead[").append(this.stepExecution.getReadSkipCount()).append("], SkipWrite[").append(this.stepExecution.getWriteSkipCount()).append("]:" ).append(this.toString()).toString());
		} catch (Exception e) {
			LOG.error("addBizMsg add error : {}", e);
		}
	}

	protected void addBizMsg(int resultCnt, String resultMsg) throws Exception {
		this.isBizMsgWrited = true;
		JobExecution jobExecution = this.stepExecution.getJobExecution();
		BatchInfoHstVo batchInfoHst = new BatchInfoHstVo(jobExecution);
		batchInfoHst.setRestCnt(resultCnt);
		batchInfoHst.setRestMsg(resultMsg);
		batchInfoService.setBatchInfoHst(batchInfoHst);
	}
	
	public void killer() throws JobExecutionException {
		this.stepExecution.setTerminateOnly();
		this.stepExecution.getJobExecution().stop();
		throw new JobExecutionException("User killed job.");
	}

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
