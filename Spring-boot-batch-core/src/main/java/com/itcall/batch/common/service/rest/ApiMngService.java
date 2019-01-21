package com.itcall.batch.common.service.rest;

import java.util.Map;

import com.itcall.batch.common.support.code.ApiCmdCd;

public interface ApiMngService {

	Map<String, Object> apiManager(ApiCmdCd cmd, String value, String options);

}
