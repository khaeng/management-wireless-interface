package com.itcall.SpringSecurityRSA.rsa;

public class DynamicKeyPairRSA{
	String privateKey;
	String publicKey;
	String publicKeyModules;
	String publicKeyExponent;
	
	public String getPrivateKey() {
		return privateKey;
	}
	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}
	public String getPublicKey() {
		return publicKey;
	}
	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}
	public String getPublicKeyModules() {
		return publicKeyModules;
	}
	public void setPublicKeyModules(String publicKeyModules) {
		this.publicKeyModules = publicKeyModules;
	}
	public String getPublicKeyExponent() {
		return publicKeyExponent;
	}
	public void setPublicKeyExponent(String publicKeyExponent) {
		this.publicKeyExponent = publicKeyExponent;
	}

	
}
