package com.itcall.SpringSecurityRSA;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication(scanBasePackages = {"com.itcall.SpringSecurityRSA"})
@EnableAutoConfiguration
public class SpringBootStarter extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		SpringApplicationBuilder builder = application.sources(SpringBootStarter.class);
		// builder. // 프로퍼티 설정
		// builder.properties("spring.thymeleaf.check-template-location=false");
		return builder;
	}
	
	public static void main(String[] args) {
		
		/********************************************
		 * Log4j-2 for Jansi Support is skiped...
		 * Need to option : -Dlog4j.skipJansi=false
		 ********************************************/
		System.setProperty("log4j.skipJansi", "false");
		/********************************************
		 * Eclipse Error in Debug >>> throw new SilentExitException();
		 ********************************************/
		System.setProperty("spring.devtools.restart.enabled", "false");
		
		SpringApplication.run(SpringBootStarter.class, args);
	}
}
