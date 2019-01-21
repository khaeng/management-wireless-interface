package com.itcall.batch.jobs.sample.dbTest;

import java.util.List;

import org.springframework.batch.core.StepExecution;

import com.itcall.batch.common.vo.batchInfo.BatchInfoVo;
import com.itcall.batch.config.batch.item.BaseWriter;

public class DbTestJobWriter extends BaseWriter<BatchInfoVo> {

	@Override
	protected void beforeStep(StepExecution stepExecution) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void writer(List<? extends BatchInfoVo> items) throws Exception {
		if(items!=null)
			for (BatchInfoVo batchInfoVo : items) {
//				if(list!=null)
//					for (BatchInfoVo batchInfoVo : list) {
						LOG.debug("DbTestJobWriter.List.List.batchInfoVo[{}]", batchInfoVo);
//					}
			}
		LOG.debug("Writer done...!!!");
	}

	@Override
	protected void afterStep(StepExecution stepExecution) {
		// TODO Auto-generated method stub
		
	}


}
