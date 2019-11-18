package kr.co.itcall.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Executor;

import javax.net.ssl.SSLContext;
import javax.swing.JOptionPane;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.http.converter.xml.SourceHttpMessageConverter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public abstract class RestTestBase {

	public static final int MAX_LOG_PRINT_TO_CONSOLE = 200;
	public static Map<String, Long> countOfThead;
	private static ServerSocket stopWaitingServerSocket;

	public static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyyMMdd.HHmmss.SSS");
	public static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SSS");
	public static final int MAX_LOOP_AND_HEADER_COUNT = 100;

	protected Executor executor;
	protected Constants constants;
	protected boolean isExitApp;

	protected static long totalTestCount = 0;
	protected static long totalProcessCount = 0;
	protected static long totalSuccCount = 0;
	protected static long errorCount = 0;
	protected static long systemErrorCount = 0;
	protected static boolean isLoopRelayTest;
	protected static int loopTestCount;
	protected static int totalMultiConnector;
	protected static ObjectMapper objectMapper;

	private RestTemplate[] arrRestTemplateForRestClient;
	private RestTemplate restTemplateForRestClientForLogin;
	private BufferedWriter bwLogger;

	protected long startTestTime;
	protected Charset charset;
	protected String logFileName;



	public RestTestBase(Constants constants) {
		this.constants = constants;
		this.charset = this.constants.getTestCharset(); // 테스트 시작과 종료시 모두 사용하므로...
		JsonFactory jsonFactiory = new JsonFactory();
		objectMapper = new ObjectMapper(jsonFactiory);
		this.constants.setObjectMapper(objectMapper);
	}

	protected void initialize(int testMultiCount) throws IOException {

		// 다중 처리를 위해서 static 변수들은 초기화 한다.
		// isExitApp = false;
		countOfThead = new TreeMap<String, Long>();
		totalTestCount = 0;
		totalProcessCount = 0;
		totalSuccCount = 0;
		errorCount = 0;
		systemErrorCount = 0;
		isLoopRelayTest = false;
		loopTestCount = 0;
		totalMultiConnector = 0;

		this.arrRestTemplateForRestClient = new RestTemplate[this.constants.getMaxRestTemplateUnitForRestClient()];

		String path = this.constants.getLogPath();
		new File(path).mkdirs();
		String logFileName = this.constants.getPropertiesFileName();
		if(logFileName.lastIndexOf(System.getProperty("file.separator", "/"))>-1) {
			logFileName.substring(logFileName.lastIndexOf(System.getProperty("file.separator", "/"))+1);
		}
		logFileName = logFileName.replaceAll("\\\\", "_").replaceAll("\\/", "_");
		this.logFileName = logFileName = path + "TestCall." + logFileName + "_" + dateTimeFormat.format(new Date())+".log";
		new File(logFileName).delete();
		bwLogger = new BufferedWriter(new FileWriter(logFileName, true));

		this.executor = getExecutor();
		
		isLoopRelayTest = this.constants.isLoopRelayTest();
		totalTestCount = this.constants.getTotalTestCount();
		if(testMultiCount>0) {
			totalMultiConnector = testMultiCount;
		} else {
			totalMultiConnector = this.constants.getTotalMultiConnector();
		}
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
		factory.setHttpClient(getHttpClientWithSSL(null)); // support SSL
		BufferingClientHttpRequestFactory bufferingClientHttpRequestFactory = new BufferingClientHttpRequestFactory(factory);
		RestTemplate restTemplate = new RestTemplate(bufferingClientHttpRequestFactory);
		if(isLogging) {
			List<ClientHttpRequestInterceptor> interceptors = new ArrayList<ClientHttpRequestInterceptor>();
			interceptors.add(new RestTemplateInterceptor(null, isLogging));
			restTemplate.setInterceptors(interceptors);
		}
		return arrRestTemplateForRestClient[indexRestTemplate] = withMessageConverters(restTemplate);
	}
	public RestTemplate restTemplateForLogin(RestTemplateInterceptor restTemplateInterceptor) {
		if(this.restTemplateForRestClientForLogin!=null) return this.restTemplateForRestClientForLogin;
		HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
		factory.setConnectTimeout(this.constants.getConnectionTimeoutForRestClient() * 1000);
		factory.setReadTimeout(this.constants.getReadTimeoutForRestClient() * 1000);
		factory.setHttpClient(getHttpClientWithSSL(restTemplateInterceptor)); // support SSL
		BufferingClientHttpRequestFactory bufferingClientHttpRequestFactory = new BufferingClientHttpRequestFactory(factory) {
			@Override
			protected ClientHttpRequest createRequest(URI uri, HttpMethod httpMethod, ClientHttpRequestFactory requestFactory) throws IOException {
				return super.createRequest(uri, httpMethod, requestFactory);
			}
		};
		
		RestTemplate restTemplate = new RestTemplate(bufferingClientHttpRequestFactory);
		if(restTemplateInterceptor!=null) {
			List<ClientHttpRequestInterceptor> interceptors = new ArrayList<ClientHttpRequestInterceptor>();
			interceptors.add(restTemplateInterceptor);
			restTemplate.setInterceptors(interceptors);
		}
		return this.restTemplateForRestClientForLogin = withMessageConverters(restTemplate);
	}

	private CloseableHttpClient getHttpClientWithSSL(RestTemplateInterceptor restTemplateInterceptor) {
		if(!StringUtils.isEmpty(this.constants.getProtocols())) {
			return getHttpClientWithTLS12only(restTemplateInterceptor, this.constants.getProtocols());
		}
		CloseableHttpClient httpClient = null;
		try {
			TrustStrategy trustStrategy = new org.apache.http.ssl.TrustStrategy() {
				public boolean isTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
					return true;
				}
			};
			SSLContext sslContext = SSLContexts.custom()//.useProtocol("TLSv1.2") // TLSv1, TLSv1.1, TLSv1.2, SSL, TLS
					.loadTrustMaterial(null, trustStrategy)
					.build();
			
			RequestConfig config = RequestConfig.custom().setConnectTimeout(this.constants.getConnectionTimeoutForRestClient() * 1000)
					.setConnectionRequestTimeout(this.constants.getReadTimeoutForRestClient() * 1000).setSocketTimeout(this.constants.getReadTimeoutForRestClient() * 1000).build();
			
			httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config)
					.setHostnameVerifier(new AllowAllHostnameVerifier())
					.setSslcontext(sslContext)
					.setRedirectStrategy(new RestTemplateRedirectInterceptor(restTemplateInterceptor))
					.setMaxConnTotal(this.constants.getMaxConnTotalForRestClient())
					.setMaxConnPerRoute(this.constants.getMaxConnPerRouteForRestClient())
					.build();
			
//			httpClient = HttpClients.custom().setDefaultRequestConfig(config)
//					.setHostnameVerifier(new AllowAllHostnameVerifier()).setSslcontext(
//							new SSLContextBuilder().loadTrustMaterial(null, new org.apache.http.ssl.TrustStrategy() {
//								public boolean isTrusted(X509Certificate[] arg0, String arg1)
//										throws CertificateException {
//									return true;
//								}
//							}).build())
//					.setRedirectStrategy(
//							new RestTemplateRedirectInterceptor(restTemplateInterceptor)
//					)
//					.setMaxConnTotal(this.constants.getMaxConnTotalForRestClient())
//					.setMaxConnPerRoute(this.constants.getMaxConnPerRouteForRestClient())
//					.setSSLContext(sslContext)
//					.build();
		} catch (NoSuchAlgorithmException  | KeyManagementException | KeyStoreException e) {
			e.printStackTrace();
		}
		
		return httpClient;
	}
	private CloseableHttpClient getHttpClientWithTLS12only(RestTemplateInterceptor restTemplateInterceptor, String protocols) {
		CloseableHttpClient httpClient = null;
		try {
			TrustStrategy trustStrategy = new org.apache.http.ssl.TrustStrategy() {
				public boolean isTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
					return true;
				}
			};
			final SSLContext sslContext = SSLContexts.custom() //.useProtocol("TLSv1.2") // TLSv1, TLSv1.1, TLSv1.2, SSL, TLS
					.loadTrustMaterial(null, trustStrategy)
					.build();
			
			RequestConfig config = RequestConfig.custom().setConnectTimeout(this.constants.getConnectionTimeoutForRestClient() * 1000)
					.setConnectionRequestTimeout(this.constants.getReadTimeoutForRestClient() * 1000).setSocketTimeout(this.constants.getReadTimeoutForRestClient() * 1000).build();
			
			String[] arrProtocal = protocols.split(","); // "SSLv3,TLSv1,TLSv1.1,TLSv1.2".split(","); // new String[] {"TLSv1.2"};
			final SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext,arrProtocal,null, NoopHostnameVerifier.INSTANCE);
			
			httpClient = HttpClients.custom() // HttpClientBuilder.create()
					.setSSLSocketFactory(sslConnectionSocketFactory)
					.setDefaultRequestConfig(config)
					// .setHostnameVerifier(new AllowAllHostnameVerifier())
					.setRedirectStrategy(new RestTemplateRedirectInterceptor(restTemplateInterceptor))
					.setMaxConnTotal(this.constants.getMaxConnTotalForRestClient())
					.setMaxConnPerRoute(this.constants.getMaxConnPerRouteForRestClient())
					.build();
			
		} catch (NoSuchAlgorithmException  | KeyManagementException | KeyStoreException e) {
			e.printStackTrace();
		}
		return httpClient;
	}
	private RestTemplate withMessageConverters(RestTemplate restTemplate) {
		if(!StringUtils.isEmpty(restTemplate)) {
			for (HttpMessageConverter<?> httpMessageConverter : restTemplate.getMessageConverters()) {
				if(!(httpMessageConverter instanceof AllEncompassingFormHttpMessageConverter))
					continue;
				
				List<HttpMessageConverter<?>> partConverterList = new ArrayList<HttpMessageConverter<?>>();
				partConverterList.add(new ByteArrayHttpMessageConverter());
				StringHttpMessageConverter stringHttpMessageConverter = new StringHttpMessageConverter(charset);
				stringHttpMessageConverter.setWriteAcceptCharset(false);
				partConverterList.add(stringHttpMessageConverter);
				partConverterList.add(new ResourceHttpMessageConverter());
				partConverterList.add(new SourceHttpMessageConverter<>());
				if(ClassUtils.isPresent("javax.xml.bind.Binder", AllEncompassingFormHttpMessageConverter.class.getClassLoader())) {
					partConverterList.add(new Jaxb2RootElementHttpMessageConverter());
				}
				if(ClassUtils.isPresent("com.fasterxml.jackson.databind.ObjectMapper", AllEncompassingFormHttpMessageConverter.class.getClassLoader())) {
					partConverterList.add(new MappingJackson2HttpMessageConverter());
				} else if(ClassUtils.isPresent("org.codehaus.jackson.map.ObjectMapper",  AllEncompassingFormHttpMessageConverter.class.getClassLoader())
						&& ClassUtils.isPresent("org.codehaus.jackson.JsonGenerator",  AllEncompassingFormHttpMessageConverter.class.getClassLoader())) {
					// partConverterList.add(new MappingJacksonHttpMessageConverter());
					partConverterList.add(new MappingJackson2HttpMessageConverter());
				}
				
				((AllEncompassingFormHttpMessageConverter) httpMessageConverter).setPartConverters(partConverterList);
				((AllEncompassingFormHttpMessageConverter) httpMessageConverter).setCharset(charset);
				((AllEncompassingFormHttpMessageConverter) httpMessageConverter).setMultipartCharset(charset);
				
			}
			
//			List<HttpMessageConverter<?>> partConverterList = new ArrayList<HttpMessageConverter<?>>();
//			partConverterList.add(new ByteArrayHttpMessageConverter());
//			StringHttpMessageConverter stringHttpMessageConverter = new StringHttpMessageConverter(charset);
//			stringHttpMessageConverter.setWriteAcceptCharset(false);
//			partConverterList.add(stringHttpMessageConverter);
//			partConverterList.add(new ResourceHttpMessageConverter());
//			partConverterList.add(new SourceHttpMessageConverter<>());
//			if(ClassUtils.isPresent("javax.xml.bind.Binder", AllEncompassingFormHttpMessageConverter.class.getClassLoader())) {
//				partConverterList.add(new Jaxb2RootElementHttpMessageConverter());
//			}
//			if(ClassUtils.isPresent("com.fasterxml.jackson.databind.ObjectMapper", AllEncompassingFormHttpMessageConverter.class.getClassLoader())) {
//				partConverterList.add(new MappingJackson2HttpMessageConverter());
//			} else if(ClassUtils.isPresent("org.codehaus.jackson.map.ObjectMapper",  AllEncompassingFormHttpMessageConverter.class.getClassLoader())
//					&& ClassUtils.isPresent("org.codehaus.jackson.JsonGenerator",  AllEncompassingFormHttpMessageConverter.class.getClassLoader())) {
//				// partConverterList.add(new MappingJacksonHttpMessageConverter());
//				partConverterList.add(new MappingJackson2HttpMessageConverter());
//			}
//			restTemplate.setMessageConverters(partConverterList);
		} else {
			// RestTemplate
		}
		return restTemplate;
	}

	protected void addLog(String log) throws IOException {
		try {
			this.bwLogger.write(log);
		} catch (IOException e) {
			if(!isExitApp)
				throw e;
		}
	}
	protected void addLogFlush(String log) throws IOException {
		this.bwLogger.write(log);
		this.bwLogger.flush();
	}
	protected void logFileClose() throws IOException {
		this.bwLogger.flush();
		this.bwLogger.close();
	}

	protected void waittingStopCmdServerTerminating() {
		if(!StringUtils.isEmpty(stopWaitingServerSocket) /*&& this.serverSocket.isBound() && !this.serverSocket.isClosed()*/) {
			try { stopWaitingServerSocket.close(); } catch (IOException e) { e.printStackTrace(); }
			stopWaitingServerSocket = null;
		}
	}
	protected void waittingStopCmdServer() {
		new Thread(() -> {
			int waitPort = constants.getWaitPort();
			if(waitPort>0) {
				// ServerSocket stopWaitingServerSocket = null;
				waittingStopCmdServerTerminating();
				try {
					stopWaitingServerSocket = new ServerSocket(waitPort);
					while (!isExitApp) {
						final Socket socket = stopWaitingServerSocket.accept();
						new Thread(() -> {
							try {
								socket.getOutputStream().write("Hello~. What are you want to now?\n".getBytes());
								socket.getOutputStream().flush();
								byte[] bts = new byte[4096];
								while (socket.getInputStream().read(bts)>0) {
									if(new String(bts).trim().toLowerCase().contains("exit")) {
										System.out.println("\n\n========================================================================\n\t사용자의 요청에 의해 테스트를 중단합니다.\n========================================================================\n");
										socket.getOutputStream().write("Good bye we will stopping test...\n".getBytes());
										socket.getOutputStream().flush();
										isExitApp = true;
										break;
									}else {
										socket.getOutputStream().write(getNowTestResult(false).getBytes(charset));
										socket.getOutputStream().flush();
										socket.getOutputStream().write("\nIf you want to finished TEST. Type to 'exit'\n Your Type > ".getBytes());
										socket.getOutputStream().flush();
									}
								}
							} catch (Exception e) {
								e.printStackTrace();
							} finally {
								if(socket!=null) try { socket.close(); } catch (IOException e) {}
							}
						}).start();
					}
//				} catch (SocketException e) {
//					// "socket closed" 인 경우는 여러 테스트 수행에 따른 인위적 closed이므로 종료하지 않는다.
//					if(e.getMessage().equalsIgnoreCase("socket closed")) {
//						
//					} else {
//						
//					}
				} catch (IOException e) {
					// "socket closed" 인 경우는 여러 테스트 수행에 따른 인위적 closed이므로 종료하지 않는다.
					if(e.getMessage().equalsIgnoreCase("socket closed")) {
						System.out.println("\n\n========================================================================\n\t다음 테스트 종료대기를 위해 현재 종료대기 소켓이 닫혔습니다.\n========================================================================\n");
					} else {
						e.printStackTrace();
						if(stopWaitingServerSocket!=null) try {stopWaitingServerSocket.close();} catch (IOException e1) {}
						System.exit(-1);
					}
				}finally {
					try {if(stopWaitingServerSocket!=null)stopWaitingServerSocket.close();} catch (IOException e) {}
				}
				if(isExitApp) {
					System.exit(-2);
				}
			}
		}).start();
	}

	protected void stopTest(String addr) throws UnknownHostException, IOException, InterruptedException {
		int waitPort = this.constants.getWaitPort();
		if(waitPort>0) {
			final Socket socket = new Socket(addr, waitPort);
			try {
				Thread readThread = new Thread(() -> {
					byte[] bts = new byte[4096];
					int readed;
					try {
						while ((readed=socket.getInputStream().read(bts))>0) {
							System.out.println(new String(Arrays.copyOf(bts, readed), charset).trim());
						}
					} catch (IOException e) { }});
				readThread.start();
				socket.getOutputStream().write("exit".getBytes());
				socket.getOutputStream().flush();
				readThread.join();
			} finally {
				if(socket!=null)
					socket.close();
			}
		}
	}



	private String getNowTestResult(boolean isEndTest) {
		StringBuffer result = new StringBuffer();
		if(isEndTest) {
			if(isExitApp)
				result.append("\n\n======================================================================================\n\t사용자의 요청에 의해 테스트가 종료되었습니다.\n\n");
			else
				result.append("\n\n======================================================================================\n\t테스트가 종료되었습니다.\n\n");
		}
		long totalThreadRunCount = 0;
		for (String threadName : countOfThead.keySet()) {
			totalThreadRunCount+=countOfThead.get(threadName);
			result.append("Thread[").append(threadName).append("] : ").append(countOfThead.get(threadName)).append(" EA\n");
		}
		result.append("Thread Total Run Count : ").append(totalThreadRunCount).append("\n");
		
		long endTestTime = System.currentTimeMillis();
		long timeGap = endTestTime - startTestTime;
		result.append("\n======================================================================================\n\n")
				.append("설정 테스트 개수 : ").append(totalTestCount).append("\n")
				.append("[순서 / 무작위] 호출 여부 : ").append(isLoopRelayTest ? "순서 처리" : "무작위 처리(동시호출과 Active프로세스는 2배차이남.)").append("\n")
				.append("동시(중복-Active) 호출 개수 : ").append(isLoopRelayTest ? totalMultiConnector : this.constants.getExecutorCorePoolSize()).append("\n")
				.append("한 개 호출그룹에 속한 호출 개수 : ").append(loopTestCount).append("\n")
				.append("정상 종료시 계획된 전체 호출 개수 : ").append(isLoopRelayTest ? (totalMultiConnector * totalTestCount * loopTestCount) : (this.constants.getExecutorCorePoolSize() * totalTestCount * loopTestCount)).append("\n")
				.append("\n")
				.append("---------- 아래 카운트는 그룹별이 아닌 전체 호출에 대한 카운트 임. -------------").append("\n")
				.append("\n")
				.append("사용 Thread 개수 : ").append(countOfThead.size()).append("\n")
				.append("전체 테스트 개수 : ").append(totalProcessCount).append("\n")
				.append("호출 테스트 성공 : ").append(totalSuccCount).append("\n")
				.append("호출 테스트 실패 : ").append(errorCount).append("\n")
				.append("시스템 에러 개수 : ").append(systemErrorCount).append("\n")
				.append("실패 시 재확인 수행된 호출 개수 : ").append(totalProcessCount - loopTestCount*totalTestCount).append(" (마이너스 값은 중간에러에 의한 종료 시 수행되지 못한 개수)").append("\n")
				.append("   (에러 시 별도수행 설정된 경우 수행되며, 기본수행은 성공으로 셋팅하고 별도수행은 호출결과에 따른다)").append("\n")
				.append("   (성공과 실패는 테스터의 단순 호출에 대한 실패카운트이며, 성공내에서 실패된 서비스는 별도 로그를 체크해야 합니다.)").append("\n")
				.append("\n")
				.append("테스트 시작시각 : ").append(dateTimeFormat.format(new Date(startTestTime))).append("\n")
				.append("테스트 종료시각 : ").append(dateTimeFormat.format(new Date(endTestTime))).append("\n")
				.append("수행 시각(MS) : ").append(String.format("%,d(ms)", timeGap)).append("\n")
				.append("수행 시각(TM) : ").append(String.format("%02d:%02d:%02d.%03d", (timeGap/(60*60*1000))%24, (timeGap/(60*1000))%60, (timeGap/(1000))%60, timeGap%1000)).append("\n")
				.append("초당 처리개수(TPS) : ").append(String.format("%,.2f(Tps)", (float)totalProcessCount/((endTestTime - startTestTime)/1000))).append("\n")
				.append("기록된 로그파일명 : ").append(logFileName).append("\n");
		if(isEndTest && isExitApp) {
			result.append("\n").append("\n☆★☆★☆★☆★ 테스트 중 사용자 종료요청에 의해 중단합니다. ☆★☆★☆★☆★").append("\n");
		}
		;
		return result.toString();
	}

	protected void endOfWork() {
		
		if(isExitApp) {
			if(constants.isStopFailed() && errorCount>0) {
				System.out.println("\n\n======================================================================================\n계획된 테스트 중 에러가 발생하여 테스트가 중지 중 입니다. 잠시만 기다려 주십시오.");
			} else {
				System.out.println("\n\n======================================================================================\n사용자의 요청에 의해 테스트가 중지 중 입니다. 잠시만 기다려 주십시오.");
			}
		} else
			System.out.println("\n\n======================================================================================\n테스트가 마무리 되었습니다. 결과를 정리중이니 잠시만 기다려 주십시오.");
		
		try {
			for (int i = 0; i < 70; i++) {
				System.out.print(i%2==0?"■":"□");
				Thread.sleep(50);
			}
			System.out.println();
			} catch (InterruptedException e) {}
		
		((ThreadPoolTaskExecutor)executor).shutdown();
		
//		if(isExitApp) // fileBuffer의 종료를 일정시간 기다려준다.
//			try {Thread.sleep(10* 1000);} catch (InterruptedException e) {}
		
		String result = getNowTestResult(true);
		
		try {addLogFlush(result);logFileClose();} catch (IOException e) {}
		System.out.println(result);
		// System.exit(0); // 연속 테스트를 위하여  remark.
	}

	protected String getValFromKey(final String key, final String fromBody) {
/***
<input type="hidden" id="RSAModulus"  value="b721b710c3c2b464b882a4a5ca62d4a0eb213127c369d4b29738d8566662a9865e32918a742a8413f7b0344320a663852744b6f1a2f5d4deda2b6c80899c79207f434b5cc49aa16935fed7c5c65ff210826a6296a6bab7a29fd76d0a8978beae3234a646cdf4ec22bfaef80c6b7c5e360fd6895eb7a4206d6ffaf2aaafae5dfc383c17181d1bb404ad563506d7f1766c178600c05fea99cf43213ac06bc64b2bcad6225f54d425db9ac4876ea501abc345490f252c80275f33215fe0ff84306e7abbe9f375bf7318c169c69b021776ae51ef9430f8f2bf7051de17ac268c1aaabce572b32cd20bbf13995fe6f7681ef5b1f29cf8d171bbf5b20de7dd72588e83" />
<input type="hidden" id="RSAExponent" value="10001" />
 */
		int workIndex = fromBody.indexOf("\""+key+"\"");
		if(workIndex<0)
			workIndex = fromBody.indexOf("'"+key+"'");
		String workStr = fromBody.substring(0, workIndex);
		workIndex = workStr.lastIndexOf("<");
		workStr = fromBody.substring(workIndex);
		workIndex = workStr.indexOf(">");
		workStr = workStr.substring(0, workIndex).replaceAll(" ", "");
		workIndex = workStr.indexOf("value=")+6;
		workStr = workStr.substring(workIndex);
		int lastTagIndex = workStr.lastIndexOf(workStr.substring(0, 1));
		workStr = workStr.substring(1, lastTagIndex);
		return workStr;
	}

	protected String getValFromBody(String resultStr, String rsaModuleStart, String rsaModuleEnd) throws Exception {
		int start = resultStr.lastIndexOf(rsaModuleStart) + rsaModuleStart.length();
		int end = resultStr.lastIndexOf(rsaModuleEnd);
		if(start>=end) throw new Exception("RSA 모듈 추출키에 해당하는 Data가 없습니다. Start[" + rsaModuleStart + "], End[" + rsaModuleEnd + "]");
		return resultStr.substring(start, end);
	}


	public static String byteArrayToHex(byte[] buf) {
		StringBuffer sb = new StringBuffer();
		for (byte b : buf) {
			sb.append(String.format("%02x", b));
		}
		return sb.toString();
	}


	protected synchronized static void addTheadCount(String threadName) {
		Long threadCount = countOfThead.get(threadName);
		if(threadCount==null) {
			countOfThead.put(threadName, 1L);
		} else {
			countOfThead.put(threadName, ++threadCount);
		}
	}

	protected static synchronized long addTotalCount() {
		return ++totalProcessCount;
	}
	protected static synchronized void addSuccessCount() {
		++totalSuccCount;
	}
	protected static synchronized void addErrorCount() {
		++errorCount;
	}

	public static Map<String, Object> switchResult(String jsonStr) {
		try {
			return objectMapper.readValue(jsonStr, Map.class);
		} catch (IOException | NullPointerException e) {
			return null;
		}
	}
	public static Map<String, Object> switchResult(String xmlStr, Charset charset) {
		try {
			XmlMapper xmlMapper = new XmlMapper();
			JsonNode jsonNode = xmlMapper.readTree(xmlStr.getBytes(charset));
			String json = objectMapper.writeValueAsString(jsonNode);
			return switchResult(json);
		} catch ( IOException e) {
			return switchResult(xmlStr);
		}
	}
	/**
	 * 이게 가장 느리다. 개선해줘야 한다.
	 * @param postFix
	 * @param testIndex
	 * @param params
	 * @param preSqlResult
	 * @param mapKeepData
	 * @param mapFirstCall
	 * @param constants
	 * @param beforeResultMap
	 * @return
	 */
	protected static String switchParams(String postFix, long testIndex, String params, List<Map<String, Object>> preSqlResult, Map<String, Object> mapKeepData, Map<String, Object> mapFirstCall, Constants constants, Map<String, Object> beforeResultMap) {
		if(StringUtils.isEmpty(params))
			return "";
		int start = params.indexOf("${");
		if(start>0 && params.charAt(start-1)=='\\')
			start = -1;
		int end = params.indexOf("}", start);
		if(-1<start && start<end) {
			String before = params.substring(0, start);
			String after = params.substring(end+1);
			String switchKey = params.substring(start+2, end);
			try {
				if(StringUtils.isEmpty(switchKey)/* || StringUtils.isEmpty(map) && StringUtils.isEmpty(mapFirstCall)*/) {
					String switchValue = inputUserPopup(String.format("[%d]번째 테스트[%s] : 지정된 키값이 잘못되었습니다. 수정하고 다시시도하거나, 직접 입력해주세요", testIndex, constants.getTestNameInfo(testIndex, postFix)), "${"+switchKey+"} 에 대응하는 값을 입력해주세요.", "");
					if(!StringUtils.isEmpty(switchValue)) {
						if(!StringUtils.isEmpty(mapKeepData)) mapKeepData.put(switchKey, switchValue);
						constants.getProperties().setProperty("_user.input.value."+switchKey, switchValue);
					}
					return switchParams(postFix, testIndex, new StringBuffer().append(before).append(switchValue).append(after).toString(), preSqlResult, mapKeepData, mapFirstCall, constants, beforeResultMap);
					// return switchParams(new StringBuffer().append(before).append("\"Not supported values...\"").append(after).toString(), preSqlResult, map, mapFirstCall, constants, beforeResultMap);
				} else {
					if(!StringUtils.isEmpty(preSqlResult) && preSqlResult.size()>0) {
						String switchValue = constants.findFromList(switchKey, preSqlResult);
						if(switchValue!=null)
							return switchParams(postFix, testIndex, new StringBuffer().append(before).append(switchValue).append(after).toString(), preSqlResult, mapKeepData, mapFirstCall, constants, beforeResultMap);
					}
					if(!StringUtils.isEmpty(mapKeepData) && !StringUtils.isEmpty(mapKeepData.get(switchKey))) {
						// String switchValue = objectMapper.writeValueAsString(map.get(switchKey));
						String switchValue = mapKeepData.get(switchKey) + "";
						return switchParams(postFix, testIndex, new StringBuffer().append(before).append(switchValue).append(after).toString(), preSqlResult, mapKeepData, mapFirstCall, constants, beforeResultMap);
					} else if(!StringUtils.isEmpty(mapKeepData)) {
						for (String key : mapKeepData.keySet()) {
							if(mapKeepData.get(key) instanceof Map) {
								if(!StringUtils.isEmpty(((Map) mapKeepData.get(key)).get(switchKey) + "") && !(((Map) mapKeepData.get(key)).get(switchKey) + "").equals("0") && !(((Map) mapKeepData.get(key)).get(switchKey) + "").equals("null")) {
									// String switchValue = objectMapper.writeValueAsString(((Map) map.get(key)).get(switchKey));
									String switchValue = ((Map) mapKeepData.get(key)).get(switchKey) + "";
									return switchParams(postFix, testIndex, new StringBuffer().append(before).append(switchValue).append(after).toString(), preSqlResult, mapKeepData, mapFirstCall, constants, beforeResultMap);
								}
							}
						}
					}
					if(!StringUtils.isEmpty(mapFirstCall) && !StringUtils.isEmpty(mapFirstCall.get(switchKey))) {
						// String switchValue = objectMapper.writeValueAsString(mapFirstCall.get(switchKey));
						String switchValue = mapFirstCall.get(switchKey) + "";
						return switchParams(postFix, testIndex, new StringBuffer().append(before).append(switchValue).append(after).toString(), preSqlResult, mapKeepData, mapFirstCall, constants, beforeResultMap);
					} else if(!StringUtils.isEmpty(mapFirstCall)) {
						for (String key : mapFirstCall.keySet()) {
							if(mapFirstCall.get(key) instanceof Map) {
								if(!StringUtils.isEmpty(((Map) mapFirstCall.get(key)).get(switchKey))) {
									// String switchValue = objectMapper.writeValueAsString(((Map) mapFirstCall.get(key)).get(switchKey));
									String switchValue = ((Map) mapFirstCall.get(key)).get(switchKey) + "";
									return switchParams(postFix, testIndex, new StringBuffer().append(before).append(switchValue).append(after).toString(), preSqlResult, mapKeepData, mapFirstCall, constants, beforeResultMap);
								}
							}
						}
					}
					if(!StringUtils.isEmpty(beforeResultMap) && !StringUtils.isEmpty(beforeResultMap.get(switchKey))) {
						// String switchValue = objectMapper.writeValueAsString(beforeResultMap.get(switchKey));
						String switchValue = beforeResultMap.get(switchKey) + "";
						return switchParams(postFix, testIndex, new StringBuffer().append(before).append(switchValue).append(after).toString(), preSqlResult, mapKeepData, mapFirstCall, constants, beforeResultMap);
					} else if(!StringUtils.isEmpty(beforeResultMap)) {
						try {
							for (String key : beforeResultMap.keySet()) {
								if(beforeResultMap.get(key) instanceof Map) {
									if(!StringUtils.isEmpty(((Map) beforeResultMap.get(key)).get(switchKey) + "") && !(((Map) beforeResultMap.get(key)).get(switchKey) + "").equals("0") && !(((Map) beforeResultMap.get(key)).get(switchKey) + "").equals("null")) {
										// String switchValue = objectMapper.writeValueAsString(((Map) beforeResultMap.get(key)).get(switchKey));
										String switchValue = ((Map) beforeResultMap.get(key)).get(switchKey)+"";
										return switchParams(postFix, testIndex, new StringBuffer().append(before).append(switchValue).append(after).toString(), preSqlResult, mapKeepData, mapFirstCall, constants, beforeResultMap);
									}
								}
							}
						}catch (Exception e) {e.printStackTrace();}
						String switchValue = constants.findFromMap(switchKey, beforeResultMap);
						if(switchValue!=null) {
							return switchParams(postFix, testIndex, new StringBuffer().append(before).append(switchValue).append(after).toString(), preSqlResult, mapKeepData, mapFirstCall, constants, beforeResultMap);
						}
					}

					// Properties에서는 한번만 찾는다.
					String switchValue = constants.getPropertyValue(switchKey);
					if(!StringUtils.isEmpty(switchValue)) {
						return switchParams(postFix, testIndex, new StringBuffer().append(before).append(switchValue).append(after).toString(), preSqlResult, mapKeepData, mapFirstCall, constants, beforeResultMap);
					}
					switchValue = constants.getProperties().getProperty("_user.input.value."+switchKey);
					if(!StringUtils.isEmpty(switchValue)) {
						return switchParams(postFix, testIndex, new StringBuffer().append(before).append(switchValue).append(after).toString(), preSqlResult, mapKeepData, mapFirstCall, constants, beforeResultMap);
					}
					switchValue = inputUserPopup(String.format("[%d]번째 테스트[%s] : 통신결과에서 값을 찾을 수 없습니다. 직접 입력해주세요", testIndex, constants.getTestNameInfo(testIndex, postFix)), "${"+switchKey+"} 에 대응하는 값을 입력해주세요.", "");
					if(!StringUtils.isEmpty(switchValue)) {
						if(!StringUtils.isEmpty(mapKeepData)) mapKeepData.put(switchKey, switchValue);
						constants.getProperties().setProperty("_user.input.value."+switchKey, switchValue);
//						if(StringUtils.isEmpty(map)) map = new HashMap<String, Object>();
//						map.put(switchKey, switchValue);
						return switchParams(postFix, testIndex, new StringBuffer().append(before).append(switchValue).append(after).toString(), preSqlResult, mapKeepData, mapFirstCall, constants, beforeResultMap);
					}
					// 멀티처리 시 다른곳에서 입력받은것이 있을 수 있으므로 다시한번 처리한다.
					switchValue = constants.getProperties().getProperty("_user.input.value."+switchKey, "");
					return switchParams(postFix, testIndex, new StringBuffer().append(before).append(switchValue).append(after).toString(), preSqlResult, mapKeepData, mapFirstCall, constants, beforeResultMap);
				}
			}catch (Exception e) {
				String switchValue = constants.getProperties().getProperty("_user.input.value."+switchKey);
				if(!StringUtils.isEmpty(switchValue)) {
					return switchParams(postFix, testIndex, new StringBuffer().append(before).append(switchValue).append(after).toString(), preSqlResult, mapKeepData, mapFirstCall, constants, beforeResultMap);
				}
				switchValue = inputUserPopup(String.format("[%d]번째 테스트[%s] : 통신결과에서 값을 찾을 수 없습니다. 직접 입력해주세요", testIndex, constants.getTestNameInfo(testIndex, postFix)), "${"+switchKey+"} 에 대응하는 값을 입력해주세요.", "\"Not convert Object to String cause [");
				if(!StringUtils.isEmpty(switchValue)) {
					if(!StringUtils.isEmpty(mapKeepData)) mapKeepData.put(switchKey, switchValue);
					constants.getProperties().setProperty("_user.input.value."+switchKey, switchValue);
//					if(StringUtils.isEmpty(map)) map = new HashMap<String, Object>();
//					map.put(switchKey, switchValue);
					return switchParams(postFix, testIndex, new StringBuffer().append(before).append(switchValue).append(after).toString(), preSqlResult, mapKeepData, mapFirstCall, constants, beforeResultMap);
				}// 멀티처리 시 다른곳에서 입력받은것이 있을 수 있으므로 다시한번 처리한다.
				switchValue = constants.getProperties().getProperty("_user.input.value."+switchKey, "");
				return switchParams(postFix, testIndex, new StringBuffer().append(before).append(switchValue).append(e.getMessage()).append("]\"").append(after).toString(), preSqlResult, mapKeepData, mapFirstCall, constants, beforeResultMap);
			}
		}
		return params;
	}

	public static String inputUserPopup(String title, String message, String defValue) {
		final String[] result = new String[] {""};
		
		Thread threadUiUx = new Thread(() -> {
			result[0] = JOptionPane.showInputDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE);
		});
		Thread threadCmd = new Thread(() -> {
			BufferedReader br = null;
			try {
				System.out.println("\n"+title);
				System.out.print(message + "\n >>> : ");
				br = new BufferedReader(new InputStreamReader(System.in));
				result[0] = br.readLine();
				// br.reset();
			} catch (IOException e) {} finally {
				// if(br!=null) try {br.close();} catch (IOException e) {}
			}
		});
		
		threadUiUx.start();
		threadCmd.start();
		while (true) {
			try {Thread.sleep(1000);} catch (InterruptedException e) {}
			if(!StringUtils.isEmpty(result[0])) {
				threadUiUx.interrupt();
				threadCmd.interrupt();
				break;
			} else if(!threadUiUx.isAlive() && !threadCmd.isAlive() || !threadCmd.isAlive()) { // 서버에서 실행 할 수있으므로 ...
				result[0] = defValue;
				break;
			}
		}
		return result[0];
	}

	public static void main(String[] args) throws Exception {
		
		String switchKey = "yyyyMMddHHmmssSSS-23";
		switchKey = "yyyyMMddHHmmssSSS+111";
		switchKey += "2019 YYYY DD hh-";
		String addPattern = "^[0-9GyMdkHmsSEDFwWahKzZYuXL]{1,100}[+]{1}[1-9]{1}[0-9]{0,18}$";
		String delPattern = "^[0-9GyMdkHmsSEDFwWahKzZYuXL]{1,100}[-]{1}[1-9]{1}[0-9]{0,18}$";
		System.out.println("ADD : " + switchKey.matches(addPattern));
		System.out.println("DEL : " + switchKey.matches(delPattern));
		String[] testArr = switchKey.split("[-]",2);
		testArr = switchKey.split("[+]",2);

		long addValue = 0;
		if(switchKey.matches(addPattern)) {
			addValue = Long.parseLong(switchKey.split("[+]",2)[1].trim());
			switchKey = switchKey.split("[+]",2)[0];
		}
		long delValue = 0;
		if(switchKey.matches(delPattern)) {
			delValue = Long.parseLong(switchKey.split("[-]",2)[1].trim());
			switchKey = switchKey.split("[-]",2)[0];
		}
		String switchValue = new SimpleDateFormat(switchKey).format(new Date());
		System.out.println(switchValue);
		System.out.println("ADD : " + addValue);
		System.out.println("DEL : " + delValue);
		if(switchValue.matches("^[1-9]{1}[0-9]{0,18}$")) {
			if(addValue>0) {
				switchValue=""+(Long.parseLong(switchValue)+addValue);
				System.out.println("DO ADD : " + switchValue);
			}
			if(delValue>0) {
				switchValue=""+(Long.parseLong(switchValue)-delValue);
				System.out.println("DO DEL : " + switchValue);
			}
		}
		System.out.println(switchValue);
		System.exit(0);
		
		Date date = new Date("Thu Aug 06 2020 14:46:51 GMT+0900 (sadfaㄴㅁㅇㄹdㅁㄴㅇㄹ)");
		System.out.println(date);
		String testStr = "userId=\"><scr<script>ipt>alert(1);var test='91094035ABC한글j';</scr<script>ipt><";
		String encStr = URLEncoder.encode(testStr, Charset.forName("UTF-8").name());
		System.out.println(encStr);
		System.out.println(URLDecoder.decode(encStr, Charset.forName("UTF-8").name()));
		if(true)return ;
		System.out.println("asdfasdf\\asdfasdf/asdf/asdf".replaceAll("\\\\", "_").replaceAll("\\/", "_"));
		String test = "{\"username\":${login.id},\"password\":${login.password}, \"subMap\" : { \"recvCtn\":${testSubMap}, \"content\":\"비즈나루 통합 SMS 단문1\"}, \"testKey\":${thisKey}, \r\n \"list\":${listResult}}";
		System.out.println(test);
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> subMap = new HashMap<String, Object>();
		subMap.put("testSubMap", "ThisIsSubMap");
		map.put("login.password", 212);
		map.put("thisKey", subMap);
		
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		list.add(subMap);
		list.add(subMap);
		list.add(subMap);
		list.add(subMap);
		list.add(subMap);
		map.put("listResult", list);
		System.out.println(switchParams("", 0, test, null, map, null, null, null));
	}
}
