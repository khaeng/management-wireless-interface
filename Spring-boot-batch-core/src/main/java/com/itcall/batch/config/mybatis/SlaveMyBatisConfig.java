package com.itcall.batch.config.mybatis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.itcall.batch.config.support.EnvironmentSupport;
import com.itcall.batch.config.support.Slave;


@Configuration
@MapperScan(basePackages = EnvironmentSupport.SLAVE_MAPPER_BASE_PACKAGE, annotationClass = Slave.class, sqlSessionFactoryRef = EnvironmentSupport.SLAVE_SQL_SESSION_FACTORY_NAME)
public class SlaveMyBatisConfig extends AbstractMybatisConfig {
	
	@Bean(name={EnvironmentSupport.SLAVE_SQL_SESSION_FACTORY_NAME})
	public SqlSessionFactory sqlSessionFactory(@Qualifier(EnvironmentSupport.SLAVE_DATA_SOURCE_NAME) DataSource slaveDataSource) throws Exception {
		SqlSessionFactoryBean slaveSessionFactoryBean = new SqlSessionFactoryBean();
		configureSqlSessionFactory(slaveSessionFactoryBean, slaveDataSource);
		return slaveSessionFactoryBean.getObject();
	}

	@Bean(name={EnvironmentSupport.SLAVE_SQL_SESSION_TEMPLATE_NAME})
	public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory slaveSqlSessionFactory) throws Exception{
		return new SqlSessionTemplate(slaveSqlSessionFactory);
	}

	/**
	 * 상속받아서 상속된 프로젝트의 별도 MAPPER_LOCATIONS_PATH를 추가하거나, DB별로 mapper를 별도로 볼수있다.
	 */
	@Override
	protected void configureSqlSessionFactory(SqlSessionFactoryBean sessionFactoryBean, DataSource dataSource) throws IOException {
		PathMatchingResourcePatternResolver pathResolver = new PathMatchingResourcePatternResolver();
		sessionFactoryBean.setDataSource(dataSource);
		sessionFactoryBean.setTypeAliasesPackage(EnvironmentSupport.SLAVE_TYPE_ALIASES_PACKAGE);
		Resource resource = pathResolver.getResource(EnvironmentSupport.SLAVE_CONFIG_LOCATION_PATH);
		if(resource==null || !resource.exists() || !resource.isReadable()) {
			resource = pathResolver.getResource(EnvironmentSupport.MAPPER_CONFIG_LOCATION_PATH);
		}
		LOG.info("Load Slave-mybatis mapper configuration file... Resource[{}]", resource);
		sessionFactoryBean.setConfigLocation(resource);

		List<Resource> listResource = new ArrayList<Resource>();
		for (String path : EnvironmentSupport.SLAVE_MAPPER_LOCATIONS_PATH) {
			try {
				listResource.addAll(Arrays.asList(pathResolver.getResources(path)));
			} catch (Exception e) {
				LOG.warn("Slave-Mapper location not found : [{}]", path);
			}
		}
		Resource[] mapperLocations = listResource.toArray(new Resource[listResource.size()]);
		sessionFactoryBean.setMapperLocations(mapperLocations);
		LOG.info("Load Slave-mapperLocations[{}] path list into sessionFactoryBean... [{}]ea Slave-mapper files loaded.", EnvironmentSupport.MASTER_MAPPER_LOCATIONS_PATH, listResource.size());
		LOG.debug("Loaded Slave-mapper files...\n\t{}", Arrays.toString(mapperLocations).replaceAll(", ", "\n\t"));
	}

}
