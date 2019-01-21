package com.itcall.batch.config.web.listener;

import javax.annotation.Resource;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.itcall.batch.common.service.batchInfo.BatchInfoService;
import com.itcall.batch.common.service.rest.BatchMngService;
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
@Component("batchServletContextListener")
public class BatchServletContextListener implements ServletContextListener {

	private final static Logger LOG = LoggerFactory.getLogger(BatchServletContextListener.class);

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
	public void contextDestroyed(ServletContextEvent event) {
		LOG.debug("BatchServletContextListener.Stopped");

	}

	@Override
	public void contextInitialized(ServletContextEvent event) {

		LOG.info("BatchServletContextListener.Started");

		// 환경초기화...
		EnvironmentSupport.initFirstSet(this.ctx);

		LOG.info("BatchServletContextListener.Ended");
	}

}
