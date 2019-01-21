package com.itcall.batch.util;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.EnvironmentStringPBEConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Base64Utils;

import com.itcall.batch.config.properties.PropertiesSecureConfig;

public class SecureUtil {

	static final Logger LOG = LoggerFactory.getLogger(SecureUtil.class);

	private static final String SEC_SPEC_KEY = "a9ac0106b3b12217de92293029e5cb4c"; // 32BIT
	private static final String IV_SPEC_PARAM = "itcall.MERGE"; // 16BIT
	private final static String JASYPT_PASSWORD = "13pHGrNliD6lxe567DAKftQeQBW7aweFcGOjA3rFN6EF0VnNhIe75vMu4vdy3ysh";

	/**
	 * Filed등 일반적인 스트링 암호화에 쓰인다.
	 * @param value
	 * @return
	 */
	public static String encrypt(String value) {
		return getSecString(true, value);
	}

	/**
	 * Filed등 일반적인 스트링 복호화에 쓰인다.
	 * @param value
	 * @return
	 */
	public static String decrypt(String value) {
		return getSecString(false, value);
	}


	public static String encryptPropKey(String value) {
		return encrypt(SEC_SPEC_KEY, IV_SPEC_PARAM, value);
	}

	private static String encrypt(String key, String ivSpecParam, String value) {
		try {
			IvParameterSpec iv = new IvParameterSpec(ivSpecParam.getBytes("UTF-8"));
			SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
			
			byte[] encrypted = cipher.doFinal(value.getBytes());

			return Base64Utils.encodeToString(encrypted);
		} catch (Exception e) {
			LOG.error("", e);
		}

		return null;
	}

	public static String decryptPropKey(String encrypted) {
		return decrypt(SEC_SPEC_KEY, IV_SPEC_PARAM, encrypted);
	}

	private static String decrypt(String key, String ivSpecParam, String encrypted) {
		try {
			IvParameterSpec iv = new IvParameterSpec(ivSpecParam.getBytes("UTF-8"));
			SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

			byte[] original = cipher.doFinal(Base64Utils.decodeFromString(encrypted));

			return new String(original);
		} catch (Exception e) {
			LOG.error("", e);
		}

		return null;
	}

	private static String getSecString(boolean isEnc, String value) {
		StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
		EnvironmentStringPBEConfig pbeConfig = new EnvironmentStringPBEConfig();
		pbeConfig.setAlgorithm(PropertiesSecureConfig.JASYPT_ALGORITHM); // PBEWITHMD5ANDDES
		pbeConfig.setPassword(/*SecureUtil.*/decryptPropKey(JASYPT_PASSWORD));//13pHGrNliD6lxe567DAKftQeQBW7aweFcGOjA3rFN6EF0VnNhIe75vMu4vdy3ysh
		encryptor.setConfig(pbeConfig);
		if(isEnc)
			return encryptor.encrypt(value);
		else
			return encryptor.decrypt(value);
	}

}
