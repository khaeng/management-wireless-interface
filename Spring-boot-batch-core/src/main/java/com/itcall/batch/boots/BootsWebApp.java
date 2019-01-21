package com.itcall.batch.boots;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.FilterType;

import com.itcall.batch.config.support.EnvironmentSupport;


@SpringBootApplication (scanBasePackages = { "com.itcall.batch.boots" })
@Configuration
@EnableAutoConfiguration(exclude = { DataSourceTransactionManagerAutoConfiguration.class, DataSourceAutoConfiguration.class })
@ComponentScan(basePackages = {EnvironmentSupport.MAPPER_BASE_PACKAGE}, excludeFilters= {
		@ComponentScan.Filter(type=FilterType.REGEX, pattern= {"com.itcall.batch.laucher.*"})
})
@DependsOn(value={"batchJobConfig"})
public class BootsWebApp extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(BootsWebApp.class);
	}

	public static void main(String[] args) {
		
		/*ConfigurableApplicationContext ctx = */EnvironmentSupport.runSpringApp(BootsWebApp.class, true, args);
		
	}

}
