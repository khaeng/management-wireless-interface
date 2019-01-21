package com.itcall.batch.jobs.sample.dbTest;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.database.Order;
import org.springframework.jdbc.core.RowMapper;

import com.itcall.batch.common.vo.batchInfo.BatchInfoVo;
import com.itcall.batch.config.batch.item.BaseDbPagingReader;
import com.itcall.batch.config.batch.item.BasePagingQueryVo;

public class DbTestJobReader extends BaseDbPagingReader<List<BatchInfoVo>> {

	@Resource
	private DataSource dataSource;
	private int chunkSize;

	public DbTestJobReader() {
		this(10);
	}
	public DbTestJobReader(int chunkSize) {
		this.chunkSize = chunkSize;
	}

	@Override
	protected BasePagingQueryVo initDbPagingReaderVo() {
		BasePagingQueryVo basePagingQueryVo = new BasePagingQueryVo();
		basePagingQueryVo.setDataSource(dataSource);
		basePagingQueryVo.setPageSize(this.chunkSize); // Default 10ea
		basePagingQueryVo.setFetchSize(this.chunkSize);
		basePagingQueryVo.setSelectClause("SELECT * ");
		basePagingQueryVo.setFromClause(" FROM BATCH_INFO ");
		basePagingQueryVo.setWhereClause("");
		basePagingQueryVo.addSortKey("kind_Cd", Order.ASCENDING);
		basePagingQueryVo.addSortKey("svr_Type_Cd", Order.ASCENDING);
		basePagingQueryVo.addSortKey("job_Name", Order.ASCENDING);
		basePagingQueryVo.setRowMapper(new RowMapper<BatchInfoVo>() {
			@Override
			public BatchInfoVo mapRow(ResultSet rs, int rowNum) throws SQLException {
				BatchInfoVo batchInfoVo = new BatchInfoVo();
				batchInfoVo.setCronCmd(		rs.getString("cron_Cmd"));
				batchInfoVo.setJobDesc(		rs.getString("job_Desc"));
				batchInfoVo.setJobInstanceId(rs.getLong("job_Instance_Id"));
				batchInfoVo.setJobKey(		rs.getString("job_Key"));
				batchInfoVo.setJobName(		rs.getString("job_Name"));
				batchInfoVo.setJobViewName(	rs.getString("job_View_Name"));
				batchInfoVo.setKindCd(		rs.getString("kind_Cd"));
				batchInfoVo.setSvrTypeCd(	rs.getString("svr_Type_Cd"));
				batchInfoVo.setUseYn(		rs.getString("use_Yn"));
				batchInfoVo.setVersion(		rs.getInt("version"));
				return batchInfoVo;
			}
		});
		return basePagingQueryVo;
	}

	@Override
	protected void beforeStep(StepExecution stepExecution) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void afterStep(StepExecution stepExecution) {
		// TODO Auto-generated method stub

	}

}
