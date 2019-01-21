package com.itcall.batch.common.vo.batchInfo;

import com.itcall.batch.common.vo.base.BaseVo;
import com.itcall.batch.config.support.EnvironmentSupport;

public class BatchInfoVo extends BaseVo {

	private static final long serialVersionUID = 1L;

	private String jobName;

	private String cronCmd; // ("0/10 * * * * *");

	private String useYn;

	private String kindCd;

	private String svrTypeCd;

	private long jobInstanceId;

	private String jobKey;

	private int version;

	private String jobViewName;

	private String jobDesc;

	public BatchInfoVo() {
		this.setKindCd(EnvironmentSupport.getBatchSysId());
		this.setSvrTypeCd(EnvironmentSupport.getSvrTypeCd());
	}

	public BatchInfoVo(String jobName) {
		this();
		this.jobName = jobName;
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public String getCronCmd() {
		return cronCmd;
	}

	public void setCronCmd(String cronCmd) {
		this.cronCmd = cronCmd;
	}

	public String getUseYn() {
		return useYn;
	}

	public void setUseYn(String useYn) {
		this.useYn = useYn;
	}

	public String getKindCd() {
		return kindCd;
	}

	public void setKindCd(String kindCd) {
		this.kindCd = kindCd;
	}

	public long getJobInstanceId() {
		return jobInstanceId;
	}

	public void setJobInstanceId(long jobInstanceId) {
		this.jobInstanceId = jobInstanceId;
	}

	public String getJobKey() {
		return jobKey;
	}

	public void setJobKey(String jobKey) {
		this.jobKey = jobKey;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public String getSvrTypeCd() {
		return svrTypeCd;
	}

	public void setSvrTypeCd(String svrTypeCd) {
		this.svrTypeCd = svrTypeCd;
	}

	public String getJobViewName() {
		return jobViewName;
	}

	public void setJobViewName(String jobViewName) {
		this.jobViewName = jobViewName;
	}

	public String getJobDesc() {
		return jobDesc;
	}

	public void setJobDesc(String jobDesc) {
		this.jobDesc = jobDesc;
	}

	public boolean isExistJob(String jobName, String kindCd, String svrTypeCd) {
		if(jobName!=null && kindCd!=null && svrTypeCd!=null && this.jobName.equals(jobName) && this.kindCd.equals(kindCd) && this.svrTypeCd.equals(svrTypeCd))
			return true;
		return false;
	}

	public boolean isExistJob(BatchInfoVo srcBatchInfoVo) {
		return isExistJob(srcBatchInfoVo.getJobName(), srcBatchInfoVo.getKindCd(), srcBatchInfoVo.getSvrTypeCd());
	}

	@Override
	public String toString() {
		return "BatchInfoVo [jobName=" + jobName + ", cronCmd=" + cronCmd + ", useYn=" + useYn + ", kindCd=" + kindCd
				+ ", svrTypeCd=" + svrTypeCd + ", jobInstanceId=" + jobInstanceId + ", jobKey=" + jobKey + ", version="
				+ version + ", jobViewName=" + jobViewName + ", jobDesc=" + jobDesc + "]";
	}



}
