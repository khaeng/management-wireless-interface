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
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.itcall.batch.config.support.EnvironmentSupport;
import com.itcall.batch.config.support.Master;

@Configuration
@MapperScan(basePackages = EnvironmentSupport.MASTER_MAPPER_BASE_PACKAGE, annotationClass = Master.class, sqlSessionFactoryRef = EnvironmentSupport.MASTER_SQL_SESSION_FACTORY_NAME)
public class MasterMyBatisConfig extends AbstractMybatisConfig {

	@Primary
	@Bean(name={"sqlSessionFactory", EnvironmentSupport.MASTER_SQL_SESSION_FACTORY_NAME})
	public SqlSessionFactory sqlSessionFactory(@Qualifier(EnvironmentSupport.MASTER_DATA_SOURCE_NAME) DataSource masterDataSource) throws Exception {
		SqlSessionFactoryBean firstSessionFactoryBean = new SqlSessionFactoryBean();
		configureSqlSessionFactory(firstSessionFactoryBean, masterDataSource);
		return firstSessionFactoryBean.getObject();
	}

	@Primary
	@Bean(name={"sqlSessionTemplate", EnvironmentSupport.MASTER_SQL_SESSION_TEMPLATE_NAME})
	public SqlSessionTemplate sqlSessionTemplate(@Qualifier(EnvironmentSupport.MASTER_SQL_SESSION_FACTORY_NAME) SqlSessionFactory masterSqlSessionFactory) throws Exception {
		return new SqlSessionTemplate(masterSqlSessionFactory);
	}

	/**
	 * 상속받아서 상속된 프로젝트의 별도 MAPPER_LOCATIONS_PATH를 추가하거나, DB별로 mapper를 별도로 볼수있다.
	 */
	@Override
	protected void configureSqlSessionFactory(SqlSessionFactoryBean sessionFactoryBean, DataSource dataSource) throws IOException {
		PathMatchingResourcePatternResolver pathResolver = new PathMatchingResourcePatternResolver();
		sessionFactoryBean.setDataSource(dataSource);
		sessionFactoryBean.setTypeAliasesPackage(EnvironmentSupport.MASTER_TYPE_ALIASES_PACKAGE);
		Resource resource = pathResolver.getResource(EnvironmentSupport.MASTER_CONFIG_LOCATION_PATH);
		if(resource==null || !resource.exists() || !resource.isReadable()) {
			resource = pathResolver.getResource(EnvironmentSupport.MAPPER_CONFIG_LOCATION_PATH);
		}
		LOG.info("Load Master-mybatis mapper configuration file... Resource[{}]", resource);
		sessionFactoryBean.setConfigLocation(resource);

		List<Resource> listResource = new ArrayList<Resource>();
		for (String path : EnvironmentSupport.MASTER_MAPPER_LOCATIONS_PATH) {
			try {
				listResource.addAll(Arrays.asList(pathResolver.getResources(path)));
			} catch (Exception e) {
				LOG.warn("Master-Mapper location not found : [{}]", path);
			}
		}
		Resource[] mapperLocations = listResource.toArray(new Resource[listResource.size()]);
		sessionFactoryBean.setMapperLocations(mapperLocations);
		LOG.info("Load Master-mapperLocations[{}] path list into sessionFactoryBean... [{}]ea Master-mapper files loaded.", EnvironmentSupport.MASTER_MAPPER_LOCATIONS_PATH, listResource.size());
		LOG.debug("Loaded Master-mapper files...\n\t{}", Arrays.toString(mapperLocations).replaceAll(", ", "\n\t"));
	}

}
