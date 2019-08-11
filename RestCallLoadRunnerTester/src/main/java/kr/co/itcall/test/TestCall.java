package kr.co.itcall.test;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
//import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientResponseException;

// import kr.co.itcall.test.lamp.util.RestTemplateLampLoggerInterceptor;

public class TestCall extends RestTestBase {

	public TestCall(Constants constants) throws IOException {
		super(constants);
	}

	public static void main(String[] args) throws Exception {
		if(args!=null && args.length>0 && args[0].equalsIgnoreCase("start")) {		
			if(args!=null && args.length>1) {
				new TestCall(new Constants(args[1])).runTest();
			} else {
				new TestCall(new Constants(null)).runTest();
			}
		}else if(args!=null && args.length>0 && args[0].equalsIgnoreCase("stop")) {
			String hostAddr = "localhost";
			if(args.length>2)
				hostAddr = args[2];
			if(args!=null && args.length>1) {
				new TestCall(new Constants(args[1])).stopTest(hostAddr);
			} else {
				new TestCall(new Constants(null)).stopTest(hostAddr);
			}
		}else {
			String guide = "Usage : run APP with parameters : [start/stop] [test-configuration-file] [host-addr]\r\n" + 
					"	[host-addr] is only stop case.\r\n" + 
					"	";
			System.out.println(guide);
		}
	}

	private HttpHeaders httpHeaders = new HttpHeaders();
	private RestTemplateInterceptor restTemplateInterceptor;

