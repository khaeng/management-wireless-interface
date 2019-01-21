package com.itcall.batch.common.controller.rest;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.UnexpectedJobExecutionException;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobExecutionNotRunningException;
import org.springframework.batch.core.launch.JobInstanceAlreadyExistsException;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.JobParametersNotFoundException;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
import org.springframework.batch.core.launch.NoSuchJobInstanceException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.itcall.batch.common.service.batchInfo.BatchInfoService;
import com.itcall.batch.common.service.rest.ApiMngService;
import com.itcall.batch.common.service.rest.BatchMngService;
import com.itcall.batch.common.support.code.ApiCmdCd;
import com.itcall.batch.common.support.code.ApiTargetCd;
import com.itcall.batch.common.support.editor.ApiParamEnumEditor;
import com.itcall.batch.common.vo.batchInfo.BatchInfoHstVo;
import com.itcall.batch.common.vo.batchInfo.BatchInfoVo;
import com.itcall.batch.config.support.EnvironmentSupport;

@RestController
public class RestServerController {

	private static final Logger LOG = LoggerFactory.getLogger(RestServerController.class);

	private static final String SECURE_API_KEY_NAME = "authorization";

	@Value("#{common['secure.api.auth.code']}")
	private String UK; 

	@Resource
	private ApplicationContext ctx;

//	@Resource
//	private WebApplicationContext wCtx;

	@Resource
	private TaskScheduler taskScheduler;

	@Resource
	private BatchMngService batchMngService;

	@Resource
	private ApiMngService apiMngService;

//	@Resource
//	private TaskExecutor batchTaskExecutor;
//	@Resource
//	private TaskExecutor batchShotTaskExecutor;

	@Resource
	private JobExplorer jobExplorer;

	@Resource
	private JobOperator jobOperator;

	@Resource
	private JobRegistry jobRegistry;

	@Resource(name="jobRepository")
	private JobRepository jobRepository;

	@Resource
	private BatchInfoService batchInfoService;

//	@Resource(name="mapJobRepositoryFactory")
//	private JobRepository mapJobRepositoryFactory;

//	@Resource
//	private SampleInfoMapper sampleInfoMapper;

	@RequestMapping("/test")
	public Map<String, String> test(HttpServletRequest request){
		Map<String, String> result = new HashMap<>();
		result.put("testId", "TestValue");
		result.put(SECURE_API_KEY_NAME, UK);
//		result.put("asdf", testUk);
//		result.put("asdfasdf", testUk2);
		return result;
	}

	@RequestMapping({"/","/{path}"})
	public String home(@PathVariable(value="path", required=false) String apiAddingPaths) {
//		List<SampleInfoVo> list = sampleInfoMapper.selectListTest();
//		for (SampleInfoVo sampleInfoVo : list) {
//			LOG.debug("batch Reader ::: {}", sampleInfoVo);
//		}
		return "Hello World without SPRING BOOT AS PARENT!!!\n\n\n" + apiAddingPaths;
	}

