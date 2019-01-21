package com.itcall.batch.config.properties;

import java.io.IOException;
import java.util.Properties;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.EnvironmentStringPBEConfig;
import org.jasypt.spring31.properties.EncryptablePropertyPlaceholderConfigurer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import com.itcall.batch.util.SecureUtil;

@Configuration
public class PropertiesSecureConfig /*extends PropertyPlaceholderConfigurer*/{

	public final static String JASYPT_ALGORITHM = "PBEWITHMD5ANDDES";
	private final static String JASYPT_PASSWORD = "OJx0Yq/pJa5DbXcegL4AstQNTtdNKHE41cIHT4rZwWI=";

	public static StandardPBEStringEncryptor standardPBEStringEncryptor;

	/************** USE LIKE THIS **************/
	// @Value("#{common['common.ldap.use']:ThisIsDefalutValue}")
	// private String testCommonPropertySomeValue;
	//
	// @Value("#{batch['batch.kos.crm.b2c.ap01']:ThisIsDefalutValue}")
	// private String testBatchPropertySomeValue;
	/************** USE LIKE THIS **************/

	@Bean("standardPBEStringEncryptor")
	public StandardPBEStringEncryptor stringEncryptor() {
		StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();

		String pwdKey = SecureUtil.decryptPropKey(JASYPT_PASSWORD);
		EnvironmentStringPBEConfig pbeConfig = new EnvironmentStringPBEConfig();
		pbeConfig.setAlgorithm(JASYPT_ALGORITHM);
		pbeConfig.setPassword(pwdKey);
		encryptor.setConfig(pbeConfig);

//		EnvironmentPBEConfig peConfig = new EnvironmentPBEConfig();
//		peConfig.setAlgorithm(JASYPT_ALGORITHM);
////		String pwdKey = SecureUtil.decryptPropKey(JASYPT_PASSWORD);
//		peConfig.setPassword(pwdKey);
////		peConfig.setPassword("!^w^!_dlrnlgoddlaksemfek_!^w^!");
////		peConfig.setPassword(SecureUtil.decryptPropKey(JASYPT_PASSWORD));
//		encryptor.setConfig(peConfig);

		standardPBEStringEncryptor = encryptor;
		return encryptor;
	}

	// @Profile("local")
	@Bean("encryptablePropertyPlaceholderConfigurer")
	@DependsOn(value= {"standardPBEStringEncryptor", "app", "common", "batch"})
	public EncryptablePropertyPlaceholderConfigurer encryptablePropertyPlaceholderConfigurer(StandardPBEStringEncryptor pbeStringEncryptor
			, @Qualifier("app") PropertiesFactoryBean appProps
			, @Qualifier("common") PropertiesFactoryBean commonProps
			, @Qualifier("batch")  PropertiesFactoryBean batchProps) throws IOException {

		
		Properties[] properties = new Properties[] {appProps.getObject(), commonProps.getObject(), batchProps.getObject()};
		/*************************************
		 * 암호화 Properties가 
		 * $으로 읽어올땐 자동으로 풀려지고.
		 * #으로 읽어올땐 자동으로 안풀려져서.
		 * 여기서 강제로 암호화 Properties는 강제로 풀려서 저장하게끔 한다.
		 *************************************/
		for (Properties props : properties) {
			if(props!=null)
				for (Object key : props.keySet()) {
					if(key!=null && key instanceof String && PropertiesConfig.isEncryptProps(props.getProperty((String) key))) {
						props.setProperty((String)key
								, PropertiesConfig.getDecryptProps(props.getProperty((String) key)));
					}
				}
		}

		EncryptablePropertyPlaceholderConfigurer configurer = new EncryptablePropertyPlaceholderConfigurer(pbeStringEncryptor);
		configurer.setPropertiesArray(properties);
		return configurer;
	}

}
