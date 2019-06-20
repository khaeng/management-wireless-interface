package com.itcall.SpringSecurityRSA.rsa;

import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SecureRsaCripto {
	private static final Logger log = LoggerFactory.getLogger(SecureRsaCripto.class);
	public static final String RSA_DYNMIC_KEY = "_RSA_Dynamic_Key_";


	public String getRSAEncode(String data, String publicKeyStr) throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException {
		return getRSAEncode(data, publicKeyStr, Charset.defaultCharset());
	}
	public String getRSAEncode(String data, String publicKeyStr, Charset charset) throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException, NoSuchPaddingException {
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		byte[] bPublicKey = Base64.getDecoder().decode(publicKeyStr.getBytes());
		PublicKey publicKey = null;
		X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(bPublicKey);
		publicKey = keyFactory.generatePublic(publicKeySpec);
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		return getRSAEncode(data, cipher, charset);
	}
	public String getRSAEncode(String data, Cipher cipher, Charset charset) {
		try {
			// 공개키 이용 암호화
			byte[] bCipher = cipher.doFinal(data.getBytes(charset));
			String sCipherBase64 = Base64.getEncoder().encodeToString(bCipher);
			return sCipherBase64;
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			log.error("RSA Secure Encode Error Cipher[{}]. error mesage[{}] :{}", cipher, e.getMessage(), e);
		}
		return null;
	}


	public static DynamicKeyPairRSA getOneTimeRSA() throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException {
		DynamicKeyPairRSA result = new DynamicKeyPairRSA();
		PublicKey publicKey = null;
		PrivateKey privateKey = null;
		SecureRandom secureRandom = new SecureRandom();
		KeyPairGenerator keyPairGenerator;
		keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(2048, secureRandom);
		KeyPair keyPair = keyPairGenerator.genKeyPair();
		KeyFactory keyFactory1 = KeyFactory.getInstance("RSA");
		publicKey = keyPair.getPublic();
		RSAPublicKeySpec rsaPublicKeySpec = keyFactory1.getKeySpec(publicKey, RSAPublicKeySpec.class);
		result.setPublicKeyModules( rsaPublicKeySpec.getModulus().toString(16));
		result.setPublicKeyExponent(rsaPublicKeySpec.getPublicExponent().toString(16));
		log.info("Public key[DynamicKeyMake] modulus : {}", result.getPublicKeyModules());
		log.info("Public key[DynamicKeyMake] exponent: {}", result.getPublicKeyExponent());
		byte[] bPublicKey = publicKey.getEncoded();
		result.setPublicKey(Base64.getEncoder().encodeToString(bPublicKey));
		log.info("Public key[DynamicKeyMake] : {}", result.getPublicKey());

		privateKey = keyPair.getPrivate();
		RSAPrivateKeySpec rsaPrivateKeySpec = keyFactory1.getKeySpec(privateKey, RSAPrivateKeySpec.class);
		log.info("Private key[DynamicKeyMake] modulus : {}", rsaPrivateKeySpec.getModulus());
		log.info("Private key[DynamicKeyMake] exponent: {}", rsaPrivateKeySpec.getPrivateExponent());
		byte[] bPrivateKey = privateKey.getEncoded();
		String sPrivateKey  = Base64.getEncoder().encodeToString(bPrivateKey);
		result.setPrivateKey(sPrivateKey);
		return result;
	}

	public static void initRsaSession(HttpServletRequest request) throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException {
		HttpSession session = request.getSession(true);
		DynamicKeyPairRSA dynamicKeyPairRSA = getOneTimeRSA();
		session.removeAttribute(RSA_DYNMIC_KEY);
		session.setAttribute(RSA_DYNMIC_KEY, dynamicKeyPairRSA.getPrivateKey());
		request.setAttribute("_RSAModules", dynamicKeyPairRSA.getPublicKeyModules());
		request.setAttribute("_RSAExponent", dynamicKeyPairRSA.getPublicKeyExponent());
	}

	public static String getRSaDecodeFromSession(HttpServletRequest request, String encStr) throws Exception {
		String privateKeyStr = request.getSession(false).getAttribute(RSA_DYNMIC_KEY).toString();
		return decryptRsa(encStr, privateKeyStr);
	}

	/**
	 * 복호화
	 * 
	 * @param privateKey
	 * @param securedValue
	 * @return
	 * @throws Exception
	 */
	private static String decryptRsa(String securedValue, String privateKeyStr) throws Exception {
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		byte[] bPrivateKey = Base64.getDecoder().decode(privateKeyStr.getBytes());
		PrivateKey privateKey = null;
		PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(bPrivateKey);
		privateKey = keyFactory.generatePrivate(privateKeySpec);
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		byte[] encryptedBytes = hexToByteArray(securedValue);
		cipher.init(Cipher.DECRYPT_MODE, privateKey);
		byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
		String decryptedValue = new String(decryptedBytes, "utf-8"); // 문자 인코딩 주의.
		return decryptedValue;
	}

	public static String encryptRsa(String data, String publicKeyStr, Charset charset) throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		byte[] bPublicKey = Base64.getDecoder().decode(publicKeyStr.getBytes());
		PublicKey publicKey = null;
		X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(bPublicKey);
		publicKey = keyFactory.generatePublic(publicKeySpec);
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		byte[] bCipher = cipher.doFinal(data.getBytes(charset));
		return byteArrayToHex(bCipher);
	}

	public static void main(String[] args) throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException {

		System.out.println(String.format("%,03.2f(Tps)",0.234));
		if(true)return ;
		String testArr = byteArrayToHex(new byte[] {0, 64, 32});
		byte[] bts = hexToByteArray(testArr);
		System.out.println(new String(bts));

		DynamicKeyPairRSA dynamicKeyPairRSA = getOneTimeRSA();
		String test = "asdfa한글test123";
		try {
			String encData = encryptRsa(test, dynamicKeyPairRSA.getPublicKey(), Charset.forName("UTF-8"));
			System.out.println(decryptRsa(encData, dynamicKeyPairRSA.getPrivateKey()));
			
			encData = encryptRsaBase64(test, dynamicKeyPairRSA.getPublicKey(), Charset.forName("UTF-8"));
			System.out.println(decryptRsaBase64(encData, dynamicKeyPairRSA.getPrivateKey()));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String decryptRsaBase64(String securedValue, String privateKeyStr) throws Exception {
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		byte[] bPrivateKey = Base64.getDecoder().decode(privateKeyStr.getBytes());
		PrivateKey privateKey = null;
		PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(bPrivateKey);
		privateKey = keyFactory.generatePrivate(privateKeySpec);
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		byte[] encryptedBytes = Base64.getDecoder().decode(securedValue.getBytes()); // hexToByteArray(securedValue);
		cipher.init(Cipher.DECRYPT_MODE, privateKey);
		byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
		String decryptedValue = new String(decryptedBytes, "utf-8"); // 문자 인코딩 주의.
		return decryptedValue;
	}

	public static String encryptRsaBase64(String data, String publicKeyStr, Charset charset) throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		KeyFactory keyFactory2 = KeyFactory.getInstance("RSA");
		byte[] bPublicKey = Base64.getDecoder().decode(publicKeyStr.getBytes());
		X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(bPublicKey);
		PublicKey publicKey = keyFactory2.generatePublic(publicKeySpec);
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		// 공개키 이용 암호화
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		byte[] bCipher = cipher.doFinal(data.getBytes());
		String sCipherBase64 = Base64.getEncoder().encodeToString(bCipher);
		return sCipherBase64;
	}

	/**
	 * 16진 문자열을 byte 배열로 변환한다.
	 * 
	 * @param hex
	 * @return
	 */
	public static byte[] hexToByteArray(String hex) {
		if (hex == null || hex.length() % 2 != 0) {
			return new byte[] {};
		}
		byte[] bytes = new byte[hex.length() / 2];
		for (int i = 0; i < hex.length(); i += 2) {
			byte value = (byte) Integer.parseInt(hex.substring(i, i + 2), 16);
			bytes[(int) Math.floor(i / 2)] = value;
		}
		return bytes;
	}

	public static String byteArrayToHex(byte[] buf) {
		StringBuffer sb = new StringBuffer();
		for (byte b : buf) {
			sb.append(String.format("%02x", b));
		}
		return sb.toString();
	}

}
