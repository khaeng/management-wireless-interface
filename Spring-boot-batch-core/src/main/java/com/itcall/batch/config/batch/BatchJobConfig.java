package com.itcall.batch.config.batch;

import java.sql.Connection;
import java.sql.SQLException;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.batch.core.configuration.support.MapJobRegistry;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.explore.support.MapJobExplorerFactoryBean;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.MapJobRepositoryFactoryBean;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.batch.BatchProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.itcall.batch.config.support.EnvironmentSupport;

@Configuration
public class BatchJobConfig extends DefaultBatchConfigurer {

	private static final Logger LOG = LoggerFactory.getLogger(BatchJobConfig.class);

//	@Resource
//	private ApplicationContext ctx;
//
//	@Resource
//	private TaskScheduler taskScheduler;
//
//	@Resource
//	private CmdJobLaucher cmdJobLaucher;
//
//	private static final Map<String, ScheduledFuture<?>> SCHEDULER_HISTORY_MAP = new HashMap<String, ScheduledFuture<?>>();
//	private static final List<String> JOB_KILLING_LIST = new ArrayList<String>();
//	private static final int MAX_TRY_COUNT = 10;
//
//	private static void sleep(final int millseconds) {
//		try {Thread.sleep(500);} catch (InterruptedException e) {e.printStackTrace();}
//	}
//
//	public Map<String, Object> getBatchInfoList(JobOperator jobOperator){
//		Map<String, Object> result = new HashMap<String, Object>();
//		result.put("SCHEDULED_INFO", SCHEDULER_HISTORY_MAP);
//		result.put("jobNames", jobOperator.getJobNames());
//		result.putAll(getRunningJobList(jobOperator, jobOperator.getJobNames()));
//		return result;
//	}
//
//	public Map<String, Object> getRunningJobList(JobOperator jobOperator, Set<String> jobNames) {
//		Map<String, Object> result = new HashMap<String, Object>();
//		result.put("jobNames", jobNames);
//		for (String jobName : jobNames) {
//			try {
//				result.put(jobName, jobOperator.getRunningExecutions(jobName));
//			} catch (NoSuchJobException e) {
//				result.put(jobName, e.getMessage());
//				LOG.error("{}",e);
//			}
//		}
//		return result;
//	}
//
//	@Async
//	public void jobAsyncStart(String jobName, String[] params) {
//		jobStart(jobName, params);
//	}
//
//	public int jobStart(String jobName, String[] params) {
//		return cmdJobLaucher.startBatchJob(jobName, params);
//	}
//
//	/**
//	 * 실행중인 Job을 정지한다.
//	 * @param jobOperator
//	 * @param jobName
//	 * @throws NoSuchJobException
//	 * @throws NoSuchJobExecutionException
//	 * @throws JobExecutionNotRunningException
//	 */
//	public boolean jobStop(JobOperator jobOperator, String jobName) throws NoSuchJobException, NoSuchJobExecutionException, JobExecutionNotRunningException {
//		Set<Long> jobExecutionIds = jobOperator.getRunningExecutions(jobName);
//		for (Long jobExecutionId : jobExecutionIds) {
//			int limitCount = 0;
//			while (!jobOperator.stop(jobExecutionId)) {
//				if(++limitCount>=MAX_TRY_COUNT) {
//					LOG.error("Job[{}], ExecutionId[{}] Stop operation failure... jobExecution summary : [{}]",jobName, jobExecutionId, jobOperator.getSummary(jobExecutionId));
//					return false;
//				}
//				LOG.warn("Job[{}], ExecutionId[{}] Stop operation retrying to[{}/{}]... jobExecution summary : [{}]",jobName, jobExecutionId, limitCount, MAX_TRY_COUNT, jobOperator.getSummary(jobExecutionId));
//				sleep(500);
//			}
//		}
//		return true;
//	}
//
//	/**
//	 * JOB의 클론정책을 등록한다. 이미 등록되어 있으면 삭제한다.
//	 * 
//	 * @param jobName
//	 * @param cronCmd
//	 * @return
//	 * @throws NoSuchMethodException
//	 * @throws SecurityException
//	 */
//	public ScheduledFuture<?> setScheduler(String jobName, String cronCmd) throws NoSuchMethodException, SecurityException, Exception {
//		try {
//			Method scheduledJobs=null;
//			BaseBatch baseBatch = (BaseBatch) ctx.getBean(jobName.endsWith(BaseBatch.JOB_CONF_POST_FIX)?jobName:jobName+BaseBatch.JOB_CONF_POST_FIX);
//			scheduledJobs = baseBatch.getClass().getMethod(BaseBatch.JOB_SCHEDULED_METHOD_NAME);
//			ScheduledMethodRunnable scheduledMethodRunnable = new ScheduledMethodRunnable(baseBatch, scheduledJobs) ;
//			CronTrigger cronTrigger = new CronTrigger(cronCmd); // ("0/10 * * * * *");
//			if(removeScheduler(jobName)) {
//				ScheduledFuture<?> scheduledFuture = taskScheduler.schedule(scheduledMethodRunnable, cronTrigger);
//				SCHEDULER_HISTORY_MAP.put(jobName, scheduledFuture);
//				LOG.info("Job[{}], Scheduler operation done... cron info[{}]", jobName, cronCmd);
//				return scheduledFuture;
//			}else {
//				LOG.error("Job[{}], Scheduler operation failure... cron info[{}]", jobName, cronCmd);
//				throw new Exception("Job[" + jobName + "] : Remove before scheduler operation failure...");
//			}
//		} catch (Exception e) {
//			throw new Exception(e);
//		}
//	}
//
////	public ScheduledFuture<?> setScheduler(BaseBatch baseBatch, String cronCmd){
////		try {
////			Method scheduledJobs=null;
////			scheduledJobs = baseBatch.getClass().getMethod(BaseBatch.JOB_SCHEDULED_METHOD_NAME);
////			ScheduledMethodRunnable scheduledMethodRunnable = new ScheduledMethodRunnable(baseBatch, scheduledJobs) ;
////			CronTrigger cronTrigger = new CronTrigger(cronCmd); // ("0/10 * * * * *");
////			if(removeScheduler(jobName)) {
////				ScheduledFuture<?> scheduledFuture = taskScheduler.schedule(scheduledMethodRunnable, cronTrigger);
////				SCHEDULER_HISTORY_MAP.put(jobName, scheduledFuture);
////				LOG.info("Job[{}], Scheduler operation done... cron info[{}]", jobName, cronCmd);
////				return scheduledFuture;
////			}else {
////				LOG.error("Job[{}], Scheduler operation failure... cron info[{}]", jobName, cronCmd);
////				throw new Exception("Job[" + jobName + "] : Remove before scheduler operation failure...");
////			}
////		} catch (Exception e) {
////			throw new Exception(e);
////		}
////	}
//
//	/**
//	 * 스케줄링된 Job을 제거한다.
//	 * @param jobName
//	 * @return
//	 */
//	public boolean removeScheduler(String jobName) {
//		ScheduledFuture<?> scheduledFuture = SCHEDULER_HISTORY_MAP.get(jobName);
//		if(scheduledFuture!=null) {
//			int limitCount = 0;
//			while (!scheduledFuture.cancel(true)) {
//				if(++limitCount>=MAX_TRY_COUNT) {
//					LOG.error("Job[{}], Remove scheduler operation failure... ScheduledFuture summary : [{}]",jobName, scheduledFuture);
//					return false;
//				}
//				LOG.warn("Job[{}], Remove scheduler operation retrying to[{}/{}]... ScheduledFuture summary : [{}]",jobName, limitCount, MAX_TRY_COUNT, scheduledFuture);
//				sleep(500);
//			}
//			SCHEDULER_HISTORY_MAP.remove(jobName);
//		}
//		return true;
//	}
//
//	public boolean killJob(JobOperator jobOperator, String jobName) throws NoSuchJobException {
//		ScheduledFuture<?> scheduledFuture = SCHEDULER_HISTORY_MAP.get(jobName);
//		ScheduledExecutorService scheduledExecutorService = ((ThreadPoolTaskScheduler)taskScheduler).getScheduledExecutor();
//		ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = ((ThreadPoolTaskScheduler)taskScheduler).getScheduledThreadPoolExecutor();
//		Set<Long> jobExecutionIds = jobOperator.getRunningExecutions(jobName);
//		for (Long jobExecutionId : jobExecutionIds) {
//			//jobOperator.
//		}
//		
//		return true;
//	}
//
//	public static boolean isKillJob(String jobName) {
//		for (String killJobName : JOB_KILLING_LIST) {
//			if(killJobName.equals(jobName)) {
//				return JOB_KILLING_LIST.remove(killJobName);
//			}
//		}
//		return false;
//	}



/*********************************************************************************************************/
/*********************************************************************************************************/
/*********************************************************************************************************/



