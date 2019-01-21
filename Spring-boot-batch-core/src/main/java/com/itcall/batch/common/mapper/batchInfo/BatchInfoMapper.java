package com.itcall.batch.common.mapper.batchInfo;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.itcall.batch.common.mapper.base.BaseMapper;
import com.itcall.batch.common.vo.batchInfo.BatchInfoHstVo;
import com.itcall.batch.common.vo.batchInfo.BatchInfoVo;
import com.itcall.batch.config.support.Master;

@Master
public interface BatchInfoMapper extends BaseMapper {

	public List<BatchInfoVo> selectBatchInfoList(BatchInfoVo batchInfoVo) throws Exception;
	public BatchInfoVo selectBatchInfo(BatchInfoVo batchInfo);
	public int insertBatchJobInfo(@Param("batchInfo") BatchInfoVo batchInfo); // 단일Insert. 위에는 다중Insert.Altibase만 다중 insert가 안먹어서 하나씩 for문으로 변경함.
	public int insertBatchJobInfoList(@Param("batchInfoList") List<BatchInfoVo> batchInfoList);
	public int updateBatchJobInfo(@Param("batchInfo") BatchInfoVo batchInfo) throws Exception;

	public List<BatchInfoVo> selectTest(@Param("batchInfoList") List<BatchInfoVo> batchInfoList) throws Exception;
	public BatchInfoVo selectBatchJobInstance(BatchInfoVo batchInfoVo) throws Exception;
	public int insertBatchJobInstance(BatchInfoVo batchInfoVo) throws Exception;
	public int deleteBatchJobInstance(BatchInfoVo batchInfoVo) throws Exception;


	public List<BatchInfoHstVo> selectBatchInfoHstList(/*@Param("batchInfoHst")*/ BatchInfoHstVo batchInfoHst) throws Exception;
	public List<BatchInfoHstVo> selectBatchInfoHstLastStatus(/*@Param("batchInfoHst")*/ BatchInfoHstVo batchInfoHst);
	public BatchInfoHstVo selectBatchInfoHstLastOne(/*@Param("batchInfoHst")*/ BatchInfoHstVo batchInfoHst) throws Exception;
	public int insertBatchInfoHst(/*@Param("batchInfoHst")*/ BatchInfoHstVo batchInfoHst) throws Exception;
	public int updateBatchInfoHst(/*@Param("batchInfoHst")*/ BatchInfoHstVo batchInfoHst) throws Exception;


}
