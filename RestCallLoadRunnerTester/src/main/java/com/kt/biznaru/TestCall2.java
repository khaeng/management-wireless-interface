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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

// import com.kt.biznaru.lamp.util.RestTemplateLampLoggerInterceptor;

public class TestCall2 {

	private static final int TASK_EXECUTOR_CORE_POOL_SIZE = 1000;
	private static final int TASK_EXECUTOR_MAX_POOL_SIZE = 2000;
	private static final int TASK_EXECUTOR_QUEUE_CAPACITY = 1000000;

	private static final int MAX_CONN_TOTAL = 100;
	private static final int MAX_CONN_PER_ROUTE = 5;
	
	private static Executor executor;
	private static long totalCount = 0;
	private static long totalSuccCount = 0;
	private static long errorCount = 0;

	private static Map<String, Long> countOfThead = new HashMap<String, Long>();

	private static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyyMMdd.HHmmss.SSS");
	private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SSS");

	public static void main(String[] args) throws IOException {
		
		String logFileName = "TestCall." + dateTimeFormat.format(new Date())+".log";
		new File(logFileName).delete();
		final BufferedWriter bw = new BufferedWriter(new FileWriter(logFileName, true));
		
//		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
//			@Override public void run() {
//				try {
//					bw.close();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}}));
		ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
		taskExecutor.setCorePoolSize(TASK_EXECUTOR_CORE_POOL_SIZE);
		taskExecutor.setMaxPoolSize(TASK_EXECUTOR_MAX_POOL_SIZE);
		taskExecutor.setQueueCapacity(TASK_EXECUTOR_QUEUE_CAPACITY);
		taskExecutor.setThreadNamePrefix("Executor-");
		taskExecutor.initialize();
		TestCall2.executor = taskExecutor;

		final String url276 = "https://tb.portal.biznaru.kt.com/ServiceBus/biznaru/shub/sb276.json";
		final String url149 = "https://tb.portal.biznaru.kt.com/ServiceBus/biznaru/shub/sb149.json";
		final String params = "{\r\n" + 
				"	\"recvCtn\":\"01011112222\",\r\n" + 
				"	\"content\":\"비즈나루 통합 SMS 단문1\"\r\n" + 
				"}";

		final TestCall2 test = new TestCall2();
		final int total = 100000;
		for (int i = 0; i < total; i++) {
			Runnable runnable = new Runnable() {@Override public void run() {
				long totCnt = ++totalCount;
				StringBuffer result = new StringBuffer();
				try {
					String threadName = Thread.currentThread().getName();
					addTheadCount(threadName);
					result.append("Start[").append(timeFormat.format(new Date())).append("] | End[] | ").append("Thread[").append(threadName).append("] | ");
					if(totCnt%3!=0) {
						result.append(test.runTest(url149, params));
					}else {
						result.append(test.runTest(url276, params));
					}
					result.insert(0, "] : ").insert(0, errorCount).insert(0, "], 실패[").insert(0, totalSuccCount).insert(0, " : 성공[").insert(0, totCnt)
							.insert(result.indexOf("End")+4, timeFormat.format(new Date()));
					bw.write(result.toString() + "\n");
					if(totCnt%1000==0) {
						System.out.println(result.toString());
					}
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					if(totCnt>=total) {
						result.setLength(0);
						result.append("테스트가 종료되었습니다.\n")
						.append("전체 테스트 개수 : ").append(totalCount).append("\n")
						.append("호출 테스트 성공 : ").append(totalSuccCount).append("\n")
						.append("호출 테스트 실패 : ").append(errorCount).append("\n")
						.append("성공과 실패는 테스터의 단순 호출에 대한 실패카운트이며, 성공내에서 실패된 서비스는 별도 로그를 체크해야 합니다.").append("\n")
						.append("사용된 Thread 개수 : ").append(countOfThead.size()).append("\n")
						;
						long totalThreadRunCount = 0;
						for (String threadName : countOfThead.keySet()) {
							totalThreadRunCount+=countOfThead.get(threadName);
							result.append("Thread[").append(threadName).append("] : ").append(countOfThead.get(threadName)).append(" EA\n");
						}
						result.append("Thread Total Run Count : ").append(totalThreadRunCount).append("\n");
						try {try {Thread.sleep(10* 1000);} catch (InterruptedException e) {} bw.write(result.toString());bw.flush(); bw.close();} catch (IOException e) {}
						System.out.println(result.toString());
						System.exit(0);
					}
				}}};
			executor.execute(runnable);
			// try {Thread.sleep(1000);} catch (InterruptedException e) {}
		}
	}

