package com.itcall.batch.jobs.sample.baseSample.processor;

import java.util.Arrays;
import java.util.List;

import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import com.itcall.batch.biz.vo.sample.bizSam.SampleInfoVo;
import com.itcall.batch.config.batch.item.BaseProcessor;

public class BaseSampleJobProcessor extends BaseProcessor<List<SampleInfoVo>, List<SampleInfoVo>> {

	@Override
	protected void beforeStep(StepExecution stepExecution) {
		LOG.debug("Do something before Job Processor");
	}

	@Override
	protected List<SampleInfoVo> processor(List<SampleInfoVo> item)
			throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {

		LOG.debug("전달된 파라메터가 전달순서와 동일하게 배열로 들어온다. Job({}), Step({}), My Parameters[{}]"
				,getStepExecution().getJobExecution().getJobInstance().getJobName()
				,getStepExecution().getStepName()
				,Arrays.toString(getJobParameters()));

		LOG.debug("batch some Process ::: {}", item);

		return item;
	}

	@Override
	protected void afterStep(StepExecution stepExecution) {
		LOG.debug("Do something after Job Processor");
	}

}
