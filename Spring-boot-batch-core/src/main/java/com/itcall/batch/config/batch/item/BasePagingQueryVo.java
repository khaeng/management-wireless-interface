package com.itcall.batch.config.batch.item;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.batch.item.database.Order;
import org.springframework.jdbc.core.RowMapper;

public class BasePagingQueryVo {

	private int fetchSize = 0;
	private int pageSize = 10;
	private DataSource dataSource;
	private String selectClause;
	private String fromClause;
	private String whereClause;
	private Map<String, Order> sortKeys = new LinkedHashMap<String, Order>();
	private RowMapper<?> rowMapper;

	public Map<String, Order> addSortKey(String key, Order order) {
		sortKeys.put(key, order);
		return sortKeys;
	}

	public int getFetchSize() {
		return fetchSize;
	}
	public void setFetchSize(int fetchSize) {
		this.fetchSize = fetchSize;
	}
	public int getPageSize() {
		return pageSize;
	}
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public String getSelectClause() {
		return selectClause;
	}

	public void setSelectClause(String selectClause) {
		this.selectClause = selectClause;
	}

	public String getFromClause() {
		return fromClause;
	}

	public void setFromClause(String fromClause) {
		this.fromClause = fromClause;
	}

	public String getWhereClause() {
		return whereClause;
	}

	public void setWhereClause(String whereClause) {
		this.whereClause = whereClause;
	}

	public Map<String, Order> getSortKeys() {
		return sortKeys;
	}
	public RowMapper<?> getRowMapper() {
		return this.rowMapper;
	}
	public void setRowMapper(RowMapper<?> rowMapper) {
		this.rowMapper = rowMapper;
	}
}