	public void runTest() throws Exception {
		
		initialize();
		
		String resultStr = "";
		
		/****************************************************************
		 * 로그인 선행.
		 ****************************************************************/

		// this.cookie = this.constants.getCookie();
		
		String loginId = this.constants.getLoginId();
		String loginPassword = this.constants.getPassword();
		if(!StringUtils.isEmpty(loginId) && !StringUtils.isEmpty(loginPassword) && this.constants.isRsaLoginPassword()) {
			String loginPageUrl = this.constants.getLoginPageUrl();
			String loginParams = this.constants.getLoginPageParams();
			HttpHeaders loginPageHttpHeaders = this.constants.getLoginPageHeaderInfo(this.httpHeaders);
			restTemplateInterceptor = new RestTemplateInterceptor(loginPageHttpHeaders, this.constants.isLogging());
			resultStr = loginTest(0, loginPageUrl, loginParams, restTemplateInterceptor, HttpMethod.GET, loginPageHttpHeaders);
			this.httpHeaders = restTemplateInterceptor.getHttpHeaders();
			// String result = runTest(loginPageUrl, "", preHttpHeaders, HttpMethod.GET);
			System.out.println(resultStr);
	//		<input type="hidden" id="RSAModulus"  value='<c:out value="${_RSAModules}"/>' />
	//		<input type="hidden" id="RSAExponent" value='<c:out value="${_RSAExponent}"/>' />
			String rsaModuleId = null;
			String rsaExponentId = null;
			String rsaPublicId = null;
			String rsaModuleKey = null;
			String rsaExponentKey = null;
			String publicKeyStr = null;
			String rsaUrl= this.constants.getRsaUrl();



			if(!StringUtils.isEmpty(rsaUrl)) {
				rsaModuleId = this.constants.getRsaModuleKey();
				rsaExponentId = this.constants.getRsaExponentKey();
				rsaPublicId = this.constants.getRsaPublicKey();
				String rsaParams = this.constants.getRsaParams();
				rsaParams = switchParams(rsaParams, null, switchResult(resultStr), null, this.constants, null); // 일차 수신데이터에서 변경.
				HttpHeaders rsaHttpHeaders = this.constants.getRsaHeaderInfo(this.httpHeaders);
				HttpMethod rsaHttpMethod = this.constants.getRsaHttpMethod();
				if(restTemplateInterceptor==null) {
					restTemplateInterceptor = new RestTemplateInterceptor(rsaHttpHeaders, this.constants.isLogging());
				} else {
					restTemplateInterceptor.setHttpHeaders(rsaHttpHeaders);
				}
				resultStr = loginTest(0, rsaUrl, rsaParams, restTemplateInterceptor, rsaHttpMethod, rsaHttpHeaders);
				System.out.println(resultStr);
				Map<String,Object> rsaResult = switchResult(resultStr);
				if(!StringUtils.isEmpty(rsaResult)) {
					publicKeyStr = !StringUtils.isEmpty(rsaResult.get(rsaPublicId))   ? (String)rsaResult.get(rsaPublicId)   : null ;
					System.out.println("추출한 RSA_PUBLIC_KEY = " + publicKeyStr);
					rsaModuleKey = !StringUtils.isEmpty(rsaResult.get(rsaModuleId))   ? (String)rsaResult.get(rsaModuleId)   : null ;
					System.out.println("추출한 RSA_MODULE_KEY = " + rsaModuleKey);
					rsaExponentKey = !StringUtils.isEmpty(rsaResult.get(rsaExponentId)) ? (String)rsaResult.get(rsaExponentId) : null ;
					System.out.println("추출한 RSA_EXPONENT_KEY = " + rsaExponentKey);
					this.httpHeaders = restTemplateInterceptor.getHttpHeaders();
				}
			} else {
				rsaModuleId = this.constants.getRsaModuleId();
				rsaExponentId = this.constants.getRsaExponentId();
				rsaPublicId = this.constants.getRsaPublicId();
				if(!StringUtils.isEmpty(rsaModuleId)) {
					try {
						rsaModuleKey = getValFromKey(rsaModuleId, resultStr);
						if(StringUtils.isEmpty(rsaModuleKey)) throw new Exception();
					} catch (Exception e) {
						rsaModuleKey = getValFromBody(resultStr, this.constants.getRsaModuleStart(), this.constants.getRsaModuleEnd());
					}
					System.out.println("추출한 RSA_MODULE_KEY = " + rsaModuleKey);
				}
				
				if(!StringUtils.isEmpty(rsaExponentId)) {
					try {
						rsaExponentKey = getValFromKey(rsaExponentId, resultStr);
						if(StringUtils.isEmpty(rsaExponentKey)) throw new Exception();
					} catch (Exception e) {
						rsaExponentKey = getValFromBody(resultStr, this.constants.getRsaExponentStart(), this.constants.getRsaExponentEnd());
					}
					System.out.println("추출한 RSA_EXPONENT_KEY = " + rsaExponentKey);
				}
				
				if(!StringUtils.isEmpty(rsaPublicId)) {
					try {
						publicKeyStr = getValFromKey(rsaPublicId, resultStr);
						if(StringUtils.isEmpty(publicKeyStr)) throw new Exception();
					} catch (Exception e) {
						publicKeyStr = getValFromBody(resultStr, this.constants.getRsaPublicStart(), this.constants.getRsaPublicEnd());
					}
					System.out.println("추출한 RSA_PUBLIC_KEY = " + publicKeyStr);
				}
			}



			/*********** 로그인을 위한 RSA 암호화 처리 ************/
			if(!StringUtils.isEmpty(publicKeyStr)) {
				loginPassword = encryptRsaBase64(loginPassword, publicKeyStr, charset);
				if(this.constants.isRsaLoginId()) {
					loginId = encryptRsaBase64(loginId, publicKeyStr, charset);
				}
			} else if(!StringUtils.isEmpty(rsaModuleKey) && !StringUtils.isEmpty(rsaExponentKey)) {
				loginPassword = encryptRsaModule(loginPassword, rsaModuleKey, rsaExponentKey, charset);
				if(this.constants.isRsaLoginId()) {
					loginId = encryptRsaModule(loginId, rsaModuleKey, rsaExponentKey, charset);
				}
			}
		}


		if(!StringUtils.isEmpty(loginId) && !StringUtils.isEmpty(loginPassword)) {
			/*********** 로그인 처리 *************/
			loginId = URLEncoder.encode(loginId, charset.name());
			loginPassword = URLEncoder.encode(loginPassword, charset.name());
	//		String cookie = "JSESSIONID=Jj8d8pUkU5YYN7gVzm6BZzTY.biz11; NSSO_CentralAuth=4b64f6691c3f6a4cbd13f48e3b96bef1dd4ae16807ad29d87c300972497249adf073d8e8b8a85f46dddd49c3020875ea460060f37e29b1469e1cdd598e25564e590c6f91cda0c7c178401c1f22545ee7; NSSO_DomainInfo_kt_com=agencynm%3d615d4936bef356e5%2cdepartment%3d615d4936bef356e5%2cdeptcd%3d754ee8914e6397fb%2chandphoneno%3d46cd230c27291259fb41303db22be0a0%2cnewuserid%3d9f836bb751a6fdb8d25bcdf1072bec86%2colduserid%3d9f836bb751a6fdb8d25bcdf1072bec86%2cusermail%3d9f836bb751a6fdb822a131b56d2be1ed8378307cfd51a9cf%2cusername%3de0efb10f74cda04a; KTSSOKey=2ec095b6a62d57c357b32f9ffc669327; KTSSOUserID=dddc009724ad5e53f54a8a4a12dd26de; gwPermKey=; nssoauthdomain=9131bfcdfa6014f274d288ce12e28d22; s_fid=5CB7C44FB05AB8F4-0AEE6FF833439F69; strCode=Web; NSSO_DomainAuth_kt_com=1d66ce763a20bb14d8fce3726454f3c4bbf84c7e1af0508010bf36c5d58aa6d2aeb58eb3a20d123630f93e4550a652e90e82d0b5f1a61dd7be573d48469b019844bc7b6d4cfeb4ea; fileDownload=true";
			String loginProcessUrl = this.constants.getLoginProcessUrl();
			String loginParams = this.constants.getLoginProcessParams(); // otpChkYn=N&userId=${login.id}&password=${login.password}
			loginParams = loginParams.replaceFirst("[$]\\{login[.]id\\}", loginId).replaceFirst("[$]\\{login[.]password\\}", loginPassword);
			loginParams = switchParams(loginParams, null, switchResult(resultStr), null, this.constants, null); // 일차 수신데이터에서 변경.
	//		RestTemplateInterceptor restTemplateInterceptor = new RestTemplateInterceptor(cookie);
			HttpHeaders loginHttpHeaders = this.constants.getLoginProcessHeaderInfo(this.httpHeaders);
			if(restTemplateInterceptor==null) {
				restTemplateInterceptor = new RestTemplateInterceptor(loginHttpHeaders, this.constants.isLogging());
			} else {
				restTemplateInterceptor.setHttpHeaders(loginHttpHeaders);
			}
			String loginResult = loginTest(0, loginProcessUrl, loginParams, restTemplateInterceptor, HttpMethod.POST, loginHttpHeaders);
			System.out.println(loginResult);
	
			this.httpHeaders = restTemplateInterceptor.getHttpHeaders();
		}




		for (long j = 0; j < MAX_LOOP_AND_HEADER_COUNT; j++) {
			String url = constants.getTestUrlInfo(j);
			if(StringUtils.isEmpty(url)) {
				loopTestCount=(int)j;
				break;
			}
		}
		int printLogTerm = this.constants.getPrintLogTerm();
		StringBuffer result = new StringBuffer();
		result.append("\n\n======================================================================================\n테스트가 시작되었습니다.\n")
				.append("설정 테스트 개수 : ").append(totalTestCount).append("\n")
				.append("[순서 / 무작위] 호출 여부 : ").append(isLoopRelayTest ? "순서 처리" : "무작위 처리(동시호출과 Active프로세스는 2배차이남.)").append("\n")
				.append("동시(중복-Active) 호출 개수 : ").append(isLoopRelayTest ? totalMultiConnector : this.constants.getExecutorCorePoolSize()).append("\n")
				.append("한 개 호출그룹에 속한 호출 개수 : ").append(loopTestCount).append("\n")
				.append("정상 종료시 계획된 전체 호출 개수 : ").append(isLoopRelayTest ? (totalMultiConnector * totalTestCount * loopTestCount) : (this.constants.getExecutorCorePoolSize() * totalTestCount * loopTestCount)).append("\n")
				.append("---------- 수행 처리 결과 (주기적으로 출력됨) -------------").append("\n").append("\n")
				.append("사용자가 테스트 중인 서버에 포트[").append(constants.getWaitPort()).append("]로 접근하여 테스트를 종료하거나, 진행상황을 조회할 수 있습니다.").append("\n")
				.append("화면에 표시되는 로그주기 : ").append(this.constants.getPrintLogTerm()).append(" 회에 도달할때마다 화면에 표시되며, 전체 결과는 로그파일에 저장됩니다. 그룹을 1회로 봅니다.").append("\n")
				.append("기록된 로그파일명 : ").append(logFileName).append("\n======================================================================================\n\n");
		;
		addLogFlush(result.toString());
		System.out.println(result.toString());

		waittingStopCmdServer();
		this.startTestTime = System.currentTimeMillis();

		/* TEST.LOOP.START */
		if(isLoopRelayTest) {
			Thread[] arrThread = new Thread[totalMultiConnector];
			for (int i = 0; i < arrThread.length; i++) {
				Runnable runnable = new Runnable() { 
					private Map<String, Object> beforeResultMap = new HashMap<String, Object>();
					@Override public void run() {
					long totalTermDoneCount = 0;
					String resultStrFirstCall=null;
					String resultStr = null;
					while (totalTestCount>totalTermDoneCount) {
						if (isExitApp/* || totalProcessCount>=totalTestCount */)
							break;
						++totalTermDoneCount;
						if(constants.getSleepTimeBeforeGroup()>0)
							try {Thread.sleep(constants.getSleepTimeBeforeGroup());} catch (InterruptedException e) {}
						for (long j = 0; j < MAX_LOOP_AND_HEADER_COUNT; j++) {
							String testName = constants.getTestNameInfo(j);
							String url = constants.getTestUrlInfo(j);
							List<Map<String, Object>> preSqlResult = getPreSqlResult(j, beforeResultMap, resultStr, resultStrFirstCall);
							String params = switchParams(constants.getTestParamsInfo(j, beforeResultMap), preSqlResult, switchResult(resultStr), switchResult(resultStrFirstCall==null?resultStrFirstCall=resultStr:resultStrFirstCall), constants, beforeResultMap);
							if(!StringUtils.isEmpty(url)) {
								if(constants.getSleepTimeBeforeTest(j)>0)
									try {Thread.sleep(constants.getSleepTimeBeforeTest(j));} catch (InterruptedException e) {}
								try {
									HttpHeaders httpHeaders = constants.getTestHeaderInfo(j, TestCall.this.httpHeaders);
									StringBuffer result = new StringBuffer();
									String threadName = Thread.currentThread().getName();
									addTheadCount(threadName);
									result.append("Start[").append(timeFormat.format(new Date())).append("] | End[] | ").append("Thread[").append(threadName).append("] | Name[").append(testName).append("] | Url[").append(url).append("] | Params[").append(params).append("] ||| ");
									if(constants.isKeepSession(j)) {
										if(restTemplateInterceptor==null) {
											restTemplateInterceptor = new RestTemplateInterceptor(httpHeaders, constants.isLogging());
										} else {
											restTemplateInterceptor.setHttpHeaders(httpHeaders);
										}
										result.append(resultStr = runTest(j, url, params, restTemplateInterceptor, constants.getTestHttpMethod(j), httpHeaders));
									} else {
										result.append(resultStr = runTest(j, url, params, httpHeaders, constants.getTestHttpMethod(j)));
									}
									try {
										Map<String, Object> resultMap = objectMapper.readValue(resultStr, Map.class);
										beforeResultMap.putAll(resultMap);
									} catch (Exception e) {}
									result.insert(0, "] : ").insert(0, errorCount).insert(0, "], 실패[").insert(0, totalSuccCount).insert(0, " : 성공[").insert(0, addTotalCount())
											.insert(result.indexOf("End")+4, timeFormat.format(new Date()));
									addLog(result.toString() + "\n");
									if(constants.isLogging() || totalTermDoneCount%printLogTerm==0) {
										System.out.println(result.toString());
									}
								} catch (IOException e) {
									++systemErrorCount;
									if(systemErrorCount>100) {
										isExitApp = true;
									}
									e.printStackTrace();
								} finally {
									
								}
							} else {
								break;
							}
						}
					}}};
				arrThread[i] = new Thread(runnable);
				arrThread[i].setName(String.format("TestCall-%05d", i+1));
				// executor.execute(runnable);
			}
			for (Thread thread : arrThread) {
				if(thread!=null) {
					thread.start();
				}
			}
			for (Thread thread : arrThread) {
				if(thread!=null) {
					thread.join();
				}
			}
		}
		
		
		else
			
			
		{
			
			
			long totalCallTestCount = totalTestCount * this.constants.getExecutorCorePoolSize();
			for (long i = 0; i < totalCallTestCount; i++) {
				if(isExitApp || totalProcessCount>=totalCallTestCount*loopTestCount)
					break;
				final long totalTermDoneCount = i;
				Runnable runnable = new Runnable() {
					private Map<String, Object> beforeResultMap = new HashMap<String, Object>();
					@Override public void run() {
					try {Thread.sleep(constants.getSleepTimeBeforeGroup());} catch (InterruptedException e) {}
					String resultStrFirstCall=null;
					String resultStr=null;
					for (long j = 0; j < MAX_LOOP_AND_HEADER_COUNT; j++) {
						String testName = constants.getTestNameInfo(j);
						String url = constants.getTestUrlInfo(j);
						List<Map<String, Object>> preSqlResult = getPreSqlResult(j, beforeResultMap, resultStr, resultStrFirstCall);
						String params = switchParams(constants.getTestParamsInfo(j, beforeResultMap), preSqlResult, switchResult(resultStr), switchResult(resultStrFirstCall==null?resultStrFirstCall=resultStr:resultStrFirstCall), constants, beforeResultMap);
						if(!StringUtils.isEmpty(url)) {
							try {Thread.sleep(constants.getSleepTimeBeforeTest(j));} catch (InterruptedException e) {}
							try {
								HttpHeaders httpHeaders = constants.getTestHeaderInfo(j, TestCall.this.httpHeaders);
								StringBuffer result = new StringBuffer();
								String threadName = Thread.currentThread().getName();
								addTheadCount(threadName);
								result.append("Start[").append(timeFormat.format(new Date())).append("] | End[] | ").append("Thread[").append(threadName).append("] | Name[").append(testName).append("] | Url[").append(url).append("] | Params[").append(params).append("] ||| ");
								if(constants.isKeepSession(j)) {
									if(restTemplateInterceptor==null) {
										restTemplateInterceptor = new RestTemplateInterceptor(httpHeaders, constants.isLogging());
									} else {
										restTemplateInterceptor.setHttpHeaders(httpHeaders);
									}
									result.append(resultStr = runTest(j, url, params, restTemplateInterceptor, constants.getTestHttpMethod(j), httpHeaders));
								} else {
									result.append(resultStr = runTest(j, url, params, httpHeaders, constants.getTestHttpMethod(j)));
								}
								try {
									Map<String, Object> resultMap = objectMapper.readValue(resultStr, Map.class);
									beforeResultMap.putAll(resultMap);
								} catch (Exception e) {}
								result.insert(0, "] : ").insert(0, errorCount).insert(0, "], 실패[").insert(0, totalSuccCount).insert(0, " : 성공[").insert(0, addTotalCount())
										.insert(result.indexOf("End")+4, timeFormat.format(new Date()));
								addLog(result.toString() + "\n");
								if(constants.isLogging() || totalTermDoneCount%printLogTerm==0) {
									System.out.println(result.toString());
								}
							} catch (IOException e) {
								++systemErrorCount;
								if(systemErrorCount>100) {
									isExitApp = true;
								}
								e.printStackTrace();
							} finally {
								
							}
						} else {
							break;
						}
					}
				}};
				try {
					executor.execute(runnable);
				}catch (Exception e) {
					ThreadPoolTaskExecutor exec  = ((ThreadPoolTaskExecutor)executor);
					System.out.println(exec.getPoolSize() + ", Queue.size() : " + exec.getThreadPoolExecutor().getQueue().size() + ", ActiveCount : " + exec.getActiveCount() + ", remainingCapacity : " + exec.getThreadPoolExecutor().getQueue().remainingCapacity());
					e.printStackTrace();
					System.exit(-1);
				}
				while (true) {
					if(((ThreadPoolTaskExecutor)executor).getThreadPoolExecutor().getQueue().remainingCapacity() > this.constants.getExecutorQueueCapacity()/3)
						break;
				}
			}
			while (true) {
				if(isExitApp || ((ThreadPoolTaskExecutor)executor).getActiveCount() <= 0)
					break;
			}
		}



		endOfWork();



	}



