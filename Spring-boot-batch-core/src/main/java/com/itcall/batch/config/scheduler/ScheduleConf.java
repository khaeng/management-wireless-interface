package com.itcall.batch.config.scheduler;

import java.util.concurrent.ScheduledExecutorService;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

//@Configuration
@Component
@Lazy
public class ScheduleConf extends WebMvcConfigurationSupport {

	Logger LOG = LoggerFactory.getLogger(ScheduleConf.class);

	@Autowired(required=false)
	private TaskScheduler taskScheduler;
	
//	@Resource
//	private TaskExecutor taskExecutor;
//	@Autowired(required=false)
//	private TaskExecutor batchTaskExecutor;
	@Autowired(required=false)
	private TaskExecutor batchShotTaskExecutor;
	
	
	@Autowired(required=false)
	private ScheduledExecutorService scheduledExecutorService;
	
	@Autowired(required=false)
	private JobBuilderFactory jobBuilderFactory;

	@Autowired(required=false)
	private StepBuilderFactory stepBuilderFactory;

	@Autowired(required=false)
	private DataSource dataSource;
	@Autowired(required=false)
	private PlatformTransactionManager transactionManager;
	@Autowired(required=false)
	private ResourcelessTransactionManager resourcelessTransactionManager;
	@Autowired(required=false)
	private JobRepository jobRepository;

//	@Resource(name="shotJobLauncher") // 이걸 사용하는 경우 에러가 발생한다. 왜?
	private JobLauncher jobLauncher;
//	@Resource(name="jobLauncher")
//	private JobLauncher oldJobLauncher;

	@Autowired(required=false)
	private JobExplorer jobExplorer;
	@Autowired(required=false)
	private JobRegistry jobRegistry;
	
//	public TaskScheduler taskScheduler() {
//		if(this.taskScheduler==null) {
//			this.taskScheduler = new SchduledEx
//		}
//	};
	
//	@Autowired
//	private ReadFilesJobConf readFilesJobConf;

