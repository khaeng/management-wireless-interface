package com.itcall.batch.config.db;

import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import com.itcall.batch.config.properties.MasterDatabaseProperties;
import com.itcall.batch.config.support.EnvironmentSupport;

import net.sf.log4jdbc.Log4jdbcProxyDataSource;
import net.sf.log4jdbc.tools.Log4JdbcCustomFormatter;
import net.sf.log4jdbc.tools.LoggingType;

@EnableAspectJAutoProxy
// @Aspect
@Configuration("masterDatabaseConfig")
@EnableTransactionManagement // <tx:annotation-driven />
//@EnableConfigurationProperties(MasterDatabaseProperties.class)
public class MasterDatabaseConfig extends AbstractDatabaseConfig {

	@Autowired
	private MasterDatabaseProperties masterDatabaseProperties;

	@Value("${spring.profiles.active:prod}")
	private String profile;

//	@Primary
	@Bean(name = {EnvironmentSupport.MASTER_LOOKUP_DATA_SOURCE_NAME})
//	@DependsOn(value= {"encryptablePropertyPlaceholderConfigurer","masterDatabaseProperties"})
	public DataSource dataSource() {
		org.apache.tomcat.jdbc.pool.DataSource masterDataSource = null;
//		if(this.environment.getActiveProfiles()!=null
//				&& Arrays.binarySearch(this.environment.getActiveProfiles(), "local")>=0) {
//			masterDataSource = new org.apache.tomcat.jdbc.pool.DataSource();
//			configureDataSource(masterDataSource, masterDatabaseProperties);
//			return masterDataSource;
//		}else{
			try {
				JndiDataSourceLookup dataSourceLookup = new JndiDataSourceLookup();
				DataSource dataSource = dataSourceLookup.getDataSource(masterDatabaseProperties.getJndiName());
				LOG.info("Looking for Jndi[{}] is Success...", masterDatabaseProperties.getJndiName());
				return dataSource;
			} catch (Exception e) {
				masterDataSource = new org.apache.tomcat.jdbc.pool.DataSource();
				configureDataSource(masterDataSource, masterDatabaseProperties);
				return masterDataSource;
			}
//		}
	}

//	@Primary
//	@Bean(name = {"dataSource", EnvironmentSupport.MASTER_DATA_SOURCE_NAME}/*, destroyMethod = "close"*/)
//	@DependsOn(value= {"encryptablePropertyPlaceholderConfigurer","masterDatabaseProperties",EnvironmentSupport.MASTER_LOOKUP_DATA_SOURCE_NAME})
//	public DataSource log4DataSource(@Qualifier(EnvironmentSupport.MASTER_LOOKUP_DATA_SOURCE_NAME) DataSource dataSource) {
//		if(this.profile!=null && profile.contains("local")) {
//			Log4JdbcCustomFormatter log4JdbcCustomFormatter = new Log4JdbcCustomFormatter();
//			log4JdbcCustomFormatter.setLoggingType(LoggingType.MULTI_LINE);
//			log4JdbcCustomFormatter.setSqlPrefix("MASTER.SQL:::     ");
//			
//			Log4jdbcProxyDataSource log4DataSource = new Log4jdbcProxyDataSource(dataSource);
//			log4DataSource.setLogFormatter(log4JdbcCustomFormatter);
//			dataSource = log4DataSource;
//		}
//		return dataSource;
//	}

	@Profile(value= {"dev","dev2","tb","tb2","prod","prd"})
	@Primary
	@Bean(name = {"dataSource", EnvironmentSupport.MASTER_DATA_SOURCE_NAME}/*, destroyMethod = "close"*/)
	@DependsOn(value= {"encryptablePropertyPlaceholderConfigurer","masterDatabaseProperties",EnvironmentSupport.MASTER_LOOKUP_DATA_SOURCE_NAME})
	public DataSource dataSource(@Qualifier(EnvironmentSupport.MASTER_LOOKUP_DATA_SOURCE_NAME) DataSource dataSource) {
		return dataSource;
	}
	@Profile(value= {"local"})
	@Primary
	@Bean(name = {"dataSource", EnvironmentSupport.MASTER_DATA_SOURCE_NAME}/*, destroyMethod = "close"*/)
	@DependsOn(value= {"encryptablePropertyPlaceholderConfigurer","masterDatabaseProperties",EnvironmentSupport.MASTER_LOOKUP_DATA_SOURCE_NAME})
	public DataSource log4DataSource(@Qualifier(EnvironmentSupport.MASTER_LOOKUP_DATA_SOURCE_NAME) DataSource dataSource) {
		Log4JdbcCustomFormatter log4JdbcCustomFormatter = new Log4JdbcCustomFormatter();
		log4JdbcCustomFormatter.setLoggingType(LoggingType.MULTI_LINE);
		log4JdbcCustomFormatter.setSqlPrefix("MASTER.SQL:::     ");
		
		Log4jdbcProxyDataSource log4DataSource = new Log4jdbcProxyDataSource(dataSource);
		log4DataSource.setLogFormatter(log4JdbcCustomFormatter);
		return log4DataSource;
	}

