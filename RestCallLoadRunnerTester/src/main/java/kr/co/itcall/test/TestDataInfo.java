package kr.co.itcall.test;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;


public class TestDataInfo {


	private TestCall testCall;
	private Constants constants;
	private StringBuffer result = new StringBuffer();
	private StringBuffer log = new StringBuffer();
	private StringBuffer conLog = new StringBuffer();
	private String postFix;
	private long index;
	private String testName;
	private String threadName;
	private String url;
	private HttpMethod method;
	// private HttpHeaders httpHeaderParams;
	private String params;
	private List<Map<String, Object>> preSqlResult;
	private Map<String,Object> beforeResultMap;
	private Map<String,Object> mapKeepData = new HashMap<String, Object>();
	private Map<String,Object> resultMapFirstCall;
	private long totalTermDoneCount;
	private int printLogTerm;
	private long sleepMs;

//	public TestDataInfo(TestCall testCall, Constants constants, Map<String,Object> mapKeepData, Map<String,Object> resultMapFirstCall, String postFix, long index, long totalTermDoneCount, int printLogTerm) {
//		this.testCall = testCall;
//		this.constants = constants;
//		this.mapKeepData = mapKeepData;
//		this.resultMapFirstCall = resultMapFirstCall;
//		this.postFix = postFix;
//		this.index = index;
//		this.totalTermDoneCount = totalTermDoneCount;
//		this.printLogTerm = printLogTerm;
//		initialize();
//	}

	public TestDataInfo(TestCall testCall, Constants constants, String postFix, long index, long totalTermDoneCount, int printLogTerm) {
		this(null, testCall, constants, postFix, index, totalTermDoneCount, printLogTerm);
	}
	public TestDataInfo(TestCall testCall, TestDataInfo testDataInfo, String postFix) {
		this(testDataInfo, testCall, testDataInfo.getConstants(), postFix, testDataInfo.getIndex(), testDataInfo.getTotalTermDoneCount(), testDataInfo.getPrintLogTerm());
	}
	public TestDataInfo(TestDataInfo testDataInfo, TestCall testCall, Constants constants, String postFix, long index, long totalTermDoneCount, int printLogTerm) {
		if(!StringUtils.isEmpty(testDataInfo)) {
			this.beforeResultMap = testDataInfo.getBeforeResultMap();
			this.resultMapFirstCall = testDataInfo.getResultMapFirstCall();
			this.mapKeepData = testDataInfo.getMapKeepData();
		}
		this.testCall = testCall;
		// this.httpHeaderParams = testCall.getHttpHeaders();
		this.constants = constants;
		this.postFix = postFix;
		this.index = index;
		this.totalTermDoneCount = totalTermDoneCount;
		this.printLogTerm = printLogTerm;
		
		initialize();
		
	}

	private void initialize() {
		testName = constants.getTestNameInfo(index, postFix);
		threadName = Thread.currentThread().getName();
		url = constants.getTestUrlInfo(index, postFix, mapKeepData, resultMapFirstCall, beforeResultMap);
		method = constants.getTestHttpMethod(index, postFix);
		preSqlResult = testCall.getPreSqlResult(postFix, index, beforeResultMap, mapKeepData, resultMapFirstCall);
		params = TestCall.switchParams(postFix, index, constants.getTestParamsInfo(index, postFix, beforeResultMap), preSqlResult, mapKeepData, resultMapFirstCall, constants, beforeResultMap);
		sleepMs = constants.getSleepTimeBeforeTest(index, postFix);
	}

	public TestCall getTestCall() {
		return testCall;
	}

	public void setTestCall(TestCall testCall) {
		this.testCall = testCall;
	}

	public Constants getConstants() {
		return constants;
	}

	public void setConstants(Constants constants) {
		this.constants = constants;
	}

	public StringBuffer getResult() {
		return result;
	}

	public void setResult(StringBuffer result) {
		this.result = result;
	}

	public StringBuffer getLog() {
		return log;
	}

	public void setLog(StringBuffer log) {
		this.log = log;
	}

	public StringBuffer getConLog() {
		return conLog;
	}

	public void setConLog(StringBuffer conLog) {
		this.conLog = conLog;
	}

	public String getPostFix() {
		return postFix;
	}

	public void setPostFix(String postFix) {
		this.postFix = postFix;
	}

	public long getIndex() {
		return index;
	}

	public void setIndex(long index) {
		this.index = index;
	}

	public String getTestName() {
		return testName;
	}

	public void setTestName(String testName) {
		this.testName = testName;
	}

	public String getThreadName() {
		return threadName;
	}

	public void setThreadName(String threadName) {
		this.threadName = threadName;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public HttpMethod getMethod() {
		return method;
	}

	public void setMethod(HttpMethod method) {
		this.method = method;
	}

//	public HttpHeaders getHttpHeaderParams() {
//		return httpHeaderParams;
//	}
//
//	public void setHttpHeaderParams(HttpHeaders httpHeaderParams) {
//		this.httpHeaderParams = httpHeaderParams;
//	}

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}