	// public static final String initBinderValueName = "options";
	@InitBinder (value = {"target","cmd", "options"})
	public void initBinder(WebDataBinder dataBinder) {
		dataBinder.registerCustomEditor(Object.class, new ApiParamEnumEditor());
//		dataBinder.registerCustomEditor(ApiTargetCd.class, new ApiTargetCdEditor());
	}
	@RequestMapping(value = {"/api/{target}/{cmd}/{value}/{options}"
			,"/api/{target}/{cmd}/{value}"
			,"/api/{target}/{cmd}"}
	, method = {/*RequestMethod.GET,*/RequestMethod.POST}, produces= {"application/json; charset=UTF-8"})
	public ResponseEntity<Object> callApiSvc(
			@PathVariable(required=true) ApiTargetCd target
			, @PathVariable(required=true) ApiCmdCd cmd
			, @PathVariable(required=false) String value
			, @PathVariable(required=false, value="") String options
			, @RequestBody(required=false) Map<String, Object> paramMap
			, @RequestHeader Map<String,String> header
			) throws JobInstanceAlreadyCompleteException, NoSuchJobExecutionException, NoSuchJobException, JobRestartException, JobParametersInvalidException, JobInstanceAlreadyExistsException, JobParametersNotFoundException, JobExecutionAlreadyRunningException, UnexpectedJobExecutionException, JobExecutionNotRunningException, NoSuchJobInstanceException, UnsupportedEncodingException, JsonProcessingException{

		Map<String,Object> result = null;

		if(paramMap!=null) { 
			if(paramMap.get("option")!=null)
				options = paramMap.get("option").toString();
			if(paramMap.get("value")!=null)
				value   = paramMap.get("value").toString();
		}

		if(!StringUtils.isEmpty(value)) {
			value   = URLDecoder.decode(value, Charset.defaultCharset().name());
		}

		result = new HashMap<String, Object>();
		result.put("TARGET", target);
		result.put("CMD", cmd);
		result.put("VALUE", value);
		result.put("OPTIONS", options);

		if(header.get(SECURE_API_KEY_NAME)==null || !header.get(SECURE_API_KEY_NAME).equals(UK)) {
			result.put("CODE", -1001);
			result.put("RESULT", "ERROR");
			result.put("ERROR", "Authorization Fail");
			result.put("TRACE", "header[authorization] is empty");
			LOG.error("Batch.RestSvr Authorization Fail ::: header[authorization][{}] is empty", header.get(SECURE_API_KEY_NAME));
		}else{

			/****************** WORKING *****************/
			switch (target) {
			case API:
				result.putAll(apiMngService.apiManager(cmd, value, options));
				break;
			case JOB:
				result.putAll(jobManager(cmd, value, options));
				break;
			case SCHEDULED:
				result.putAll(scheduleManager(cmd, value, options));
				break;
			default:
			}
		}

		LOG.debug("batchMngService ==> {}", result);

// 		String resultStr = new ObjectMapper().writeValueAsString(result);
		return response(result);
		// return response(model.asMap());
	}

	@RequestMapping("/healthCheck")
	public @ResponseBody String healthCheck(){
		return "OK(" + new Date() + ")";
	}

	/**
	 * 성공 시 HTTP code / HTTP Header를 직접 지정하여 반환한다.
	 * @param httpHeaders
	 * @param body
	 * @param httpStatus
	 * @return
	 */
	protected ResponseEntity<Object> response(HttpHeaders httpHeaders, Object body, HttpStatus httpStatus) {
		HttpStatus newHttpStatus = httpStatus;
		if(newHttpStatus==null){
			newHttpStatus = HttpStatus.OK;
		}
		if(httpHeaders==null){
			httpHeaders = new HttpHeaders();
			httpHeaders.add("Content-Type", "application/json; charset=UTF-8");
		}
		return new ResponseEntity<Object>(body, httpHeaders, newHttpStatus);
	}
	protected ResponseEntity<Object> response(Object body){
		return response(null, body, null);
	}

	@ExceptionHandler(value = {IllegalArgumentException.class,HttpClientErrorException.class,HttpServerErrorException.class,RestClientException.class,NumberFormatException.class})
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	protected ResponseEntity<Object> exceptionHandler(Object obj) {
		LOG.error("<== {}", obj);
		return new ResponseEntity<Object>(obj,null);
	};

	@ExceptionHandler(value = {Exception.class, NullPointerException.class})
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public void exceptionHandler(Exception e){
		LOG.error("<== {}", e);
	}



//	@Resource
//	private BatchJobConfig batchJobConfig;

