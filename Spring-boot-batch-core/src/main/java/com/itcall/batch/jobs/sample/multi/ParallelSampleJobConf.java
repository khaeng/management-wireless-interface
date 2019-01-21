package com.itcall.batch.jobs.sample.multi;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;

import com.itcall.batch.config.batch.BaseJobBatch;

//@ScheduledJob
public class ParallelSampleJobConf extends BaseJobBatch {

	@Override
	public JobExecution scheduledJobs() throws JobExecutionAlreadyRunningException, JobRestartException,
			JobInstanceAlreadyCompleteException, JobParametersInvalidException {
		return doLaucher(parallelSampleJob(), new String[]{"1111", "2222"});
	}

	@Bean
	public Job parallelSampleJob() {
		RunIdIncrementer runIdIncrementer = new RunIdIncrementer();
		return getJobBuilder()
				.incrementer(runIdIncrementer)
				.start(jobStartedStep())
				.split(batchTaskExecutor)
				.add(parallelSampleFlow01(), parallelSampleFlow02(), parallelSampleFlow03())
				.next(jobEndedStep())
				.build()
				.build();
	}

	@Bean
	public Step jobStartedStep() {
		return getStepBuilder().tasklet(new Tasklet() {
			@Override
			public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
				LOG.debug("jobStartedStep to RepeatStatus.FINISHED");
				Thread.sleep(1000);
				return RepeatStatus.FINISHED;
				// throw new Exception("그냥 테스트"); // 실패가 발생해도 다음스탭으로 진행한다.
			}
		}).build();
	}
	@Bean
	public Step jobEndedStep() {
		return getStepBuilder().tasklet(new Tasklet() {
			@Override
			public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
				LOG.debug("jobEndedStep to RepeatStatus.FINISHED");
				Thread.sleep(1000);
				return RepeatStatus.FINISHED;
			}
		}).build();
	}

	@Bean
	public Flow parallelSampleFlow01() {
		return getFlowBuilder()
				.start(swithFlowStartedStep())
				.from(swithFlowStartedStep())
						.on(ExitStatus.FAILED.getExitCode())
						.to(errorStep())
						.next(flowAllEndedStep())
				.from(swithFlowStartedStep())
						.on("*")
						.to(swithFlowStartedNormalNextStep())
						.next(flowAllEndedStep())
				.build();
	}
	@Bean
	public Flow parallelSampleFlow02() {
		return getFlowBuilder()
				.start(parallelSampleStepEndTest())
				.end();
	}
	@Bean
	public Flow parallelSampleFlow03() {
		return getFlowBuilder()
				.start(parallelSampleStepBuildTest())
				.build();
	}


	@Bean
	public Step swithFlowStartedStep() {
		return getStepBuilder().tasklet(new Tasklet() {
			@Override
			public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
				if(System.currentTimeMillis()%2==0) {
					LOG.debug("swithFlowStartedStep to RepeatStatus.FINISHED");
					Thread.sleep(1000);
					return RepeatStatus.FINISHED;
				}else {
					LOG.debug("swithFlowStartedStep to {}", "throw new Exception(\"인위적으로 실패처리한다.\")");
					Thread.sleep(1000);
					throw new Exception("인위적으로 실패처리한다.");
				}
			}
		}).build();
	}
	@Bean
	public Step swithFlowStartedNormalNextStep() {
		return getStepBuilder().tasklet(new Tasklet() {
			@Override
			public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
				LOG.debug("swithFlowStartedNormalNextStep to RepeatStatus.FINISHED");
				Thread.sleep(1000);
				return RepeatStatus.FINISHED;
			}
		}).build();
	}
	@Bean
	public Step parallelSampleStepEndTest() {
		return getStepBuilder().tasklet(new Tasklet() {
			private int countContinues = 0;
			@Override
			public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
				if(System.currentTimeMillis()%5==0) {
					LOG.debug("parallelSampleStepEndTest.countContinues[{}] : return RepeatStatus.FINISHED", ++countContinues);
					Thread.sleep(1000);
					return RepeatStatus.FINISHED;
				}else{
					LOG.debug("parallelSampleStepEndTest.countContinues[{}] : return RepeatStatus.CONTINUABLE", ++countContinues);
					Thread.sleep(1000);
					return RepeatStatus.CONTINUABLE;
				}
			}
		}).build();
	}
	@Bean
	public Step parallelSampleStepBuildTest() {
		return getStepBuilder().tasklet(new Tasklet() {
			private int countContinues = 0;
			@Override
			public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
				if(System.currentTimeMillis()%5==0) {
					LOG.debug("parallelSampleStepBuildTest.countContinues[{}] : return RepeatStatus.FINISHED", ++countContinues);
					Thread.sleep(1000);
					return RepeatStatus.FINISHED;
				}else{
					LOG.debug("parallelSampleStepBuildTest.countContinues[{}] : return RepeatStatus.CONTINUABLE", ++countContinues);
					Thread.sleep(1000);
					return RepeatStatus.CONTINUABLE;
				}
			}
		}).build();
	}
	@Bean
	public Step errorStep() {
		return getStepBuilder().tasklet(new Tasklet() {
			@Override
			public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
				LOG.debug("errorStep to RepeatStatus.FINISHED");
				Thread.sleep(1000);
				return RepeatStatus.FINISHED;
			}
		}).build();
	}
	@Bean
	public Step flowAllEndedStep() {
		return getStepBuilder().tasklet(new Tasklet() {
			@Override
			public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
				LOG.debug("flowAllEndedStep to RepeatStatus.FINISHED");
				Thread.sleep(1000);
				return RepeatStatus.FINISHED;
			}
		}).build();
	}

}