	@PostConstruct
	// @Bean
	public void initAllScheduledService() {
		
//		LOG.debug("taskScheduler            ==> {}", taskScheduler);
////		LOG.debug("taskExecutor             ==> {}", taskExecutor);
////		LOG.debug("batchTaskExecutor        ==> {}", batchTaskExecutor);
//		LOG.debug("batchShotTaskExecutor    ==> {}", batchShotTaskExecutor);
//		LOG.debug("scheduledExecutorService ==> {}", scheduledExecutorService);
//		LOG.debug("jobBuilderFactory        ==> {}", jobBuilderFactory);
//		LOG.debug("stepBuilderFactory       ==> {}", stepBuilderFactory);
//
//		
//		LOG.debug("dataSource               ==> {}", dataSource);
//		LOG.debug("transactionManager       ==> {}", transactionManager);
//		LOG.debug("resourcelessTransactionManager==> {}", resourcelessTransactionManager);
//		LOG.debug("jobRepository            ==> {}", jobRepository);
//		LOG.debug("jobLauncher              ==> {}", jobLauncher);
////		LOG.debug("jobLauncher.OLD          ==> {}", oldJobLauncher);
//		LOG.debug("jobExplorer              ==> {}", jobExplorer);
//		LOG.debug("jobRegistry              ==> {}", jobRegistry);
//		
//		LOG.debug("jobExplorer.getJobNames()==> {}", jobExplorer.getJobNames());
//		LOG.debug("jobRegistry.getJobNames()==> {}", jobRegistry.getJobNames());
		
//		try {
//			Method scheduledJobs=null;
//			scheduledJobs = readFilesJobConf.getClass().getMethod("scheduledJobs");
//			ScheduledMethodRunnable scheduledMethodRunnable = new ScheduledMethodRunnable(readFilesJobConf, scheduledJobs) ;
//			CronTrigger cronTrigger = new CronTrigger("0/10 * * * * *");
//			ScheduledFuture<?> scheduledFuture = taskScheduler.schedule(scheduledMethodRunnable, cronTrigger);
//			 scheduledFuture.cancel(true); // 작동함.
////			안먹음. ((ThreadPoolTaskScheduler)taskScheduler).getScheduledThreadPoolExecutor().remove(scheduledMethodRunnable);
//		} catch (NoSuchMethodException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (SecurityException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		//((ThreadPoolTaskScheduler)taskScheduler).getScheduledThreadPoolExecutor().remove(task)
		
//		ScheduledAnnotationBeanPostProcessor scheduledAnnotationBeanPostProcessor = new ScheduledAnnotationBeanPostProcessor();
//		scheduledAnnotationBeanPostProcessor.postProcessAfterInitialization(bean, beanName)
		
		
		// ScheduledAnnotationBeanPostProcessor 에서 processScheduled(scheduled, method, bean)를 호출함.
//		스케줄테스크를 사용함. ?
//		private final Map<Object, Set<ScheduledTask>> scheduledTasks =
//				new IdentityHashMap<Object, Set<ScheduledTask>>(16);
		// 아래에서의 registrar는 private final ScheduledTaskRegistrar registrar = new ScheduledTaskRegistrar();임.
		// ScheduledAnnotationBeanPostProcessor.processScheduled
//		// Check cron expression
//		String cron = scheduled.cron();
//		if (StringUtils.hasText(cron)) {
//			Assert.isTrue(initialDelay == -1, "'initialDelay' not supported for cron triggers");
//			processedSchedule = true;
//			String zone = scheduled.zone();
//			if (this.embeddedValueResolver != null) {
//				cron = this.embeddedValueResolver.resolveStringValue(cron);
//				zone = this.embeddedValueResolver.resolveStringValue(zone);
//			}
//			TimeZone timeZone;
//			if (StringUtils.hasText(zone)) {
//				timeZone = StringUtils.parseTimeZoneString(zone);
//			}
//			else {
//				timeZone = TimeZone.getDefault();
//			}
//			tasks.add(this.registrar.scheduleCronTask(new CronTask(runnable, new CronTrigger(cron, timeZone))));
//		}
		
		
		// 특정 bean에서 스케줄 어노테이션을 꺼네온다.
//		public Object postProcessAfterInitialization(final Object bean, String beanName) {
//			Class<?> targetClass = AopUtils.getTargetClass(bean);
//			if (!this.nonAnnotatedClasses.contains(targetClass)) {
//				Map<Method, Set<Scheduled>> annotatedMethods = MethodIntrospector.selectMethods(targetClass,
//						new MethodIntrospector.MetadataLookup<Set<Scheduled>>() {
//							@Override
//							public Set<Scheduled> inspect(Method method) {
//								Set<Scheduled> scheduledMethods = AnnotatedElementUtils.getMergedRepeatableAnnotations(
//										method, Scheduled.class, Schedules.class);
//								return (!scheduledMethods.isEmpty() ? scheduledMethods : null);
//							}
//						});
//				if (annotatedMethods.isEmpty()) {
//					this.nonAnnotatedClasses.add(targetClass);
//					if (logger.isTraceEnabled()) {
//						logger.trace("No @Scheduled annotations found on bean class: " + bean.getClass());
//					}
//				}
//				else {
//					// Non-empty set of methods
//					for (Map.Entry<Method, Set<Scheduled>> entry : annotatedMethods.entrySet()) {
//						Method method = entry.getKey();
//						for (Scheduled scheduled : entry.getValue()) {
//							processScheduled(scheduled, method, bean);
//						}
//					}
//					if (logger.isDebugEnabled()) {
//						logger.debug(annotatedMethods.size() + " @Scheduled methods processed on bean '" + beanName +
//								"': " + annotatedMethods);
//					}
//				}
//			}
//			return bean;
//		}
		

	}
}
