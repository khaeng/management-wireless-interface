package com.itcall.batch.config.mybatis;

import java.io.IOException;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.itcall.batch.config.support.EnvironmentSupport;

public abstract class AbstractMybatisConfig {

	protected Logger LOG = LoggerFactory.getLogger(getClass());

// 	public static final String MAPPER_BASE_PACKAGE          = "com.itcall.batch.common.mapper";
	public static final String MAPPER_BASE_PACKAGE          = EnvironmentSupport.MAPPER_BASE_PACKAGE;
	public static final String TYPE_ALIASES_PACKAGE         = "com.itcall.batch.common.vo";
	public static final String CONFIG_LOCATION_PATH         = EnvironmentSupport.CONFIG_LOCATION_PATH;
	public static final String MAPPER_LOCATIONS_COMMON_PATH = "classpath:mybatis/common-mapper/**/*Mapper.xml";
	public static final String MAPPER_LOCATIONS_APP_PATH    = "classpath:mybatis/mapper/**/*Mapper.xml";

	public abstract SqlSessionFactory sqlSessionFactory(DataSource masterDataSource) throws Exception;

	public abstract SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory masterSqlSessionFactory) throws Exception;

	/**
	 * 상속받아서 상속된 프로젝트의 별도 MAPPER_LOCATIONS_PATH를 추가하거나, DB별로 mapper를 별도로 볼수있다.
	 */
	protected void configureSqlSessionFactory(SqlSessionFactoryBean sessionFactoryBean, DataSource dataSource) throws IOException {
		PathMatchingResourcePatternResolver pathResolver = new PathMatchingResourcePatternResolver();
		sessionFactoryBean.setDataSource(dataSource);
		sessionFactoryBean.setTypeAliasesPackage(TYPE_ALIASES_PACKAGE);
		sessionFactoryBean.setConfigLocation(pathResolver.getResource(CONFIG_LOCATION_PATH));

		org.springframework.core.io.Resource[] resourcesCommon = new org.springframework.core.io.Resource[0];
		try {
			resourcesCommon = pathResolver.getResources(MAPPER_LOCATIONS_COMMON_PATH);
		} catch (Exception e) {
			LOG.warn("Mapper location not found : [{}]", MAPPER_LOCATIONS_COMMON_PATH);
		}
		org.springframework.core.io.Resource[] resourcesApp    = new org.springframework.core.io.Resource[0];
		try {
			resourcesApp    = pathResolver.getResources(MAPPER_LOCATIONS_APP_PATH);
		} catch (Exception e) {
			LOG.warn("Mapper location not found : [{}]", MAPPER_LOCATIONS_APP_PATH);
		}

		org.springframework.core.io.Resource[] resources = new org.springframework.core.io.Resource[resourcesCommon.length + resourcesApp.length];
		System.arraycopy(resourcesCommon, 0, resources, 0, resourcesCommon.length);
		System.arraycopy(resourcesApp, 0, resources, resourcesCommon.length, resourcesApp.length);
		sessionFactoryBean.setMapperLocations(resources);
	}
}