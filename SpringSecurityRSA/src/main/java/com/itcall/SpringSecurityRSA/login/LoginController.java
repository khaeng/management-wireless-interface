package com.itcall.SpringSecurityRSA.login;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.NoSuchPaddingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

import com.itcall.SpringSecurityRSA.rsa.SecureRsaCripto;

@Controller
public class LoginController {

	private static final Logger log = LoggerFactory.getLogger(LoginController.class);

	@Autowired
	private UserSecurityService userSecurityService;

	@PreAuthorize("isAuthenticated()")
	@RequestMapping(value = {"/service/test"})
	public ResponseEntity<Object> test(HttpServletRequest request, Model model) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		User user = (User)authentication.getPrincipal();
		model.addAttribute("name", request.getRequestURI() + " : TEST");
		model.addAttribute("username", user.getUsername());
		model.addAttribute("userInfo", user);
		return new ResponseEntity<Object>(model, HttpStatus.OK);
	}

	@RequestMapping(value = {"/service/test2"}, method = RequestMethod.POST , produces = {"application/json"})
	public ResponseEntity<Object> getComnCodePage(HttpServletRequest request, ModelMap map) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		User user = (User)authentication.getPrincipal();
		map.addAttribute("name", request.getRequestURI() + " : TEST");
		map.addAttribute("username", user.getUsername());
		map.addAttribute("userInfo", user);
		return new ResponseEntity<Object>(map, HttpStatus.OK);
	}

	@PreAuthorize("isAuthenticated()")
	@RequestMapping(value = {"/main", "/home"})
	public String main(HttpServletRequest request, Model model) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		User user = (User)authentication.getPrincipal();
		model.addAttribute("name", "Spring-boot Security with RSA test...");
		model.addAttribute("username", user.getUsername());
		return "hello";
	}

	// 로그인
	@RequestMapping("/login")
	public String login(HttpServletRequest request, Model model, String error, String logout) throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException {
		if (logout != null) {
			model.addAttribute("logout", "You have been logged out successfully.");
		}
		SecureRsaCripto.initRsaSession(request);
		return "login/login";
	}

	@RequestMapping("/loginCust")
	public String loginCust(HttpServletRequest request, Model model, String error, String logout) throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException {
		if (logout != null) {
			model.addAttribute("logout", "You have been logged out successfully.");
		}
		SecureRsaCripto.initRsaSession(request);
		return "login/login_cust";
	}

	// 로그인 실패시
	@RequestMapping(value = "/loginError")
	public String loginError(HttpServletRequest request, Model model) throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException {
		model.addAttribute("error", "Your username and password is invalid.");
		try {
			String username = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest().getAttribute("username").toString();
			model.addAttribute("username", username);
		} catch (Exception e) {
		}
		SecureRsaCripto.initRsaSession(request);
		return "login/login";
	}

	// Redirect with POST 로그인(자동) 처리
	@PostMapping(value = "/loginRedirect")
	public ModelAndView loginRedirect(HttpServletRequest request, HttpServletResponse response, Model model, String username, String password) throws Exception {
		request.setAttribute(View.RESPONSE_STATUS_ATTRIBUTE, HttpStatus.TEMPORARY_REDIRECT);
		return new ModelAndView("redirect:/login_post");
	}

	// 로그인(자동) 처리
	@PostMapping(value = "/loginProcess")
	public String loginProcess(HttpServletRequest request, HttpServletResponse response, Model model, String username, String password) throws Exception {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		log.info("before /loginProcess Principal[{}]", authentication);
		username = SecureRsaCripto.getRSaDecodeFromSession(request, username);
		password = SecureRsaCripto.getRSaDecodeFromSession(request, password);
		request.setAttribute("username", username);
		userSecurityService.autologin(request, username, password);
		authentication = SecurityContextHolder.getContext().getAuthentication();
		User user = (User)authentication.getPrincipal();
		log.info("alfter /loginProcess Principal[{}]", user);
		return "redirect:/main";// return "hello";
	}


}
