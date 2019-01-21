package com.itcall.batch.common.service.batchInfo.impl;

import java.util.List;

import javax.annotation.Resource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.itcall.batch.common.mapper.batchInfo.BatchInfoMapper;
import com.itcall.batch.common.service.base.BaseService;
import com.itcall.batch.common.service.batchInfo.BatchInfoService;
import com.itcall.batch.common.vo.batchInfo.BatchInfoHstVo;
import com.itcall.batch.common.vo.batchInfo.BatchInfoVo;

@Service
// @Transactional
// @Rollback
public class BatchInfoServiceImpl extends BaseService implements BatchInfoService {

	@Resource
	private SqlSessionFactory sqlSessionFactory;

	@Resource
	private BatchInfoMapper batchInfoMapper;

	// <mapper namespace="com.itcall.common.mapper.batchInfo.BatchInfoMapper">
	// <select id="selectBatchInfoList"
	@Override
	// @Transactional
	public List<BatchInfoVo> getBatchInfoList(BatchInfoVo batchInfoVo) throws Exception {
		List<BatchInfoVo> list = batchInfoMapper.selectBatchInfoList(batchInfoVo);

//		SqlSession sqlSession = this.sqlSessionFactory.openSession(ExecutorType.BATCH, false);
//		try {
//			for (BatchInfoVo infoVo : list) {
//				sqlSession.update("com.itcall.common.mapper.batchInfo.BatchInfoMapper.updateBatchInfoList", infoVo);
//			}
//			sqlSession.flushStatements();
//			sqlSession.commit();
//		} catch (Exception e) {
//			sqlSession.rollback();
//		}finally {
//			sqlSession.close();
//		}

		return list;
	}

	@Override
	public BatchInfoVo getBatchInfo(BatchInfoVo batchInfo) throws Exception {
		return batchInfoMapper.selectBatchInfo(batchInfo);
	}

	@Override
	public int addBatchJobInfoList(List<BatchInfoVo> batchInfoList) throws Exception {
//		for (BatchInfoVo vo : batchInfoList) {
//			batchInfoMapper.insertBatchJobInfo(vo);
//		}
//		return batchInfoList.size();
		return batchInfoMapper.insertBatchJobInfoList(batchInfoList);
	}

	@Override
	public int setBatchInfo(BatchInfoVo batchInfo) throws Exception {
		return batchInfoMapper.updateBatchJobInfo(batchInfo);
	}









	@Override
	public BatchInfoVo getBatchJobInstance(BatchInfoVo batchInfoVo) throws Exception {
		return batchInfoMapper.selectBatchJobInstance(batchInfoVo);
	}

	@Override
	public int addBatchJobInstance(BatchInfoVo batchInfoVo) throws Exception {
		// batchInfoMapper.deleteBatchJobInstance(batchInfoVo);
		return batchInfoMapper.insertBatchJobInstance(batchInfoVo);
		// throw new RuntimeException("인위적으로 저장하지 않게 하기 위해서 익셉션 처리함."); // 정상적일경우 RuntimeException에서만 RollBack이 일어난다.
		// throw new Exception("인위적으로 저장하지 않게 하기 위해서 익셉션 처리함.");
	}



	@Override
	public List<BatchInfoHstVo> getBatchInfoHstList(BatchInfoHstVo batchInfoHst) throws Exception {
		return batchInfoMapper.selectBatchInfoHstList(batchInfoHst);
	}

	@Override
	public List<BatchInfoHstVo> getBatchInfoHstLastStatus(BatchInfoHstVo batchInfoHst) {
		LOG.debug("JobName[{}] 배치 실행 전 모든 서버에서 마지막 실행상태조회.시작. ******* 중복실행 체크를 위한 기존 서버별 마지막 실행정보 조회 Before ****", batchInfoHst.getJobName());
		List<BatchInfoHstVo> result = batchInfoMapper.selectBatchInfoHstLastStatus(batchInfoHst);
		LOG.debug("JobName[{}] 배치 실행 전 모든 서버에서 마지막 실행상태조회.종료. ******* 중복실행 체크를 위한 기존 서버별 마지막 실행정보 조회 After ****", batchInfoHst.getJobName());
		return result;
	}

	@Override
	public BatchInfoHstVo getBatchInfoHstLastOne(BatchInfoHstVo batchInfoHst) throws Exception {
		LOG.debug("JobName[{}] 배치 실행 전 JobId Number의 Sync을 위해 마지막 실행결과조회.시작. ******* JobName별 전체조회 Before ****", batchInfoHst.getJobName());
		BatchInfoHstVo result = batchInfoMapper.selectBatchInfoHstLastOne(batchInfoHst);
		LOG.debug("JobName[{}] 배치 실행 전 JobId Number의 Sync을 위해 마지막 실행결과조회.시작. ******* JobName별 전체조회 After ****", batchInfoHst.getJobName());
		return result;
	}

	@Override
	@Transactional(propagation=Propagation.REQUIRES_NEW) // 배치 프로세스와 다르게 별도 커밋/롤백이 되도록 설정함.
	public int addBatchInfoHst(BatchInfoHstVo batchInfoHst) throws Exception {
		return batchInfoMapper.insertBatchInfoHst(batchInfoHst);
	}

	@Override
	@Transactional(propagation=Propagation.REQUIRES_NEW) // 배치 프로세스와 다르게 별도 커밋/롤백이 되도록 설정함.
	public int setBatchInfoHst(BatchInfoHstVo batchInfoHst) throws Exception {
		return batchInfoMapper.updateBatchInfoHst(batchInfoHst);
	}




}