	@Primary
	@Bean(name = {"transactionManager", "masterTransactionManager"}) // DataSourceTransactionManager, PlatformTransactionManager
	public PlatformTransactionManager transactionManager(@Qualifier(EnvironmentSupport.MASTER_DATA_SOURCE_NAME) DataSource masterDataSource) {
		DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(masterDataSource);
		transactionManager.setGlobalRollbackOnParticipationFailure(false);
		return transactionManager;
	}

	@Bean
	public Advisor txAdviceAdvisor(@Qualifier("masterTransactionManager") PlatformTransactionManager transactionManager) {
		AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
		/*pointcut.setExpression("execution(* com.itcall.batch.common..impl.*Impl.*(..)) or execution(* com.itcall.batch.rest..*Impl.*(..))");*/
		pointcut.setExpression(
				new StringBuffer().append("execution(public * com.itcall.batch.common.service..impl.*Impl.*(..))")
				.append(" or ")
				.append("execution(public * com.itcall.batch.biz.service..*Impl.*(..))")
				/*********************************************************************************
				 *		JOB으로 선언된 class들은 별도 Transaction으로 묶여있기 때문에
				 *		여기서 정의해주면 컨테이너에 이중으로 등록되는 에러가 발생한다.
				.append(" or ")
				.append("execution(public * com.itcall.batch.jobs..*Tasklet.*(..))")
				.append(" or ")
				.append("execution(public * com.itcall.batch.jobs..*Writer.*(..))")
				*********************************************************************************/
				.toString());
		return new DefaultPointcutAdvisor(pointcut, txAdvice(transactionManager));
	}

	public TransactionInterceptor txAdvice(PlatformTransactionManager transactionManager) {
//		MatchAlwaysTransactionAttributeSource source = new MatchAlwaysTransactionAttributeSource();
//		RuleBasedTransactionAttribute transactionAttribute = new RuleBasedTransactionAttribute();
//		transactionAttribute.setName("*");
//		// transactionAttribute.setRollbackRules(Collections.singletonList(new RollbackRuleAttribute(Exception.class)));
//		transactionAttribute.setRollbackRules(Arrays.asList(new RollbackRuleAttribute(Exception.class))); // RuntimeException 에서만 Rollback이 일어나는게 기본값임.
//		source.setTransactionAttribute(transactionAttribute);
//		TransactionInterceptor txAdvice = new TransactionInterceptor(transactionManager, source);


		Properties props = new Properties();
		props.setProperty("add*", "PROPAGATION_REQUIRED,-java.lang.RuntimeException"); // RuntimeException 에서만 Rollback이 일어나는게 기본값임.
		props.setProperty("save*", "PROPAGATION_REQUIRED,-java.lang.RuntimeException");
		props.setProperty("insert*", "PROPAGATION_REQUIRED,-java.lang.RuntimeException");
		props.setProperty("set*", "PROPAGATION_REQUIRED,-java.lang.RuntimeException");
		props.setProperty("update*", "PROPAGATION_REQUIRED,-java.lang.RuntimeException");
		props.setProperty("remove*", "PROPAGATION_REQUIRED,-java.lang.RuntimeException");
		props.setProperty("del*", "PROPAGATION_REQUIRED,-java.lang.RuntimeException");
		props.setProperty("*", "PROPAGATION_NOT_SUPPORTED,readOnly");
		TransactionInterceptor txAdvice = new TransactionInterceptor(transactionManager, props);
		return txAdvice;
	}

}