	/** DATABASE.META.DATA 전용 *** 배치환경설정 **/

	@Resource(name=EnvironmentSupport.MASTER_DATA_SOURCE_NAME)
	private /*org.apache.tomcat.jdbc.pool.*/DataSource dataSource;

//	@Resource // (name=BATCH_TRANSACTION_MANAGER)
//	private PlatformTransactionManager transactionManager;
//
////	@Bean(name="transactionManager")
////	public PlatformTransactionManager transactionManager(DataSource dataSource) {
////		return new DataSourceTransactionManager(dataSource);
////	}
////
////	@Bean(name= {"dbJobRepository","jobRepository"})
////	public JobRepository jobRepository(
////			@Qualifier(BATCH_DATA_SOURCE) DataSource jdbcDataSource,
////			@Qualifier("transactionManager") PlatformTransactionManager transactionManager) throws Exception {
////		JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
////		factory.setDataSource(jdbcDataSource);
////		factory.setTransactionManager(transactionManager);
////		factory.setDatabaseType("oracle");
////		factory.afterPropertiesSet();
////		return  factory.getObject();
////	}
//
//	@Override
//	protected JobRepository createJobRepository() throws Exception {
//		JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
//		factory.setDataSource(dataSource);
//		factory.setTransactionManager(transactionManager);
//		if(dataSource.getDriverClassName().startsWith(ALTIBASE_DRIVER_CLASSNAME)) {
//			factory.setDatabaseType(DatabaseType.ORACLE.getProductName());
//		}
//		factory.afterPropertiesSet();
//		return  factory.getObject();
//		// return super.createJobRepository();
//	}
//
//////	@Bean 
//////	JobRepositoryFactoryBean jobRepositoryFactoryBean(DataSource dataSource) throws Exception{
//////		JobRepositoryFactoryBean jobRepositoryFactoryBean = new JobRepositoryFactoryBean();
//////		jobRepositoryFactoryBean.setDataSource(dataSource);
//////		jobRepositoryFactoryBean.setDatabaseType("oracle");
//////		return jobRepositoryFactoryBean;
//////	}
////
//	@Bean(name="jobExplorer")
//	public JobExplorer jobExplorer(DataSource dataSource) throws Exception {
//		JobExplorerFactoryBean jobExplorerFactoryBean = new JobExplorerFactoryBean();
//		jobExplorerFactoryBean.setDataSource(dataSource);
//		jobExplorerFactoryBean.afterPropertiesSet();
//		return jobExplorerFactoryBean.getObject();
//	}
////
//////	@Bean(name= {"jobLauncher","shotJobLauncher"})
//////	public JobLauncher jobLauncher(
//////			@Qualifier("batchTaskExecutor") ThreadPoolTaskExecutor batchTaskExecutor,
//////			@Qualifier("jobRepository") JobRepository jobRepository) throws Exception {
//////		SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
//////		jobLauncher.setTaskExecutor(batchTaskExecutor);
//////		jobLauncher.setJobRepository(jobRepository);
//////		jobLauncher.afterPropertiesSet();
//////		return jobLauncher;
//////	}



/*********************************************************************************************************/
/*********************************************************************************************************/
/*********************************************************************************************************/


