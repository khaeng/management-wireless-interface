package kr.co.itcall.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
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

import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class RestTestBase {

	public static Map<String, Long> countOfThead = new TreeMap<String, Long>();

	public static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyyMMdd.HHmmss.SSS");
	public static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SSS");


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
	}

	protected void initialize() throws IOException {
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
		totalMultiConnector = this.constants.getTotalMultiConnector();
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
		return this.restTemplateForRestClientForLogin = restTemplate;
	}

	private CloseableHttpClient getHttpClientWithSSL(RestTemplateInterceptor restTemplateInterceptor) {
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
					.setRedirectStrategy(
							new RestTemplateRedirectInterceptor(restTemplateInterceptor)
					)
					.setMaxConnTotal(this.constants.getMaxConnTotalForRestClient())
					.setMaxConnPerRoute(this.constants.getMaxConnPerRouteForRestClient())
					.build();
		} catch (NoSuchAlgorithmException  | KeyManagementException | KeyStoreException e) {
			e.printStackTrace();
		}
		return httpClient;
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

	protected void waittingStopCmdServer() {
		new Thread(new Runnable() { @Override public void run() {
			int waitPort = constants.getWaitPort();
			if(waitPort>0) {
				ServerSocket serverSocket = null;
				try {
					serverSocket = new ServerSocket(waitPort);
					while (!isExitApp) {
						final Socket socket = serverSocket.accept();
						new Thread(new Runnable() { @Override public void run() {
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
								
							} finally {
								if(socket!=null) try { socket.close(); } catch (IOException e) {}
							}}}).start();
					}
				} catch (IOException e) {
					e.printStackTrace();
					if(serverSocket!=null) try {serverSocket.close();} catch (IOException e1) {}
					System.exit(-1);
				}finally {
					try {if(serverSocket!=null)serverSocket.close();} catch (IOException e) {}
				}
			}}
		}).start();
	}

	protected void stopTest(String addr) throws UnknownHostException, IOException, InterruptedException {
		int waitPort = this.constants.getWaitPort();
		if(waitPort>0) {
			final Socket socket = new Socket(addr, waitPort);
			try {
				Thread readThread = new Thread(new Runnable() { @Override public void run() {
					byte[] bts = new byte[4096];
					int readed;
					try {
						while ((readed=socket.getInputStream().read(bts))>0) {
							System.out.println(new String(Arrays.copyOf(bts, readed), charset).trim());
						}
					} catch (IOException e) { }}});
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
				.append("---------- 아래 카운트는 그룹별이 아닌 각 호출에 대한 카운트 임. -------------").append("\n")
				.append("전체 테스트 개수 : ").append(totalProcessCount).append("\n")
				.append("호출 테스트 성공 : ").append(totalSuccCount).append("\n")
				.append("호출 테스트 실패 : ").append(errorCount).append("\n")
				.append("시스템 에러 개수 : ").append(systemErrorCount).append("\n")
				.append("성공과 실패는 테스터의 단순 호출에 대한 실패카운트이며, 성공내에서 실패된 서비스는 별도 로그를 체크해야 합니다.").append("\n")
				.append("사용된 Thread 개수 : ").append(countOfThead.size()).append("\n\n")
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
		
		if(isExitApp)
			System.out.println("\n\n======================================================================================\n사용자의 요청에 의해 테스트가 중지 중 입니다. 잠시만 기다려 주십시오.");
		else
			System.out.println("\n\n======================================================================================\n테스트가 마무리 되었습니다. 결과를 정리중이니 잡시만 기다려 주십시오.");
		
		try {
			for (int i = 0; i < 30; i++) {
				System.out.print(i%2==0?"■":"□");
				Thread.sleep(500);
			}
			System.out.println();
			} catch (InterruptedException e) {}
		
		((ThreadPoolTaskExecutor)executor).shutdown();
		
//		if(isExitApp) // fileBuffer의 종료를 일정시간 기다려준다.
//			try {Thread.sleep(10* 1000);} catch (InterruptedException e) {}
		
		String result = getNowTestResult(true);
		
		try {addLogFlush(result);logFileClose();} catch (IOException e) {}
		System.out.println(result);
		System.exit(0);
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


	protected static synchronized long addTotalCount() {
		return ++totalProcessCount;
	}
	protected static synchronized void addSuccessCount() {
		++totalSuccCount;
	}
	protected static synchronized void addErrorCount() {
		++errorCount;
	}

	protected static Map<String, Object> switchResult(String resultStr) {
		if(StringUtils.isEmpty(objectMapper)) {
			JsonFactory jsonFactiory = new JsonFactory();
			objectMapper = new ObjectMapper(jsonFactiory); 
		}
		try {
			return objectMapper.readValue(resultStr, Map.class);
		} catch (IOException | NullPointerException e) {
			return null;
		}
	}
	protected static String switchParams(String params, Map<String, Object> map, Map<String, Object> mapFirstCall) {
		if(StringUtils.isEmpty(params))
			return "";
		if(StringUtils.isEmpty(objectMapper)) {
			JsonFactory jsonFactiory = new JsonFactory();
			objectMapper = new ObjectMapper(jsonFactiory); 
		}
		int start = params.indexOf("${");
		int end = params.indexOf("}", start);
		if(-1<start && start<end) {
			String before = params.substring(0, start);
			String after = params.substring(end+1);
			String switchKey = params.substring(start+2, end);
			try {
				if(StringUtils.isEmpty(switchKey) || StringUtils.isEmpty(map) && StringUtils.isEmpty(mapFirstCall)) {
					return switchParams(new StringBuffer().append(before).append("\"Not supported values...\"").append(after).toString(), map, mapFirstCall);
				} else if(!StringUtils.isEmpty(map) && !StringUtils.isEmpty(map.get(switchKey))) {
					String switchValue = objectMapper.writeValueAsString(map.get(switchKey));
					return switchParams(new StringBuffer().append(before).append(switchValue).append(after).toString(), map, mapFirstCall);
				} else {
					if(!StringUtils.isEmpty(map)) {
						for (String key : map.keySet()) {
							if(map.get(key) instanceof Map) {
								if(!StringUtils.isEmpty(((Map) map.get(key)).get(switchKey))) {
									String switchValue = objectMapper.writeValueAsString(((Map) map.get(key)).get(switchKey));
									return switchParams(new StringBuffer().append(before).append(switchValue).append(after).toString(), map, mapFirstCall);
								}
							}
						}
					}
					if(!StringUtils.isEmpty(mapFirstCall) && !StringUtils.isEmpty(mapFirstCall.get(switchKey))) {
						String switchValue = objectMapper.writeValueAsString(mapFirstCall.get(switchKey));
						return switchParams(new StringBuffer().append(before).append(switchValue).append(after).toString(), map, mapFirstCall);
					} else if(!StringUtils.isEmpty(mapFirstCall)) {
						for (String key : mapFirstCall.keySet()) {
							if(mapFirstCall.get(key) instanceof Map) {
								if(!StringUtils.isEmpty(((Map) mapFirstCall.get(key)).get(switchKey))) {
									String switchValue = objectMapper.writeValueAsString(((Map) mapFirstCall.get(key)).get(switchKey));
									return switchParams(new StringBuffer().append(before).append(switchValue).append(after).toString(), map, mapFirstCall);
								}
							}
						}
					}
					return switchParams(new StringBuffer().append(before).append("").append(after).toString(), map, mapFirstCall);
				}
			}catch (JsonProcessingException e) {
				return switchParams(new StringBuffer().append(before).append("\"Not convert Object to String cause [").append(e.getMessage()).append("]\"").append(after).toString(), map, mapFirstCall);
			}
		}
		return params;
	}

	public static void main(String[] args) throws Exception {
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
		System.out.println(switchParams(test, map, null));
	}
}
