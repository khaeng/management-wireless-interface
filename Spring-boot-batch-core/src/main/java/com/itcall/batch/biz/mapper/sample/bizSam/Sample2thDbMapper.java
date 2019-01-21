package com.itcall.batch.biz.mapper.sample.bizSam;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.itcall.batch.biz.vo.sample.bizSam.SampleInfoVo;
import com.itcall.batch.common.mapper.base.BaseMapper;
import com.itcall.batch.config.support.Slave;

@Slave
public interface Sample2thDbMapper extends BaseMapper {

	public List<SampleInfoVo> selectListTest(@Param("arrParam") String[] params);

}
