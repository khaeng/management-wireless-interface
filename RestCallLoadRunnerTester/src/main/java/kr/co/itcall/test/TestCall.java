package kr.co.itcall.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
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
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

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

	private static final String resultStrFirstCall = null;
	private static boolean isFailedToStopInMulti = true;

	public TestCall(Constants constants) throws IOException {
		super(constants);
	}

	public static String choiceAndRunTestCaseConfFile(String inputUserData) throws IOException {
		String testMultiCount = "";
		String testFileName = "";
		String log="";
		BufferedReader br = null;
		List<File> testConfFiles = new ArrayList<File>();
		System.out.println("================= 테스트 환경파일 리스트를 출력합니다. ==============");
		System.out.println("--- Num - : ----- 테스트 환경파일명 -----------------------------------");
		System.out.println("--------------------------------------------------------------------------");
		Arrays.asList(new File("./conf/").listFiles()) // .stream().filter(filePath -> filePath.exists()&&filePath.isFile()&&filePath.canRead()&&filePath.getName().endsWith(".conf"))
				.forEach(filePath -> {
					if(filePath.exists()&&filePath.isFile()&&filePath.canRead()&&filePath.getName().endsWith(".conf")) {
						testConfFiles.add(filePath);
						System.out.println(String.format("  [ %3d ] : %s", testConfFiles.size(), filePath.getPath().split("conf")[1]));
		}}); /***** ^^^윗쪽^^^은 steam을 이용한 람다표현식이며, vvv아래vvv;는 일반표현식. 일반표현식은 Exception을 부모로 넘길 수 있지만, 람다는 불가능함. ****/
//		for (File filePath : new File("./conf/").listFiles()) {
//			if(filePath.exists()&&filePath.isFile()&&filePath.canRead()&&filePath.getName().endsWith(".conf")) {
//				testConfFiles.add(filePath);
//				System.out.println(testConfFiles.size() + " : " + filePath.getPath());
//			}
//		}
		System.out.println("===========================================================================");
		System.out.print  ("출력된 파일번호를 입력하시면 해당 테스트 파일로 테스트를 진행합니다.\n선택할 파일번호는 콤마(,) / And(&) 또는 하이픈(-)으로 범위 선택가능(중복으로 사용불가)\n콤마(,)와 하이픈(-)은 순차실행하며, And(&)는 병렬(동시)실행합니다.\n### 입력라인 마지막에 *와 숫자 입력 시 선택된 전체 테스트를 지정한 숫자만큼 병렬(동시)실행합니다.\n >>> : ");
		if(StringUtils.isEmpty(inputUserData)) {
			br = new BufferedReader(new InputStreamReader(System.in));
			inputUserData = br.readLine().trim();
			// br.reset();
		} else {
			System.out.println("사용자 자동입력 인수 : " + inputUserData);
		}
		if(inputUserData.equalsIgnoreCase("all")) {
			inputUserData = "1-" + testConfFiles.size();
		}
		int userSelectedIndex = -1;
		
		// 선택된 테스트(들)을 몇번 동시실행할지 여부(중복실행 개수)
		if(inputUserData.split("[*]",2).length==2) {
			testMultiCount = "*" + Integer.parseInt(inputUserData.split("[*]",2)[1].trim());
			inputUserData = inputUserData.split("[*]",2)[0].trim();
		}
		
		if(inputUserData.contains("&")) { // 다중선택을 병렬(동시)실행한다.
			System.out.println("===========================================================================");
			for (String selectedFileName : inputUserData.split("&")) {
				userSelectedIndex = Integer.parseInt(selectedFileName.trim());
				selectedFileName = testConfFiles.get(userSelectedIndex-1).getPath();
				File choiceFile = new File(selectedFileName);
				if(!choiceFile.canRead() || !choiceFile.isFile()) {
					throw new IOException("정상적인 파일을 선택하지 않았거나, 파일을 읽을 수 없습니다.[num:"+userSelectedIndex+", name:" + selectedFileName + "]");
				}
				log += userSelectedIndex + " : " + selectedFileName +"\n\t";
				testFileName += selectedFileName + testMultiCount +"&";
			}
			testFileName = testFileName.substring(0, testFileName.lastIndexOf("&"));
			System.out.println("\t" + log + "\n::: " + inputUserData.split("&").length + "개의 파일들을 동시실행(&)으로 선택했습니다. 테스트를 진행합니다.");
			System.out.println("===========================================================================");
		} else if(inputUserData.contains(",")) { // 다중선택을 순차적으로 실행한다.
				System.out.println("===========================================================================");
				for (String selectedFileName : inputUserData.split(",")) {
					userSelectedIndex = Integer.parseInt(selectedFileName.trim());
					selectedFileName = testConfFiles.get(userSelectedIndex-1).getPath();
					File choiceFile = new File(selectedFileName);
					if(!choiceFile.canRead() || !choiceFile.isFile()) {
						throw new IOException("정상적인 파일을 선택하지 않았거나, 파일을 읽을 수 없습니다.[num:"+userSelectedIndex+", name:" + selectedFileName + "]");
					}
					log += userSelectedIndex + " : " + selectedFileName +"\n\t";
					testFileName += selectedFileName + testMultiCount +",";
				}
				testFileName = testFileName.substring(0, testFileName.lastIndexOf(","));
				System.out.println("\t" + log + "\n::: " + inputUserData.split(",").length + "개의 파일들을 순차실행(,)으로 선택했습니다. 테스트를 진행합니다.");
				System.out.println("===========================================================================");
		} else if(inputUserData.contains("-")) {
			System.out.println("===========================================================================");
			int start = Integer.parseInt(inputUserData.split("-", 2)[0].trim());
			int end = Integer.parseInt(inputUserData.split("-", 2)[1].trim());
			for (int i=start; i <= end; i++) {
				String selectedFileName = testConfFiles.get(i-1).getPath();
				File choiceFile = new File(selectedFileName);
				if(!choiceFile.canRead() || !choiceFile.isFile()) {
					throw new IOException("정상적인 파일을 선택하지 않았거나, 파일을 읽을 수 없습니다.[num:"+i+", name:" + selectedFileName + "]");
				}
				log += i + " : " + selectedFileName +"\n\t";
				testFileName += selectedFileName + testMultiCount +",";
			}
			testFileName = testFileName.substring(0, testFileName.lastIndexOf(","));
			System.out.println("\t" + log + "\n::: " + (end-start+1) + "개의 파일들을 선택했습니다. 테스트를 진행합니다.");
			System.out.println("===========================================================================");
		} else {
			userSelectedIndex = Integer.parseInt(inputUserData.trim());
			testFileName = testConfFiles.get(userSelectedIndex-1).getPath();
			System.out.println("===========================================================================");
			System.out.println("\t[" + testFileName + "] 파일을 선택했습니다. 테스트를 진행합니다.");
			System.out.println("===========================================================================");
			File choiceFile = new File(testFileName);
			if(!choiceFile.canRead() || !choiceFile.isFile()) {
				throw new IOException("정상적인 파일을 선택하지 않았거나, 파일을 읽을 수 없습니다.[" + testFileName + "]");
			}
			testFileName += testMultiCount;
		}
		return testFileName;
	}

	public static String runTestMain(String fileName) throws Exception {
		int testMultiCount = 0;
		if(fileName.contains("&")) {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			System.out.print("\n병렬테스트 중 하나라도 실패하면 테스트를 멈출까요?(기본값 N : Y/N) : ");
			String inputUserData = br.readLine().trim();
			isFailedToStopInMulti = (""+inputUserData).toUpperCase().contains("Y");
			System.out.println("\n병렬테스트 중 실패 시 멈출지 여부는 " + isFailedToStopInMulti + " 입니다.");
			br.close();
			final List<String> fileList = Arrays.asList(fileName.split("&"));
			multiTestFileCount = fileList.size();
			final TestCall[] testCalls = new TestCall[multiTestFileCount];
			final Thread[] threads = new Thread[multiTestFileCount];
			long startTestTime = System.currentTimeMillis();
			for (int i=0; i<multiTestFileCount; i++) {
				final int testIndex = i;
				// 선택된 테스트(들)을 몇번 동시실행할지 여부(중복실행 개수)
				if(fileList.get(testIndex).split("[*]",2).length==2) {
					testMultiCount = Integer.parseInt(fileList.get(testIndex).split("[*]",2)[1].trim());
					fileList.set(testIndex, fileList.get(testIndex).split("[*]",2)[0].trim());
				}
				final int testMultiCountFinal = testMultiCount;
				threads[i] = new Thread(() -> {
					try {
						testCalls[testIndex] = new TestCall(new Constants(fileList.get(testIndex), testMultiCountFinal, multiTestFileCount));
						testCalls[testIndex].runTest(testMultiCountFinal);
						if(testCalls[testIndex].isExitApp) {
							throw new Exception("테스트 파일[" + fileList.get(testIndex) + "] 수행 중 에러/예상결과가 도출되지 않아 임의로 종료합니다.");
						}
					}catch (Exception e) {
						e.printStackTrace();
					}
				});
				threads[i].start();
			}
			for (Thread thread : threads) {
				thread.join(); // 모든 테스트가 종료될때까지 기다린다.
			}
			long endTestTime = System.currentTimeMillis();
			long timeGap = endTestTime - startTestTime;
			StringBuffer result = new StringBuffer();
			result.append("\n============================================================================\n")
			.append("설정 테스트 개수 : ").append(totalTestCount * multiTestFileCount).append("\n")
			.append("[순서 / 무작위] 호출 여부 : ").append(isLoopRelayTest ? "순차 처리" : "무작위 처리(동시호출과 Active프로세스는 2배차이남.)").append("\n")
			.append("동일파일 내 동시(중복) 호출 개수 : ").append(totalMultiConnector).append("\n")
			.append("다중선택한 Test파일 개수 : ").append(multiTestFileCount).append("\n")
			.append("병렬(동시) 호출 전체 개수 : ").append(totalMultiConnector * multiTestFileCount).append("\n")
			.append("한 개 호출그룹에 속한 호출 개수 : ").append(loopTestCount).append("\n")
			.append("정상 종료시 계획된 전체 호출 개수 : ").append(totalMultiConnector * totalTestCount * loopTestCount * multiTestFileCount).append("\n")
			.append("\n")
			.append("---------- 아래 카운트는 그룹별이 아닌 전체 호출에 대한 카운트 임. -------------").append("\n")
			.append("\n")
			.append("사용 Thread 개수 : ").append(countOfThead.size() * multiTestFileCount).append("\n")
			.append("전체 테스트 개수 : ").append(totalProcessCount).append("\n")
			.append("호출 테스트 성공 : ").append(totalSuccCount).append("\n")
			.append("호출 테스트 실패 : ").append(errorCount).append("\n")
			.append("시스템 에러 개수 : ").append(systemErrorCount).append("\n")
			.append("실패 시 재확인 수행된 호출 개수 : ").append(totalProcessCount - totalMultiConnector * totalTestCount * loopTestCount * multiTestFileCount).append(" (마이너스 값은 중간에러에 의한 종료 시 수행되지 못한 개수)").append("\n")
			.append("   (에러 시 별도수행 설정된 경우 수행되며, 기본수행은 성공으로 셋팅하고 별도수행은 호출결과에 따른다)").append("\n")
			.append("   (성공과 실패는 테스터의 단순 호출에 대한 실패카운트이며, 성공내에서 실패된 서비스는 별도 로그를 체크해야 합니다.)").append("\n")
			.append("\n")
			.append("테스트 시작시각 : ").append(dateTimeViewFormat.format(new Date(startTestTime))).append("\n")
			.append("테스트 종료시각 : ").append(dateTimeViewFormat.format(new Date(endTestTime))).append("\n")
			.append("수행 시각(MS) : ").append(String.format("%,d(ms)", timeGap)).append("\n")
			.append("수행 시각(TM) : ").append(String.format("%02d:%02d:%02d.%03d", (timeGap/(60*60*1000))%24, (timeGap/(60*1000))%60, (timeGap/(1000))%60, timeGap%1000)).append("\n")
			.append("초당 처리개수(TPS) : ").append(String.format("%,.2f(Tps)", (float)totalProcessCount/((endTestTime - startTestTime)/1000))).append("\n")
			.append("병렬(동시) 실행 테스트파일 개수 : ").append(multiTestFileCount).append("\n")
			.append("병렬(동시) 실행 테스트파일 리스트======================================================").append("\n").append(fileList.toString().replaceAll(",", "\n"));
			return "병렬(동시) 실행 테스트 전체 결과" + result.toString();
		} else {
			// 선택된 테스트(들)을 몇번 동시실행할지 여부(중복실행 개수)
			if(fileName.split("[*]",2).length==2) {
				testMultiCount = Integer.parseInt(fileName.split("[*]",2)[1].trim());
				fileName = fileName.split("[*]",2)[0].trim();
			}
			TestCall testCall = null;
			testCall = new TestCall(new Constants(fileName, testMultiCount));
			testCall.runTest(testMultiCount);
			if(testCall.isExitApp) {
				throw new Exception("테스트 파일[" + fileName + "] 수행 중 에러/예상결과가 도출되지 않아 임의로 종료합니다.");
			}
			return "테스트 파일[" + fileName + "] : 성공";
		}
	}
	public static void main(String[] args) throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
