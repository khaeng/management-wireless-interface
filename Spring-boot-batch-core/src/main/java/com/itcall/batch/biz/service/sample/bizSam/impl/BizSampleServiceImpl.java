package com.itcall.batch.biz.service.sample.bizSam.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.itcall.batch.biz.mapper.sample.bizSam.SampleInfoMapper;
import com.itcall.batch.biz.service.sample.bizSam.BizSampleService;
import com.itcall.batch.biz.vo.sample.bizSam.SampleInfoVo;
import com.itcall.batch.common.service.base.BaseService;

@Service
public class BizSampleServiceImpl extends BaseService implements BizSampleService {

	@Resource
	private SampleInfoMapper sampleInfoMapper;

	@Override
	public String bizSampleMethod(String sample) throws Exception {
		return "I'm your service class Your data is '" + sample + "'";
	}

	@Override
	public List<SampleInfoVo> selectListTest() throws Exception {
		List<SampleInfoVo> list = sampleInfoMapper.selectListTest(new String[] {"TEST_01","Test_02","Test 03"});
		return list;
	}

	
}
