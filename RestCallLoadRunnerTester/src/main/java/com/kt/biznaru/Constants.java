package com.kt.biznaru;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.StringUtils;

public class Constants {

	private static final String BASE_PROPERTIES_FILE_NAME = "base.conf";
	private Properties properties;

	/* for TaskExecutor */
	private int executorCorePoolSize = 10;
	private int executorMaxPoolSize = 20;
	private int executorQueueCapacity = 1000000;

	/* for RestUtil */
	private int maxConnTotalForRestClient = 100;
	private int maxConnPerRouteForRestClient = 100;
	private int maxRestTemplateUnitForRestClient = 10;
	private int connectionTimeoutForRestClient = 90;
	private int readTimeoutForRestClient = 90;

	public Constants(String propertiesFile) throws IOException {
		if(StringUtils.isEmpty(propertiesFile)) {
			this.properties = readProperties(new File(BASE_PROPERTIES_FILE_NAME));
		} else {
			this.properties = readProperties(new File(propertiesFile));
		}
		this.executorCorePoolSize = getTotalMultiConnector();
	}

	

	public int getExecutorCorePoolSize() {
		return executorCorePoolSize;
	}
	public void setExecutorCorePoolSize(int executorCorePoolSize) {
		this.executorCorePoolSize = executorCorePoolSize;
	}
	public int getExecutorMaxPoolSize() {
		return executorMaxPoolSize;
	}
	public void setExecutorMaxPoolSize(int executorMaxPoolSize) {
		this.executorMaxPoolSize = executorMaxPoolSize;
	}
	public int getExecutorQueueCapacity() {
		return executorQueueCapacity;
	}
	public void setExecutorQueueCapacity(int executorQueueCapacity) {
		this.executorQueueCapacity = executorQueueCapacity;
	}
	public int getMaxConnTotalForRestClient() {
		return maxConnTotalForRestClient;
	}
	public void setMaxConnTotalForRestClient(int maxConnTotalForRestClient) {
		this.maxConnTotalForRestClient = maxConnTotalForRestClient;
	}
	public int getMaxConnPerRouteForRestClient() {
		return maxConnPerRouteForRestClient;
	}
	public void setMaxConnPerRouteForRestClient(int maxConnPerRouteForRestClient) {
		this.maxConnPerRouteForRestClient = maxConnPerRouteForRestClient;
	}
	public int getMaxRestTemplateUnitForRestClient() {
		return maxRestTemplateUnitForRestClient;
	}
	public void setMaxRestTemplateUnitForRestClient(int maxRestTemplateUnitForRestClient) {
		this.maxRestTemplateUnitForRestClient = maxRestTemplateUnitForRestClient;
	}
	public int getConnectionTimeoutForRestClient() {
		return connectionTimeoutForRestClient;
	}
	public void setConnectionTimeoutForRestClient(int connectionTimeoutForRestClient) {
		this.connectionTimeoutForRestClient = connectionTimeoutForRestClient;
	}
	public int getReadTimeoutForRestClient() {
		return readTimeoutForRestClient;
	}
	public void setReadTimeoutForRestClient(int readTimeoutForRestClient) {
		this.readTimeoutForRestClient = readTimeoutForRestClient;
	}



	/**
	 * Properties파일을 Load 한다.
	 * @param propsFile
	 * @return
	 * @throws IOException 
	 */
	private Properties readProperties(File propsFile) throws IOException {
		FileSystemResource fileSystemResource = new FileSystemResource(propsFile);
//		if(fileSystemResource.exists()) {
//			try {
				PropertiesFactoryBean bean = new PropertiesFactoryBean();
				bean.setLocation(fileSystemResource);
				bean.afterPropertiesSet();
				Properties properties = bean.getObject();

				// Properties 정보Value가 파일로 존재할 경우 해당 파일정보를 Property Value로 치환해준다.
				properties = loadPropertiesCheckRelateFiles(properties);
				return properties;
//			} catch (Exception e) {
//				log.error("(Re)Loaded UnitInfoProperties on ERROR fileName[{}], errorMessage[{}], errorCause[{}] {}", propsFile.getName(), e.getMessage(), e.getCause(), e);
//			}
//		}else {
//			log.warn("(Re)Loaded UnitInfoProperties on WARN filePath[{}] <<< file not exist???", propsFile.getAbsolutePath());
//		}
//		return null;
	}

