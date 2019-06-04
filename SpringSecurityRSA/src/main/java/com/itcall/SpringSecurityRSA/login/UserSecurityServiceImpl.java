package com.itcall.SpringSecurityRSA.login;

import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Service;

@Service
public class UserSecurityServiceImpl implements UserSecurityService {
	
	private static final Logger log = LoggerFactory.getLogger(UserSecurityServiceImpl.class);

	@Autowired
	private UserDetailsService userDetailsService;

	@Autowired
	private AuthenticationManager authenticationManager;

	@Override
	public String findLoggedInUsername() {
		Object userDetails = SecurityContextHolder.getContext().getAuthentication().getDetails();
		if (userDetails instanceof UserDetails) {
			return ((UserDetails) userDetails).getUsername();
		}
		return null;
	}

	@Override
	public void autologin(HttpServletRequest request, String username, String password) {

//		Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
//		grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
//		grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_USER"));
//		User userDetails = new User(username, password, true, true, true, true, grantedAuthorities);
		
		User userDetails = (User) userDetailsService.loadUserByUsername(username);

		UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
				userDetails, password, userDetails.getAuthorities());
		// SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

//		request.getSession();

		usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetails(request));

		log.info("회원가입 성공 후 자동 로그인처리 ::: 사용자정보[{}]", userDetails);

//		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		
		Authentication authenticatedUser = authenticationManager.authenticate(usernamePasswordAuthenticationToken);
		SecurityContextHolder.getContext().setAuthentication(authenticatedUser);

//		authentication = SecurityContextHolder.getContext().getAuthentication();

		if (usernamePasswordAuthenticationToken.isAuthenticated()) {
			SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
			log.info("Auto login[{}]/[{}] successfully!", username, password);
		}
	}


//	@Override
//	public void autologin(HttpServletRequest request, String username, String password) {
//		UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, password);
//		authToken.setDetails(new WebAuthenticationDetails(request));
//
//		Authentication authentication = authenticationManager.authenticate(authToken);
//
//		SecurityContextHolder.getContext().setAuthentication(authentication);
//	}

}
