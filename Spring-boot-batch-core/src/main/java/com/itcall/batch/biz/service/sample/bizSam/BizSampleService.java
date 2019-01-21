package com.itcall.batch.biz.service.sample.bizSam;

import java.util.List;

import com.itcall.batch.biz.vo.sample.bizSam.SampleInfoVo;

public interface BizSampleService {

	public String bizSampleMethod(String sample) throws Exception;

	public List<SampleInfoVo> selectListTest() throws Exception;

}
