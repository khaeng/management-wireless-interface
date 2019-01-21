package com.itcall.batch.config.web.filter;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebFilterConfig {

	@Bean
	public FilterRegistrationBean myReportLoggerFilter(){
		FilterRegistrationBean registrationBean = new FilterRegistrationBean();
//		registrationBean.setFilter(new MyReportLoggerFilter());
		registrationBean.setAsyncSupported(true);
		registrationBean.addUrlPatterns("/*");
		return registrationBean;
	}

}