	/** MEMORY.META.DATA 전용 *** 배치환경설정 **/

	@Override
	public void setDataSource(DataSource dataSource) {
		super.setDataSource(null);
	}

	@Bean(name="resourcelessTransactionManager")
	public ResourcelessTransactionManager resourcelessTransactionManager() {
		return new ResourcelessTransactionManager();
	}

	@Bean(name="mapJobRepositoryFactory")
	public MapJobRepositoryFactoryBean mapJobRepositoryFactory(
			@Qualifier("resourcelessTransactionManager") 
			ResourcelessTransactionManager resourcelessTransactionManager) throws Exception {
		MapJobRepositoryFactoryBean factory = new MapJobRepositoryFactoryBean(resourcelessTransactionManager);
		factory.afterPropertiesSet();
		return factory;
	}

	@Bean(name="jobRepository")
	public JobRepository mapJobRepository(
			@Qualifier("mapJobRepositoryFactory") 
			MapJobRepositoryFactoryBean mapJobRepository) throws Exception {
		return mapJobRepository.getObject();
	}

	@Bean(name="jobExplorer")
	public JobExplorer jobExplorer(
			@Qualifier("mapJobRepositoryFactory") MapJobRepositoryFactoryBean mapJobRepository) throws Exception {
		MapJobExplorerFactoryBean jobExplorerFactory = new MapJobExplorerFactoryBean(mapJobRepository);
		jobExplorerFactory.afterPropertiesSet();
		return jobExplorerFactory.getObject();
	}