	private synchronized static void addTheadCount(String threadName) {
		Long threadCount = countOfThead.get(threadName);
		if(threadCount==null) {
			countOfThead.put(threadName, 1L);
		} else {
			countOfThead.put(threadName, ++threadCount);
		}
	}

	public String runTest(long index, String url, String params, HttpHeaders httpHeaders) {
		return runTest(index, url, params, httpHeaders, HttpMethod.POST);
	}
	public String runTest(long index, String url, String params, HttpHeaders httpHeaders, HttpMethod httpMethod) {
		
//		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
//
//		if (httpHeaders != null) {
//			for (String key : httpHeaders.keySet()) {
//				headers.put(key, httpHeaders.get(key));
//			}
//		}

		HttpEntity<Object> entity = null;
		
		if(httpHeaders.getContentType().equals(MediaType.MULTIPART_FORM_DATA)) {
			MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
			this.constants.setMultipartFile(index, body);
			try{
				Map<String, Object> mapParams = switchResult(params);
				for (String key : mapParams.keySet()) {
					body.add(key, mapParams.get(key));
				}
			}catch (Exception e) {
				for (String element : params.split("&")) {
						String key = element.split("=",2)[0];
						try {
//							try {
//								Long.parseLong(element.split("=",2)[1]);
//								body.add(key, element.split("=",2)[1]);
//							}catch (Exception ex) {
//								try {
//									new Date(element.split("=",2)[1]);
									body.add(key, element.split("=",2)[1]);
//								} catch (Exception e2) {
//									body.add(key, URLEncoder.encode(element.split("=",2)[1], "MS949"));
//								}
//							}
						}catch (Exception ex) {
							body.add(key, "");
						}
				}
			}
			// MediaType mediaType = new MediaType("application", "x-www-form-urlencoded", Charset.forName("EUC-KR"));
			// httpHeaders.setContentType(mediaType);
			entity = new HttpEntity<>(body, httpHeaders);
		} else {
			try{
				Map<String, Object> mapParams = switchResult(params);
				if(StringUtils.isEmpty(mapParams)) {
					entity = new HttpEntity<Object>(params, httpHeaders);
				} else {
					entity = new HttpEntity<Object>(mapParams, httpHeaders);
				}
			}catch (Exception e) {
				entity = new HttpEntity<Object>(params, httpHeaders);
			}
		}
		
		String response = null;
		try {
			switch (httpMethod) {
//			case POST:
//				// response = restTemplate(this.constants.isLogging()).postForObject(url, entity, String.class, params);
//				response = restTemplate(this.constants.isLogging()).postForObject(url, entity, String.class);
//				break;
			default:
				// ResponseEntity<String> result = restTemplate(this.constants.isLogging()).exchange(url, httpMethod, entity, String.class);
				ResponseEntity<String> result = restTemplate(this.constants.isLogging()).exchange(url, httpMethod, entity, String.class);
				response = result.getBody();
				break;
			}
			addSuccessCount();
		} catch (Exception e) {
			addErrorCount();
			if(e instanceof RestClientResponseException) {
				response = "Exception.ERROR ::: " + e.getMessage() + " | " + ((RestClientResponseException)e).getResponseBodyAsString();
			} else {
				response = "Exception.ERROR ::: " + e.getMessage() + " | " + e;
			}
			
		}
		return response;
	}

