package com.itcall.SpringSecurityRSA.config.handler;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.itcall.SpringSecurityRSA.rsa.SecureRsaCripto;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@WebListener
public class RsaSessionListener implements HttpSessionListener {

	private static final int DEF_SESSION_LIMIT_SECONDS = 3600; // 기본 세션 유지기간 3600초 (1시간)
	private static final int DEF_SESSION_LIMIT_MINIMIZE_SECONDS = 180; // 세션 유지기간은 최소 180초(3분) 이하로 설정할 수 없다.

	@Value("${session.timeout:"+DEF_SESSION_LIMIT_SECONDS+"}")
	private int sessionLimitSeconds;

	@Override
	public void sessionCreated(HttpSessionEvent event) {
		HttpSessionListener.super.sessionCreated(event);
		int sessionLimitTime = this.sessionLimitSeconds>=DEF_SESSION_LIMIT_MINIMIZE_SECONDS?this.sessionLimitSeconds:DEF_SESSION_LIMIT_SECONDS;
		event.getSession().setMaxInactiveInterval(sessionLimitTime); // 초단위이며, 요청발생할 때마다 갱신해준다.
		log.debug("=== Session is created for setMaxInactiveInterval([{}]seconds) === [{}]", sessionLimitTime, event.getSession());
		try {
			HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
			log.debug("=== Request URI[{}]", request.getRequestURI());
			if ( StringUtils.isEmpty(event.getSession().getAttribute(SecureRsaCripto.RSA_PUB_MODULE)) && (request.getRequestURI().endsWith("login") || request.getRequestURI().endsWith("loginCust") || request.getRequestURI().endsWith("loginError") /* || request.getRequestURI().endsWith("") */)) {
				SecureRsaCripto.initRsaSession(request, event.getSession());
			}
		} catch (Exception e) {
			log.error("Can't created Dynamic RSA component : [{}]\n{}", e.getMessage(), e);
		}
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent event) {
		HttpSessionListener.super.sessionDestroyed(event);
		log.debug("=== Session is destroyed === [{}]", event.getSession());
	}

}
