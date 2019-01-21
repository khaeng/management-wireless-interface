package com.itcall.batch.common.service.rest;

import java.util.Map;
import java.util.concurrent.ScheduledFuture;

public interface BatchMngService {

	public Map<String, Object> getBatchInfoList() throws Exception;

	public Map<String, Object> getRunningJobList() throws Exception;

	public int jobStart(String value, String[] parameters) throws Exception;

	public void jobAsyncStart(String value, String[] parameters) throws Exception;

	public int jobStop(String jobName) throws Exception;

	public ScheduledFuture<?> setScheduler(String jobName, String cronCmd) throws Exception;

	public boolean removeScheduler(String jobName) throws Exception;


}
