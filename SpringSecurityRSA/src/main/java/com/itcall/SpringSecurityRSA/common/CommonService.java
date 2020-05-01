package com.itcall.SpringSecurityRSA.common;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.ui.Model;

public interface CommonService {

	public Map<String, String> getCommonMap(HttpServletRequest request, Model model);

}
