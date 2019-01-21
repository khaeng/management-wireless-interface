package com.itcall.batch.config.batch;

import java.util.Arrays;
import java.util.Date;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.itcall.batch.common.service.batchInfo.BatchInfoService;
import com.itcall.batch.common.vo.batchInfo.BatchInfoHstVo;

@Component
public class BatchJobLaucher {

	private static final Logger LOG = LoggerFactory.getLogger(BatchJobLaucher.class);

	@Resource
	private ApplicationContext ctx;

	@Resource(name="jobLauncher")
	private JobLauncher jobLauncher;

	@Resource
	private BatchInfoService batchInfoService;


	public int startBatchJob(String jobName, String...args) {
		Job job = null;
		job = (Job) ctx.getBean(jobName);
		JobExecution execution = startBatchJob(job, args);
		if(execution==null) {
			return -2; // JOB 실행 시 Exception
		}else  if(execution.getExitStatus().getExitCode().contains(ExitStatus.FAILED.getExitCode())) {
			LOG.error("Job[{}] finished at : {} <== with status :[{}], detail[{}]", job.getName(), new Date(), execution.getStatus(), execution);
			return -1; // JOB의 실패
		}else if(!execution.getExitStatus().getExitCode().contains(ExitStatus.COMPLETED.getExitCode())) {
			LOG.warn("Job[{}] finished at : {} <== with status :[{}], detail[{}]", job.getName(), new Date(), execution.getStatus(), execution);
			return 1; // JOB의 성공/실패 이외의 결과
		}else {
			LOG.info("Job[{}] finished at : {} <== with status :[{}], detail[{}]", job.getName(), new Date(), execution.getStatus(), execution);
			return 0; // JOB의 성공
		}
	}
	public JobExecution startBatchJob(Job job, String...args) {

		LOG.debug("jobLauncher              ==> {}", jobLauncher);

		JobExecution execution = null;

		try {

			JobParameters params = JobParamCode.makeJobParameters(args);

			LOG.debug("Job[{}] Started at : {} <== Parameters[{}]", job.getName(), new Date(), params);
	
			execution = jobLauncher.run(job, params);

			// try {Thread.sleep(1000);}catch (Exception e) {}
			// insertJobStatus(job, execution);

			int checkUpdateTerm = 1;
			while (execution.getExitStatus().isRunning()) {
				try {Thread.sleep(1000);}catch (Exception e) {}
				if(BaseBatch.LOG_UPDATE_TIME_SECONDS/1000 < checkUpdateTerm++) {
					updateJobStatus(execution, null);
					// LOG.debug("Job[{}] Chack job each [{}] second - Job is still running ==> with status :[{}]", job.getName(), BaseBatch.LOG_UPDATE_TIME_SECONDS/1000, execution.getStatus().name());
					checkUpdateTerm = 0;
				}
			}

			if(execution.getExitStatus().getExitCode().contains(ExitStatus.FAILED.getExitCode())) {
				LOG.error("Job[{}] finished at : {} <== with status :[{}], detail[{}]", job.getName(), new Date(), execution.getStatus().name(), execution);
				// return execution; // JOB의 실패
			}else if(!execution.getExitStatus().getExitCode().contains(ExitStatus.COMPLETED.getExitCode())) {
				LOG.warn("Job[{}] finished at : {} <== with status :[{}], detail[{}]", job.getName(), new Date(), execution.getStatus().name(), execution);
				// return execution; // JOB의 성공/실패 이외의 결과
//			}else {
				// LOG.info("Job[{}] finished at : {} <== with status :[{}], detail[{}]", job.getName(), new Date(), execution.getStatus().name(), execution);
				// return execution; // JOB의 성공
			}

			// 정상종료(중지/완료) 시에는 Listener에서 Update을 하기때문에 여기서는 하지 않는다.
			// return updateJobStatus(execution, null);
			return execution;

		} catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException | JobParametersInvalidException e) {
			LOG.error("Job[{}] failure at : {} <== with status :[{}], detail[{}]\n===========================[Exception({})===========================\n{}", job.getName(), new Date(), execution==null?execution:execution.getStatus().name(), execution, e.getClass().getSimpleName(), e);
			// return null;  // JOB 실행 시 Exception
			return updateJobStatus(execution, e);
		}

	}

//	private void insertJobStatus(Job job, JobExecution execution){
//		BatchInfoHstVo batchInfoHst = new BatchInfoHstVo(execution);
//		try {
//			batchInfoService.addBatchInfoHst(batchInfoHst);
//		} catch (Exception e) {
//			LOG.error("BatchInfoHst INSERT Error : [{}], {}", batchInfoHst, e );
//		}
//	}

	private JobExecution updateJobStatus(JobExecution execution, Exception e) {
		BatchInfoHstVo batchInfoHst = new BatchInfoHstVo(execution);
		batchInfoHst.setEndTime(execution.getEndTime());
		batchInfoHst.setLastUpdated(execution.getLastUpdated());
		batchInfoHst.setStatus(execution.getStatus().name());
		batchInfoHst.setExitCode(execution.getExitStatus().getExitCode());
		batchInfoHst.setExitMsg(Arrays.toString(execution.getStepExecutions().toArray(new StepExecution[execution.getStepExecutions().size()])));
		batchInfoHst.setRestMsg(e!=null ? e.getMessage() + "\n" + e.getLocalizedMessage() : null);
		try {
			batchInfoService.setBatchInfoHst(batchInfoHst);
		} catch (Exception ex) {
			LOG.error("BatchInfoHst UPDATE Error : [{}], {}", batchInfoHst , ex );
		}
		if(e!=null)
			return null;
		else
			return execution;
	}

}
