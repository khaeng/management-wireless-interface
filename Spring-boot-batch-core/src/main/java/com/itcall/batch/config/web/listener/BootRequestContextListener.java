package com.itcall.batch.config.web.listener;

import javax.servlet.annotation.WebListener;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextListener;

@Configuration
@WebListener
public class BootRequestContextListener extends RequestContextListener {

	/**********************************************************
	 * For
	 * ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()) 
	 **********************************************************/
}