	public List<Map<String, Object>> getPreSqlResult() {
		return preSqlResult;
	}

	public void setPreSqlResult(List<Map<String, Object>> preSqlResult) {
		this.preSqlResult = preSqlResult;
	}

	public Map<String, Object> getBeforeResultMap() {
		return beforeResultMap;
	}

	public void setBeforeResultMap(Map<String, Object> beforeResultMap) {
		this.beforeResultMap = beforeResultMap;
	}

	public Map<String, Object> getMapKeepData() {
		return mapKeepData;
	}

	public void setMapKeepData(Map<String, Object> mapKeepData) {
		this.mapKeepData = mapKeepData;
	}

	public Map<String, Object> getResultMapFirstCall() {
		return resultMapFirstCall;
	}

	public void setResultMapFirstCall(Map<String, Object> resultMapFirstCall) {
		this.resultMapFirstCall = resultMapFirstCall;
	}

	public long getTotalTermDoneCount() {
		return totalTermDoneCount;
	}

	public void setTotalTermDoneCount(long totalTermDoneCount) {
		this.totalTermDoneCount = totalTermDoneCount;
	}

	public int getPrintLogTerm() {
		return printLogTerm;
	}

	public void setPrintLogTerm(int printLogTerm) {
		this.printLogTerm = printLogTerm;
	}

	public long getSleepMs() {
		return sleepMs;
	}

	public void setSleepMs(long sleepMs) {
		this.sleepMs = sleepMs;
	}






	/**********************************************************************************************
	 * 사전조회쿼리와 test.[num].sql.key에 해당하는 값이 존재하면 본 테스트는 건너뛴다.
	 * 즉, 없을경우 등록하는 업무를 여기서 처리한다.
	 */
	public boolean isExistValueForPreSqlResult() throws IOException {
		boolean result = this.constants.isExistValueForPreSqlResult(index, postFix, preSqlResult);
		if(result) {
			TestCall.addSuccessCount();
			this.log.append(String.format("%d : 성공[%d], 실패[%d] : Start[%s] | End[-cancel-] | Thread[%s] | Name[%s] | Url[%s] ||| 사전조건조회값[%s]이 존재하여 성공으로 처리하며, 수행하지는 않습니다. ||| 사전조건조회 쿼리결과를 다음인수로 전달합니다.\n"
					, TestCall.addTotalCount(), TestCall.totalSuccCount, TestCall.errorCount, TestCall.timeFormat.format(new Date()), threadName, testName, url, constants.getPropertyValue("test." + index + postFix + ".sql.key", "")));
			testCall.addLog(this.log.toString());
			if(constants.isLogging() || totalTermDoneCount%printLogTerm==0) {
				System.out.print(this.conLog.append(this.log.toString()).toString());
			}
			try {
				this.result.append(Constants.objectMapper.writeValueAsString(preSqlResult));
				testCall.addLog("\t사전조회결과값 : " + this.result.toString() + "\n");
				if(constants.isLogging() || totalTermDoneCount%printLogTerm==0) {
					System.out.println(this.conLog.append("\t사전조회결과값 : ").append(this.result.toString()).toString());
				}
			}catch (JsonProcessingException e) {}
		}
		return result;
	}

//	public StringBuffer addLog(Object obj) {
//		return this.log.append(obj);
//	}
//	public StringBuffer insertLog(int index, Object obj) {
//		return this.log.insert(index, obj);
//	}

