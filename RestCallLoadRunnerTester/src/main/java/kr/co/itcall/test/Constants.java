package kr.co.itcall.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Properties;

import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.StringUtils;

public class Constants {

	private static final String BASE_PROPERTIES_FILE_NAME = "base.conf";
	private Properties properties;

	/* for TaskExecutor */
	private int executorCorePoolSize = 10;	// 멀티프로세스 카운트.
	private int executorMaxPoolSize = 20;	// 멀티프로세스의 2배.(Active)
	private int executorQueueCapacity = 1000000; // Active 개수의 100배.(Max의 100배)

	/* for RestUtil */
	private int maxConnTotalForRestClient = 100;
	private int maxConnPerRouteForRestClient = 100;
	private int maxRestTemplateUnitForRestClient = 10;
	private int connectionTimeoutForRestClient = 90;
	private int readTimeoutForRestClient = 90;

	private String propertiesFileName = BASE_PROPERTIES_FILE_NAME;

	public Constants(String propertiesFile) throws IOException {
		if(StringUtils.isEmpty(propertiesFile)) {
			this.properties = readProperties(new File(BASE_PROPERTIES_FILE_NAME));
		} else {
			this.propertiesFileName = propertiesFile;
			this.properties = readProperties(new File(propertiesFile));
		}
		this.executorQueueCapacity = (this.executorMaxPoolSize = (this.executorCorePoolSize = getTotalMultiConnector()) * 2) * 100;
	}

	public String getPropertiesFileName() {
		return propertiesFileName;
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
	public String getLoginPageUrl() {
		return this.properties.getProperty("login.page.url", "http://localhost:8080/login");
	}
	public String getRsaModuleId() {
		return this.properties.getProperty("login.rsa.module.id"); // , "RSAModulus");
	}
	public String getRsaModuleStart() {
		return this.properties.getProperty("login.rsa.module.start");
	}
	public String getRsaModuleEnd() {
		return this.properties.getProperty("login.rsa.module.end");
	}
	public String getRsaExponentId() {
		return this.properties.getProperty("login.rsa.exponent.id"); // , "RSAExponent");
	}
	public String getRsaExponentStart() {
		return this.properties.getProperty("login.rsa.exponent.start");
	}
	public String getRsaExponentEnd() {
		return this.properties.getProperty("login.rsa.exponent.end");
	}
	public String getRsaPublicId() {
		return this.properties.getProperty("login.rsa.public.id"); // , "RSAPublic");
	}
	public String getRsaPublicStart() {
		return this.properties.getProperty("login.rsa.Public.start");
	}
	public String getRsaPublicEnd() {
		return this.properties.getProperty("login.rsa.Public.end");
	}
	public String getLoginProcessUrl() {
		return this.properties.getProperty("login.process.url", "http://localhost:8080/login_post");
	}
	public String getLoginProcessParams() {
		return this.properties.getProperty("login.process.params", "otpChkYn=N&userId=${login.id}&password=${login.password}"); // "{\r\n	\"userId\":\"${login.id}\",\r\n	\"password\":\"${login.password}\"\r\n}";
	}
	/** 멀티 호출개수만큼 테스트 Runnalbe을 만들고. 테스트한다. 각 테스트는 앞 테스트가 종료(완료)된 후 실행된다. 한 루프가 돌면 다시 처음부터 호출된다. **/
	public boolean isLoopRelayTest() {
		return "YESTRUE".contains(this.properties.getProperty("test.loop.relay.yn", "N").toUpperCase());
	}
	public long getSleepTimeBeforeGroup() {
		return Long.parseLong(this.properties.getProperty("test.group.sleep", "100"));
	}
	public long getSleepTimeBeforeTest(long testNum) {
		return Long.parseLong(this.properties.getProperty("test."+testNum+".sleep", "10"));
	}
	public HttpMethod getTestHttpMethod(long testNum) {
		return HttpMethod.valueOf(this.properties.getProperty("test."+testNum+".method", "POST"));
	}
	public HttpHeaders getTestHeaderInfo(long testNum, HttpHeaders httpHeaders) {
		return getHeaderInfoFromProperties("test."+testNum+".header.", httpHeaders);
	}
	public String getTestUrlInfo(long testNum) {
		return this.properties.getProperty("test."+testNum+".url");
	}
	public String getTestParamsInfo(long testNum) {
		return this.properties.getProperty("test."+testNum+".params");
	}
	public boolean isKeepSession(long testNum) {
		return "YESTRUE".contains(this.properties.getProperty("test."+testNum+".keep.session.yn", "N").toUpperCase());
	}
	public int getWaitPort() {
		return Integer.parseInt(this.properties.getProperty("test.wait.port", "9991"));
	}

	public HttpHeaders getLoginPageHeaderInfo(HttpHeaders httpHeaders) {
		return getHeaderInfoFromProperties("login.page.header.", httpHeaders);
	}
	public HttpHeaders getLoginProcessHeaderInfo(HttpHeaders httpHeaders) {
		return getHeaderInfoFromProperties("login.process.header.", httpHeaders);
	}
	public HttpHeaders getHeaderInfoFromProperties(String baseKey, HttpHeaders httpHeaders) {
		HttpHeaders outputHeaders = new HttpHeaders();
		if(httpHeaders!=null) {
			for (String header: httpHeaders.keySet()) {
				outputHeaders.put(header, httpHeaders.get(header));
			}
		}
		for (int i = 0; i < 100; i++) {
			String key = this.properties.getProperty(baseKey+i+".key");
			if(StringUtils.isEmpty(key))
				break;
			outputHeaders.remove(key); // 키가 존재하면 원래 해더의 키는 무조건 삭제한다.
			String value = this.properties.getProperty(baseKey+i+".value");
			if(value!=null) {
				outputHeaders.add(key, value); // value가 존재하면 무조건 Overwirte한다. 공백이면, 삭제하는 효과.
			}
		}
		return outputHeaders;
	}

	public int getPrintLogTerm() {
		return Integer.parseInt(this.properties.getProperty("log.print.term", "10"));
	}

	public Charset getTestCharset() {
		return Charset.forName(this.properties.getProperty("test.charset", "UTF-8"));
	}

}