	@Bean(name= {"jobLauncher","shotJobLauncher"})
	public JobLauncher shotJobLauncher(
			@Qualifier("batchTaskExecutor") ThreadPoolTaskExecutor batchTaskExecutor, 
			@Qualifier("jobRepository") JobRepository mapJobRepository) throws Exception {
		SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
		jobLauncher.setTaskExecutor(batchTaskExecutor);
		jobLauncher.setJobRepository(mapJobRepository);
		jobLauncher.afterPropertiesSet();
		return jobLauncher;
	}



/*********************************************************************************************************/
/*********************************************************************************************************/
/*********************************************************************************************************/


	/** MEMORY.META.DATA && DATABASE.META.DATA *** 공통으로 필요한 배치환경설정 **/

	@Bean
	public JobRegistry jobRegistry() {
		JobRegistry jobRegistry = new MapJobRegistry();
		return jobRegistry;
	}

	@Bean
	public JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor(JobRegistry jobRegistry) {
		JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor = new JobRegistryBeanPostProcessor();
		jobRegistryBeanPostProcessor.setJobRegistry(jobRegistry);
		return jobRegistryBeanPostProcessor;
	}

	@Bean
	@Primary
	public BatchProperties batchProperties() {
		BatchProperties batchProperties = new BatchProperties();
		/**************  
		 * 없는 schema를 로드하면 에러가 발생한다. 
		 * 따라서 appication.properties에 property로 로드안하는것으로 처리함. 
		 * Meta-DB를 사용할 경우 풀어줘야 한다.
		 * # Map.Job.Repository를 사용할때는 Schema로드를 하지 않는다.
		 * spring.batch.initializer.enabled=false/true
		 * spring.batch.initialize-schema=naver/always # SpringBoot2.x에서부터 적용된다.
		 * 2018.09.06 MapJobRepository를 사용할 경우 Programable로 제어한다.(Properties에서 제거함)
		 *   **************/
		if(getTransactionManager() instanceof ResourcelessTransactionManager) {
			// batchProperties.setSchema(null); // EnvironmentSupport.MAP_META_BATCH_SCHEMA_LOCATION);
			// LOG.info("Spring-Batch schema changed to mapJobRepository file[{}]",EnvironmentSupport.MAP_META_BATCH_SCHEMA_LOCATION);
			batchProperties.getInitializer().setEnabled(false);
			LOG.info("Spring-Batch schema changed to mapJobRepository batchProperties.getInitializer().isEnabled()[{}]",batchProperties.getInitializer().isEnabled());
			return batchProperties;
		}
		String customBatchSchema = null;
		if(this.dataSource instanceof org.apache.tomcat.jdbc.pool.DataSource) {
			org.apache.tomcat.jdbc.pool.DataSource convertDataSource = (org.apache.tomcat.jdbc.pool.DataSource) this.dataSource;
			LOG.debug("MasterDatabase[{}], DriverName[{}], DbUrl[{}]", convertDataSource.getName()
					, convertDataSource.getDriverClassName(), convertDataSource.getUrl());
			if(convertDataSource.getDriverClassName().startsWith(EnvironmentSupport.ALTIBASE_DRIVER_CLASSNAME)) {
				customBatchSchema = EnvironmentSupport.ALTIBASE_BATCH_SCHEMA_LOCATION;
			}
		}else {
			Connection connection = null;
			try {
				connection = this.dataSource.getConnection();
				LOG.debug("MasterDatabase[{}], DriverName[{}], DbUrl[{}]", connection.getMetaData().getDatabaseProductName()
						, connection.getMetaData().getDriverName()
						, connection.getMetaData().getURL());
				if(connection.getMetaData().getDatabaseProductName().equalsIgnoreCase(EnvironmentSupport.ALTIBASE_PRODUCT_NAME)) {
					customBatchSchema = EnvironmentSupport.ALTIBASE_BATCH_SCHEMA_LOCATION;
				}
				connection.close();
			} catch (SQLException e) {
				LOG.error("MasterDatabase Detect Error for BatchSchemaDetecting... : {}", e);
			} finally {
				try {if(connection!=null)connection.close();} catch (SQLException e) {
					LOG.error("MasterDatabase Connection Close Error for BatchSchemaDetecting... : {}", e);
				}
			}
		}
		
		if(customBatchSchema!=null) {
			batchProperties.setSchema(customBatchSchema);
			LOG.info("Spring-Batch schema changed to [{}]",customBatchSchema);
		}
		return batchProperties;
	}

}
