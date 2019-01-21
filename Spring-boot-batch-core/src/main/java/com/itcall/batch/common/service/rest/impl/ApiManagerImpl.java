package com.itcall.batch.common.service.rest.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.itcall.batch.common.service.rest.ApiMngService;
import com.itcall.batch.common.support.code.ApiCmdCd;

@Service
public class ApiManagerImpl implements ApiMngService {

	@Override
	public Map<String, Object> apiManager(ApiCmdCd cmd, String value, String options) {
		Map<String, Object> result = new HashMap<String, Object>();
		switch (cmd) {
		case ADD:
			break;
		case ALL_VALUES:
			break;
		case CANCLE:
			break;
		case GET:
			break;
		case LIST:
			break;
		case SAVE:
			break;
		case START:
			break;
		case STOP:
			break;
		default:
		}
		return result;
	}

}
