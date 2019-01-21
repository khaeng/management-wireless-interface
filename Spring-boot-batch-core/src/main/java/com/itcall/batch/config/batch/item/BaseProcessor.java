package com.itcall.batch.config.batch.item;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import com.itcall.batch.config.batch.JobParamCode;

public abstract class BaseProcessor<I,O> implements ItemProcessor<I, O> {

	protected Logger LOG = LoggerFactory.getLogger(getClass());

	private StepExecution stepExecution;

	private String[] jobParameters;

	protected abstract void beforeStep(StepExecution stepExecution);
	protected abstract O processor(I item) throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException ;
	protected abstract void afterStep(StepExecution stepExecution);

	@BeforeStep
	public void processBeforeStep(StepExecution stepExecution) {
		this.stepExecution = stepExecution;
		String[] params = JobParamCode.getParameters(stepExecution.getJobParameters());
		this.jobParameters = params;
		beforeStep(stepExecution);
	}

	@Override
	public O process(I item) throws Exception {
		return processor(item);
	}

	@AfterStep
	public void processAfterStep(StepExecution stepExecution) {
		afterStep(stepExecution);
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
