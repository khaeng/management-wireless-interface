package com.kt.biznaru;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

public abstract class RestTestBase {

	public static Map<String, Long> countOfThead = new HashMap<String, Long>();

	public static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyyMMdd.HHmmss.SSS");
	public static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SSS");


	protected Executor executor;
	protected Constants constants;

	private RestTemplate[] arrRestTemplateForRestClient;
	private RestTemplate restTemplateForRestClientForLogin;
	private BufferedWriter bwLogger;

	public RestTestBase(Constants constants) throws IOException {
		this.constants = constants;
		this.arrRestTemplateForRestClient = new RestTemplate[this.constants.getMaxRestTemplateUnitForRestClient()];

		String path = this.constants.getLogPath();
		new File(path).mkdirs();
		String logFileName = path + "TestCall." + dateTimeFormat.format(new Date())+".log";
		new File(logFileName).delete();
		bwLogger = new BufferedWriter(new FileWriter(logFileName, true));

		this.executor = getExecutor();
	}

	public Executor getExecutor() {
		if(executor!=null)
			return this.executor;
		ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
		taskExecutor.setCorePoolSize(this.constants.getExecutorCorePoolSize());
		taskExecutor.setMaxPoolSize(this.constants.getExecutorMaxPoolSize());
		taskExecutor.setQueueCapacity(this.constants.getExecutorQueueCapacity());
		taskExecutor.setThreadNamePrefix("RestTesterExec-");
		taskExecutor.initialize();
		return this.executor = taskExecutor;
	}

	public RestTemplate restTemplate(boolean isLogging) {
		int indexRestTemplate = (int)(System.currentTimeMillis()%this.constants.getMaxRestTemplateUnitForRestClient());
		if(this.arrRestTemplateForRestClient[indexRestTemplate]!=null) return this.arrRestTemplateForRestClient[indexRestTemplate];
		HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
		factory.setConnectTimeout(this.constants.getConnectionTimeoutForRestClient() * 1000);
		factory.setReadTimeout(this.constants.getReadTimeoutForRestClient() * 1000);
		factory.setHttpClient(getHttpClientWithSSL()); // support SSL
		BufferingClientHttpRequestFactory bufferingClientHttpRequestFactory = new BufferingClientHttpRequestFactory(factory);
		RestTemplate restTemplate = new RestTemplate(bufferingClientHttpRequestFactory);
		if(isLogging) {
			List<ClientHttpRequestInterceptor> interceptors = new ArrayList<ClientHttpRequestInterceptor>();
			interceptors.add(new RestTemplateInterceptor());
			restTemplate.setInterceptors(interceptors);
		}
		return arrRestTemplateForRestClient[indexRestTemplate] = restTemplate;
	}
	public RestTemplate restTemplateForLogin(RestTemplateInterceptor restTemplateInterceptor) {
		if(this.restTemplateForRestClientForLogin!=null) return this.restTemplateForRestClientForLogin;
		HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
		factory.setConnectTimeout(this.constants.getConnectionTimeoutForRestClient() * 1000);
		factory.setReadTimeout(this.constants.getReadTimeoutForRestClient() * 1000);
		factory.setHttpClient(getHttpClientWithSSL()); // support SSL
		BufferingClientHttpRequestFactory bufferingClientHttpRequestFactory = new BufferingClientHttpRequestFactory(factory);
		RestTemplate restTemplate = new RestTemplate(bufferingClientHttpRequestFactory);
		if(restTemplateInterceptor!=null) {
			List<ClientHttpRequestInterceptor> interceptors = new ArrayList<ClientHttpRequestInterceptor>();
			interceptors.add(restTemplateInterceptor);
			restTemplate.setInterceptors(interceptors);
		}
		return this.restTemplateForRestClientForLogin = restTemplate;
	}

	private CloseableHttpClient getHttpClientWithSSL() {
		CloseableHttpClient httpClient = null;
		try {
			RequestConfig config = RequestConfig.custom().setConnectTimeout(this.constants.getConnectionTimeoutForRestClient() * 1000)
					.setConnectionRequestTimeout(this.constants.getReadTimeoutForRestClient() * 1000).setSocketTimeout(this.constants.getReadTimeoutForRestClient() * 1000).build();
			httpClient = HttpClients.custom().setDefaultRequestConfig(config)
					.setHostnameVerifier(new AllowAllHostnameVerifier()).setSslcontext(
							new SSLContextBuilder().loadTrustMaterial(null, new org.apache.http.ssl.TrustStrategy() {
								public boolean isTrusted(X509Certificate[] arg0, String arg1)
										throws CertificateException {
									return true;
								}
							}).build())
					.setMaxConnTotal(this.constants.getMaxConnTotalForRestClient())
					.setMaxConnPerRoute(this.constants.getMaxConnPerRouteForRestClient())
					.build();
		} catch (NoSuchAlgorithmException  | KeyManagementException | KeyStoreException e) {
			e.printStackTrace();
		}
		return httpClient;
	}

	protected void addLog(String log) throws IOException {
		this.bwLogger.write(log);
	}
	protected void addLogFlush(String log) throws IOException {
		this.bwLogger.write(log);
		this.bwLogger.flush();
	}
	protected void logFileClose() throws IOException {
		this.bwLogger.close();
	}

}
