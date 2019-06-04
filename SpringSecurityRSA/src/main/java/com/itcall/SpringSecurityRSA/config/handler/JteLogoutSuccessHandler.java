package com.itcall.SpringSecurityRSA.config.handler;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

public class JteLogoutSuccessHandler implements LogoutSuccessHandler/* extends SimpleUrlLogoutSuccessHandler */ {

	@Override
	public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

		// http.logout().logoutUrl("/logout").logoutSuccessUrl("/login") 에서 처리됨. 
		// setDefaultTargetUrl("/login");

	}

}
