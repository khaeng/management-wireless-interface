package com.itcall.batch.jobs.sample.sampleBatch.writer;

import java.util.List;

import org.springframework.batch.core.StepExecution;

import com.itcall.batch.config.batch.item.BaseWriter;

public class SampleBatchJobWriter extends BaseWriter<String> {

	private int writerErrorCount = 0;

	@Override
	protected void beforeStep(StepExecution stepExecution) {

	}

	@Override
	protected void writer(List<? extends String> items) throws Exception {
		
		if(System.currentTimeMillis()%2==0)
			throw new Exception("50%의 확률로 인위적인 에러를 발생합니다... Writer의 현재 에러카운트 " + ++this.writerErrorCount  );
		
		for (String item : items) {
			LOG.debug(" Writer <<< = {}", item);
			Thread.sleep(100);
		}
		LOG.debug(" Writer Done. Size[{}]", items.size());
	}

	@Override
	protected void afterStep(StepExecution stepExecution) {

	}

}
