package com.itcall.SpringSecurityRSA.common;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

@Profile({"default", "!dev && !prd && !prod"})
@Service("commonService")
public class CommonServiceDef implements CommonService {

	@Value("${spring.profiles.active:none}")
	private String activeProfile;

	@Override
	public Map<String, String> getCommonMap(HttpServletRequest request, Model model) {
		Map<String, String> result = new HashMap<String, String>();
		result.put("profile", "default");
		result.put("active", activeProfile);
		result.put("class", this.getClass().getName());
		return result;
	}

}
