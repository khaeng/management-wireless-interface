package com.itcall.SpringSecurityRSA.rsa;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class PasswordCheckerSample implements PasswordEncoder {

	Logger log = LoggerFactory.getLogger(PasswordCheckerSample.class);

	@Override
	public String encode(CharSequence rawPassword) {
//		if(rawPassword.equals("testPassword"))
//			return "testPassword";
//		return "passwordNotMatched...Using Sha256-Hash-algorithm-with-Salt";
		return rawPassword.toString();
	}

	@Override
	public boolean matches(CharSequence rawPassword, String encodedPassword) {
		try {
			HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
			rawPassword = SecureRsaCripto.getRSaDecodeFromSession(request, rawPassword.toString());
		} catch (Exception e) {
			log.warn("RSA Decode Error rawPassword[{}]", rawPassword);
		}
		boolean result = encode(rawPassword).equals(encodedPassword);
		return result;
	}

}
