package com.itcall.SpringSecurityRSA.common;

import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/common")
public class CommonController {

	@Resource
	private CommonService commonService;
	
	@Resource
	private String getCommonBean;
	
	@RequestMapping(value = { "/get/commonMap" }, /* method = RequestMethod.POST, */produces = {"application/json; charset=UTF-8"} )
	public ResponseEntity<?> getTest4(HttpServletRequest request, @RequestParam Map<String, Object> param, Model model) {
		model.addAttribute("name", request.getRequestURI() + " : Profile Test.");
		model.addAllAttributes(param);
		model.addAttribute("result", commonService.getCommonMap(request, model));
		model.addAttribute("bean", getCommonBean);
		log.info("Service[{}] : {}", request.getRequestURL(), model);
		return ResponseEntity.ok(model);
	}

}
