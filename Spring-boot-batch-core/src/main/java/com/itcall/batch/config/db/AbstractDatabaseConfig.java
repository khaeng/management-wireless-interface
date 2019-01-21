package com.itcall.batch.config.db;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;

import com.itcall.batch.config.properties.AbstractDatabaseProperties;
import com.itcall.batch.config.support.EnvironmentSupport;

public abstract class AbstractDatabaseConfig {

	protected Logger LOG = LoggerFactory.getLogger(getClass());

	@Bean
	public abstract DataSource dataSource();

	protected void configureDataSource(org.apache.tomcat.jdbc.pool.DataSource dataSource, AbstractDatabaseProperties databaseProperties) {

		LOG.info("configureDataSource = {} ::: [{}]", databaseProperties, databaseProperties.getUrl());

		dataSource.setDriverClassName(databaseProperties.getDriverClassName());
		dataSource.setUrl(databaseProperties.getUrl());
		dataSource.setUsername(databaseProperties.getUserName());
		dataSource.setPassword(databaseProperties.getPassword());
		dataSource.setMaxActive(databaseProperties.getMaxActive());
		dataSource.setMaxIdle(databaseProperties.getMaxIdle());
		dataSource.setMinIdle(databaseProperties.getMinIdle());
		dataSource.setMaxWait(databaseProperties.getMaxWait());
		dataSource.setInitialSize(databaseProperties.getInitialSize());
		if(dataSource.getDriverClassName().startsWith(EnvironmentSupport.ALTIBASE_DRIVER_CLASSNAME)) {
			/*************************************************************
			 * At-DB의 경우 BLOB/CLOB 데이터 처리시 AutoCommit모드가 true일때 오류가 발생한다.
			 * 특히 SqlSession을 가져와서 Open하여 사용할땐 꼭 옵션을  false를 사용해야 한다.
			 *************************************************************/
			dataSource.setDefaultAutoCommit(true); // 원래기본값이 true이고 false일 경우 꼭 commit을 업무단위에서 해야한다.
		}
		dataSource.setTestOnBorrow(false);
		dataSource.setTestOnReturn(false);
	}
}
