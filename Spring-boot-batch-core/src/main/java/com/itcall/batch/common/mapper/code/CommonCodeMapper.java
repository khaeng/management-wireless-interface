package com.itcall.batch.common.mapper.code;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.itcall.batch.common.mapper.base.BaseMapper;
import com.itcall.batch.config.support.Master;

@Master
public interface CommonCodeMapper extends BaseMapper {
	/************** 시스템 플레그 ******************/
	public List<Map<String,String>> selectSysContlFlagList(@Param("param") String param);

}
