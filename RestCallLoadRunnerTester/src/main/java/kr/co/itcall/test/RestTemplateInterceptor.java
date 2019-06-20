package kr.co.itcall.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.http.Header;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StringUtils;

public class RestTemplateInterceptor implements ClientHttpRequestInterceptor {

	private HttpHeaders httpHeaders;
	private String cookie;
	private boolean isLogging;

	public RestTemplateInterceptor() {
		this(null, false);
	}
//	public RestTemplateInterceptor(String cookie) {
//		this.cookie = cookie;
//	}
	public RestTemplateInterceptor(HttpHeaders httpHeaders, boolean isLogging) {
		setHttpHeaders(httpHeaders);
		this.isLogging = isLogging;
	}

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {

		ClientHttpResponse response = null;
		String respBody = null;
		try {
			if(isLogging) traceRequest(request, body);
			response = execution.execute(request, body);
			respBody = traceResponse(response);
		} finally {
		}
		return response;
	}
	
	private void traceRequest(HttpRequest request, byte[] body) throws IOException {
		System.out.println("===========================[ RestTemplate Outbound Request Begin ]============================================");
		System.out.println("URI         : " + request.getURI());
		System.out.println("Method      : " + request.getMethod());
		System.out.println("Headers     : " + request.getHeaders());
		System.out.println("Request body: " + new String(body, "UTF-8"));
		System.out.println("===========================[ RestTemplate Outbound Request End ]============================================");
	}

	private String traceResponse(ClientHttpResponse response) throws IOException {
		StringBuilder inputStringBuilder = new StringBuilder();
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getBody(), "UTF-8"));
		String line = bufferedReader.readLine();
		while (line != null) {
			inputStringBuilder.append(line);
			inputStringBuilder.append('\n');
			line = bufferedReader.readLine();
		}
		if(this.httpHeaders!=null) {
			// this.httpHeaders = response.getHeaders();
			List<String> setCookie = response.getHeaders().get(HttpHeaders.SET_COOKIE);
			if(setCookie!=null && setCookie.size()>0) {
				String newCookie = setCookie.get(0);
				if(isLogging) System.out.println("요청결과에 의한 신규수신 쿠키 : " + newCookie);
				newCookie = newCookie.substring(0, newCookie.indexOf(";"));
				this.cookie = newCookie + (cookie==null||cookie.indexOf(";")<0?"":cookie.substring(cookie.indexOf(";")));
				// this.httpHeaders = new HttpHeaders();
				this.httpHeaders.set(HttpHeaders.COOKIE, cookie);
				this.httpHeaders.remove(HttpHeaders.SET_COOKIE);
				if(isLogging) System.out.println("요청결과에 의한 최종 변경적용 쿠키 : " + this.cookie);
			}
		}
		if(isLogging) {
			System.out.println("===========================[ RestTemplate Outbound Response Begin ]============================================");
			System.out.println("Status code  : " + response.getStatusCode());
			System.out.println("Status text  : " + response.getStatusText());
			System.out.println("Headers      : " + response.getHeaders());
			System.out.println("Response body: " + inputStringBuilder.toString());
			System.out.println("===========================[ RestTemplate Outbound Response End ]============================================");
		}
		return inputStringBuilder.toString();
	}

	public HttpHeaders getHttpHeaders() {
		return httpHeaders;
	}
	public void setHttpHeaders(HttpHeaders httpHeaders) {
		this.httpHeaders = httpHeaders;
		if(this.httpHeaders!=null) {
			if(this.httpHeaders.get(HttpHeaders.COOKIE)!=null && this.httpHeaders.get(HttpHeaders.COOKIE).size()>0) {
				this.cookie = this.httpHeaders.get(HttpHeaders.COOKIE).get(0);
			}
		}
	}


	public String getCookie() {
		return cookie;
	}
	public void setCookie(String setCookie) {
		if(isLogging) System.out.println("요청결과에 의한 redirection 신규수신 쿠키 : " + setCookie);
		this.cookie = setCookie + (!StringUtils.isEmpty(this.cookie) && this.cookie.indexOf(";")>=0 ? cookie.substring(cookie.indexOf(";")):"");
		// this.httpHeaders = new HttpHeaders();
		this.httpHeaders.set("Cookie", cookie);
		this.httpHeaders.remove(HttpHeaders.SET_COOKIE);
		if(isLogging) System.out.println("요청결과에 의한 redirection 변경적용 쿠키 : " + this.cookie);
	}

	public boolean isLogging() {
		return isLogging;
	}

}
