package com.itcall.batch.common.vo.batchInfo;

import java.util.Arrays;
import java.util.Date;

import org.springframework.batch.core.JobExecution;

import com.itcall.batch.common.vo.base.BaseVo;
import com.itcall.batch.config.batch.JobParamCode;
import com.itcall.batch.config.support.EnvironmentSupport;

public class BatchInfoHstVo extends BaseVo {

	private static final long serialVersionUID = 1L;

	private String jobName;
	private Date createTime;

	private String kindCd;
	private String svrTypeCd;

	private long jobId;
	private long jobExecId;

	private Date startTime;
	private Date endTime;
	private String status;
	private String exitCode;
	private String exitMsg;
	private Date lastUpdated;
	private String parameters;

	private int restCnt;
	private String restMsg;

	public BatchInfoHstVo() {
		this.setKindCd(EnvironmentSupport.getBatchSysId());
		this.setSvrTypeCd(EnvironmentSupport.getSvrTypeCd());
	}
	public BatchInfoHstVo(JobExecution jobExecution) {
		this();
		if(jobExecution!=null) {
			this.setJobId(jobExecution.getJobId());
			this.setJobName(jobExecution.getJobInstance().getJobName());
			this.setJobExecId(jobExecution.getId());
			this.setCreateTime(jobExecution.getCreateTime());
			this.setStartTime(jobExecution.getStartTime());
			this.setStatus(jobExecution.getStatus().name());
			this.setParameters(Arrays.toString(JobParamCode.getParameters(jobExecution.getJobParameters())));
		}
	}
	public String getJobName() {
		return jobName;
	}
	public void setJobName(String jobName) {
		this.jobName = jobName;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public String getKindCd() {
		return kindCd;
	}
	public void setKindCd(String kindCd) {
		this.kindCd = kindCd;
	}
	public String getSvrTypeCd() {
		return svrTypeCd;
	}
	public void setSvrTypeCd(String svrTypeCd) {
		this.svrTypeCd = svrTypeCd;
	}
	public long getJobId() {
		return jobId;
	}
	public void setJobId(long jobId) {
		this.jobId = jobId;
	}
	public long getJobExecId() {
		return jobExecId;
	}
	public void setJobExecId(long jobExecId) {
		this.jobExecId = jobExecId;
	}
	public Date getStartTime() {
		return startTime;
	}
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	public Date getEndTime() {
		return endTime;
	}
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getExitCode() {
		return exitCode;
	}
	public void setExitCode(String exitCode) {
		this.exitCode = exitCode;
	}
	public String getExitMsg() {
		return exitMsg;
	}
	public void setExitMsg(String exitMsg) {
		this.exitMsg = exitMsg;
	}
	public Date getLastUpdated() {
		return lastUpdated;
	}
	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}
	public String getParameters() {
		return parameters;
	}
	public void setParameters(String parameters) {
		this.parameters = parameters;
	}
	public int getRestCnt() {
		return restCnt;
	}
	public void setRestCnt(int restCnt) {
		this.restCnt = restCnt;
	}
	public String getRestMsg() {
		return restMsg;
	}
	public void setRestMsg(String restMsg) {
		this.restMsg = restMsg;
	}
	@Override
	public String toString() {
		return "BatchInfoHstVo [jobName=" + jobName + ", createTime=" + createTime + ", kindCd=" + kindCd
				+ ", svrTypeCd=" + svrTypeCd + ", jobId=" + jobId + ", jobExecId=" + jobExecId + ", startTime="
				+ startTime + ", endTime=" + endTime + ", status=" + status + ", exitCode=" + exitCode
				+ ", exitMsg=" + exitMsg + ", lastUpdated=" + lastUpdated + ", parameters=" + parameters
				+ ", restCnt=" + restCnt + ", restMsg=" + restMsg + "]";
	}
	/**
	 * 조회된 실행내역의 종료코드(exitCode)가 Job의 종료상태 코드(COMPLETED, STOPPED, FAILED)로 되어있는지 체크하여 반환한다.
	 * @return
	 */
	public boolean isRunning() {
		// 배치종료상태 값 : NULL, EXECUTING, COMPLETED, NOOP, STOPPED, FAILED, UNKNOWN
		// JOB의 종료를 보장하는 값 : COMPLETED, STOPPED, FAILED
		if(this.exitCode!=null && "COMPLETED, STOPPED, FAILED".toUpperCase().contains(this.exitCode.toUpperCase()))
			return false;
		return true;
	}

}
