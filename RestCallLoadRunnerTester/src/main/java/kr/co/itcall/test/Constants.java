package kr.co.itcall.test;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.apache.commons.codec.net.URLCodec;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Constants {

	private static final String BASE_PROPERTIES_FILE_NAME = "base.conf";
	
	protected static ObjectMapper objectMapper;
	
	private Properties properties;

	/* for TaskExecutor */
	private int executorCorePoolSize = 10;	// 멀티프로세스 카운트.
	private int executorMaxPoolSize = 20;	// 멀티프로세스의 2배.(Active)
	private int executorQueueCapacity = 1000000; // Active 개수의 100배.(Max의 100배)

	/* for RestUtil */
	private int maxConnTotalForRestClient = 100;
	private int maxConnPerRouteForRestClient = 100;
	private int maxRestTemplateUnitForRestClient = 10;
	private int connectionTimeoutForRestClient = 90;
	private int readTimeoutForRestClient = 90;

	private String propertiesFileName = BASE_PROPERTIES_FILE_NAME;

	public Constants(String propertiesFile) throws IOException {
		if(StringUtils.isEmpty(propertiesFile)) {
			this.properties = readProperties(new File(BASE_PROPERTIES_FILE_NAME));
		} else {
			this.propertiesFileName = propertiesFile;
			this.properties = readProperties(new File(propertiesFile));
		}
		this.executorQueueCapacity = (this.executorMaxPoolSize = (this.executorCorePoolSize = getTotalMultiConnector()) * 2) * 100;
	}

	public String getPropertiesFileName() {
		return propertiesFileName;
	}

	public int getExecutorCorePoolSize() {
		return executorCorePoolSize;
	}
	public void setExecutorCorePoolSize(int executorCorePoolSize) {
		this.executorCorePoolSize = executorCorePoolSize;
	}
	public int getExecutorMaxPoolSize() {
		return executorMaxPoolSize;
	}
	public void setExecutorMaxPoolSize(int executorMaxPoolSize) {
		this.executorMaxPoolSize = executorMaxPoolSize;
	}
	public int getExecutorQueueCapacity() {
		return executorQueueCapacity;
	}
	public void setExecutorQueueCapacity(int executorQueueCapacity) {
		this.executorQueueCapacity = executorQueueCapacity;
	}
	public int getMaxConnTotalForRestClient() {
		return maxConnTotalForRestClient;
	}
	public void setMaxConnTotalForRestClient(int maxConnTotalForRestClient) {
		this.maxConnTotalForRestClient = maxConnTotalForRestClient;
	}
	public int getMaxConnPerRouteForRestClient() {
		return maxConnPerRouteForRestClient;
	}
	public void setMaxConnPerRouteForRestClient(int maxConnPerRouteForRestClient) {
		this.maxConnPerRouteForRestClient = maxConnPerRouteForRestClient;
	}
	public int getMaxRestTemplateUnitForRestClient() {
		return maxRestTemplateUnitForRestClient;
	}
	public void setMaxRestTemplateUnitForRestClient(int maxRestTemplateUnitForRestClient) {
		this.maxRestTemplateUnitForRestClient = maxRestTemplateUnitForRestClient;
	}
	public int getConnectionTimeoutForRestClient() {
		return connectionTimeoutForRestClient;
	}
	public void setConnectionTimeoutForRestClient(int connectionTimeoutForRestClient) {
		this.connectionTimeoutForRestClient = connectionTimeoutForRestClient;
	}
	public int getReadTimeoutForRestClient() {
		return readTimeoutForRestClient;
	}
	public void setReadTimeoutForRestClient(int readTimeoutForRestClient) {
		this.readTimeoutForRestClient = readTimeoutForRestClient;
	}



	/**
	 * Properties파일을 Load 한다.
	 * @param propsFile
	 * @return
	 * @throws IOException 
	 */
	private Properties readProperties(File propsFile) throws IOException {
		FileSystemResource fileSystemResource = new FileSystemResource(propsFile);
//		if(fileSystemResource.exists()) {
//			try {
				PropertiesFactoryBean bean = new PropertiesFactoryBean();
				bean.setLocation(fileSystemResource);
				bean.afterPropertiesSet();
				Properties properties = bean.getObject();

				// 프로퍼티를 실제 파일에서 다시 로드한다. (한글등이 깨지므로...)
				reloadPropertiesFromFile(properties, propsFile);

				// Properties 정보Value가 파일로 존재할 경우 해당 파일정보를 Property Value로 치환해준다.
				properties = loadPropertiesCheckRelateFiles(properties);
				return properties;
//			} catch (Exception e) {
//				log.error("(Re)Loaded UnitInfoProperties on ERROR fileName[{}], errorMessage[{}], errorCause[{}] {}", propsFile.getName(), e.getMessage(), e.getCause(), e);
//			}
//		}else {
//			log.warn("(Re)Loaded UnitInfoProperties on WARN filePath[{}] <<< file not exist???", propsFile.getAbsolutePath());
//		}
//		return null;
	}

	/**
	 * Properties 정보Value가 파일로 존재할 경우 해당 파일정보를 Property Value로 치환해준다.
	 * @param properties
	 * @return
	 */
	private Properties loadPropertiesCheckRelateFiles(final Properties properties) {
		for (Object key : properties.keySet()) {
			try {
				StringBuffer sb = new StringBuffer();
				BufferedReader br = new BufferedReader(new FileReader(properties.getProperty((String) key)));
				String readLine = null;
				while ((readLine=br.readLine())!=null) {
					sb.append(readLine).append("\n");
				}
				br.close();
				properties.setProperty((String)key, sb.toString().trim());
			} catch (Exception e) {
				// Property의 Value가 파일이 아니므로 기존 값을 그대로 보관한다.
			}
		}
		return properties;
	}

	/**
	 * 로드된 프로퍼티를 파일에서 다시 로드한다.(읽지 못한 한글등을 replace한다.)
	 * @param properties
	 * @param propertiesFile
	 * @return
	 */
	public Properties reloadPropertiesFromFile(Properties properties, File propertiesFile) {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(propertiesFile));
			while (true) {
				String value = br.readLine();
				if(value==null)
					break;
				if(!StringUtils.isEmpty(value) && !value.startsWith("\t") && !value.startsWith(" ") && !value.startsWith("#") && !value.startsWith("#")
						&& value.split("=", 2).length==2
						&& !StringUtils.isEmpty(properties.getProperty(value.split("=", 2)[0]))) {
					properties.setProperty(value.split("=", 2)[0], value.split("=", 2)[1]);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(br!=null) try {br.close();} catch (IOException e) {}
		}
		return properties;
	}
	public String getPropertyValue(String key) {return getPropertyValue(key, "");}
	public String getPropertyValue(String key, String defValue) {
		String value = defValue;
		// 프로퍼티 파일을 읽을때부터 한글처리 끝났음.
//		BufferedReader br = null;
//		try {
//			br = new BufferedReader(new FileReader(this.propertiesFileName));
//			while (true) {
//				value = br.readLine();
//				if(value.startsWith(key)) {
//					value = value.split("=", 2)[1];
//					break;
//				}
//			}
//		} catch (Exception e) {
			value = this.properties.getProperty(key);
//		} finally {
//			if(br!=null) try {br.close();} catch (IOException e) {}
//		}
		if(StringUtils.isEmpty(value)) {
			return defValue;
		}
		try {
			StringBuffer sb = new StringBuffer();
			String keepValue = "";
			for (char ch : value.toCharArray()) {
				switch (ch) {
				case '%':
					if(StringUtils.isEmpty(keepValue) || keepValue.length() % 3 == 0) {
						keepValue += "%";
					} else {
						keepValue = "";
						sb.append(keepValue).append("%");
					}
					break;
				default:
					if(!StringUtils.isEmpty(keepValue) && keepValue.length() % 3 == 0) {
						sb.append(URLDecoder.decode(keepValue, "UTF-8")).append(ch);
						keepValue="";
					}else if(keepValue.startsWith("%") && "0123456789ABCDEFabcdef".contains(ch+"")) {
						keepValue+=ch;
					} else {
						sb.append(keepValue).append(ch);
						keepValue="";
					}
					break;
				}
			}
			return sb.toString();
		} catch (UnsupportedEncodingException e) {
			return value;
		}
	}
	
	public String getLogPath() {
		return this.getPropertyValue("log.path", "./log/");
	}

	public boolean isLogging() {
		return "YESTRUE".contains(this.properties.getProperty("log.logging.yn", "N").toUpperCase());
	}

	public long getTotalTestCount() {
		return Long.parseLong(this.properties.getProperty("test.total.count", "100000"));
	}
	public int getTotalMultiConnector() {
		return this.executorCorePoolSize = Integer.parseInt(this.properties.getProperty("test.multi.count", ""+this.executorCorePoolSize));
	}

	public boolean isRsaLoginId() {
		return "YESTRUE".contains(this.properties.getProperty("login.rsa.id.yn", "N").toUpperCase());
	}
	public boolean isRsaLoginPassword() {
		return "YESTRUE".contains(this.properties.getProperty("login.rsa.password.yn", "N").toUpperCase());
	}
	public String getLoginId() {
		return this.getPropertyValue("login.id");
	}
	public String getPassword() {
		return this.getPropertyValue("login.password");
	}
	public String getLoginPageUrl() {
		return this.getPropertyValue("login.page.url");
	}
	public String getLoginPageParams() {
		return switchParams(this.getPropertyValue("login.page.params"), null);
	}
	public String getRsaModuleId() {
		return this.getPropertyValue("login.rsa.module.id"); // , "RSAModulus");
	}
	public String getRsaModuleStart() {
		return this.getPropertyValue("login.rsa.module.start");
	}
	public String getRsaModuleEnd() {
		return this.getPropertyValue("login.rsa.module.end");
	}
	public String getRsaExponentId() {
		return this.getPropertyValue("login.rsa.exponent.id"); // , "RSAExponent");
	}
	public String getRsaExponentStart() {
		return this.getPropertyValue("login.rsa.exponent.start");
	}
	public String getRsaExponentEnd() {
		return this.getPropertyValue("login.rsa.exponent.end");
	}
	public String getRsaPublicId() {
		return this.getPropertyValue("login.rsa.public.id"); // , "RSAPublic");
	}
	public String getRsaPublicStart() {
		return this.getPropertyValue("login.rsa.Public.start");
	}
	public String getRsaPublicEnd() {
		return this.getPropertyValue("login.rsa.Public.end");
	}
	public String getRsaUrl() {
		return this.getPropertyValue("login.rsa.url");
	}
	public String getRsaParams() {
		return this.getPropertyValue("login.rsa.params");
	}
	public HttpHeaders getRsaHeaderInfo(HttpHeaders httpHeaders) {
		return getHeaderInfoFromProperties("login.rsa.header.", httpHeaders);
	}
	public HttpMethod getRsaHttpMethod() {
		return HttpMethod.valueOf(this.getPropertyValue("login.rsa.method", "POST"));
	}
	public String getRsaModuleKey() {
		return this.getPropertyValue("login.rsa.module.key");
	}
	public String getRsaExponentKey() {
		return this.getPropertyValue("login.rsa.exponent.key");
	}
	public String getRsaPublicKey() {
		return this.getPropertyValue("login.rsa.public.key");
	}
	public String getLoginProcessUrl() {
		return this.getPropertyValue("login.process.url");
	}
	public String getLoginProcessParams() {
		return switchParams(this.getPropertyValue("login.process.params"), null);
	}
	/** 멀티 호출개수만큼 테스트 Runnalbe을 만들고. 테스트한다. 각 테스트는 앞 테스트가 종료(완료)된 후 실행된다. 한 루프가 돌면 다시 처음부터 호출된다. **/
	public boolean isLoopRelayTest() {
		return "YESTRUE".contains(this.properties.getProperty("test.loop.relay.yn", "N").toUpperCase());
	}
	public long getSleepTimeBeforeGroup() {
		return Long.parseLong(this.properties.getProperty("test.group.sleep", "0"));
	}
	public long getSleepTimeBeforeTest(long testNum) {
		return Long.parseLong(this.properties.getProperty("test."+testNum+".sleep", "0"));
	}
	public HttpMethod getTestHttpMethod(long testNum) {
		return HttpMethod.valueOf(this.properties.getProperty("test."+testNum+".method", "POST"));
	}
	public HttpHeaders getTestHeaderInfo(long testNum, HttpHeaders httpHeaders) {
		return getHeaderInfoFromProperties("test."+testNum+".header.", httpHeaders);
	}
	public String getTestNameInfo(long testNum) {
		return this.getPropertyValue("test."+testNum+".name", "Test-"+testNum);
	}
	public String getTestUrlInfo(long testNum) {
		return this.getPropertyValue("test."+testNum+".url");
	}
	public String getTestParamsInfo(long testNum, Map<String, Object> beforeResultMap) {
		return switchParams(this.getPropertyValue("test."+testNum+".params"), beforeResultMap);
	}
	public boolean isKeepSession(long testNum) {
		return "YESTRUE".contains(this.properties.getProperty("test."+testNum+".keep.session.yn", "N").toUpperCase());
	}
	public int getWaitPort() {
		return Integer.parseInt(this.properties.getProperty("test.wait.port", "9991"));
	}

	public HttpHeaders getLoginPageHeaderInfo(HttpHeaders httpHeaders) {
		return getHeaderInfoFromProperties("login.page.header.", httpHeaders);
	}
	public HttpHeaders getLoginProcessHeaderInfo(HttpHeaders httpHeaders) {
		return getHeaderInfoFromProperties("login.process.header.", httpHeaders);
	}
	public HttpHeaders getHeaderInfoFromProperties(String baseKey, HttpHeaders httpHeaders) {
		HttpHeaders outputHeaders = new HttpHeaders();
		if(httpHeaders!=null) {
			for (String header: httpHeaders.keySet()) {
				outputHeaders.put(header, httpHeaders.get(header));
			}
		}
		for (int i = 0; i < RestTestBase.MAX_LOOP_AND_HEADER_COUNT; i++) {
			String key = this.properties.getProperty(baseKey+i+".key");
			if(StringUtils.isEmpty(key))
				break;
			outputHeaders.remove(key); // 키가 존재하면 원래 해더의 키는 무조건 삭제한다.
			String value = this.properties.getProperty(baseKey+i+".value");
			if(value!=null) {
				outputHeaders.add(key, value); // value가 존재하면 무조건 Overwirte한다. 공백이면, 삭제하는 효과.
				if(value.startsWith("multipart/form-data")) {
					outputHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
				}
			}
		}
		return outputHeaders;
	}

	public void setMultipartFile(long index, MultiValueMap<String, Object> body) {
		for (int i = 0; i < RestTestBase.MAX_LOOP_AND_HEADER_COUNT; i++) {
			String key = this.getPropertyValue("test."+index+".file."+i+".key");
			if(StringUtils.isEmpty(key))
				break;
//			body.add("listBdFile", new FileSystemResource("D:/test한글파일테스트test.txt"));
//			body.add("listBdFile", new FileSystemResource("D:/test한글파일테스트test - 복사본.txt"));
//			// body.add("listBdFile", new FileSystemResource("D:/test한글파일테스트test.log"));
			String path = this.getPropertyValue("test."+index+".file."+i+".path", "");
			final FileSystemResource[] arrFile = new FileSystemResource[] {new FileSystemResource(path)};
			if(!arrFile[0].isFile() || !arrFile[0].isReadable()) {
				
				
				final String[] result = new String[] {""};
				
				Thread threadUiUx = new Thread(new Runnable() {
					@Override public void run() {
						int len = path.lastIndexOf("/");
						if(len<=0)len=path.length();
						JFileChooser chooser = new JFileChooser(path.substring(0, len));
						chooser.setApproveButtonText("Key["+key+"]에 맵핑되어 업로드할 파일을 지정해주세요.");
						chooser.setDialogTitle("업로드 파일 선택 : Err[" + path + "]");
						chooser.setToolTipText("지정된 파일["+path+"]은 정상적이지 않습니다.");
						chooser.setApproveButtonToolTipText("취소하면 해당 파일만 제거하고 수행됩니다.");
						chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
//						chooser.addActionListener(new ActionListener() {@Override
//							public void actionPerformed(ActionEvent evt) {
//								System.out.println(evt.getActionCommand());
//							}
//						});
						// System.out.println("파일저장 다이얼로그에서 업로드할 파일명을 선택해 주세요...");
						if(chooser.showSaveDialog(null)==0) {
							arrFile[0] = new FileSystemResource(chooser.getSelectedFile());
							if(arrFile[0].isFile() && arrFile[0].isReadable()) {
								body.add(key, arrFile[0]);
							}
						}
						result[0] = "UI/UX로 파일지정이 완료되었습니다.";
					}
				});
				Thread threadCmd = new Thread(new Runnable() {
					@Override public void run() {
						BufferedReader br = null;
						try {
							System.out.println("업로드 파일 선택 : Err[" + path + "]");
							System.out.print("Key["+key+"]에 맵핑되어 업로드할 파일을 지정해주세요." + "\n >>> : ");
							br = new BufferedReader(new InputStreamReader(System.in));
							arrFile[0] = new FileSystemResource(br.readLine());
							// br.reset();
							if(arrFile[0].isFile() && arrFile[0].isReadable()) {
								body.add(key, arrFile[0]);
							}
						} catch (IOException e) {} finally {
							// if(br!=null) try {br.close();} catch (IOException e) {}
							result[0] = "Console로 파일지정이 완료되었습니다.";
						}
						
					}
				});
				
				threadUiUx.start();
				threadCmd.start();
				while (true) {
					try {Thread.sleep(1000);} catch (InterruptedException e) {}
					if(!StringUtils.isEmpty(result[0])) {
						threadUiUx.interrupt();
						threadCmd.interrupt();
						break;
					} else if(!threadUiUx.isAlive() && !threadCmd.isAlive()) {
						result[0] = "";
						break;
					}
				}
				
			} else {
				body.add(key, arrFile[0]);
			}
		}
	}

	public int getPrintLogTerm() {
		return Integer.parseInt(this.properties.getProperty("log.print.term", "10"));
	}

	public Charset getTestCharset() {
		return Charset.forName(this.properties.getProperty("test.charset", "UTF-8"));
	}

	public String switchParams(String params, Map<String, Object> beforeResultMap) {
		return switchParams(0, params, beforeResultMap);
	}
	public String switchParams(int index, String params, Map<String, Object> beforeResultMap) {
		if(StringUtils.isEmpty(params))
			return params;
		int start = params.indexOf("#{", index);
		int end = params.indexOf("}", start);
		if(-1<start && start<end) {
			String before = params.substring(0, start);
			String after = params.substring(end+1);
			String switchKey = params.substring(start+2, end);
			try {
				String switchValue = new SimpleDateFormat(switchKey).format(new Date());
				return switchParams(end, new StringBuffer().append(before).append(switchValue).append(after).toString(), beforeResultMap);
			} catch (Exception e) {
				Object switchValue = null;
				if(!StringUtils.isEmpty(beforeResultMap) && !StringUtils.isEmpty(switchValue = beforeResultMap.get(switchKey))) {
					return switchParams(end, new StringBuffer().append(before).append(switchValue).append(after).toString(), beforeResultMap);
				}
				
				switchValue = findFromMap(switchKey, beforeResultMap);
				if(switchValue!=null) {
					return switchParams(end, new StringBuffer().append(before).append(switchValue).append(after).toString(), beforeResultMap);
				}
				
				// Properties에서는 한번만 찾는다.
				if(!StringUtils.isEmpty(getPropertyValue(switchKey))) {
					switchValue = getPropertyValue(switchKey);
					return switchParams(end, new StringBuffer().append(before).append(switchValue).append(after).toString(), beforeResultMap);
				}
				
				
				return switchParams(end, params, beforeResultMap);
			}
		} else {
			return params;
		}
	}

	public String findFromList(String switchKey, List<Map<String, Object>> beforeResultMap) {
		String beforeResultStr = "";
		switchKey = "\"" + switchKey + "\"";
		if(!StringUtils.isEmpty(beforeResultMap) && beforeResultMap.size()>0){
			try {
				beforeResultStr = objectMapper.writeValueAsString(beforeResultMap);
				return findFromMap(switchKey, beforeResultStr);
			}catch (Exception e) {e.printStackTrace();}
		}
		return null;
	}
	public String findFromMap(String switchKey, Map<String, Object> beforeResultMap) {
		String beforeResultStr = "";
		switchKey = "\"" + switchKey + "\"";
		if(!StringUtils.isEmpty(beforeResultMap)){
			try {
				beforeResultStr = objectMapper.writeValueAsString(beforeResultMap);
				return findFromMap(switchKey, beforeResultStr);
			}catch (Exception e) {e.printStackTrace();}
		}
		return null;
	}
	public String findFromMap(String switchKey,String beforeResultStr) {
				int start = beforeResultStr.indexOf(switchKey);
				if(start<0)
					return null;
				start+=switchKey.length();
				beforeResultStr = beforeResultStr.substring(beforeResultStr.indexOf(":", start)+1).trim();
				boolean isStr = false; // 숫자형
				if(beforeResultStr.startsWith("\"")) { // 문자열
					isStr = true;
					beforeResultStr = beforeResultStr.substring(1); // 문자열 내부만 추출한다.
				}
				StringBuffer sb = new StringBuffer();
				boolean isEscape = false;
				int cnt = 0;
				for (char ch : beforeResultStr.toCharArray()) {
					if(isEscape && ch=='\\') {
						sb.append(ch);
						isEscape=!isEscape;
						continue;
					} else if (ch=='\\') {
						isEscape=!isEscape;
						continue;
					}
					if(!isStr && !"0123456789.".contains(ch+"")) {
						break; // 더이상 숫자형이 아니면 탈출한다.
					}
					if(!isEscape && ch=='"') {
						break; // 문자열의 끝이 왔으면 탈출한다.
					}else if(ch=='"') {
						isEscape=false; // 예외문자를 사용할 것이므로...
					}
					sb.append(ch);
					++cnt;
				}
				if(cnt<1 || sb.toString().equals("0"))
					return findFromMap(switchKey, beforeResultStr);
				return sb.toString();
	}

	public void setObjectMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}


}