	public void doRunningTestCall() throws IOException {
		/**********
		 * 로그인 유지 여부에 따라서 testCall.getHttpHeaders()를 사용할지 말지 프로퍼티.키로 받아 처리한다. (keep.session.yn과 비슷한 역할)
		 **********/
		HttpHeaders httpHeaders = constants.getTestHeaderInfo(index, postFix, testCall.getHttpHeaders(), beforeResultMap);
		this.log.append("Start[").append(TestCall.timeFormat.format(new Date())).append("] | End[] | ").append("Thread[").append(threadName).append("] | Name[").append(testName).append("] | Url[").append(url).append("]\n\t요청 Params[").append(params).append("]\n\t응답 ");
		if(constants.isKeepSession(index, postFix)) {
			RestTemplateInterceptor restTemplateInterceptor = this.testCall.getRestTemplateInterceptor();
			if(restTemplateInterceptor==null) {
				restTemplateInterceptor = new RestTemplateInterceptor(httpHeaders, constants.isLogging());
			} else {
				restTemplateInterceptor.setHttpHeaders(httpHeaders);
			}
			this.log.append(this.result.append(
					// testCall.runTest(postFix, index, url, params, restTemplateInterceptor, method, httpHeaders, beforeResultMap, totalTermDoneCount, printLogTerm, mapKeepData, resultMapFirstCall)
					testCall.runTest(this, restTemplateInterceptor, httpHeaders)
					));
		} else {
			this.log.append(this.result.append(
					// testCall.runTest(postFix, index, url, params, httpHeaders, method, beforeResultMap, totalTermDoneCount, printLogTerm, mapKeepData, resultMapFirstCall)
					testCall.runTest(this, httpHeaders)
					));
		}
		
		/*** 바로전 호출에서 저장하라는 Key가 있고, 해당 결과값이 있으면 영구저장 버튼에 저장한다. ***/
		this.mapKeepData = constants.addKeepDataToMap(mapKeepData, postFix, index, result.toString());
		
//		try {
//			beforeResultMap.putAll(objectMapper.readValue(result, Map.class));
//		} catch (Exception e) {}
		this.log.insert(0, "] : ").insert(0, TestCall.errorCount).insert(0, "], 실패[").insert(0, TestCall.totalSuccCount).insert(0, " : 성공[").insert(0, TestCall.addTotalCount())
				.insert(this.log.indexOf("End")+4, TestCall.timeFormat.format(new Date()));
		// 선행쿼리가 있으면 로그에 출력해준다.
		if(!StringUtils.isEmpty(constants.getPropertyValue("test."+index+postFix+".query")) && !StringUtils.isEmpty(preSqlResult) && !preSqlResult.isEmpty()) {
			this.log.append("\n\tbefore Query : ").append(constants.getPropertyValue("test."+index+postFix+".query")).append("\n\tquery Result (다중쿼리는 마지막결과만 출력됨) : ");
			try {
				this.log.append(Constants.objectMapper.writeValueAsString(preSqlResult));
			}catch (JsonProcessingException e) {
				this.log.append("Cannot Convert from ObjectMapper : error[").append(e.getMessage()).append(", ").append(e.getLocalizedMessage()).append(", ").append(e.getCause()).append("]");
			}
		}
		testCall.addLog(this.log.append("\n").toString());
		if(constants.isLogging() || totalTermDoneCount%printLogTerm==0) {
			String callInfo = this.log.substring(0, this.log.indexOf("\n\t요청"));
			String callReq = this.log.substring(this.log.indexOf("\n\t요청 Params["), this.log.indexOf("\n\t응답 "));
			String callRes = this.log.substring(this.log.indexOf("\n\t응답 "));
			this.conLog.append(callInfo);
			callReq = callReq.length()>TestCall.MAX_LOG_PRINT_TO_CONSOLE?callReq.substring(0, TestCall.MAX_LOG_PRINT_TO_CONSOLE)+"...":callReq;
			this.conLog.append(callReq);
			callRes = callRes.length()>TestCall.MAX_LOG_PRINT_TO_CONSOLE?callRes.substring(0, TestCall.MAX_LOG_PRINT_TO_CONSOLE)+"...":callRes;
			this.conLog.append(callRes);
			if(!StringUtils.isEmpty(preSqlResult) && !preSqlResult.isEmpty()) {
				this.conLog.append(" ||| 선행쿼리 조회 (다중 쿼리의 경우엔 마지막 수행쿼리만 출력됨) Count : " + preSqlResult.size());
			}
			System.out.println(this.conLog.toString());
		}
	}

	public void doErrorProcess(Exception e) {
		try {
			testCall.addLogFlush(this.log.append("\n Error[").append(e.getMessage()).append("], Cause[").append(e.getCause()).append("], LocalError[").append(e.getLocalizedMessage()).append("]").toString());
			if(constants.isLogging() || totalTermDoneCount%printLogTerm==0) {
				this.conLog.append("\n Error[").append(e.getMessage()).append("], Cause[").append(e.getCause()).append("], LocalError[").append(e.getLocalizedMessage()).append("]").toString();
				System.out.println(this.conLog.toString());
			}
		} catch (IOException e1) {}
		++TestCall.systemErrorCount;
		if(TestCall.systemErrorCount>100) {
			testCall.isExitApp = true;
		}
		e.printStackTrace();
	}

	public void waitBeforeTest() {
		if(getSleepMs()>0) {
			try {Thread.sleep(getSleepMs());} catch (InterruptedException e) {}
		}
	}

	public void beforeCallProcess() {
		if(!StringUtils.isEmpty(result.toString())) {
			if(StringUtils.isEmpty(beforeResultMap)) {
				beforeResultMap = TestCall.switchResult(result.toString());
			} else {
				Map<String, Object> convertBeforeResultMap = TestCall.switchResult(result.toString(), constants.getTestCharset(index, postFix));
				if(!StringUtils.isEmpty(convertBeforeResultMap)) {
					beforeResultMap.putAll(convertBeforeResultMap);
				}
			}
		}
		if(StringUtils.isEmpty(resultMapFirstCall)) {
			resultMapFirstCall = beforeResultMap;
		}
	}

	
}