	private Map<String, Object> jobManager(ApiCmdCd cmd, String value, String options) {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("CODE", 0);
		try {
			switch (cmd) {
			case START:
				/**************************************************************************************************************************************************************************************
				 * 배치 중복실행을 막기 위해서 선행배치 체크 및 배치 실행/등록 시점을 동기화한다. 동기화부분이 2군데 임으로 job Object로 맞춰서 동일 job에 대해 synchronized를 수행한다.
				 **************************************************************************************************************************************************************************************/
				synchronized ((Job) ctx.getBean(value)) {
					String[] params = (options==null?"":options).split(",");
					if(!StringUtils.isEmpty(params)) {
						for (int i = 0; i < params.length; i++) {
							params[i] = URLDecoder.decode(params[i], Charset.defaultCharset().name());
						}
					}
					if(isRunningJob(batchInfoService, value)) {
						result.put("CODE", -2);
						result.put("RESULT", "Job[" + value + "]은 이미 실행중입니다.\n종료하거나 이미종료되었다면 배치종료상태값을 변경해주세요.(COMPLETED, STOPPED, FAILED)");
					}else {
						batchMngService.jobAsyncStart(value, params);
						result.put("RESULT", "Confirmed Async JobRunner["+value+"]"/*batchJobConfig.jobStart(value, options.split(","))*/);
					}
				}
				break;
			case STOP:
				// 실행중인 Job를 종료한다.
				int stopCnt = batchMngService.jobStop(value);
				result.put("CODE", stopCnt);
				if(stopCnt>0) {
					result.put("RESULT", stopCnt + " 건의 실행중인 Job[" + value + "]에 종료 요청되었습니다.\n종료결과는 이력에서 확인하십시오.");
				}else if(stopCnt==0) {
					result.put("RESULT", "실행중인 Job[" + value + "]이 없습니다.");
				}else {
					result.put("RESULT", "Job[" + value + "]을 종료할 수 없습니다.");
				}
				break;
			case GET:
				// APP에 실행중인 JobList를 가져온다.
				result.put("RESULT", batchMngService.getRunningJobList());
				break;
			case LIST:
				// APP에 등록된 JobList를 가져온다.
				result.put("RESULT", batchMngService.getBatchInfoList());
				break;
			default:
				result.put("CODE", -100);
				result.put("RESULT", "Not yet support your command...");
				break;
			}
		}catch (Exception e) {
			result.put("CODE", -1);
			result.put("RESULT", "ERROR");
			result.put("ERROR", e.getMessage());
			result.put("TRACE", e);
			LOG.error("Job[{}] control Management error ::: Result[{}] ::: Trace {}", value, result, e);
		}
		return result;
	}

	public static boolean isRunningJob(BatchInfoService batchInfoService, String jobName) {
		// 배치실행내역이 없거나 최종상태가 COMPLETED / FAILED / STOPPED 인 경우에만 수행한다. ::: 중복수행될 가능성을 제거한다.
		// 배치상태 : COMPLETED, STARTING, STARTED, STOPPING, STOPPED, FAILED, ABANDONED, UNKNOWN
		// 배치종료상태 : EXECUTING, COMPLETED, NOOP, STOPPED, FAILED, UNKNOWN
		try {
			Thread.sleep(1000); // 중복실행을 방지하기 위해서 1초가 딜레이한다 . 먼저 실행으로 진입된 JOB이 History에 Insert할 시간을 준다.
		} catch (Exception e) {}
		BatchInfoHstVo batchInfoHst = new BatchInfoHstVo();
		batchInfoHst.setJobName(jobName);
		batchInfoHst.setSvrTypeCd(null);// Active든 StandBy든 해당 Job이 실행중일땐 실행하지 못하게 해야한다. 따라서 생성지 자동으로 부여되는 서버주소를 제거한다.
		List<BatchInfoHstVo> list = batchInfoService.getBatchInfoHstLastStatus(batchInfoHst);
		if(list!=null && list.size()>0) {
			for (BatchInfoHstVo vo : list) {
				if(vo!=null && vo.isRunning()) {
					LOG.warn("Job[{}] Already running status. If your not running then update your db-table-batch-hst-info : BatchInfo[{}]", vo.getJobName(), vo);
					return true;
				}
			}
		}
		return false;
	}

