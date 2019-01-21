package com.itcall.batch.config.properties;

import javax.annotation.Resource;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class PropertiesConfig {

	private static final String JASYPT_STD_WITH = "ENC(";
	private static final String JASYPT_END_WITH = ")";

	@Resource
	StandardPBEStringEncryptor standardPBEStringEncryptor;

	public String getDecrypt(String value) {
		String retValue = value;
		if(standardPBEStringEncryptor!=null&&value!=null&&value.startsWith(JASYPT_STD_WITH)&&value.endsWith(JASYPT_END_WITH)) {
			retValue = value.substring(JASYPT_STD_WITH.length(), value.length()-JASYPT_END_WITH.length());
			retValue = standardPBEStringEncryptor.decrypt(retValue);
		}
		return retValue;
	}
	public static boolean isEncryptProps(String value) {
		if(value!=null&&value.startsWith(JASYPT_STD_WITH)&&value.endsWith(JASYPT_END_WITH)) {
			return true;
		}
		return false;
	}
	public static String getDecryptProps(String value) {
		String retValue = value;
		if(PropertiesSecureConfig.standardPBEStringEncryptor!=null&&value!=null&&value.startsWith(JASYPT_STD_WITH)&&value.endsWith(JASYPT_END_WITH)) {
			retValue = value.substring(JASYPT_STD_WITH.length(), value.length()-JASYPT_END_WITH.length());
			retValue = PropertiesSecureConfig.standardPBEStringEncryptor.decrypt(retValue);
		}
		return retValue;
	}

	@Profile("prod")
	@Bean(name = "app")
	public PropertiesFactoryBean getProdAppProperties() {
		PropertiesFactoryBean bean = new PropertiesFactoryBean();
		bean.setLocation(new ClassPathResource("/application-prod.properties"));
		return bean;
	}

	@Profile("dev")
	@Bean(name = "app")
	public PropertiesFactoryBean getDevAppProperties() {
		PropertiesFactoryBean bean = new PropertiesFactoryBean();
		bean.setLocation(new ClassPathResource("/application-dev.properties"));
		return bean;
	}

	@Profile("dev2")
	@Bean(name = "app")
	public PropertiesFactoryBean getDev2AppProperties() {
		PropertiesFactoryBean bean = new PropertiesFactoryBean();
		bean.setLocation(new ClassPathResource("/application-dev2.properties"));
		return bean;
	}

	@Profile("tb")
	@Bean(name = "app")
	public PropertiesFactoryBean getTbAppProperties() {
		PropertiesFactoryBean bean = new PropertiesFactoryBean();
		bean.setLocation(new ClassPathResource("/application-tb.properties"));
		return bean;
	}

	@Profile("tb2")
	@Bean(name = "app")
	public PropertiesFactoryBean getTb2AppProperties() {
		PropertiesFactoryBean bean = new PropertiesFactoryBean();
		bean.setLocation(new ClassPathResource("/application-tb2.properties"));
		return bean;
	}

	@Profile("local")
	@Bean(name = "app")
	public PropertiesFactoryBean getLocalAppProperties() {
		PropertiesFactoryBean bean = new PropertiesFactoryBean();
		bean.setLocation(new ClassPathResource("/application-local.properties"));
		return bean;
	}

	@Profile("prod")
	@Bean(name = "common")
	public PropertiesFactoryBean getProdCommonProperties() {
		PropertiesFactoryBean bean = new PropertiesFactoryBean();
		bean.setLocation(new ClassPathResource("/properties/common-prod.properties"));
		return bean;
	}

	@Profile("dev")
	@Bean(name = "common")
	public PropertiesFactoryBean getDevCommonProperties() {
		PropertiesFactoryBean bean = new PropertiesFactoryBean();
		bean.setLocation(new ClassPathResource("/properties/common-dev.properties"));
		return bean;
	}

	@Profile("dev2")
	@Bean(name = "common")
	public PropertiesFactoryBean getDev2CommonProperties() {
		PropertiesFactoryBean bean = new PropertiesFactoryBean();
		bean.setLocation(new ClassPathResource("/properties/common-dev2.properties"));
		return bean;
	}

	@Profile("tb")
	@Bean(name = "common")
	public PropertiesFactoryBean getTbCommonProperties() {
		PropertiesFactoryBean bean = new PropertiesFactoryBean();
		bean.setLocation(new ClassPathResource("/properties/common-tb.properties"));
		return bean;
	}

	@Profile("tb2")
	@Bean(name = "common")
	public PropertiesFactoryBean getTb2CommonProperties() {
		PropertiesFactoryBean bean = new PropertiesFactoryBean();
		bean.setLocation(new ClassPathResource("/properties/common-tb2.properties"));
		return bean;
	}

	@Profile("local")
	@Bean(name = "common")
	public PropertiesFactoryBean getLocalCommonProperties() {
		PropertiesFactoryBean bean = new PropertiesFactoryBean();
		bean.setLocation(new ClassPathResource("/properties/common-local.properties"));
		return bean;
	}

	@Profile("prod")
	@Bean(name = "batch")
	public PropertiesFactoryBean getProdBatchProperties() {
		PropertiesFactoryBean bean = new PropertiesFactoryBean();
		bean.setLocation(new ClassPathResource("/properties/batch-prod.properties"));
		return bean;
	}

	@Profile("dev")
	@Bean(name = "batch")
	public PropertiesFactoryBean getDevBatchProperties() {
		PropertiesFactoryBean bean = new PropertiesFactoryBean();
		bean.setLocation(new ClassPathResource("/properties/batch-dev.properties"));
		return bean;
	}

	@Profile("dev2")
	@Bean(name = "batch")
	public PropertiesFactoryBean getDev2BatchProperties() {
		PropertiesFactoryBean bean = new PropertiesFactoryBean();
		bean.setLocation(new ClassPathResource("/properties/batch-dev2.properties"));
		return bean;
	}

	@Profile("tb")
	@Bean(name = "batch")
	public PropertiesFactoryBean getTbBatchProperties() {
		PropertiesFactoryBean bean = new PropertiesFactoryBean();
		bean.setLocation(new ClassPathResource("/properties/batch-tb.properties"));
		return bean;
	}

	@Profile("tb2")
	@Bean(name = "batch")
	public PropertiesFactoryBean getTb2BatchProperties() {
		PropertiesFactoryBean bean = new PropertiesFactoryBean();
		bean.setLocation(new ClassPathResource("/properties/batch-tb2.properties"));
		return bean;
	}

	@Profile("local")
	@Bean(name = "batch")
	public PropertiesFactoryBean getLocalBatchProperties() {
		PropertiesFactoryBean bean = new PropertiesFactoryBean();
		bean.setLocation(new ClassPathResource("/properties/batch-local.properties"));
		return bean;
	}

}
