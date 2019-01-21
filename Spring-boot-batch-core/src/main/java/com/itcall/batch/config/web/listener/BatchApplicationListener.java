package com.itcall.batch.config.web.listener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.itcall.batch.common.service.batchInfo.BatchInfoService;
import com.itcall.batch.common.service.rest.BatchMngService;
import com.itcall.batch.common.vo.batchInfo.BatchInfoVo;
import com.itcall.batch.config.batch.BaseBatch;
import com.itcall.batch.config.cache.CacheConfig;
import com.itcall.batch.config.support.EnvironmentSupport;

/********************************************************
 * ServletContext가 로드된 후 수행되는 리스너.
 * @Primay등으로 선언된것들을 포함하여 필수 컨테이너가
 * 로드된 이후 호출된다.
 * 즉, ServletContext가 생성된 후 호출된다.
 * Application이 모두 로드된(완성된) 이후 수행하고자 하는 
 * 로직들은 ApplicationListener에서 구현해야한다.
 * 여기는 업무로직 전 먼저 수행하고자 하는 로직을 구현한다.
 * ServletContext > 
 * @author khaeng@nate.com
 ********************************************************/
@Component("batchApplicationListener")
public class BatchApplicationListener implements ApplicationListener<ApplicationReadyEvent> {

	Logger LOG = LoggerFactory.getLogger(BatchApplicationListener.class);

	@Resource
	private ApplicationContext ctx;

	@Resource
	private JobExplorer jobExplorer;

	@Resource
	private BatchInfoService batchInfoService;

	@Resource
	private BatchMngService batchMngService;

//	@Value("${batch.server.id:BATCH}")
//	private String batchSysId;

	@Value("#{batch['batch.app.war.path']?:'/app/wars/MY-BOOT-${batch.server.id:BATCH}-1.0.war'}")
	private String warPath;

//	@Value("${jboss.bind.address:}") // jboss.bind.address.management
//	private String appIpAddr;

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {

		LOG.info("BatchApplicationListener.Started");

		// 환경초기화...
		EnvironmentSupport.initFirstSet(this.ctx);

		LOG.info("initializeCacheManager Before loading...");
		CacheConfig.reloadAllCache();
		LOG.info("initializeCacheManager After loaded... CacheList[{}]", CacheConfig.getCacheList());


		BatchInfoVo batchInfoBaseVo = new BatchInfoVo();
		List<BatchInfoVo> batchInfoListFromDB = null;
		List<BatchInfoVo> batchInfoListAddDB= new ArrayList<BatchInfoVo>();
		try {
			/**
			 * 생성시 자동부여되는 sysId와 ipAddr로 을 이용하여 모든 JOB을 불러온다.
			 */
			batchInfoListFromDB = batchInfoService.getBatchInfoList(batchInfoBaseVo);
			LOG.debug("sysId[{}] / svrAddr[{}], Loading Batch Job info from Database... : {}", batchInfoBaseVo.getKindCd(), batchInfoBaseVo.getSvrTypeCd()
					, Arrays.toString(batchInfoListFromDB.toArray(new BatchInfoVo[batchInfoListFromDB.size()])));

		} catch (Exception e) {
			LOG.error("sysId[{}] / svrAddr[{}], Loading Error... Batch Job Application Laucher in Initialized failure... : {}", batchInfoBaseVo.getKindCd(), batchInfoBaseVo.getSvrTypeCd(), e);
			System.exit(-1);
		}
		String[] schedulerNames = this.ctx.getBeanNamesForType(BaseBatch.class);
		String schedulerNamesText = Arrays.toString(schedulerNames);
		String[] arrJobId = this.ctx.getBeanNamesForType(Job.class);
		LOG.info("sysId[{}] / svrAddr[{}], Ready to batch scheduler ==> {}", batchInfoBaseVo.getKindCd(), batchInfoBaseVo.getSvrTypeCd(), schedulerNamesText);
		LOG.info("sysId[{}] / svrAddr[{}], Ready to Job names ==> {}", batchInfoBaseVo.getKindCd(), batchInfoBaseVo.getSvrTypeCd(), Arrays.toString(arrJobId));

		String cronCmd = null; // "12 0/10 * * * ?";
		try {
			for (String jobId : arrJobId) {
				// 기본적으로 JOB을 등록하기 위한 사전 데이터 입력작업.
				BatchInfoVo addVo = new BatchInfoVo(jobId);
				addVo.setUseYn("N"); // 최초 등록시에는 사용불가로 셋팅한다. 이기능은 스케줄에 대해서만 기능하며, 수동실행일 경우 의미가 없다.
				if(!schedulerNamesText.contains(jobId+BaseBatch.JOB_CONF_POST_FIX)){
					// Schedule이 불가능한 JOB
					addVo.setCronCmd(EnvironmentSupport.NOT_SUPPORT_SCHEDULED_JOB);
				}
				for (BatchInfoVo voDB : batchInfoListFromDB) {
					if(voDB.isExistJob(addVo)) {
						// JOB이 이미 등록되어 있으면 신규대상에서 제외한다.
						addVo = null;
						cronCmd = voDB.getCronCmd();
						if(cronCmd!=null
								&& !cronCmd.trim().isEmpty()
								/********************************************************************
								 * 스케줄은 무조건 등록하고 스케줄 실행시 체크해서 실행안한다.
								 * 언제든 USE_YN을 풀어주면 자동으로 실행되게끔 하는 요청에 의한
								 * 조치임. 요청일자 2018-11-05
								&& voDB.getUseYn()!=null && voDB.getUseYn().equalsIgnoreCase("Y")
								********************************************************************/
								&& schedulerNamesText.contains(jobId+BaseBatch.JOB_CONF_POST_FIX)) {
							// JOB이 Scheduler 저장대상이고 Cron정보가 존재하면 스케줄에 등록해준다.
							LOG.info("Try to Scheduler operation... JobInfoVo[{}]",voDB);
							batchMngService.setScheduler(jobId, cronCmd);
						}
						break;
					}
				}
				if(addVo!=null)
					batchInfoListAddDB.add(addVo);
			}
			if(batchInfoListAddDB.size()>0) {
				batchInfoService.addBatchJobInfoList(batchInfoListAddDB);
			}
		} catch (Exception e) {
			LOG.error("sysId[{}] / svrAddr[{}], Scheduler operation failure... cron info[{}]\n{}",batchInfoBaseVo.getKindCd(), batchInfoBaseVo.getSvrTypeCd(), cronCmd, e);
			System.exit(-2);
		}
		LOG.info("BatchApplicationListener.Ended");
	}

}
