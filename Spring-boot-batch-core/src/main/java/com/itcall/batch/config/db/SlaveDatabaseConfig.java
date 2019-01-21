package com.itcall.batch.config.db;

import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import com.itcall.batch.config.properties.SlaveDatabaseProperties;
import com.itcall.batch.config.support.EnvironmentSupport;

import net.sf.log4jdbc.Log4jdbcProxyDataSource;
import net.sf.log4jdbc.tools.Log4JdbcCustomFormatter;
import net.sf.log4jdbc.tools.LoggingType;

//@EnableAspectJAutoProxy
@Configuration("slaveDatabaseConfig")
@EnableTransactionManagement // <tx:annotation-driven />
// @EnableConfigurationProperties(SlaveDatabaseProperties.class)
public class SlaveDatabaseConfig extends AbstractDatabaseConfig {

	@Autowired
	private SlaveDatabaseProperties slaveDatabaseProperties;

//	@Value("${spring.profiles.active:prod}")
//	private String profile;

	@Bean(name = EnvironmentSupport.SLAVE_LOOKUP_DATA_SOURCE_NAME/*, destroyMethod = "close"*/)
	@DependsOn(value= {"slaveDatabaseProperties"})
//	@Bean(name = {EnvironmentSupport.SLAVE_DATA_SOURCE_NAME}/*, destroyMethod = "close"*/)
//	@DependsOn(value= {"encryptablePropertyPlaceholderConfigurer","slaveDatabaseProperties"})
	public DataSource dataSource() {
		org.apache.tomcat.jdbc.pool.DataSource slaveDataSource = null;
//		if(this.environment.getActiveProfiles()!=null
//				&& Arrays.binarySearch(this.environment.getActiveProfiles(), "local")>=0) {
//			slaveDataSource = new org.apache.tomcat.jdbc.pool.DataSource();
//			configureDataSource(slaveDataSource, slaveDatabaseProperties);
//			return slaveDataSource;
//		}else{
			try {
				JndiDataSourceLookup dataSourceLookup = new JndiDataSourceLookup();
				DataSource dataSource = dataSourceLookup.getDataSource(slaveDatabaseProperties.getJndiName());
				LOG.info("Looking for Jndi[{}] is Success...", slaveDatabaseProperties.getJndiName());
				return dataSource;
			}catch (Exception e) {
				slaveDataSource = new org.apache.tomcat.jdbc.pool.DataSource();
				configureDataSource(slaveDataSource, slaveDatabaseProperties);
				return slaveDataSource;
			}
//		}
	}

//	@Bean(name = {EnvironmentSupport.SLAVE_DATA_SOURCE_NAME}/*, destroyMethod = "close"*/)
//	@DependsOn(value= {"encryptablePropertyPlaceholderConfigurer","slaveDatabaseProperties",EnvironmentSupport.SLAVE_LOOKUP_DATA_SOURCE_NAME})
//	public DataSource log4DataSource(@Qualifier(EnvironmentSupport.SLAVE_LOOKUP_DATA_SOURCE_NAME) DataSource dataSource) {
//		if(this.profile!=null && profile.contains("local")) {
//			Log4JdbcCustomFormatter log4JdbcCustomFormatter = new Log4JdbcCustomFormatter();
//			log4JdbcCustomFormatter.setLoggingType(LoggingType.MULTI_LINE);
//			log4JdbcCustomFormatter.setSqlPrefix("SLAVE.SQL:::     ");
//			
//			Log4jdbcProxyDataSource log4DataSource = new Log4jdbcProxyDataSource(dataSource);
//			log4DataSource.setLogFormatter(log4JdbcCustomFormatter);
//			dataSource = log4DataSource;
//		}
//		return dataSource;
//	}

	@Profile(value= {"dev","dev2","tb","tb2","prod","prd"})
	@Bean(name = {EnvironmentSupport.SLAVE_DATA_SOURCE_NAME}/*, destroyMethod = "close"*/)
	@DependsOn(value= {"encryptablePropertyPlaceholderConfigurer","slaveDatabaseProperties",EnvironmentSupport.SLAVE_LOOKUP_DATA_SOURCE_NAME})
	public DataSource dataSource(@Qualifier(EnvironmentSupport.SLAVE_LOOKUP_DATA_SOURCE_NAME) DataSource dataSource) {
		return dataSource;
	}
	@Profile(value= {"local"})
	@Bean(name = {EnvironmentSupport.SLAVE_DATA_SOURCE_NAME}/*, destroyMethod = "close"*/)
	@DependsOn(value= {"encryptablePropertyPlaceholderConfigurer","slaveDatabaseProperties",EnvironmentSupport.SLAVE_LOOKUP_DATA_SOURCE_NAME})
	public DataSource log4DataSource(@Qualifier(EnvironmentSupport.SLAVE_LOOKUP_DATA_SOURCE_NAME) DataSource dataSource) {
		Log4JdbcCustomFormatter log4JdbcCustomFormatter = new Log4JdbcCustomFormatter();
		log4JdbcCustomFormatter.setLoggingType(LoggingType.MULTI_LINE);
		log4JdbcCustomFormatter.setSqlPrefix("SLAVE.SQL:::     ");

		Log4jdbcProxyDataSource log4DataSource = new Log4jdbcProxyDataSource(dataSource);
		log4DataSource.setLogFormatter(log4JdbcCustomFormatter);
		return log4DataSource;
	}

	@Bean(name = "slaveTransactionManager")
	public PlatformTransactionManager transactionManager(@Qualifier(EnvironmentSupport.SLAVE_DATA_SOURCE_NAME) DataSource slaveDataSource) {
		DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(slaveDataSource);
		transactionManager.setGlobalRollbackOnParticipationFailure(false);
		return transactionManager;
	}

	@Bean
	public Advisor txAdviceAdvisor(@Qualifier("slaveTransactionManager") PlatformTransactionManager transactionManager) {
		AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
		/*********** Slave는 이후 생성되면 추가한다. ***********/
		pointcut.setExpression(
				new StringBuffer().append("execution(public * com.itcall.batch.slave.biz.service..*Impl.*(..))")
				/*********************************************************************************
				 *		JOB으로 선언된 class들은 별도 Transaction으로 묶여있기 때문에
				 *		여기서 정의해주면 컨테이너에 이중으로 등록되는 에러가 발생한다.
				.append(" or ")
				.append("execution(public * com.itcall.batch.slave.batch..*Tasklet.*(..))")
				.append(" or ")
				.append("execution(public * com.itcall.batch.slave.batch..*Writer.*(..))")
				*********************************************************************************/
				.toString()
				);
		return new DefaultPointcutAdvisor(pointcut, txAdvice(transactionManager));
	}

	public TransactionInterceptor txAdvice(PlatformTransactionManager transactionManager) {
		Properties props = new Properties();
		props.setProperty("add*", "PROPAGATION_REQUIRED,-java.lang.RuntimeException");
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
