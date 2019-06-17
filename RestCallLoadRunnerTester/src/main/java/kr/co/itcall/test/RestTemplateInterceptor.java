package kr.co.itcall.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StringUtils;

public class RestTemplateInterceptor implements ClientHttpRequestInterceptor {

	private HttpHeaders httpHeaders;
	private String cookie;

	public RestTemplateInterceptor() {
		this("");
	}
	public RestTemplateInterceptor(String cookie) {
		this.cookie = cookie;
	}

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {

		ClientHttpResponse response = null;
		String respBody = null;
		try {
			traceRequest(request, body);
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
		if(!StringUtils.isEmpty(this.cookie)) {
			// this.httpHeaders = response.getHeaders();
			List<String> setCookie = response.getHeaders().get(HttpHeaders.SET_COOKIE);
			String newCookie = setCookie.get(0);
			System.out.println(newCookie);
			newCookie = newCookie.substring(0, newCookie.indexOf(";"));
			System.out.println(newCookie);
			this.cookie = newCookie + cookie.substring(cookie.indexOf(";"));
			System.out.println(this.cookie);
			this.httpHeaders = new HttpHeaders();
			this.httpHeaders.set("Cookie", cookie);
			this.httpHeaders.remove(HttpHeaders.SET_COOKIE);
		}
		System.out.println("===========================[ RestTemplate Outbound Response Begin ]============================================");
		System.out.println("Status code  : " + response.getStatusCode());
		System.out.println("Status text  : " + response.getStatusText());
		System.out.println("Headers      : " + response.getHeaders());
		System.out.println("Response body: " + inputStringBuilder.toString());
		System.out.println("===========================[ RestTemplate Outbound Response End ]============================================");
		return inputStringBuilder.toString();
	}

	public HttpHeaders getHttpHeaders() {
		return httpHeaders;
	}

	public String getCookie() {
		return cookie;
	}

}