//		System.out.println(encryptRsaBase64("new1234!", "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjQ4H3EW26Kvvdmo6/Ttsu16mN+mf5Y2T8gox5KLhpLnG6y8uX+W6fPfWnhzwt95vNieMrXmw92Xgm8RKFeHnUHMzWabFxeKv4slky6pbLq/TMbITAXVwdZVdfF2QDjU3HTJUmvgO79t782y6s5Be3js8cIX5eSKnWXSBUq6zZRmTHwpd0Y7ejCy/1JHPi05i9hBPEpAs31xh3CU/bUhdc86+nQeeFf8vGQ+xtLjq2085pK/s6WNnrqHwjQw7rw0TLSLM6m8TuquJfrBIkh2Rwy/Xx8MihlCOxXJcQFd24BnJab3RIolOeaqfEg7j+gYlfjW0jaS6gvsTXujNqyGEXwIDAQAB", Charset.forName("UTF-8")));
//		if(true)
//			return;
//		System.out.println("asdfsadf sdfasdf".replaceAll(" ", "+"));
//		String queryData = "/*** 회원등록을 수행하기전에 BIZNARU.DB에 존재하는 회원정보를 삭제해야 정상적인 등록과정을 수행할 수 있다. ***/DELETE FROM MB_CUST_ACC_BAS WHERE ACC_ID = '${test.val.acc.id}' ; SELECT * FROM MB_CUST_ACC_BAS WHERE ACC_ID = '${test.val.acc.id}'";
//		String result = clearRemarkStr(queryData, "/*", "*/", 0);
//		System.out.println(result);
//		if(!StringUtils.isEmpty(result))return;
		try {
			if(args!=null && args.length>0 && args[0].equalsIgnoreCase("start")) { 
				if(args!=null && args.length>1 && new File(args[1]).isFile()) {
					System.out.println("==================================================================\n\t" + runTestMain(args[1]) + "\n==================================================================");
				} else {
					String testResult = "";
					String[] callTargetList = choiceAndRunTestCaseConfFile(args.length>2?args[1]:"").split(",");
					long timeToStartMillseconds = System.currentTimeMillis();
					for (String callTarget : callTargetList) {
						testResult += runTestMain(callTarget) + "\n\t";
					}
					System.out.println("\n\n");
					System.out.println("☆★☆★☆★☆★☆★☆★☆★☆★☆★☆★☆★☆★☆★☆★☆★☆★☆★☆★☆★☆★☆★☆★☆★☆★☆★☆★☆★☆★");
					System.out.println("============================================================================\n\t" + testResult.substring(0, testResult.length()-2) + "\n============================================================================");
					long timeToEndMillseconds = System.currentTimeMillis();
					long timeGap = timeToEndMillseconds - timeToStartMillseconds;
					
					if(callTargetList.length>1) {
						System.out.println(String.format("\t전체테스트 시작시각 : %s\n\t전체테스트 종료시각 : %s\n\t전체수행 시각(MS) : %,d(ms)\n\t전체수행 시각(TM) : %02d:%02d:%02d.%03d", dateTimeFormat.format(new Date(timeToStartMillseconds)), dateTimeFormat.format(new Date(timeToEndMillseconds)), timeGap, (timeGap/(60*60*1000))%24, (timeGap/(60*1000))%60, (timeGap/(1000))%60, timeGap%1000));
					}
				}
				System.out.println("☆★☆★☆★☆★ 모든 테스트가 성공했습니다. [Succeed in your All Test Process.] ☆★☆★☆★☆★");
				System.out.println("☆★☆★☆★☆★☆★☆★☆★☆★☆★☆★☆★☆★☆★☆★☆★☆★☆★☆★☆★☆★☆★☆★☆★☆★☆★☆★☆★☆★");
			}else if(args!=null && args.length>0 && args[0].equalsIgnoreCase("stop")) {
				String hostAddr = "localhost";
				if(args.length>2)
					hostAddr = args[2];
				if(args!=null && args.length>1) {
					new TestCall(new Constants(args[1], 0)).stopTest(hostAddr);
				} else {
					String[] callTargetList = choiceAndRunTestCaseConfFile(args.length>2?args[1]:"").split(",");
					for (String callTarget : callTargetList) {
						new TestCall(new Constants(callTarget, 0)).stopTest(hostAddr);
					}
				}
			}else {
				String guide = "Usage : run APP with parameters : [start/stop] [test-configuration-file] [host-addr]\r\n" +
						"	required paramters : start or stop.\r\n" + 
						"	[host-addr] is only stop case.\r\n" + 
						"	";
				System.out.println(guide);
			}
		} catch (Exception e) {
			e.printStackTrace();
			String guide = "\n" + "Usage : run APP with parameters : [start/stop] [test-configuration-file] [host-addr]\r\n" +
					"\trequired paramters : start or stop.\r\n" + 
					"\t[host-addr] is only stop case.\r\n" +
					"\r\n[테스트 시 에러발생]" + 
					String.format("\r\n\t☆★○ message[%s]\r\n\t☆★○ cause[%s]\r\n\t☆★○ localMsg[%s]", e.getMessage(), e.getCause(), e.getLocalizedMessage()) +
					"\r\n";
			System.out.println(guide);
		}
		System.exit(0); // 정상종료를 위한 호출.
	}

	private HttpHeaders httpHeaders = new HttpHeaders();
	private RestTemplateInterceptor restTemplateInterceptor;
	public HttpHeaders getHttpHeaders() {
		return httpHeaders;
	}
	public RestTemplateInterceptor getRestTemplateInterceptor() {
		return restTemplateInterceptor;
	}

	public void runTest(int testMultiCount) throws Exception {
		
		initialize(testMultiCount);
		
		String resultStr = "";
		
		/****************************************************************
		 * 로그인 선행.
		 ****************************************************************/

		// this.cookie = this.constants.getCookie();
		String loginId = this.constants.getLoginId();
		String loginPassword = this.constants.getPassword();
		Map<String, Object> convertBeforeResultMap = null;
		if(!StringUtils.isEmpty(loginId) && !StringUtils.isEmpty(loginPassword) && this.constants.isRsaLoginPassword()) {
			String loginPageUrl = this.constants.getLoginPageUrl();
			String loginParams = this.constants.getLoginPageParams();
			HttpMethod loginPageHttpMethod = this.constants.getRsaHttpMethod();
			HttpHeaders loginPageHttpHeaders = this.constants.getLoginPageHeaderInfo(this.httpHeaders, null, null); // 최초 호출이므로 사전호출정보나, 저장한정보가 없음.
			restTemplateInterceptor = new RestTemplateInterceptor(loginPageHttpHeaders, this.constants.isLogging());
			resultStr = loginTest(0, loginPageUrl, loginParams, restTemplateInterceptor, loginPageHttpMethod, loginPageHttpHeaders);
			this.httpHeaders = restTemplateInterceptor.getHttpHeaders();
			// String result = runTest(loginPageUrl, "", preHttpHeaders, HttpMethod.GET);
			System.out.println(resultStr);
	//		<input type="hidden" id="RSAModulus"  value='<c:out value="${_RSAModules}"/>' />
	//		<input type="hidden" id="RSAExponent" value='<c:out value="${_RSAExponent}"/>' />
			convertBeforeResultMap = switchResult(resultStr, constants.getTestCharset("login.page.charset"));
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
				rsaParams = switchParams("", -1, rsaParams, null, convertBeforeResultMap, null, this.constants, null); // 일차 수신데이터에서 변경.
				HttpHeaders rsaHttpHeaders = this.constants.getRsaHeaderInfo(this.httpHeaders, convertBeforeResultMap, null);
				HttpMethod rsaHttpMethod = this.constants.getRsaHttpMethod();
				if(restTemplateInterceptor==null) {
					restTemplateInterceptor = new RestTemplateInterceptor(rsaHttpHeaders, this.constants.isLogging());
				} else {
					restTemplateInterceptor.setHttpHeaders(rsaHttpHeaders);
				}
				resultStr = loginTest(0, rsaUrl, rsaParams, restTemplateInterceptor, rsaHttpMethod, rsaHttpHeaders);
				System.out.println(resultStr);
				Map<String,Object> rsaResult = switchResult(resultStr, constants.getTestCharset("login.rsa.charset")); // switchResult(resultStr);
				if(!StringUtils.isEmpty(rsaResult)) {
					convertBeforeResultMap.putAll(rsaResult);
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
			loginParams = switchParams("", -1, loginParams, null, convertBeforeResultMap, null, this.constants, convertBeforeResultMap); // 일차 수신데이터에서 변경.
	//		RestTemplateInterceptor restTemplateInterceptor = new RestTemplateInterceptor(cookie);
			HttpHeaders loginHttpHeaders = this.constants.getLoginProcessHeaderInfo(this.httpHeaders, convertBeforeResultMap, convertBeforeResultMap);
			HttpMethod loginProcessHttpMethod = this.constants.getLoginProcessHttpMethod();
			if(restTemplateInterceptor==null) {
				restTemplateInterceptor = new RestTemplateInterceptor(loginHttpHeaders, this.constants.isLogging());
			} else {
				restTemplateInterceptor.setHttpHeaders(loginHttpHeaders);
			}
			String loginResult = loginTest(0, loginProcessUrl, loginParams, restTemplateInterceptor, loginProcessHttpMethod, loginHttpHeaders);
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
				.append("테스트 설정에 대한 반복개수 : ").append(totalTestCount).append("\n")
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

		final boolean isStopWhenWeMeetFail = constants.isStopFailed() && isFailedToStopInMulti;
		/* TEST.LOOP.START */
		if(isLoopRelayTest) {
			Thread[] arrThread = new Thread[totalMultiConnector];
			for (int i = 0; i < arrThread.length; i++) {
				Runnable runnable = () -> {
					long totalTermDoneCount = 0;
					TestDataInfo testDataInfo = null;
					while (totalTestCount>totalTermDoneCount) {
						if (isExitApp/* || totalProcessCount>=totalTestCount */)
							break;
						++totalTermDoneCount;
						if(constants.getSleepTimeBeforeGroup()>0)
							try {Thread.sleep(constants.getSleepTimeBeforeGroup());} catch (InterruptedException e) {}
						for (long j = 0; j < MAX_LOOP_AND_HEADER_COUNT; j++) {
							if(isStopWhenWeMeetFail && errorCount>0) {
								isExitApp = true;
								break;
							}
							if(!StringUtils.isEmpty(testDataInfo)) {
								testDataInfo.beforeCallProcess();
								testDataInfo = new TestDataInfo(testDataInfo, TestCall.this, constants, "", j, totalTermDoneCount, printLogTerm);
							} else {
								testDataInfo = new TestDataInfo(TestCall.this, constants, "", j, totalTermDoneCount, printLogTerm);
							}
							// resultStr = processToCallUrl(j, TestCall.this.httpHeaders, beforeResultMap, totalTermDoneCount, printLogTerm, mapKeepData, resultMapFirstCall);
							processToCallUrl(testDataInfo);
							
						}
					}
				};
				arrThread[i] = new Thread(runnable);
				arrThread[i].setName(String.format("TestCall-%05d", i+1));
				// executor.execute(runnable);
			}
			
			// Arrays.asList(arrThread).stream().filter(thread -> !StringUtils.isEmpty(thread)).forEach(thread -> thread.start());
			Arrays.asList(arrThread).stream().filter(thread -> thread!=null).forEach(thread -> thread.start());
			// Arrays.asList(arrThread).stream().filter(StringUtils::isEmpty).forEach(System.out::println); // 비어있는 경우만 출력된다.
//			for (Thread thread : arrThread) {
//				if(thread!=null) {
//					thread.start();
//				}
//			}
			// Arrays.asList(arrThread).stream().filter(thread -> thread!=null).forEach(thread -> thread.join()); // try...catch...를 부모로 넘길수없다.
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
				Runnable runnable = () -> {
					try {Thread.sleep(constants.getSleepTimeBeforeGroup());} catch (InterruptedException e) {}
					TestDataInfo testDataInfo = null;
					for (long j = 0; j < MAX_LOOP_AND_HEADER_COUNT; j++) {
						if(isStopWhenWeMeetFail && errorCount>0) {
							isExitApp = true;
							break;
						}
						if(!StringUtils.isEmpty(testDataInfo)) {
							testDataInfo.beforeCallProcess();
							testDataInfo = new TestDataInfo(TestCall.this, constants, "", j, totalTermDoneCount, printLogTerm);
						} else {
							testDataInfo = new TestDataInfo(testDataInfo, TestCall.this, constants, "", j, totalTermDoneCount, printLogTerm);
						}
						// resultStr = processToCallUrl(j, TestCall.this.httpHeaders, beforeResultMap, totalTermDoneCount, printLogTerm, mapKeepData, resultMapFirstCall);
						processToCallUrl(testDataInfo);
						
					}
				};
				try {
					executor.execute(runnable);
				}catch (Exception e) {
					ThreadPoolTaskExecutor exec  = ((ThreadPoolTaskExecutor)executor);
					System.out.println(exec.getPoolSize() + ", Queue.size() : " + exec.getThreadPoolExecutor().getQueue().size() + ", ActiveCount : " + exec.getActiveCount() + ", remainingCapacity : " + exec.getThreadPoolExecutor().getQueue().remainingCapacity());
					e.printStackTrace();
					System.exit(-1);
				}
				while (true) {
					try {Thread.sleep(1000);} catch (InterruptedException e) {}
					if(((ThreadPoolTaskExecutor)executor).getThreadPoolExecutor().getQueue().remainingCapacity() > this.constants.getExecutorQueueCapacity()/3)
						break;
				}
			}
			while (true) {
				try {Thread.sleep(1000);} catch (InterruptedException e) {}
				if(isExitApp || ((ThreadPoolTaskExecutor)executor).getActiveCount() <= 0)
					break;
			}
		}



		endOfWork();



	}



//	public String runTest(String postFix, long index, String url, String params, HttpHeaders httpHeaders, Map<String, Object> beforeResultMap, long totalTermDoneCount, int printLogTerm, Map<String,Object> mapKeepData, Map<String,Object> mapFirstCall) {
//		return runTest(postFix, index, url, params, httpHeaders, HttpMethod.POST, beforeResultMap, totalTermDoneCount, printLogTerm, mapKeepData, mapFirstCall);
//	}
//	public String runTest(String postFix, long index, String url, String params, HttpHeaders httpHeaders, HttpMethod httpMethod, Map<String, Object> beforeResultMap, long totalTermDoneCount, int printLogTerm, Map<String,Object> mapKeepData, Map<String,Object> mapFirstCall) {
	public String runTest(TestDataInfo testDataInfo, HttpHeaders httpHeaders) {
		String postFix = testDataInfo.getPostFix();
		long index = testDataInfo.getIndex();
		String url = testDataInfo.getUrl();
		String params = testDataInfo.getParams();
		HttpMethod httpMethod = testDataInfo.getMethod();
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
				mapParams.forEach((key, value) -> body.add(key, value));
//				for (String key : mapParams.keySet()) {
//					body.add(key, mapParams.get(key));
//				}
			}catch (Exception e) {
				Arrays.asList(params.split("&")).forEach(element -> {
					String key = element.split("=",2)[0];
					try {
						body.add(key, element.split("=",2)[1]);
					} catch (Exception ex) {
						body.add(key, "");
					}
				});
//				for (String element : params.split("&")) {
//						String key = element.split("=",2)[0];
//						try {
////							try {
////								Long.parseLong(element.split("=",2)[1]);
////								body.add(key, element.split("=",2)[1]);
////							}catch (Exception ex) {
////								try {
////									new Date(element.split("=",2)[1]);
//									body.add(key, element.split("=",2)[1]);
////								} catch (Exception e2) {
////									body.add(key, URLEncoder.encode(element.split("=",2)[1], "MS949"));
////								}
////							}
//						}catch (Exception ex) {
//							body.add(key, "");
//						}
//				}
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
			checkResultIsSuccess(testDataInfo, response);
			addSuccessCount();
		} catch (Exception e) {
			/***************************************
			 * 실패 시 실패카운트 전에 실패일경우
			 * 호출하는 케이스가 있다면...
			 * 실패시 호출케이스의 결과에 따라 결정한다.
			 **************************************/
			if(this.constants.isExistFailedProcess(index, postFix)) {
				addSuccessCount(); // 실패 시 호출이 존재하므로 현재 실패는 성공으로 간주한다. : 전체 카운트를 맞추기 위함.
				// return response = processToCallUrl(postFix+".failed", index, httpHeaders, beforeResultMap, totalTermDoneCount, printLogTerm, mapKeepData, mapFirstCall);
				TestDataInfo failToCallDataInfo = new TestDataInfo(this, testDataInfo, testDataInfo.getPostFix()+".failed");
				// testDataInfo.setPostFix(testDataInfo.getPostFix()+".failed");
				return response = String.format("::현재호출[%d%s] 실패시 추가호출 postFix[%s], 최종응답 : %s", index, postFix, failToCallDataInfo.getPostFix(), processToCallUrl(failToCallDataInfo).getResult().toString()); // (postFix+".failed", index, httpHeaders, beforeResultMap, totalTermDoneCount, printLogTerm, mapKeepData, mapFirstCall);
			}
			
			addErrorCount();
			if(e instanceof RestClientResponseException) {
				response = "Exception.ERROR ::: " + e.getMessage() + " | " + ((RestClientResponseException)e).getResponseBodyAsString();
			} else {
				response = "Exception.ERROR ::: " + e.getMessage() + " | " + e;
			}
			
		}
		return response;
	}

	private void checkResultIsSuccess(TestDataInfo testDataInfo, String response) throws Exception {
		// String postFix, long index, String response, Map<String, Object> mapKeepData, Map<String, Object> mapFirstCall, Map<String, Object> beforeResultMap;
		String postFix = testDataInfo.getPostFix();
		long index = testDataInfo.getIndex();
		Map<String, Object> beforeResultMap = testDataInfo.getBeforeResultMap();
		Map<String,Object> mapKeepData = testDataInfo.getMapKeepData();
		Map<String,Object> mapFirstCall = testDataInfo.getResultMapFirstCall();
		
		String resultLike = constants.getTestResultLike(index, postFix, mapKeepData, mapFirstCall, beforeResultMap);
		if(!StringUtils.isEmpty(resultLike)){
			int foundIndex;
			for (String likeCut : resultLike.split(",")) {
				String targetStr = response;
				if(!StringUtils.isEmpty(likeCut)) {
					foundIndex = 0;
					for (String like : likeCut.split("[*]")) {
						if(!StringUtils.isEmpty(like)) {
							boolean isNor = false;
							if(like.startsWith("!")) {
								isNor = true;
								like = like.substring(1);
								foundIndex = targetStr.indexOf(like);
								if(foundIndex>=0) {
									throw new Exception(String.format("통신은 성공이나 결과문자열에 에러일 경우 예상된 값[%s]이 존재하여 실패처리 되었습니다. 실패시 결과예상패턴[%s], 통신결과[%s]", like, resultLike, response));
								}
								targetStr = targetStr.substring(foundIndex+like.length());
							} else {
								foundIndex = targetStr.indexOf(like);
								if(foundIndex<0) {
									throw new Exception(String.format("통신은 성공이나 결과문자열에 예상된 값[%s]이 존재하지 않아 실패처리 되었습니다. 결과예상패턴[%s], 통신결과[%s]", like, resultLike, response));
								}
								targetStr = targetStr.substring(foundIndex+like.length());
							}
						}
					}
				}
			}
		}
	}

