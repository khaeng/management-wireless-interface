package com.itcall.batch.common.service.batchInfo;

import java.util.List;

import com.itcall.batch.common.vo.batchInfo.BatchInfoHstVo;
import com.itcall.batch.common.vo.batchInfo.BatchInfoVo;

public interface BatchInfoService {

	public List<BatchInfoVo> getBatchInfoList(BatchInfoVo batchInfoVo) throws Exception;

	public BatchInfoVo getBatchInfo(BatchInfoVo batchInfo) throws Exception;

	public int addBatchJobInfoList(List<BatchInfoVo> batchInfoList) throws Exception;

	public BatchInfoVo getBatchJobInstance(BatchInfoVo batchInfoVo) throws Exception;

	public int addBatchJobInstance(BatchInfoVo batchInfoVo) throws Exception;

	public int setBatchInfo(BatchInfoVo batchInfo) throws Exception;




	public List<BatchInfoHstVo> getBatchInfoHstList(BatchInfoHstVo batchInfoHst) throws Exception;

	public List<BatchInfoHstVo> getBatchInfoHstLastStatus(BatchInfoHstVo batchInfoHst);

	public BatchInfoHstVo getBatchInfoHstLastOne(BatchInfoHstVo batchInfoHst) throws Exception;

	public int addBatchInfoHst(BatchInfoHstVo batchInfoHst) throws Exception;

	public int setBatchInfoHst(BatchInfoHstVo batchInfoHst) throws Exception;




}
