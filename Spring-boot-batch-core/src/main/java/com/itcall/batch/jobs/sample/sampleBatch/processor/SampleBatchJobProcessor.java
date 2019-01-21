package com.itcall.batch.jobs.sample.sampleBatch.processor;

import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import com.itcall.batch.config.batch.item.BaseProcessor;

public class SampleBatchJobProcessor extends BaseProcessor<String, String> {

	private int processorErrorCount = 0;

	@Override
	protected void beforeStep(StepExecution stepExecution) {

	}

	@Override
	protected String processor(String item)
			throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
		LOG.debug(" Process <<< = {}", item);

		if(System.currentTimeMillis()%10==0)
			throw new Exception("10%의 확률로 인위적인 에러를 발생합니다... Processor의 현재 에러카운트 " + ++this.processorErrorCount  );

		Thread.sleep(200);
		return item + " >>> Process";
	}

	@Override
	protected void afterStep(StepExecution stepExecution) {

	}

}
