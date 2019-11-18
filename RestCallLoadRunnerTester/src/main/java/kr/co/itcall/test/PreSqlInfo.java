package kr.co.itcall.test;

import java.util.List;
import java.util.Map;

public class PreSqlInfo {

	private String preSqlQuery;
	
	private String realSqlQuery;
	
	private List<Map<String, Object>> preSqlResult;

	public String getPreSqlQuery() {
		return preSqlQuery;
	}

	public void setPreSqlQuery(String preSqlQuery) {
		this.preSqlQuery = preSqlQuery;
	}

	public String getRealSqlQuery() {
		return realSqlQuery;
	}

	public void setRealSqlQuery(String realSqlQuery) {
		this.realSqlQuery = realSqlQuery;
	}

	public List<Map<String, Object>> getPreSqlResult() {
		return preSqlResult;
	}

	public void setPreSqlResult(List<Map<String, Object>> preSqlResult) {
		this.preSqlResult = preSqlResult;
	}

}
