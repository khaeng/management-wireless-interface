package com.itcall.batch.jobs.sample.taskletSample.tasklet;

import java.util.Arrays;

import javax.annotation.Resource;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;

import com.itcall.batch.biz.service.sample.bizSam.BizSampleService;
import com.itcall.batch.config.batch.item.BaseTasklet;

public class TaskletSampleJobTasklet extends BaseTasklet {

	@Resource
	private BizSampleService bizSampleService;

	@Value("${spring.profiles.active:prod}")
	private String profile;

	@Override
	public RepeatStatus execTasklet(StepContribution contribution, ChunkContext chunkContext) throws Exception {

		LOG.error("테스트 ::: {}", profile);

		LOG.debug("전달된 파라메터가 전달순서와 동일하게 배열로 들어온다. Job({}), Step({}), My Parameters[{}]"
				,chunkContext.getStepContext().getStepExecution().getJobExecution().getJobInstance().getJobName()
				,chunkContext.getStepContext().getStepExecution().getStepName()
				,Arrays.toString(getJobParameters()));

		LOG.debug("batch Tasklet ::: {} <== StepContribution({}), ChunkContext({})", bizSampleService.bizSampleMethod("테스트 서비스 호출"), contribution, chunkContext);
		return addBizMsgAndClose(1, "현재 Tasklet의 업무적 성격을 기술합니다. from TaskletSampleJobTasklet");
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		
	}

}
