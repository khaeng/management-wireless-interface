package com.itcall.batch.jobs.sample.baseSample.reader;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import com.itcall.batch.biz.mapper.sample.bizSam.Sample2thDbMapper;
import com.itcall.batch.biz.mapper.sample.bizSam.SampleInfoMapper;
import com.itcall.batch.biz.vo.sample.bizSam.SampleInfoVo;
import com.itcall.batch.config.batch.item.BaseReader;

public class BaseSampleJobReader extends BaseReader<List<SampleInfoVo>> {

	private int readCount = 0;

	@Resource
	private SampleInfoMapper sampleInfoMapper;

	@Resource
	private Sample2thDbMapper sample2thDbMapper;

	@Override
	protected void beforeStep(StepExecution stepExecution) {
		LOG.debug("Do something before Job Reader");
	}

	@Override
	public List<SampleInfoVo> reader()
			throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {

		LOG.debug("전달된 파라메터가 전달순서와 동일하게 배열로 들어온다. Job({}), Step({}), My Parameters[{}]"
				,getStepExecution().getJobExecution().getJobInstance().getJobName()
				,getStepExecution().getStepName()
				,Arrays.toString(getJobParameters()));

		if(++readCount>10) {
			// 초기화 후 처리데이터가 끝났다는 표시로 null보낸다. (나중에 다시 실행할걸 생각해서 초기화 해주고...) 즉, 처리할 데이터가 없어야 배치는 끝난다.
			readCount=0;
			addBizMsg(readCount, "종료메시지를 입력한다.");
			return null;
		}
		List<SampleInfoVo> list = sampleInfoMapper.selectListTest(getJobParameters());
		for (SampleInfoVo sampleInfoVo : list) {
			LOG.debug("batch Reader sampleInfoMapper.selectListTest ::: {}", sampleInfoVo);
		}
		list = sample2thDbMapper.selectListTest(getJobParameters());
		for (SampleInfoVo sampleInfoVo : list) {
			LOG.debug("batch Reader sample2thDbMapper.selectListTest ::: {}", sampleInfoVo);
		}
		return list;
	}

	@Override
	protected void afterStep(StepExecution stepExecution) {
		LOG.debug("Do something after Job Reader");
	}

}
