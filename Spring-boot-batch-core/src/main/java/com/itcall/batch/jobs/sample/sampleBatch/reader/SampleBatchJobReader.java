package com.itcall.batch.jobs.sample.sampleBatch.reader;

import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import com.itcall.batch.config.batch.item.BaseReader;

public class SampleBatchJobReader extends BaseReader<String> {

	private int count = 0;
	private int readerErrorCount = 0;

	@Override
	protected void beforeStep(StepExecution stepExecution) {

	}

	@Override
	protected String reader()
			throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
		if(System.currentTimeMillis()%10==0)
			throw new Exception("10%의 확률로 인위적인 에러를 발생합니다... Reader의 현재 에러카운트 " + ++this.readerErrorCount );
		if(++count<100) {
				Thread.sleep(500);
			LOG.debug(" Reader <<< = {}", "Reader String Test");
			return "Reader String Test";
		}else {
			addBizMsg(this.count, "SampleBatchJob Working Done... from SampleBatchJobReader WorkingCount[" + this.count + "]");
			count = 0;
			return null;
		}
	}

	@Override
	protected void afterStep(StepExecution stepExecution) {

	}

}
