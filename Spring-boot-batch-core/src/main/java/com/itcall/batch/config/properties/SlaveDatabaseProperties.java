package com.itcall.batch.config.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
// @ConfigurationProperties(prefix = SlaveDatabaseProperties.PREFIX)
public class SlaveDatabaseProperties implements AbstractDatabaseProperties {

	public static final String PREFIX = "datasource.slave"; 

	@Value("${"+PREFIX+".jndi-name:********}")
	private String jndiName;

	@Value("${"+PREFIX+".driver-class-name:********}")
	private String driverClassName;
	
	@Value("${"+PREFIX+".url:********}")
	private String url;
	
	@Value("${"+PREFIX+".user-name:********}")
	private String userName;
	
	@Value("${"+PREFIX+".password:********}")
	private String password;
	
	@Value("${"+PREFIX+".initial-size:10}")
	private int initialSize;
	
	@Value("${"+PREFIX+".max-active:10}")
	private int maxActive;
	
	@Value("${"+PREFIX+".max-idle:10}")
	private int maxIdle;
	
	@Value("${"+PREFIX+".min-idle:10}")
	private int minIdle;
	
	@Value("${"+PREFIX+".max-wait:3000}")
	private int maxWait;


	public String getJndiName() {
		return jndiName;
	}

	public void setJndiName(String jndiName) {
		this.jndiName = jndiName;
	}

	public String getDriverClassName() {
		return driverClassName;
	}

	public void setDriverClassName(String driverClassName) {
		this.driverClassName = driverClassName;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getInitialSize() {
		return initialSize;
	}

	public void setInitialSize(int initialSize) {
		this.initialSize = initialSize;
	}

	public int getMaxActive() {
		return maxActive;
	}

	public void setMaxActive(int maxActive) {
		this.maxActive = maxActive;
	}

	public int getMaxIdle() {
		return maxIdle;
	}

	public void setMaxIdle(int maxIdle) {
		this.maxIdle = maxIdle;
	}

	public int getMinIdle() {
		return minIdle;
	}

	public void setMinIdle(int minIdle) {
		this.minIdle = minIdle;
	}

	public int getMaxWait() {
		return maxWait;
	}

	public void setMaxWait(int maxWait) {
		this.maxWait = maxWait;
	}
	
	@Override
	public String toString() {
		return "SlaveDatabaseProperties[" + this.driverClassName + "]";
	}
}