// 	public String runTest(String postFix, long index, String url, String params, RestTemplateInterceptor restTemplateInterceptor, HttpMethod httpMethod, HttpHeaders httpHeaders, Map<String, Object> beforeResultMap, long totalTermDoneCount, int printLogTerm, Map<String,Object> mapKeepData, Map<String,Object> mapFirstCall) {
	public String runTest(TestDataInfo testDataInfo, RestTemplateInterceptor restTemplateInterceptor, HttpHeaders httpHeaders) {
		String postFix = testDataInfo.getPostFix();
		long index = testDataInfo.getIndex();
		String url = testDataInfo.getUrl();
		String params = testDataInfo.getParams();
		HttpMethod httpMethod = testDataInfo.getMethod();
		
		String response;
		try {
			response = loginTest(index, url, params, restTemplateInterceptor, httpMethod, httpHeaders);
			checkResultIsSuccess(testDataInfo, response);
			addSuccessCount();
			return response;
		} catch (Exception e) {
			/***************************************
			 * 실패 시 실패카운트 전에 실패일경우
			 * 호출하는 케이스가 있다면...
			 * 실패시 호출케이스의 결과에 따라 결정한다.
			 **************************************/
			if(this.constants.isExistFailedProcess(index, postFix)) {
				addSuccessCount(); // 실패 시 호출이 존재하므로 현재 실패는 성공으로 간주한다. : 전체 카운트를 맞추기 위함.
				TestDataInfo failToCallDataInfo = new TestDataInfo(this, testDataInfo, testDataInfo.getPostFix()+".failed");
				// testDataInfo.setPostFix(testDataInfo.getPostFix()+".failed");
				// return response = processToCallUrl(failToCallDataInfo).getResult().toString(); // (postFix+".failed", index, httpHeaders, beforeResultMap, totalTermDoneCount, printLogTerm, mapKeepData, mapFirstCall);
				return response = String.format("::현재호출[%d%s] 실패시 추가호출 postFix[%s], 최종응답 : %s", index, postFix, failToCallDataInfo.getPostFix(), processToCallUrl(failToCallDataInfo).getResult().toString()); // (postFix+".failed", index, httpHeaders, beforeResultMap, totalTermDoneCount, printLogTerm, mapKeepData, mapFirstCall);
			}
			
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
			throw new Exception(String.format("Result call error[%s], index[%d], url[%s|%s], params[%s]", e.getMessage(), index, httpMethod.name(), url, params), e);
		}
		return response;
	}

	public static String clearRemarkStr(String data, String startRemark, String endRemark, int position) {
//		if(StringUtils.isEmpty(data)) return data;
//		// data=data.replaceAll("\r", "");
//		int start = data.indexOf(startRemark);
//		if(start>=0) {
//			if(StringUtils.isEmpty(endRemark)) {
//				int end = data.indexOf("\n", start);
//				if(start<end) {
//					if(data.lastIndexOf("\n", start)>=0){
//						return clearRemarkStr(data.substring(0, start) + (data.substring(data.lastIndexOf("\n", start)+1, start).trim()!="" ? "\n" : "") + data.substring((end+1)>data.length()?data.length():end+1), startRemark, endRemark, position);
//					}else{
//						return clearRemarkStr(data.substring(0, start) + /*(data.charAt(start-1)!="\n" ? "\n" : "") + */data.substring((end+1)>data.length()?data.length():end+1), startRemark, endRemark, position);
//					}
//				} else {
//					return data.substring(0, start);
//				}
//			} else {
//				int end = data.indexOf(endRemark, start+startRemark.length());
//				if(start<end){
//					return clearRemarkStr(data.substring(0, start) + data.substring(end+endRemark.length()), startRemark, endRemark, position);
//				} else {
//					return data.substring(0, start);
//				}
//			}
//		}
//		return data;
		
		
		if(StringUtils.isEmpty(data)) return data;
		// data=data.replace(/[\r]/g, ""); // 자바스크립트용.
		if(StringUtils.isEmpty(position)){
			position = 0;
		}
		int start = data.indexOf(startRemark, position);
		if(start>=0) {
			int startAt = data.lastIndexOf("\n", start)+1;
			int endAt = data.indexOf("\n", start);
			if(endAt<0)
				endAt = data.length();
			if(startAt>=0 && startAt<endAt && getWithoutConstStr(data.substring(startAt, endAt)).indexOf(startRemark)<0){
				return clearRemarkStr(data, startRemark, endRemark, start+startRemark.length());
			}

			if(StringUtils.isEmpty(endRemark)) {
				int end = data.indexOf("\n", start);
				if(start<end) {
//					if(data.lastIndexOf("\n", start)>=0){
						return clearRemarkStr(data.substring(0, start) + (data.substring(startAt, start).trim()!="" ? "\n" : "") + data.substring((end+1)>data.length()?data.length():end+1), startRemark, endRemark, position);
//					}else{
//						return libFrame._clearRemarkStr(data.substring(0, start) + /*(data.charAt(start-1)!="\n" ? "\n" : "") + */data.substring((end+1)>data.length?data.length:end+1), startRemark, endRemark, position);
//					}
				} else {
					return data.substring(0, start);
				}
			} else {
				int end = data.indexOf(endRemark, start+startRemark.length());
				startAt = data.lastIndexOf("\n", end)+1;
				endAt = data.indexOf("\n", end);
				if(endAt<0)
					endAt = data.length();
				while (start<end && startAt>=0 && startAt<endAt && getWithoutConstStr(data.substring(startAt, endAt)).indexOf(endRemark)<0) {
					end = data.indexOf(endRemark, endAt+1);
					startAt = data.lastIndexOf("\n", end)+1;
					endAt = data.indexOf("\n", end);
					if(endAt<0)
						endAt = data.length();
				}
				if(start<end){
					return clearRemarkStr(data.substring(0, start) + data.substring(end+endRemark.length()), startRemark, endRemark, position);
				} else {
					return data.substring(0, start);
				}
			}
		}
		return data;
	}
	public static String getWithoutConstStr(String row){
		// 상수로 선언된 데이터는 제외한다. 단일라인에서...
//		var startAt = data.lastIndexOf("\n", start)+1;
//		var endAt = data.indexOf("\n", start);
//		var row = data.substring(startAt, endAt);
		String keptRow = "";
//		boolean isSlush = false; // 자바스크립트용.
		boolean isBSlush = false;
		boolean isSConst = false;
		boolean isDConst = false;
		boolean isConst = false;
		for (int i = 0; i < row.length(); i++) {
//			if(!isConst && row.charAt(i)=='/')
//				isSlush = !isSlush;
			if(!isBSlush && row.charAt(i)=='\\'){
				isBSlush = true;
			}else if(isBSlush){
				isBSlush = false;
				continue;
			}
			if(isSConst && row.charAt(i)=='\''){
				isSConst = isConst = false;
				continue;
			}
			if(isDConst && row.charAt(i)=='"'){
				isDConst = isConst = false;
				continue;
			}
			if(isConst/* || isSlush*/) continue;

			if(row.charAt(i)=='\''){
				isSConst = isConst = true;
				continue;
			}
			if(row.charAt(i)=='"'){
				isDConst = isConst = true;
				continue;
			}
			keptRow+=row.charAt(i);
		}
		// console.log(keptRow);
		return keptRow;
	};
	public PreSqlInfo getPreSqlResult(String postFix, long index, Map<String, Object> beforeResultMap, Map<String,Object> mapKeepData, Map<String,Object> resultMapFirstCall) {
		PreSqlInfo preSqlInfo = new PreSqlInfo();
		List<Map<String, Object>> result = null;
		String dbKey = constants.getPropertyValue("test."+index+postFix+".db");
		String query = constants.getPropertyValue("test."+index+postFix+".query");
		if(StringUtils.isEmpty(dbKey) || StringUtils.isEmpty(query))
			return preSqlInfo;
		preSqlInfo.setPreSqlQuery(query);
		String dbName   = constants.getPropertyValue(dbKey+".name");
		String dbDriver = constants.getPropertyValue(dbKey+".driver");
		String dbUrl    = constants.getPropertyValue(dbKey+".url");
		String dbUserId = constants.getPropertyValue(dbKey+".username");
		String dbPwd    = constants.getPropertyValue(dbKey+".password");
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			
			query = switchParams(postFix, index, constants.switchParams(0, query, beforeResultMap), null, mapKeepData, resultMapFirstCall, constants, beforeResultMap);
			query = clearRemarkStr(query, "/*", "*/", 0);
			query = clearRemarkStr(query, "//", null, 0);
			query = clearRemarkStr(query, "--", null, 0);
			preSqlInfo.setRealSqlQuery(query);
			Class.forName(dbDriver);
			conn = DriverManager.getConnection(dbUrl, dbUserId, dbPwd);
			/************ 다중으로 입력되는 쿼리는 마지막 수행문만 취한다. ************/
			String[] runningQuerys = query.split(";");
			for (String eachQuery : runningQuerys) {
				if(StringUtils.isEmpty(eachQuery) && StringUtils.isEmpty(eachQuery.trim()))
					continue;
				pstmt = conn.prepareStatement(eachQuery);
				if(eachQuery.trim().toLowerCase().startsWith("insert") || eachQuery.trim().toLowerCase().startsWith("delete") || eachQuery.trim().toLowerCase().startsWith("update") || eachQuery.trim().toLowerCase().startsWith("merge")){
					System.out.println("test."+index+postFix+".query 수행절차값의 사전쿼리문 실행 : " + eachQuery);
					System.out.println("test."+index+postFix+".query 수행절차값의 사전쿼리문 결과 : " + pstmt.executeUpdate());
				} else {
					rs = pstmt.executeQuery();
				}
			}
			if(StringUtils.isEmpty(rs)) {
				return null;
			}
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
		preSqlInfo.setPreSqlResult(result);
		return preSqlInfo;
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

//	public TestDataInfo processToCallUrl(TestDataInfo testDataInfo) {
//		// (long index, HttpHeaders httpHeaderParams, Map<String, Object> beforeResultMap, long totalTermDoneCount, int printLogTerm, Map<String,Object> mapKeepData, Map<String,Object> resultMapFirstCall) {
//		// return processToCallUrl("", index, httpHeaderParams, beforeResultMap, totalTermDoneCount, printLogTerm, mapKeepData, resultMapFirstCall);
//		return processToCallUrl(testDataInfo);
//	}

	public TestDataInfo processToCallUrl
			(TestDataInfo testDataInfo) {
			// (String postFix, long index, HttpHeaders httpHeaderParams, Map<String, Object> beforeResultMap, long totalTermDoneCount, int printLogTerm, Map<String,Object> mapKeepData, Map<String,Object> resultMapFirstCall) {
//		String result = null;
//		String testName = constants.getTestNameInfo(index, postFix);
//		String threadName = Thread.currentThread().getName();
//		String url = constants.getTestUrlInfo(index, postFix, mapKeepData, resultMapFirstCall, beforeResultMap);
//		List<Map<String, Object>> preSqlResult = getPreSqlResult(postFix, index, beforeResultMap, mapKeepData, resultMapFirstCall);
//		String params = switchParams(postFix, index, constants.getTestParamsInfo(index, postFix, beforeResultMap), preSqlResult, mapKeepData, resultMapFirstCall, constants, beforeResultMap);
		if(!StringUtils.isEmpty(testDataInfo.getUrl())) {
			testDataInfo.waitBeforeTest();
			// StringBuffer sb = new StringBuffer();
			try {
				addTheadCount(testDataInfo.getThreadName());
				if(testDataInfo.isExistValueForPreSqlResult()) {
//					/**********************************************************************************************
//					 * 사전조회쿼리와 test.[num].sql.key에 해당하는 값이 존재하면 본 테스트는 건너뛴다.
//					 * 즉, 없을경우 등록하는 업무를 여기서 처리한다.
//					 */
//					addSuccessCount();
//					String prePassResult = String.format("%d : 성공[%d], 실패[%d] : Start[%s] | End[-cancel-] | Thread[%s] | Name[%s] | Url[%s] ||| 사전조건조회값[%s]이 존재하여 성공으로 처리하며, 수행하지는 않습니다. ||| 사전조건조회 쿼리결과를 다음인수로 전달합니다.\n"
//							, addTotalCount(), totalSuccCount, errorCount, timeFormat.format(new Date()), threadName, testName, url, constants.getPropertyValue("test." + index + postFix + ".sql.key", ""));
//					addLog(prePassResult);
//					if(constants.isLogging() || totalTermDoneCount%printLogTerm==0) {
//						System.out.print(prePassResult);
//					}
//					try {
//						result = objectMapper.writeValueAsString(preSqlResult);
//						addLog("\t사전조회결과값 : " + result + "\n");
//						if(constants.isLogging() || totalTermDoneCount%printLogTerm==0) {
//							System.out.println("\t사전조회결과값 : " + result);
//						}
//					}catch (JsonProcessingException e) {}
//					return result;
					return testDataInfo;
				}
				testDataInfo.doRunningTestCall();
//				HttpHeaders httpHeaders = testDataInfo.getTestHeaderInfo();
//				testDataInfo.addLog("Start[").append(timeFormat.format(new Date())).append("] | End[] | ").append("Thread[").append(threadName).append("] | Name[").append(testName).append("] | Url[").append(url).append("]\n\t요청 Params[").append(params).append("]\n\t응답 ");
//				if(constants.isKeepSession(index, postFix)) {
//					if(restTemplateInterceptor==null) {
//						restTemplateInterceptor = new RestTemplateInterceptor(httpHeaders, constants.isLogging());
//					} else {
//						restTemplateInterceptor.setHttpHeaders(httpHeaders);
//					}
//					sb.append(result = runTest(postFix, index, url, params, restTemplateInterceptor, constants.getTestHttpMethod(index, postFix), httpHeaders, beforeResultMap, totalTermDoneCount, printLogTerm, mapKeepData, resultMapFirstCall));
//				} else {
//					sb.append(result = runTest(postFix, index, url, params, httpHeaders, constants.getTestHttpMethod(index, postFix), beforeResultMap, totalTermDoneCount, printLogTerm, mapKeepData, resultMapFirstCall));
//				}
//				
//				/*** 바로전 호출에서 저장하라는 Key가 있고, 해당 결과값이 있으면 영구저장 버튼에 저장한다. ***/
//				constants.addKeepDataToMap(mapKeepData, postFix, index, result);
//				
////				try {
////					beforeResultMap.putAll(objectMapper.readValue(result, Map.class));
////				} catch (Exception e) {}
//				sb.insert(0, "] : ").insert(0, errorCount).insert(0, "], 실패[").insert(0, totalSuccCount).insert(0, " : 성공[").insert(0, addTotalCount())
//						.insert(sb.indexOf("End")+4, timeFormat.format(new Date()));
//				// 선행쿼리가 있으면 로그에 출력해준다.
//				if(!StringUtils.isEmpty(constants.getPropertyValue("test."+index+postFix+".query")) && !StringUtils.isEmpty(preSqlResult) && !preSqlResult.isEmpty()) {
//					sb.append("\n\tbefore Query : ").append(constants.getPropertyValue("test."+index+postFix+".query")).append("\n\tquery Result (다중쿼리는 마지막결과만 출력됨) : ");
//					try {
//						sb.append(objectMapper.writeValueAsString(preSqlResult));
//					}catch (JsonProcessingException e) {
//						sb.append("Cannot Convert from ObjectMapper : error[").append(e.getMessage()).append(", ").append(e.getLocalizedMessage()).append(", ").append(e.getCause()).append("]");
//					}
//				}
//				addLog(sb.append("\n").toString());
//				if(constants.isLogging() || totalTermDoneCount%printLogTerm==0) {
//					String callInfo = sb.substring(0, sb.indexOf("\n\t요청"));
//					String callReq = sb.substring(sb.indexOf("\n\t요청 Params["), sb.indexOf("\n\t응답 "));
//					String callRes = sb.substring(sb.indexOf("\n\t응답 "));
//					System.out.print(callInfo);
//					System.out.print(callReq.length()>MAX_LOG_PRINT_TO_CONSOLE?callReq.substring(0, MAX_LOG_PRINT_TO_CONSOLE)+"...":callReq);
//					System.out.print(callRes.length()>MAX_LOG_PRINT_TO_CONSOLE?callRes.substring(0, MAX_LOG_PRINT_TO_CONSOLE)+"...":callRes);
//					if(!StringUtils.isEmpty(preSqlResult) && !preSqlResult.isEmpty()) {
//						System.out.println(" ||| 선행쿼리 조회 (다중 쿼리의 경우엔 마지막 수행쿼리만 출력됨) Count : " + preSqlResult.size());
//					} else {
//						System.out.println("");
//					}
//				}
			} catch (Exception e) {
				testDataInfo.doErrorProcess(e);
//				try {addLogFlush(sb.append("\n Error[").append(e.getMessage()).append("], Cause[").append(e.getCause()).append("], LocalError[").append(e.getLocalizedMessage()).append("]").toString());} catch (IOException e1) {}
//				++systemErrorCount;
//				if(systemErrorCount>100) {
//					isExitApp = true;
//				}
//				e.printStackTrace();
			} finally {
				
			}
		} else {
			// break;
		}
		return testDataInfo;
	}

}
