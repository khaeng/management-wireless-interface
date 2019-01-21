package com.itcall.batch.config.batch.item;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.support.AbstractSqlPagingQueryProvider;
import org.springframework.batch.item.database.support.Db2PagingQueryProvider;
import org.springframework.batch.item.database.support.DerbyPagingQueryProvider;
import org.springframework.batch.item.database.support.H2PagingQueryProvider;
import org.springframework.batch.item.database.support.HsqlPagingQueryProvider;
import org.springframework.batch.item.database.support.MySqlPagingQueryProvider;
import org.springframework.batch.item.database.support.OraclePagingQueryProvider;
import org.springframework.batch.item.database.support.PostgresPagingQueryProvider;
import org.springframework.batch.item.database.support.SqlServerPagingQueryProvider;
import org.springframework.batch.item.database.support.SqlitePagingQueryProvider;
import org.springframework.batch.item.database.support.SybasePagingQueryProvider;
import org.springframework.batch.support.DatabaseType;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.MetaDataAccessException;

import com.itcall.batch.common.service.batchInfo.BatchInfoService;
import com.itcall.batch.common.vo.batchInfo.BatchInfoHstVo;
import com.itcall.batch.config.batch.JobParamCode;

public abstract class BaseDbPagingReader<I> extends JdbcPagingItemReader<I> {

	protected Logger LOG = LoggerFactory.getLogger(getClass());

	private StepExecution stepExecution;
	private String[] jobParameters;
	@Resource
	private BatchInfoService batchInfoService;
	private boolean isBizMsgWrited;

	protected PagingQueryProvider queryProvider;
	protected DatabaseType databaseType;

	protected abstract BasePagingQueryVo initDbPagingReaderVo();
	protected abstract void beforeStep(StepExecution stepExecution);
	protected abstract void afterStep(StepExecution stepExecution);


	@SuppressWarnings("unchecked")
	@PostConstruct
	public void initialize() throws MetaDataAccessException {
		BasePagingQueryVo basePagingQueryVo = initDbPagingReaderVo();
		setDataSource(basePagingQueryVo.getDataSource());
		setPageSize(basePagingQueryVo.getPageSize()); // Default 10ea
		setFetchSize(basePagingQueryVo.getFetchSize());
		try {
			this.databaseType = DatabaseType.fromMetaData(basePagingQueryVo.getDataSource());
		} catch (Exception e) {
			this.databaseType = DatabaseType.ORACLE;
		}
		switch (databaseType) {
		case DB2:
		case DB2AS400:
		case DB2VSE:
		case DB2ZOS:
			queryProvider = new Db2PagingQueryProvider();
			break;
		case DERBY:
			queryProvider = new DerbyPagingQueryProvider();
			break;
		case H2:
			queryProvider = new H2PagingQueryProvider();
			break;
		case HSQL:
			queryProvider = new HsqlPagingQueryProvider();
			break;
		case MYSQL:
			queryProvider = new MySqlPagingQueryProvider();
			break;
		case ORACLE:
			queryProvider = new OraclePagingQueryProvider();
			break;
		case POSTGRES:
			queryProvider = new PostgresPagingQueryProvider();
			break;
		case SQLITE:
			queryProvider = new SqlitePagingQueryProvider();
			break;
		case SQLSERVER:
			queryProvider = new SqlServerPagingQueryProvider();
			break;
		case SYBASE:
			queryProvider = new SybasePagingQueryProvider();
			break;
		default:
			queryProvider = new OraclePagingQueryProvider(); // Will be make Custom query provider...
			break;
		}

		((AbstractSqlPagingQueryProvider) queryProvider).setSelectClause(basePagingQueryVo.getSelectClause());
		((AbstractSqlPagingQueryProvider) queryProvider).setFromClause(basePagingQueryVo.getFromClause());
		((AbstractSqlPagingQueryProvider) queryProvider).setWhereClause(basePagingQueryVo.getWhereClause());
		((AbstractSqlPagingQueryProvider) queryProvider).setSortKeys(basePagingQueryVo.getSortKeys());
		setQueryProvider(queryProvider);
		if(basePagingQueryVo.getRowMapper()!=null)
			setRowMapper((RowMapper<I>) basePagingQueryVo.getRowMapper());
	}

	@BeforeStep
	public void readerBeforeStep(StepExecution stepExecution) {
		this.stepExecution = stepExecution;
		String[] params = JobParamCode.getParameters(stepExecution.getJobParameters());
		this.jobParameters = params;
		beforeStep(stepExecution);
	}

	@AfterStep
	public void readerAfterStep(StepExecution stepExecution) {
		afterStep(stepExecution);
	}

	@Override
	public void close() {
		try {
			if(!this.isBizMsgWrited && this.queryProvider!=null && this.stepExecution!=null && this.stepExecution.getReadCount()>0)
				addBizMsg(this.stepExecution.getReadCount(), new StringBuffer().append("ReadCount[").append(this.stepExecution.getReadCount()).append("], WriteCount[").append(this.stepExecution.getWriteCount()).append("], SkipRead[").append(this.stepExecution.getReadSkipCount()).append("], SkipWrite[").append(this.stepExecution.getWriteSkipCount()).append("]:" ).append(this.toString()).toString());
		} catch (Exception e) {
			LOG.error("addBizMsg add error : {}", e);
		}
		super.close();
	}

	protected void addBizMsg(int resultCnt, String resultMsg) throws Exception {
		this.isBizMsgWrited = true;
		JobExecution jobExecution = this.stepExecution.getJobExecution();
		BatchInfoHstVo batchInfoHst = new BatchInfoHstVo(jobExecution);
		batchInfoHst.setRestCnt(resultCnt);
		batchInfoHst.setRestMsg(resultMsg);
		batchInfoService.setBatchInfoHst(batchInfoHst);
	}

	public StepExecution getStepExecution() {
		return stepExecution;
	}

	public String[] getJobParameters() {
		return jobParameters;
	}

	public void setJobParameters(String[] jobParameters) {
		this.jobParameters = jobParameters;
	}

}
