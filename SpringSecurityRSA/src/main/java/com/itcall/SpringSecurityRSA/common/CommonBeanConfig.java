package com.itcall.SpringSecurityRSA.common;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class CommonBeanConfig {

	@Profile({"default", "!prd && !prod"})
	@Bean("getCommonBean")
	public String getDefCommonBean() {
		return "기본 Bean값";
	}
	
	@Profile({"prd", "prod"})
	@Bean("getCommonBean")
	public String getPrdCommonBean() {
		return "prd Bean값";
	}

}
