package com.itcall.SpringSecurityRSA.login;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.itcall.SpringSecurityRSA.rsa.SecureRsaCripto;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class RestApiController {

	@PostMapping(path = {"/getRsaPublicInfo"}, produces = {"application/json; charset=UTF-8"})
	private ResponseEntity<?> getRsaPublicInfo( HttpServletRequest request, ModelMap model) {
		try {
			HttpSession session = request.getSession(false);
			if(StringUtils.isEmpty(session.getAttribute(SecureRsaCripto.RSA_PUB_MODULE))) {
				SecureRsaCripto.initRsaSession(request, session);
			}
			model.addAttribute("rsaModule",  session.getAttribute(SecureRsaCripto.RSA_PUB_MODULE));
			model.addAttribute("rsaExponent",  session.getAttribute(SecureRsaCripto.RSA_PUB_EXPONENT));
			// model.addAttribute("rsaPublic",  session.getAttribute(SecureRsaCripto.RSA_PUB_KEY));
			return ResponseEntity.ok(model);
		}catch (Exception e) {
			model.addAttribute("rsaExponent", e.getMessage());
		}
		return ResponseEntity.ok(model);
	}

	@PreAuthorize("isAuthenticated()")
	@RequestMapping(value = {"/service/test"})
	public ResponseEntity<Object> getTest(HttpServletRequest request, Model model) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		User user = (User)authentication.getPrincipal();
		model.addAttribute("name", request.getRequestURI() + " : TEST1");
		model.addAttribute("username", user.getUsername());
		model.addAttribute("userInfo", user);
		return new ResponseEntity<Object>(model, HttpStatus.OK);
	}
	@RequestMapping(value = {"/service/test2"}, method = RequestMethod.POST , produces = {"application/json"})
	public ResponseEntity<Object> getTest2(HttpServletRequest request, ModelMap map) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		User user = (User)authentication.getPrincipal();
		map.addAttribute("name", request.getRequestURI() + " : TEST2");
		map.addAttribute("username", user.getUsername());
		map.addAttribute("userInfo", user);
		return new ResponseEntity<Object>(map, HttpStatus.OK);
	}
	@RequestMapping(value = {"/service/test3" }, method = RequestMethod.POST, produces = {"application/json; charset=UTF-8"} )
	public ResponseEntity<?> getTest3(HttpServletRequest request, Map<String, Object> param, ModelMap map) {
		map.addAttribute("name", request.getRequestURI() + " : TEST3");
		map.addAllAttributes(param);
		log.info("/service/test3 : {}", map);
		return ResponseEntity.ok(map);
	}
	@RequestMapping(value = {"/service/test4" }, method = RequestMethod.POST, produces = {"application/json; charset=UTF-8"} )
	public ResponseEntity<?> getTest4(HttpServletRequest request, @RequestParam Map<String, Object> param, ModelMap map) {
		map.addAttribute("name", request.getRequestURI() + " : TEST4");
		map.addAllAttributes(param);
		log.info("/service/test4 : {}", map);
		return ResponseEntity.ok(map);
	}

}
