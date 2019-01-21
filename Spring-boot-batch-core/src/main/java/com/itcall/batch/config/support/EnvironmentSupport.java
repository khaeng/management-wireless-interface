package com.itcall.batch.config.support;

import java.net.Inet4Address;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.SimpleCommandLinePropertySource;

public class EnvironmentSupport {

	private static final Logger LOG = LoggerFactory.getLogger(EnvironmentSupport.class);

	private static final String SPRING_PROFILES_ACTIVE_DEF_VALUE = "local"; // "prod";

// 	public static final String MAPPER_BASE_PACKAGE          = "com.itcall.batch.common.mapper";
	public static final String MAPPER_BASE_PACKAGE          = "com.itcall";
	public static final String TYPE_ALIASES_PACKAGE         = "com.itcall.batch.biz.vo";
	public static final String CONFIG_LOCATION_PATH         = "classpath:mybatis/mybatis-batch.xml";
	public static final String MAPPER_LOCATIONS_COMMON_PATH = "classpath:mybatis/common-mapper/**/*Mapper.xml";
	public static final String MAPPER_LOCATIONS_APP_PATH    = "classpath:mybatis/mapper/**/*Mapper.xml";
	public static final String MAPPER_CONFIG_LOCATION_PATH         = "classpath:mybatis/mybatis-batch.xml";

	public static final String MASTER_LOOKUP_DATA_SOURCE_NAME      = "masterLookupDataSource";
	public static final String MASTER_DATA_SOURCE_NAME             = "masterDataSource";
	public static final String MASTER_SQL_SESSION_FACTORY_NAME     = "masterSqlSessionFactory";
	public static final String MASTER_SQL_SESSION_TEMPLATE_NAME    = "masterSqlSessionTemplate";
	public static final String MASTER_MAPPER_BASE_PACKAGE          = EnvironmentSupport.MAPPER_BASE_PACKAGE;
	public static final String MASTER_TYPE_ALIASES_PACKAGE         = "com.itcall.batch.biz.vo";
	public static final String MASTER_CONFIG_LOCATION_PATH         = "classpath:mybatis/mybatis-master.xml";
	public static final String[] MASTER_MAPPER_LOCATIONS_PATH = {"classpath:mybatis/common-mapper/**/*Mapper.xml", "classpath:mybatis/mapper/**/*Mapper.xml", "classpath:mybatis/master-mapper/**/*Mapper.xml"};

	public static final String SLAVE_LOOKUP_DATA_SOURCE_NAME      = "slaveLookupDataSource";
	public static final String SLAVE_DATA_SOURCE_NAME             = "slaveDataSource";
	public static final String SLAVE_SQL_SESSION_FACTORY_NAME     = "slaveSqlSessionFactory";
	public static final String SLAVE_SQL_SESSION_TEMPLATE_NAME    = "slaveSqlSessionTemplate";
	public static final String SLAVE_MAPPER_BASE_PACKAGE          = EnvironmentSupport.MAPPER_BASE_PACKAGE;
	public static final String SLAVE_TYPE_ALIASES_PACKAGE         = "com.itcall.batch.biz.vo";
	public static final String SLAVE_CONFIG_LOCATION_PATH         = "classpath:mybatis/mybatis-slave.xml";
	public static final String[] SLAVE_MAPPER_LOCATIONS_PATH = {"classpath:mybatis/common-mapper/**/*Mapper.xml", "classpath:mybatis/mapper/**/*Mapper.xml", "classpath:mybatis/slave-mapper/**/*Mapper.xml"};

	public static final String ALTIBASE_PRODUCT_NAME          = "Altibase";
	public final static String ALTIBASE_DRIVER_CLASSNAME      = "Altibase.jdbc.driver.AltibaseDriver";
	public final static String ALTIBASE_BATCH_SCHEMA_LOCATION = "classpath:mybatis/batch-meta/schema-altibase.sql";
//	public final static String ALTIBASE_BATCH_SCHEMA_LOCATION = "classpath:mybatis/batch-meta/schema-altibase-energy.sql";
	public static final String PPAS_PRODUCT_NAME          = "PostgreSQL";
	public final static String PPAS_BATCH_SCHEMA_LOCATION = "classpath:org/springframework/batch/core/schema-postgresql.sql";

	public static final String NOT_SUPPORT_SCHEDULED_JOB = "NOT_SUPPORT_SCHEDULE";


	private static String svrTypeCd = null;
	private static String batchSysId = null;
	
	public static void setProfilesLocation(SpringApplication app, String...args) {
		SimpleCommandLinePropertySource source = new SimpleCommandLinePropertySource(args);
		if (!source.containsProperty("spring.profiles.active")
				&& !System.getenv().containsKey("SPRING_PROFILES_ACTIVE")) {
			app.setAdditionalProfiles(SPRING_PROFILES_ACTIVE_DEF_VALUE);
		}
	}

	public static ConfigurableApplicationContext runSpringApp(Class<?> clazz, boolean isWithWeb, String...args) {
		SpringApplication application = new SpringApplication(clazz);
		EnvironmentSupport.setProfilesLocation(application, args);
		application.setWebEnvironment(isWithWeb);
		ConfigurableApplicationContext context = application.run(args);
		// initFirstSet(context); // ApplicationListener / CommandLineRunner.run 에서 모두 호출하는것으로 변경함.
		return context;
	}

	public static void initFirstSet(ApplicationContext ctx) {
		if(EnvironmentSupport.batchSysId==null) {
			EnvironmentSupport.batchSysId = ctx.getEnvironment().getProperty("batch.server.id", "BATCH");
			if(EnvironmentSupport.svrTypeCd==null||EnvironmentSupport.svrTypeCd.trim().isEmpty()) {
				try {
					EnvironmentSupport.svrTypeCd = Inet4Address.getLocalHost().getHostAddress();
				} catch (UnknownHostException e) {
					LOG.error("Application Start Error ::: Not detected ipaddress[{}] {}", EnvironmentSupport.svrTypeCd, e);
					System.exit(-1);
				}
			}
		}
		LOG.info("Initialized Environment System properties... sysId[{}], svrTypeCd[{}]", EnvironmentSupport.batchSysId, EnvironmentSupport.svrTypeCd);
	}

//	public static void setSvrTypeCd(String svrTypeCd) {
//		EnvironmentSupport.svrTypeCd = svrTypeCd;
//	}
	public static String getSvrTypeCd() {
		return EnvironmentSupport.svrTypeCd;
	}
//	public static void setBatchSysId(String batchSysId) {
//		EnvironmentSupport.batchSysId = batchSysId;
//	}
	public static String getBatchSysId() {
		return batchSysId;
	}

}
