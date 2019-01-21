package com.itcall.batch.jobs.sample.readFiles.tasklet;

import java.io.File;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

import com.itcall.batch.biz.mapper.sample.bizSam.Sample2thDbMapper;
import com.itcall.batch.biz.mapper.sample.bizSam.SampleInfoMapper;
import com.itcall.batch.config.batch.item.BaseTasklet;

public class ReadFilesJobTasklet extends BaseTasklet {

	@javax.annotation.Resource
	private SampleInfoMapper sampleInfoMapper;

	@javax.annotation.Resource
	private Sample2thDbMapper sample2thDbMapper;

	@Value("#{batch['biz.batch.readFileJob.directory']?:'file:/'}")
	private Resource directory;

	@Value("${sec.value.test:test}") // 어떤 properties파일이던 키를 찾아가져오면(중복에대한 추출보장없음) 암호화된 데이터는 복호화 해준다. 없으면 기본값 test
	private String secTestApp;
	@Value("${common.sec.value.test:commonTest}") // 어떤 properties파일이던 키를 찾아가져오면(중복에대한 추출보장없음) 암호화된 데이터는 복호화 해준다. 없으면 기본값 commonTest
	private String secTestCommon;
	@Value("#{batch['batch.sec.value.test']?:'batchTest'}") // batch-local.properties파일이에서 키를 가져오며 암호화된 데이터는 복호화 해준다. 없으면 기본값 batchTest
	private String secTestBatch;

	private int repeatCount;

	@Override
	public RepeatStatus execTasklet(StepContribution contribution, ChunkContext chunkContext) throws Exception {

		LOG.debug("RepeatCount[{}], sec.value.test=[{}], common.sec.value.test=[{}], batch.sec.value.test=[{}]", ++this.repeatCount, secTestApp, secTestCommon, secTestBatch);

//		LOG.debug("전달된 파라메터가 전달순서와 동일하게 배열로 들어온다. Job({}), Step({}), My Parameters[{}]"
//				,chunkContext.getStepContext().getStepExecution().getJobExecution().getJobInstance().getJobName()
//				,chunkContext.getStepContext().getStepExecution().getStepName()
//				,Arrays.toString(getJobParameters()));
//
//		List<SampleInfoVo> list = sampleInfoMapper.selectListTest(getJobParameters());
//		LOG.debug("<=={}", list);
//
//		List<SampleInfoVo> list2th = sample2thDbMapper.selectListTest(getJobParameters());
//		LOG.debug("<=={}", list2th);

		File dir = directory.getFile();
		Assert.state(dir.isDirectory());

		File[] files = dir.listFiles();
		for (int i = 0; i < files.length; i++) {
//			boolean deleted = files[i].delete();
//			if (!deleted) {
//				throw new UnexpectedJobExecutionException("Could not delete file " + files[i].getPath());
//			} else {
//				System.out.println(files[i].getPath() + " is deleted!");
//			}
			Thread.sleep(300);
			LOG.info("You try file or directory deleted : {}",files[i].getAbsoluteFile());
		}
		
		if(this.repeatCount>=30) {
			this.repeatCount=0;
			return addBizMsgAndClose(files.length, files.length + "건의 파일처리가 완료되었습니다."); // RepeatStatus.FINISHED;
		}else {
			if(System.currentTimeMillis()%20==0)
				throw new Exception("5%의 확률로 인위적인 에러를 발생합니다... 현재 처리카운트 " + this.repeatCount);
			return addBizMsgAndContinue(files.length, files.length + "건의 파일처리가 완료되었습니다."); // RepeatStatus.CONTINUABLE;
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(directory, "directory must be set");
	}

}
