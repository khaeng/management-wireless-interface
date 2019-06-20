package kr.co.itcall.test;

import java.util.Arrays;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.protocol.HttpContext;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;

public class RestTemplateRedirectInterceptor extends LaxRedirectStrategy {

	private RestTemplateInterceptor restTemplateInterceptor;

	private boolean isLogging;

	public RestTemplateRedirectInterceptor() {
		this(null);
	}
	public RestTemplateRedirectInterceptor(RestTemplateInterceptor restTemplateInterceptor) {
		this.restTemplateInterceptor = restTemplateInterceptor;
		if(restTemplateInterceptor!=null) {
			this.isLogging = restTemplateInterceptor.isLogging();
		}
	}
	
	@Override
	public boolean isRedirected(HttpRequest request, HttpResponse response, HttpContext context) throws ProtocolException {
		if(this.restTemplateInterceptor!=null && !StringUtils.isEmpty(this.restTemplateInterceptor.getCookie())) {
			if(response.getStatusLine().getStatusCode()==302) {
				if(isLogging) System.out.println("Call Status ::: " + response.getStatusLine());
				Header[] headers = response.getAllHeaders();
				for (int i = 0; i < headers.length; i++) {
					if(isLogging) System.out.println("HeaderInfo[" + headers[i].getName() + "] : " + headers[i].getValue());
				}
			}
			String setCookie = response.getFirstHeader(HttpHeaders.SET_COOKIE)!=null?response.getFirstHeader(HttpHeaders.SET_COOKIE).getValue():null;
			if(!StringUtils.isEmpty(setCookie)) {
				this.restTemplateInterceptor.setCookie(setCookie.substring(0, setCookie.indexOf(";")));
			}
		}
		return super.isRedirected(request, response, context);
	}

}
