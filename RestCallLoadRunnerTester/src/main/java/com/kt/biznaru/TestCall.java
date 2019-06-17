package com.kt.biznaru;

import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

import javax.crypto.Cipher;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

// import com.kt.biznaru.lamp.util.RestTemplateLampLoggerInterceptor;

public class TestCall extends RestTestBase {

	public static long totalCount = 0;
	public static long totalSuccCount = 0;
	public static long errorCount = 0;

	public TestCall(Constants constants) throws IOException {
		super(constants);
	}

	public static void main(String[] args) throws Exception {
		if(args!=null && args.length>0) {
			new TestCall(new Constants(args[0])).runTest();
		} else {
			new TestCall(new Constants(null)).runTest();
		}
	}

	private long totalTestCount;
	private String cookie;
	private HttpHeaders httpHeaders = new HttpHeaders();

	public void runTest() throws Exception {
		
		String result = "";
		
		this.totalTestCount = this.constants.getTotalTestCount();
		int totalMultiConnector = this.constants.getTotalMultiConnector();
		
		
		/****************************************************************
		 * 로그인 선행.
		 ****************************************************************/

		this.cookie = this.constants.getCookie();
		RestTemplateInterceptor restTemplateInterceptor = new RestTemplateInterceptor(cookie);

		String loginId = this.constants.getLoginId();
		String loginPassword = this.constants.getPassword();
		if(!StringUtils.isEmpty(loginId) && !StringUtils.isEmpty(loginPassword) && this.constants.isRsaLoginPassword()) {
			String loginPageUrl = this.constants.getLoginPageUrl();
			HttpHeaders preHttpHeaders = new HttpHeaders();
			preHttpHeaders.set("Content-Type", this.constants.getLoginPageContentType());
			preHttpHeaders.set("Accept", this.constants.getLoginPageAccept());
			result = loginTest(loginPageUrl, "", restTemplateInterceptor, HttpMethod.GET, preHttpHeaders);
			// String result = runTest(loginPageUrl, "", preHttpHeaders, HttpMethod.GET);
			System.out.println(result);
	//		<input type="hidden" id="RSAModulus"  value='<c:out value="${_RSAModules}"/>' />
	//		<input type="hidden" id="RSAExponent" value='<c:out value="${_RSAExponent}"/>' />
			String rsaModuleId = this.constants.getRsaModuleId();
			String rsaExponentId = this.constants.getRsaExponentId();
			String rsaPublicId = this.constants.getRsaPublicId();

			int start = 0;
			int end = 0;
			String rsaModuleKey = null;
			if(!StringUtils.isEmpty(rsaModuleId)) {
				rsaModuleKey = result.substring(0, result.indexOf(rsaModuleId));
				start = rsaModuleKey.lastIndexOf("<");
				end = result.indexOf(rsaModuleId);
				rsaModuleKey = result.substring(end);
				end += rsaModuleKey.indexOf(">");
				rsaModuleKey = result.substring(start, end).replaceAll(" ", "");
				rsaModuleKey = rsaModuleKey.substring(rsaModuleKey.indexOf("value=")+6);
				if(rsaModuleKey.indexOf("'")>0)
					rsaModuleKey = rsaModuleKey.substring(0, rsaModuleKey.indexOf("'"));
				if(rsaModuleKey.indexOf("\"")>0)
					rsaModuleKey = rsaModuleKey.substring(0, rsaModuleKey.indexOf("\""));
				System.out.println("추출한 RSA_MODULE_KEY = " + rsaModuleKey);
			}
			
			String rsaExponentKey = null;
			if(!StringUtils.isEmpty(rsaExponentId)) {
				rsaExponentKey = result.substring(0, result.indexOf(rsaExponentId));
				start = rsaExponentKey.lastIndexOf("<");
				end = result.indexOf(rsaExponentId);
				rsaExponentKey = result.substring(end);
				end += rsaExponentKey.indexOf(">");
				rsaExponentKey = result.substring(start, end).replaceAll(" ", "");
				rsaExponentKey = rsaExponentKey.substring(rsaExponentKey.indexOf("value=")+6);
				if(rsaExponentKey.indexOf("'")>0)
					rsaExponentKey = rsaExponentKey.substring(0, rsaExponentKey.indexOf("'"));
				if(rsaExponentKey.indexOf("\"")>0)
					rsaExponentKey = rsaExponentKey.substring(0, rsaExponentKey.indexOf("\""));
				System.out.println("추출한 RSA_EXPONENT_KEY = " + rsaExponentKey);
			}
			
			String publicKeyStr = null;
			if(!StringUtils.isEmpty(rsaPublicId)) {
				publicKeyStr = result.substring(0, result.indexOf(rsaPublicId));
				start = publicKeyStr.lastIndexOf("<");
				end = result.indexOf(rsaPublicId);
				publicKeyStr = result.substring(end);
				end += publicKeyStr.indexOf(">");
				publicKeyStr = result.substring(start, end).replaceAll(" ", "");
				publicKeyStr = publicKeyStr.substring(publicKeyStr.indexOf("value=")+6);
				if(publicKeyStr.indexOf("'")>0)
					publicKeyStr = publicKeyStr.substring(0, publicKeyStr.indexOf("'"));
				if(publicKeyStr.indexOf("\"")>0)
					publicKeyStr = publicKeyStr.substring(0, publicKeyStr.indexOf("\""));
				System.out.println("추출한 RSA_PUBLIC_KEY = " + publicKeyStr);
			}
			
			String charset = "UTF-8";
			
			/*********** 로그인을 위한 RSA 암호화 처리 ************/
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			PublicKey publicKey = null;
			if(!StringUtils.isEmpty(publicKeyStr)) {
				publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyStr.getBytes())));
			} else {
				publicKey = keyFactory.generatePublic(new RSAPublicKeySpec(new BigInteger(rsaModuleKey, 16), new BigInteger(rsaExponentKey, 16)));
			}
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			// 공개키 이용 암호화
			byte[] bCipher = cipher.doFinal(loginPassword.getBytes(charset));
			loginPassword = Base64.getEncoder().encodeToString(bCipher);
			if(this.constants.isRsaLoginId()) {
				bCipher = cipher.doFinal(loginId.getBytes(charset));
				loginId = Base64.getEncoder().encodeToString(bCipher);
			}
		}

		if(!StringUtils.isEmpty(loginId) && !StringUtils.isEmpty(loginPassword)) {
			/*********** 로그인 처리 *************/
	//		String cookie = "JSESSIONID=Jj8d8pUkU5YYN7gVzm6BZzTY.biz11; NSSO_CentralAuth=4b64f6691c3f6a4cbd13f48e3b96bef1dd4ae16807ad29d87c300972497249adf073d8e8b8a85f46dddd49c3020875ea460060f37e29b1469e1cdd598e25564e590c6f91cda0c7c178401c1f22545ee7; NSSO_DomainInfo_kt_com=agencynm%3d615d4936bef356e5%2cdepartment%3d615d4936bef356e5%2cdeptcd%3d754ee8914e6397fb%2chandphoneno%3d46cd230c27291259fb41303db22be0a0%2cnewuserid%3d9f836bb751a6fdb8d25bcdf1072bec86%2colduserid%3d9f836bb751a6fdb8d25bcdf1072bec86%2cusermail%3d9f836bb751a6fdb822a131b56d2be1ed8378307cfd51a9cf%2cusername%3de0efb10f74cda04a; KTSSOKey=2ec095b6a62d57c357b32f9ffc669327; KTSSOUserID=dddc009724ad5e53f54a8a4a12dd26de; gwPermKey=; nssoauthdomain=9131bfcdfa6014f274d288ce12e28d22; s_fid=5CB7C44FB05AB8F4-0AEE6FF833439F69; strCode=Web; NSSO_DomainAuth_kt_com=1d66ce763a20bb14d8fce3726454f3c4bbf84c7e1af0508010bf36c5d58aa6d2aeb58eb3a20d123630f93e4550a652e90e82d0b5f1a61dd7be573d48469b019844bc7b6d4cfeb4ea; fileDownload=true";
			String loginProcessUrl = this.constants.getLoginProcessUrl();
			String loginParams = this.constants.getLoginProcessParams(); // otpChkYn=N&userId=${login.id}&password=${login.password}
			loginParams = loginParams.replaceFirst("${login.id}", loginId).replaceFirst("${login.password}", loginPassword);
	//		RestTemplateInterceptor restTemplateInterceptor = new RestTemplateInterceptor(cookie);
			String loginResult = loginTest(loginProcessUrl, loginParams, restTemplateInterceptor);
			System.out.println(loginResult);
	
			this.httpHeaders = restTemplateInterceptor.getHttpHeaders();
		}

		httpHeaders.set("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
		result = runTest("http://dev.biznaru.kt.com/om/basMgt/codeMgt/getCode", "page=1&take=20&rows=20&cdGroupId=&cdId=&cdNm=", this.httpHeaders);
		System.out.println(result);

		httpHeaders.set("Content-Type", "application/json; charset=UTF-8");
		result = runTest("http://dev.biznaru.kt.com/om/basMgt/codeMgt/getCode", "{\"page\":1, \"take\":20, \"rows\":20, \"cdGroupId\":, \"cdId\":, \"cdNm\":", this.httpHeaders);
		System.out.println(result);

		httpHeaders.set("Content-Type", "application/json; charset=UTF-8");
		result = runTest("http://dev.biznaru.kt.com/om/common/myInfo", "", this.httpHeaders, HttpMethod.GET);
		System.out.println(result);
		
		
		
		
		// ServiceBus테스트용 헤드(로그인 필요없음)
		httpHeaders.set("Content-Type", "application/json;charset=UTF-8");
		httpHeaders.set("Accept", "application/json;charset=UTF-8");
		httpHeaders.set("authorization", "Basic ZG9tYWluX3VzZXI6ZG9tYWluX3VzZXI=");
		httpHeaders.remove(HttpHeaders.COOKIE);


		final String url276 = "https://tb.portal.biznaru.kt.com/ServiceBus/biznaru/shub/sb276.json";
		final String url149 = "https://tb.portal.biznaru.kt.com/ServiceBus/biznaru/shub/sb149.json";
		final String params = "{\r\n" + 
				"	\"recvCtn\":\"01011112222\",\r\n" + 
				"	\"content\":\"비즈나루 통합 SMS 단문1\"\r\n" + 
				"}";

		for (long i = 0; i < totalTestCount; i++) {
			Runnable runnable = new Runnable() {@Override public void run() {
				long totCnt = ++totalCount;
				StringBuffer result = new StringBuffer();
				try {
					String threadName = Thread.currentThread().getName();
					addTheadCount(threadName);
					result.append("Start[").append(timeFormat.format(new Date())).append("] | End[] | ").append("Thread[").append(threadName).append("] | ");
					if(totCnt%3!=0) {
						result.append(runTest(url149, params, httpHeaders));
					}else {
						result.append(runTest(url276, params, httpHeaders));
					}
					result.insert(0, "] : ").insert(0, errorCount).insert(0, "], 실패[").insert(0, totalSuccCount).insert(0, " : 성공[").insert(0, totCnt)
							.insert(result.indexOf("End")+4, timeFormat.format(new Date()));
					addLog(result.toString() + "\n");
					if(totCnt%1000==0) {
						System.out.println(result.toString());
					}
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					if(totCnt>=totalTestCount) {
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
						try {try {Thread.sleep(10* 1000);} catch (InterruptedException e) {} addLogFlush(result.toString());logFileClose();} catch (IOException e) {}
						System.out.println(result.toString());
						System.exit(0);
					}
				}
				try {Thread.sleep(1000);} catch (InterruptedException e) {}
				}};
			executor.execute(runnable);
			// try {Thread.sleep(100);} catch (InterruptedException e) {}
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

	public String runTest(String url, String params, HttpHeaders httpHeaders) {
		return runTest(url, params, httpHeaders, HttpMethod.POST);
	}
	public String runTest(String url, String params, HttpHeaders httpHeaders, HttpMethod httpMethod) {
		
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
//		headers.add("Content-Type", "application/json;charset=UTF-8");
//		headers.add("Accept", "application/json;charset=UTF-8");
//		headers.add("authorization", "Basic ZG9tYWluX3VzZXI6ZG9tYWluX3VzZXI=");
		headers.add("Accept-Charset", "UTF-8");
//		
//		
//		headers.add("Host", "dev.biznaru.kt.com");
//		headers.add("Connection", "keep-alive");
		if(httpHeaders.get("Content-Type")==null || httpHeaders.get("Content-Type").size()<=0)
			httpHeaders.add("Content-Type", "application/json; charset=UTF-8");
		if(httpHeaders.get("Accept")==null || httpHeaders.get("Accept").size()<=0)
			headers.add("Accept", "*/*"); // headers.add("Accept", "application/json, text/javascript, */*; q=0.01");
//		headers.add("Origin", "http://dev.biznaru.kt.com");
//		headers.add("X-Requested-With", "XMLHttpRequest");
//		headers.add("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36");
//		headers.put("Content-Type", httpHeaders.get("Content-Type"));
//		headers.add("Referer", "http://dev.biznaru.kt.com/om/");
//		headers.add("Accept-Encoding", "gzip, deflate");
//		headers.add("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7,mt;q=0.6,fr;q=0.5");
//		headers.put("Cookie", httpHeaders.get(HttpHeaders.COOKIE)); // 진입전에 넣어준다.

		if (httpHeaders != null) {
			for (String key : httpHeaders.keySet()) {
				headers.put(key, httpHeaders.get(key));
			}
		}

		HttpEntity<Object> entity = new HttpEntity<Object>(params, headers);
		// entity.getHeaders().set("Accept-Charset", "UTF-8");;
		String response = null;
		try {
			switch (httpMethod) {
//			case GET:
//				response = restTemplate(false).getForObject(url, String.class, entity);
//				break;
			case POST:
				response = restTemplate(this.constants.isLogging()).postForObject(url, entity, String.class);
				break;
			default:
				ResponseEntity<String> result = restTemplate(this.constants.isLogging()).exchange(url, httpMethod, entity, String.class);
				response = result.getBody();
				break;
			}
			++totalSuccCount;
		} catch (Exception e) {
			++errorCount ;
			response = "Exception.ERROR ::: " + e.getMessage() + " | " + e;
//			e.printStackTrace();
		}
		return response;
	}

	public String loginTest(String url, String params, RestTemplateInterceptor restTemplateInterceptor) throws Exception {
		return loginTest(url, params, restTemplateInterceptor, HttpMethod.POST, null);
	}
	public String loginTest(String url, String params, RestTemplateInterceptor restTemplateInterceptor, HttpMethod httpMethod, HttpHeaders httpHeaders) throws Exception {
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
//		headers.add("Content-Type", "application/json;charset=UTF-8");
//		headers.add("Accept", "application/json;charset=UTF-8");
		// headers.add("authorization", "Basic ZG9tYWluX3VzZXI6ZG9tYWluX3VzZXI=");
		headers.add("Accept-Charset", "UTF-8");
		
		headers.add("Host", "dev.biznaru.kt.com");
		headers.add("Connection", "keep-alive");
//		if(httpHeaders.get("Content-Type")==null || httpHeaders.get("Content-Type").size()<=0)
//			httpHeaders.add("Content-Type", "application/json; charset=UTF-8");
//		if(httpHeaders.get("Accept")==null || httpHeaders.get("Accept").size()<=0)
//			headers.add("Accept", "application/json, text/javascript, */*; q=0.01");
		headers.add("Accept", "*/*");
		headers.add("Origin", "http://dev.biznaru.kt.com");
		headers.add("X-Requested-With", "XMLHttpRequest");
		headers.add("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36");
		headers.add("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
		headers.add("Referer", "http://dev.biznaru.kt.com/om/login");
		headers.add("Accept-Encoding", "gzip, deflate");
		headers.add("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7,mt;q=0.6,fr;q=0.5");
		headers.add("Cookie", restTemplateInterceptor.getCookie());
		// headers.add("Cookie", "");
		
		if (httpHeaders != null) {
			for (String key : httpHeaders.keySet()) {
				headers.put(key, httpHeaders.get(key));
			}
		}
		
		HttpEntity<Object> entity = new HttpEntity<Object>(params, headers);
		String response = null;
		try {
			switch (httpMethod) {
//			case GET:
//				response = restTemplateForLogin(restTemplateInterceptor).getForObject(url, String.class, entity);
//				break;
			case POST:
				response = restTemplateForLogin(restTemplateInterceptor).postForObject(url, entity, String.class);
				break;
			default:
				ResponseEntity<String> result = restTemplateForLogin(restTemplateInterceptor).exchange(url, httpMethod, entity, String.class);
				response = result.getBody();
				break;
			}
		} catch (Exception e) {
			response = "Exception.ERROR for LOGIN ::: " + e.getMessage() + " | " + e;
			throw new Exception(e);
		}
		return response;
	}


}
