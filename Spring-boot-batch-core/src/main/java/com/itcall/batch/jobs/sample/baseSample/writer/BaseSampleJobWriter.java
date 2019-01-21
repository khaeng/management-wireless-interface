package com.itcall.batch.jobs.sample.baseSample.writer;

import java.util.Arrays;
import java.util.List;

import org.springframework.batch.core.StepExecution;

import com.itcall.batch.biz.vo.sample.bizSam.SampleInfoVo;
import com.itcall.batch.config.batch.item.BaseWriter;

public class BaseSampleJobWriter extends BaseWriter<List<SampleInfoVo>> {

	@Override
	protected void beforeStep(StepExecution stepExecution) {
		LOG.debug("Do something before Job Writer");
	}

	@Override
	public void writer(List<? extends List<SampleInfoVo>> items) throws Exception {

		LOG.debug("전달된 파라메터가 전달순서와 동일하게 배열로 들어온다. Job({}), Step({}), My Parameters[{}]"
				,getStepExecution().getJobExecution().getJobInstance().getJobName()
				,getStepExecution().getStepName()
				,Arrays.toString(getJobParameters()));

		LOG.debug("batch Writer ::: size({}) ::: {}", items.size(), items);
	}

	@Override
	protected void afterStep(StepExecution stepExecution) {
		LOG.debug("Do something after Job Writer");
	}

}