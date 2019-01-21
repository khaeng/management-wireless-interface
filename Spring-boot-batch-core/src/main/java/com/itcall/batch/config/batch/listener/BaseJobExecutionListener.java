package com.itcall.batch.config.batch.listener;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.MapJobRepositoryFactoryBean;

import com.itcall.batch.common.service.batchInfo.BatchInfoService;
import com.itcall.batch.common.vo.batchInfo.BatchInfoHstVo;

public class BaseJobExecutionListener implements JobExecutionListener {

	private static final Logger LOG = LoggerFactory.getLogger(BaseJobExecutionListener.class);

	private BatchInfoService batchInfoService;
	private JobRegistry jobRegistry; 
	private JobRepository jobRepository;
	private BatchInfoHstVo batchInfoHst;

	public BaseJobExecutionListener(BatchInfoService batchInfoService, JobRegistry jobRegistry, JobRepository jobRepository) {
		this.batchInfoService = batchInfoService;
		this.jobRegistry = jobRegistry;
		this.jobRepository = jobRepository;
	}

	@Override public void beforeJob(JobExecution jobExecution) {
		try {
			this.batchInfoHst = new BatchInfoHstVo(jobExecution);
			BatchInfoHstVo lastExcutedJobInstansInfo = new BatchInfoHstVo();
			lastExcutedJobInstansInfo.setJobName(jobExecution.getJobInstance().getJobName());
			lastExcutedJobInstansInfo = this.batchInfoService.getBatchInfoHstLastOne(lastExcutedJobInstansInfo);
			if(lastExcutedJobInstansInfo!=null && lastExcutedJobInstansInfo.getJobId()>=0) {
				/***************************************************
				 * 직접 Execution정보를 수정하고 싶지만, MapJobExecutionDao 에서 에러가 발생한다.
				 * jobRegistry 등록시 job정보를 수정하는게 맞지만, 시간이 부족한 관계로.
				 * 히스토리 누적용 vo만 변경하여 로그를 쌓기로 한다. 
				 * app가 실제 돌때는 0부터 카운트 하지만, 로그는 결과이므로... 결과는 DB에 누적된 정보 후 부터 진행한다.
//				JobInstance jobInstance = jobExecution.getJobInstance();
//				jobInstance.setId(lastExcutedJobInstansInfo.getJobId() + 1);
//				jobExecution.setJobInstance(jobInstance);
//				jobExecution.setId(lastExcutedJobInstansInfo.getJobExecId() + 1);
				 ***************************************************/
 				this.batchInfoHst.setJobId(lastExcutedJobInstansInfo.getJobId() + 1);
				this.batchInfoHst.setJobExecId(lastExcutedJobInstansInfo.getJobExecId() + 1);
			}
			batchInfoService.addBatchInfoHst(this.batchInfoHst);
		} catch (Exception e) {
			LOG.error("BatchInfoHst INSERT Error : [{}], {}", this.batchInfoHst, e );
		}
	}
	@Override public void afterJob(JobExecution jobExecution) {
		this.batchInfoHst.setEndTime(jobExecution.getEndTime());
		this.batchInfoHst.setLastUpdated(jobExecution.getLastUpdated());
		this.batchInfoHst.setStatus(jobExecution.getStatus().name());
		this.batchInfoHst.setExitCode(jobExecution.getExitStatus().getExitCode());
		this.batchInfoHst.setExitMsg(Arrays.toString(jobExecution.getStepExecutions().toArray(new StepExecution[jobExecution.getStepExecutions().size()])));
		try {
			batchInfoService.setBatchInfoHst(this.batchInfoHst);
		} catch (Exception ex) {
			LOG.error("BatchInfoHst UPDATE Error : [{}], {}", this.batchInfoHst , ex );
		}finally {
			jobRegistry.unregister(jobExecution.getJobInstance().getJobName());
			if(jobRepository != null && jobRepository instanceof MapJobRepositoryFactoryBean) {
				((MapJobRepositoryFactoryBean) jobRepository).clear();
//					((MapJobRepositoryFactoryBean) jobRepository).getJobExecutionDao().clear();
//					((MapJobRepositoryFactoryBean) jobRepository).getJobInstanceDao().clear();
//					((MapJobRepositoryFactoryBean) jobRepository).getStepExecutionDao().clear();
//					((MapJobRepositoryFactoryBean) jobRepository).getExecutionContextDao().clear();
			}
		}
	}

}