	private synchronized static void addTheadCount(String threadName) {
		Long threadCount = countOfThead.get(threadName);
		if(threadCount==null) {
			countOfThead.put(threadName, 1L);
		} else {
			countOfThead.put(threadName, ++threadCount);
		}
	}

	public String runTest(String url, String params) {
		
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add("Content-Type", "application/json;charset=UTF-8");
		headers.add("Accept", "application/json;charset=UTF-8");
		headers.add("authorization", "Basic ZG9tYWluX3VzZXI6ZG9tYWluX3VzZXI=");
		headers.add("Accept-Charset", "UTF-8");

//		if (reqHeaders != null) {
//			for (String key : reqHeaders.keySet()) {
//				headers.add(key, (String) reqHeaders.get(key));
//			}
//		}

		HttpEntity<Object> entity = new HttpEntity<Object>(params, headers);
		// entity.getHeaders().set("Accept-Charset", "UTF-8");;
		String response = null;
		try {
			response = restTemplate().postForObject(url, entity, String.class);
			++totalSuccCount;
		} catch (Exception e) {
			++errorCount ;
			response = "Exception.ERROR ::: " + e.getMessage() + " | " + e;
//			e.printStackTrace();
		}
		return response;
	}

	private static RestTemplate[] arrRestTemplate = new RestTemplate[10];
	private static String apiAuthKey = "Basic ZG9tYWluX3VzZXI6ZG9tYWluX3VzZXI=";
	private static int timeout = 90;
	public static RestTemplate restTemplate() {
		int indexRestTemplate = (int)(System.currentTimeMillis()%10);
		if(arrRestTemplate[indexRestTemplate]!=null) return arrRestTemplate[indexRestTemplate];
		HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
		factory.setConnectTimeout(timeout * 1000);
		factory.setReadTimeout(timeout * 1000);
		factory.setHttpClient(getHttpClientWithSSL()); // support SSL
		BufferingClientHttpRequestFactory bufferingClientHttpRequestFactory = new BufferingClientHttpRequestFactory(factory);
		RestTemplate restTemplate = new RestTemplate(bufferingClientHttpRequestFactory);
//		List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
//		interceptors.add(new RestTemplateLampLoggerInterceptor(apiAuthKey));
//		restTemplate.setInterceptors(interceptors);
		return arrRestTemplate[indexRestTemplate] = restTemplate;
	}

	private static CloseableHttpClient getHttpClientWithSSL() {
		CloseableHttpClient httpClient = null;
		try {
			RequestConfig config = RequestConfig.custom().setConnectTimeout(timeout * 1000)
					.setConnectionRequestTimeout(timeout * 1000).setSocketTimeout(timeout * 1000).build();
			httpClient = HttpClients.custom().setDefaultRequestConfig(config)
					.setHostnameVerifier(new AllowAllHostnameVerifier()).setSslcontext(
							new SSLContextBuilder().loadTrustMaterial(null, new org.apache.http.ssl.TrustStrategy() {
								public boolean isTrusted(X509Certificate[] arg0, String arg1)
										throws CertificateException {
									return true;
								}
							}).build())
					.setMaxConnTotal(MAX_CONN_TOTAL)
					.setMaxConnPerRoute(MAX_CONN_PER_ROUTE)
					.build();
		} catch (NoSuchAlgorithmException  | KeyManagementException | KeyStoreException e) {
			e.printStackTrace();
		}
		return httpClient;
	}

}
