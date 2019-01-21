package com.itcall.batch.config.batch.item;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ItemWriter;

import com.itcall.batch.config.batch.JobParamCode;

public abstract class BaseWriter<I> implements ItemWriter<I> {

	protected Logger LOG = LoggerFactory.getLogger(getClass());

	private StepExecution stepExecution;

	private String[] jobParameters;

	protected abstract void beforeStep(StepExecution stepExecution);
	protected abstract void writer(List<? extends I> items) throws Exception;
	protected abstract void afterStep(StepExecution stepExecution);

	@BeforeStep
	public void readerBeforeStep(StepExecution stepExecution) {
		this.stepExecution = stepExecution;
		String[] params = JobParamCode.getParameters(stepExecution.getJobParameters());
		this.jobParameters = params;
		beforeStep(stepExecution);
	}

	@Override
	public void write(List<? extends I> items) throws Exception {
		writer(items);
	}

	@AfterStep
	public void readerAfterStep(StepExecution stepExecution) {
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
