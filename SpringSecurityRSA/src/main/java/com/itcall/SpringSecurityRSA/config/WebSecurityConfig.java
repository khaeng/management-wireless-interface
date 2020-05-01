package com.itcall.SpringSecurityRSA.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import com.itcall.SpringSecurityRSA.config.handler.RsaAuthenticationFailureHandler;
import com.itcall.SpringSecurityRSA.config.handler.RsaAuthenticationSuccessHandler;
import com.itcall.SpringSecurityRSA.config.handler.RsaLogoutSuccessHandler;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled=true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Value("${session.duplicated.max.count:1}") // 동일ID 중복접속자 허용 수 //  테스트를 위함.
	private int sessionDuplicatedMaxCount;
	@Value("${session.max.cnt:0}") // 동일ID 중복접속자 허용 수 //  테스트를 위함.
	private int sessionDuplicatedMaxCountByCmd;
	
	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private UserDetailsService userDetailsService;

	@Override
	public void configure(WebSecurity web) throws Exception
	{
		web.ignoring().antMatchers("/common/**", "/favicon.ico", "/css/**", "/js/**", "/image/**", "/error/**", "/loginProcess", "/loginRedirect", "/loginCust", "/getRsaPublicInfo");
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		int sessionDuplicatedMaxCount = sessionDuplicatedMaxCountByCmd>0 ? sessionDuplicatedMaxCountByCmd : this.sessionDuplicatedMaxCount;

		http.authorizeRequests()
				.antMatchers("/common/**", "/css/**", "/js/**", "/image/**", "/loginProcess", "/loginRedirect", "/loginCust", "/getRsaPublicInfo").permitAll()
				.antMatchers("/login", "/loginCust").permitAll()
				.antMatchers("/api/root/**").hasRole("ROOT")
				.antMatchers("/api/admin/**").hasAnyRole("ROOT", "ADMIN")
				.antMatchers("/api/user/**").hasAnyRole("ROOT", "ADMIN", "USER")
				.anyRequest().authenticated();

		http.formLogin().loginPage("/login") // default
				// 아래부분은 구현해야할지 테스트해봐야 한다.
				.loginProcessingUrl("/login_post").failureUrl("/login?error") // default
				.defaultSuccessUrl("/main").usernameParameter("username").passwordParameter("password")
				.successHandler(new RsaAuthenticationSuccessHandler()) // 로그인 성공 핸들러
				.failureHandler(new RsaAuthenticationFailureHandler()) // 로그인 실패 핸들러
				.permitAll();

		http.logout().logoutUrl("/logout") // default
				.logoutSuccessUrl("/login")
				.logoutSuccessHandler(new RsaLogoutSuccessHandler())
				.invalidateHttpSession(true)
				.permitAll();
//		http.logout().invalidateHttpSession(true)
//				.logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
//				.logoutSuccessUrl("/login")
//				.logoutSuccessHandler(new RsaLogoutSuccessHandler())
//				.permitAll();

		http.exceptionHandling().accessDeniedPage("/error/error");

		http.sessionManagement()
				// .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED) // default.
		 		.sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
				.sessionFixation()
				// .migrateSession() // 존재하는 세션을 복사해서 세로운 세션에 담아 새롭게 만든다.
				.newSession() /// 그냥 새로 만듬.
				.invalidSessionUrl("/login")
				.maximumSessions(sessionDuplicatedMaxCount)
				.maxSessionsPreventsLogin(false) // false : 최대값일 경우 최초 세션을 삭제한다.   true : 최대값에 오면 더이상 세션을 만들지 않는다.
				.expiredUrl("/login");

		http
				// .csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()).and()
				.csrf().disable()
				
				.headers().frameOptions().sameOrigin().disable()
				// .addFilterBefore(beforeFilter01, SwitchUserFilter.class)
				// .addFilterBefore(beforeFilter02, SwitchUserFilter.class)
				// .addFilterAfter(afterFilter01, SwitchUserFilter.class) // 세션이 정상적인지 체크하는 Filter 처리.
				// .addFilterAfter(afterFilter02, SwitchUserFilter.class) // 세션이 정상적인지 체크하는 Filter 처리.
				.httpBasic()
				.authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"));
	}

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		// Password check in Spring-Security
		auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
	}

	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		// return super.authenticationManagerBean();
		return authenticationManager();
	}

	// 직접 사용자를 인증하는 방법.
//	@Autowired
//	private UserAuthenticationProvider authenticationProvider;
//	
//	@Override
//	protected void configure(AuthenticationManagerBuilder auth) {
//		auth.authenticationProvider(authenticationProvider);
//	}

}