	private Map<String, Object> scheduleManager(ApiCmdCd cmd, String value, String options) {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("CODE", 0);
		BatchInfoVo batchInfo = new BatchInfoVo(value);
		try {
			batchInfo = batchInfoService.getBatchInfo(batchInfo);
			switch (cmd) {
			case SAVE:
				// (기존 스케줄을 취소하고, ) 새로 등록한다.
				// 스케줄을 새로 등록하는 경우는 DB의 정보를 수정(변경)한다.
				String oldCronCmd = batchInfo.getCronCmd();
				if(oldCronCmd!=null && oldCronCmd.equals(EnvironmentSupport.NOT_SUPPORT_SCHEDULED_JOB)) {
					result.put("CODE", -3);
					result.put("RESULT", "스케줄이 지원되지 않는 JOB입니다.");
					LOG.warn("Adding schedule to Job[{}] ::: Result[{}]", value, result);
					break;
				}
				String cronCmd = options /*(options==null?"":options).replaceAll("-", "/").replaceAll("!", "?")*/;
				if(cronCmd.trim().length()>3 && cronCmd.matches("^[0-9]*$|^\\s*($|#|\\w+\\s*=|(\\?|\\*|(?:[0-5]?\\d)(?:(?:-|\\/|\\,)(?:[0-5]?\\d))?(?:,(?:[0-5]?\\d)(?:(?:-|\\/|\\,)(?:[0-5]?\\d))?)*)\\s+(\\?|\\*|(?:[0-5]?\\d)(?:(?:-|\\/|\\,)(?:[0-5]?\\d))?(?:,(?:[0-5]?\\d)(?:(?:-|\\/|\\,)(?:[0-5]?\\d))?)*)\\s+(\\?|\\*|(?:[01]?\\d|2[0-3])(?:(?:-|\\/|\\,)(?:[01]?\\d|2[0-3]))?(?:,(?:[01]?\\d|2[0-3])(?:(?:-|\\/|\\,)(?:[01]?\\d|2[0-3]))?)*)\\s+(\\?|\\*|(?:0?[1-9]|[12]\\d|3[01])(?:(?:-|\\/|\\,)(?:0?[1-9]|[12]\\d|3[01]))?(?:,(?:0?[1-9]|[12]\\d|3[01])(?:(?:-|\\/|\\,)(?:0?[1-9]|[12]\\d|3[01]))?)*)\\s+(\\?|\\*|(?:[1-9]|1[012])(?:(?:-|\\/|\\,)(?:[1-9]|1[012]))?(?:L|W)?(?:,(?:[1-9]|1[012])(?:(?:-|\\/|\\,)(?:[1-9]|1[012]))?(?:L|W)?)*|\\?|\\*|(?:JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC)(?:(?:-)(?:JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC))?(?:,(?:JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC)(?:(?:-)(?:JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC))?)*)\\s+(\\?|\\*|(?:[0-6])(?:(?:-|\\/|\\,|#)(?:[0-6]))?(?:L)?(?:,(?:[0-6])(?:(?:-|\\/|\\,|#)(?:[0-6]))?(?:L)?)*|\\?|\\*|(?:MON|TUE|WED|THU|FRI|SAT|SUN)(?:(?:-)(?:MON|TUE|WED|THU|FRI|SAT|SUN))?(?:,(?:MON|TUE|WED|THU|FRI|SAT|SUN)(?:(?:-)(?:MON|TUE|WED|THU|FRI|SAT|SUN))?)*)(|\\s)+(\\?|\\*|(?:|\\d{4})(?:(?:-|\\/|\\,)(?:|\\d{4}))?(?:,(?:|\\d{4})(?:(?:-|\\/|\\,)(?:|\\d{4}))?)*))$")) {
					batchInfo.setCronCmd(cronCmd);
					result.put("RESULT", batchMngService.setScheduler(value, cronCmd));
					batchInfoService.setBatchInfo(batchInfo);
				}else {
					result.put("CODE", -5);
					result.put("RESULT", "등록할 스케줄이 없거나, 스케줄패턴이 아닙니다. schedule["+cronCmd+"]");
					LOG.warn("Adding schedule to Job[{}] ::: Result[{}]", value, result);
				}
				break;
			case REMOVE:
				// 기존 스케줄을 삭제한다. ::: 스케줄을 종료하는 경우는 DB의 정보를 수정하지 않는다.
				batchInfo.setCronCmd(null);
				result.put("RESULT", batchMngService.removeScheduler(value));
				batchInfoService.setBatchInfo(batchInfo);
				break;
			default:
				result.put("CODE", -100);
				result.put("RESULT", "Not yet support your command...");
				break;
			}
		}catch (Exception e) {
			result.put("CODE", -1);
			result.put("RESULT", "ERROR");
			result.put("ERROR", e.getMessage());
			result.put("TRACE", e);
			LOG.error("Job[{}] schedule Management error ::: Result[{}] ::: Trace {}", value, result, e);
		}
		return result;
	}

}