	/**
	 * Properties 정보Value가 파일로 존재할 경우 해당 파일정보를 Property Value로 치환해준다.
	 * @param properties
	 * @return
	 */
	private Properties loadPropertiesCheckRelateFiles(final Properties properties) {
		for (Object key : properties.keySet()) {
			try {
				StringBuffer sb = new StringBuffer();
				BufferedReader br = new BufferedReader(new FileReader(properties.getProperty((String) key)));
				String readLine = null;
				while ((readLine=br.readLine())!=null) {
					sb.append(readLine).append("\n");
				}
				br.close();
				properties.setProperty((String)key, sb.toString());
			} catch (Exception e) {
				// Property의 Value가 파일이 아니므로 기존 값을 그대로 보관한다.
			}
		}
		return properties;
	}



	public String getLogPath() {
		return this.properties.getProperty("log.path", "./log/");
	}

	public boolean isLogging() {
		return "YESTRUE".contains(this.properties.getProperty("log.logging.yn", "N").toUpperCase());
	}

	public long getTotalTestCount() {
		return Long.parseLong(this.properties.getProperty("test.total.count", "100000"));
	}
	public int getTotalMultiConnector() {
		return this.executorCorePoolSize = Integer.parseInt(this.properties.getProperty("test.multi.count", ""+this.executorCorePoolSize));
	}

	public String getCookie() {
		return this.properties.getProperty("login.cookie", "JSESSIONID=Jj8d8pUkU5YYN7gVzm6BZzTY.biz11; NSSO_CentralAuth=4b64f6691c3f6a4cbd13f48e3b96bef1dd4ae16807ad29d87c300972497249adf073d8e8b8a85f46dddd49c3020875ea460060f37e29b1469e1cdd598e25564e590c6f91cda0c7c178401c1f22545ee7; NSSO_DomainInfo_kt_com=agencynm%3d615d4936bef356e5%2cdepartment%3d615d4936bef356e5%2cdeptcd%3d754ee8914e6397fb%2chandphoneno%3d46cd230c27291259fb41303db22be0a0%2cnewuserid%3d9f836bb751a6fdb8d25bcdf1072bec86%2colduserid%3d9f836bb751a6fdb8d25bcdf1072bec86%2cusermail%3d9f836bb751a6fdb822a131b56d2be1ed8378307cfd51a9cf%2cusername%3de0efb10f74cda04a; KTSSOKey=2ec095b6a62d57c357b32f9ffc669327; KTSSOUserID=dddc009724ad5e53f54a8a4a12dd26de; gwPermKey=; nssoauthdomain=9131bfcdfa6014f274d288ce12e28d22; s_fid=5CB7C44FB05AB8F4-0AEE6FF833439F69; strCode=Web; NSSO_DomainAuth_kt_com=1d66ce763a20bb14d8fce3726454f3c4bbf84c7e1af0508010bf36c5d58aa6d2aeb58eb3a20d123630f93e4550a652e90e82d0b5f1a61dd7be573d48469b019844bc7b6d4cfeb4ea; fileDownload=true");
	}

	public boolean isRsaLoginId() {
		return "YESTRUE".contains(this.properties.getProperty("login.rsa.id.yn", "N").toUpperCase());
	}
	public boolean isRsaLoginPassword() {
		return "YESTRUE".contains(this.properties.getProperty("login.rsa.password.yn", "N").toUpperCase());
	}
	public String getLoginId() {
		return this.properties.getProperty("login.id", "91094035");
	}
	public String getPassword() {
		return this.properties.getProperty("login.password", "rjsdud12!@");
	}
	public String getLoginPageContentType() {
		return this.properties.getProperty("login.page.content.type", "text/html; charset=UTF-8");
	}
	public String getLoginPageAccept() {
		return this.properties.getProperty("login.page.accept", "text/html; charset=UTF-8");
	}
	public String getLoginPageUrl() {
		return this.properties.getProperty("login.page.url", "http://dev.biznaru.kt.com/om/login");
	}
	public String getRsaModuleId() {
		return this.properties.getProperty("login.rsa.module.id", "RSAModulus");
	}
	public String getRsaExponentId() {
		return this.properties.getProperty("login.rsa.exponent.id", "RSAExponent");
	}
	public String getRsaPublicId() {
		return this.properties.getProperty("login.rsa.public.id", "RSAPublic");
	}
	public String getLoginProcessUrl() {
		return this.properties.getProperty("login.process.url", "http://dev.biznaru.kt.com/om/login_post");
	}
	public String getLoginProcessParams() {
		return this.properties.getProperty("login.process.params", "otpChkYn=N&userId=${login.id}&password=${login.password}"); // "{\r\n	\"userId\":\"${login.id}\",\r\n	\"password\":\"${login.password}\"\r\n}";
	}
}
