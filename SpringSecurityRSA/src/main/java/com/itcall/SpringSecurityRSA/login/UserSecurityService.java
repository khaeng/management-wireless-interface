package com.itcall.SpringSecurityRSA.login;

import javax.servlet.http.HttpServletRequest;

public interface UserSecurityService {

	public String findLoggedInUsername();

	public void autologin(HttpServletRequest request,String username, String password);

}