	public String runTest(long index, String url, String params, RestTemplateInterceptor restTemplateInterceptor, HttpMethod httpMethod, HttpHeaders httpHeaders) {
		String response;
		try {
			 response = loginTest(index, url, params, restTemplateInterceptor, httpMethod, httpHeaders);
			addSuccessCount();
			return response;
		} catch (Exception e) {
			addErrorCount();
			if(e instanceof RestClientResponseException) {
				response = "Exception.ERROR ::: " + e.getMessage() + " | " + ((RestClientResponseException)e).getResponseBodyAsString();
			} else {
				response = "Exception.ERROR ::: " + e.getMessage() + " | " + e;
			}
		}
		return response;
	}


	public String loginTest(long index, String url, String params, RestTemplateInterceptor restTemplateInterceptor) throws Exception {
		return loginTest(index, url, params, restTemplateInterceptor, HttpMethod.POST, null);
	}
	public String loginTest(long index, String url, String params, RestTemplateInterceptor restTemplateInterceptor, HttpMethod httpMethod, HttpHeaders httpHeaders) throws Exception {
//		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
//		
//		if (httpHeaders != null) {
//			for (String key : httpHeaders.keySet()) {
//				headers.put(key, httpHeaders.get(key));
//			}
//		}
		
		HttpEntity<Object> entity = new HttpEntity<Object>(params, httpHeaders);
		String response = null;
		try {
			switch (httpMethod) {
			case POST:
				response = restTemplateForLogin(restTemplateInterceptor).postForObject(url, entity, String.class);
				break;
			default:
				ResponseEntity<String> result = restTemplateForLogin(restTemplateInterceptor).exchange(url, httpMethod, entity, String.class);
				response = result.getBody();
				break;
			}
		} catch (Exception e) {
			if(e instanceof RestClientResponseException) {
				response = "Exception.ERROR for LOGIN ::: " + e.getMessage() + " | " + ((RestClientResponseException)e).getResponseBodyAsString();
			} else {
				response = "Exception.ERROR for LOGIN ::: " + e.getMessage() + " | " + e;
			}
			throw new Exception(e);
		}
		return response;
	}


