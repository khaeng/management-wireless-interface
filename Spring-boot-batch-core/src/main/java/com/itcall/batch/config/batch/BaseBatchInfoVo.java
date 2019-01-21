package com.itcall.batch.config.batch;

public class BaseBatchInfoVo {

	private String jobNm;
	private String[] arrStapNm;
	private String scheduled;
	private String nextJobNm;
	private String[] arrJobParam;
	private boolean withoutMetaDB;

	public BaseBatchInfoVo() {
		super();
	}

	public BaseBatchInfoVo(String jobNm, String[] arrStapNm, String scheduled, String nextJobNm, String[] arrJobParam, boolean withoutMetaDB) {
		super();
		this.jobNm = jobNm;
		this.arrStapNm = arrStapNm;
		this.scheduled = scheduled;
		this.nextJobNm = nextJobNm;
		this.arrJobParam = arrJobParam;
		this.withoutMetaDB = withoutMetaDB;
	}

	public String getJobNm() {
		return jobNm;
	}

	public void setJobNm(String jobNm) {
		this.jobNm = jobNm;
	}

	public String[] getArrStapNm() {
		return arrStapNm;
	}

	public void setArrStapNm(String[] arrStapNm) {
		this.arrStapNm = arrStapNm;
	}

	public String getScheduled() {
		return scheduled;
	}

	public void setScheduled(String scheduled) {
		this.scheduled = scheduled;
	}

	public String getNextJobNm() {
		return nextJobNm;
	}

	public void setNextJobNm(String nextJobNm) {
		this.nextJobNm = nextJobNm;
	}

	public String[] getArrJobParam() {
		return arrJobParam;
	}

	public void setArrJobParam(String[] arrJobParam) {
		this.arrJobParam = arrJobParam;
	}

	public boolean isWithoutMetaDB() {
		return withoutMetaDB;
	}

	public void setWithoutMetaDB(boolean withoutMetaDB) {
		this.withoutMetaDB = withoutMetaDB;
	}

}