	private List<Map<String, Object>> getPreSqlResult(long index, Map<String, Object> beforeResultMap, String resultStr, String resultStrFirstCall) {
		List<Map<String, Object>> result = null;
		String dbKey = constants.getPropertyValue("test."+index+".db");
		String query = constants.getPropertyValue("test."+index+".query");
		if(StringUtils.isEmpty(dbKey) || StringUtils.isEmpty(query))
			return result;
		String dbName   = constants.getPropertyValue(dbKey+".name");
		String dbDriver = constants.getPropertyValue(dbKey+".driver");
		String dbUrl    = constants.getPropertyValue(dbKey+".url");
		String dbUserId = constants.getPropertyValue(dbKey+".username");
		String dbPwd    = constants.getPropertyValue(dbKey+".password");
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			
			query = switchParams(constants.switchParams(0, query, beforeResultMap), null, switchResult(resultStr), switchResult(resultStrFirstCall==null?resultStrFirstCall=resultStr:resultStrFirstCall), constants, beforeResultMap);
			
			Class.forName(dbDriver);
			conn = DriverManager.getConnection(dbUrl, dbUserId, dbPwd);
			pstmt = conn.prepareStatement(query);
			rs = pstmt.executeQuery();
			ResultSetMetaData metaData = rs.getMetaData();
			int sizeOfColumn = metaData.getColumnCount();
			result = new ArrayList<Map<String,Object>>();
			Map<String, Object> row = new HashMap<String, Object>();
			String colName;
			while (rs.next()) {
				for (int i = 0; i < sizeOfColumn; i++) {
					colName = metaData.getColumnName(i+1);
					row.put(colName, rs.getObject(i+1));
				}
				result.add(row);
			}
		}catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(rs!=null)try {rs.close();} catch (SQLException e) {e.printStackTrace();}
			if(pstmt!=null)try {pstmt.close();} catch (SQLException e) {e.printStackTrace();}
			if(conn!=null)try {conn.close();} catch (SQLException e) {e.printStackTrace();}
		}
		return result;
	}

	public static String encryptRsaBase64(String data, String publicKeyStr, Charset charset) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		byte[] bPublicKey = Base64.decode(publicKeyStr.getBytes()); // for Spring
//		byte[] bPublicKey = Base64.getDecoder().decode(publicKeyStr.getBytes());
		X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(bPublicKey);
		PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		byte[] bCipher = cipher.doFinal(data.getBytes());
		String encodedBase64 = new String(Base64.encode(bCipher)); // for Spring
//		String encodedBase64 = new String(Base64.getEncoder().encode(bCipher));
		return encodedBase64;
	}
	public static String encryptRsaModule(String data, String rsaModuleKey, String rsaExponentKey, Charset charset) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		PublicKey publicKey = keyFactory.generatePublic(new RSAPublicKeySpec(new BigInteger(rsaModuleKey, 16), new BigInteger(rsaExponentKey, 16)));
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		// 공개키 이용 암호화
		byte[] bCipher = cipher.doFinal(data.getBytes(charset));
		String encodedHex = byteArrayToHex(bCipher);
		return encodedHex;
	}

}
