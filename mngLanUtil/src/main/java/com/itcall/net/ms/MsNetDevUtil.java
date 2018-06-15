package com.itcall.net.ms;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wired.Network
 * 1. ipconfig / renew "InterfaceDeviceName"
 * 2. ipconfig / release "InterfaceDeviceName"
 * 
 * Wireless.Network
 * 1. Disconnecting : 			netsh wlan disconnect
 * 2. Stop network : 			netsh interface set interface "InterfaceDeviceName" DISABLED
 * 3. Start network : 			netsh interface set interface "InterfaceDeviceName" ENABLED
 * 4. Connecting : 				netsh wlan name="ProfileName" ssid="SsidNameIsSameOfProfileName" interface="InterfaceName"
 * 
 * Sample
 * 			netsh wlan disconnect
 * 			netsh interface set interface "무선 네트워크 연결 5" DISABLED
 * 			netsh interface set interface "무선 네트워크 연결 5" ENABLED
 * 			netsh wlan name="KT_Free_Wifi_AP_Name" ssid="KT_Free_Wifi_AP_Name" interface="무선 네트워크 연결 5"
 * 
 * @author haeng
 */
public class MsNetDevUtil {

	private static final Logger LOG = LoggerFactory.getLogger(MsNetDevUtil.class);

	private static final String checkUrlStr = "http://www.google.com/";

	public static void checkNetAndCtrl(String interfaceDeviceName) throws Exception{
		checkNetAndCtrl(interfaceDeviceName, checkUrlStr);
	}

	public static void checkNetAndCtrl(String interfaceDeviceName, String checkUrl) throws Exception{
		if(!isNetAvailable(checkUrl))
			reStartNetDevInterface(interfaceDeviceName);
	}

	public static boolean isNetAvailable(){
		return isNetAvailable(checkUrlStr);
	}

	public static boolean isNetAvailable(String checkUrl) {
		try {
			URL url = new URL(checkUrl);
			URLConnection conn = url.openConnection();
			conn.connect();
			conn.getInputStream().close();
			return true;
		} catch (Exception e) {
			LOG.error("[{]} is not response. Network has problem... ::: {}", checkUrl, e.getMessage());
		}
		return false;
	}

	public static void reStartNetDevInterface(String interfaceDeviceName) throws Exception{
		LOG.debug("Try to [{}] interface network restart...", interfaceDeviceName);
		switch (checkNetworkDevice(interfaceDeviceName)) {
		case WIRED:
			stopLanInterface(interfaceDeviceName);
			Thread.sleep(1 * 1000);
			startLanInterface(interfaceDeviceName);
			break;
		case WIRELESS:
			stopWlanInterface(interfaceDeviceName);
			Thread.sleep(1 * 1000);
			startWlanInterface(interfaceDeviceName);
			break;
		default:
			LOG.warn("No has [{}] network device name.", interfaceDeviceName);
			break;
		}
	}

	public static void startWlanInterface(String interfaceDeviceName) throws Exception {
		try{
			Process process = Runtime.getRuntime().exec("cmd /c netsh interface set interface \"" + interfaceDeviceName + "\" ENABLED");
			process.waitFor();
			process.destroy();
			LOG.debug("[{}] is started...", interfaceDeviceName);
		}catch (Exception e){
			LOG.error("[{}] is start error ::: {}", interfaceDeviceName, e.getMessage());
			throw e;
		}
	}

	public static void stopWlanInterface(String interfaceDeviceName) throws Exception {
		try{
			Process process = Runtime.getRuntime().exec("cmd /c netsh interface set interface \"" + interfaceDeviceName + "\" DISABLED");
			process.waitFor();
			process.destroy();
			LOG.debug("[{}] is stoped!", interfaceDeviceName);
		}catch (Exception e){
			LOG.error("[{}] is stop error ::: {}", interfaceDeviceName, e.getMessage());
			throw e;
		}
	}

	public static void startLanInterface(String interfaceDeviceName) throws Exception {
		try{
			Process process = Runtime.getRuntime().exec("cmd /c ipconfig /renew \"" + interfaceDeviceName + "\"");
			process.waitFor();
			process.destroy();
			LOG.debug("[{}] is started...", interfaceDeviceName);
		}catch (Exception e){
			LOG.error("[{}] is start error ::: {}", interfaceDeviceName, e.getMessage());
			throw e;
		}
	}

	public static void stopLanInterface(String interfaceDeviceName) throws Exception {
		try{
			Process process = Runtime.getRuntime().exec("cmd /c ipconfig /release \"" + interfaceDeviceName + "\"");
			process.waitFor();
			process.destroy();
			LOG.debug("[{}] is stoped!", interfaceDeviceName);
		}catch (Exception e){
			LOG.error("[{}] is stop error ::: {}", interfaceDeviceName, e.getMessage());
			throw e;
		}
	}

	/**
	 * Check kind of network device.
	 * 
	 * @param interfaceDeviceName
	 * @return NetWireCd
	 * 			WIRELESS : Wireless network adapter
	 * 			WIRED    : Wired network adapter
	 * 			NONE     : No has network adapter
	 */
	public static NetWireCd checkNetworkDevice(String interfaceDeviceName){
		if(isWlanDev(interfaceDeviceName)){
			return NetWireCd.WIRELESS;
		}else if(isLanDev(interfaceDeviceName)){
			return NetWireCd.WIRED;
		}
		return NetWireCd.NONE;
	}

	public static boolean isWlanDev(String interfaceDeviceName){
		try{
			Process process = Runtime.getRuntime().exec("cmd /c netsh wlan show interface");
			process.waitFor();
			ByteBuffer byteBuffer = ByteBuffer.allocate(1024*1024);
			InputStream inputStream = process.getInputStream();
			while (inputStream.available()>0) {
				byteBuffer.put((byte)inputStream.read());
			}
			process.destroy();
			return new String(byteBuffer.array(), Charset.forName("MS949")).contains(interfaceDeviceName);
		}catch (Exception e){
			LOG.error("isWlanDev({}) error ::: {}", interfaceDeviceName, e.getMessage());
		}
		return false;
	}

	public static boolean isLanDev(String interfaceDeviceName){
		if(isWlanDev(interfaceDeviceName)) return false; // This is Wireless Network Adapter.
		try{
			Process process = Runtime.getRuntime().exec("cmd /c ipconfig /all");
			process.waitFor();
			ByteBuffer byteBuffer = ByteBuffer.allocate(1024*1024);
			InputStream inputStream = process.getInputStream();
			while (inputStream.available()>0) {
				byteBuffer.put((byte)inputStream.read());
			}
			process.destroy();
			return new String(byteBuffer.array(), Charset.forName("MS949")).contains(interfaceDeviceName);
		}catch (Exception e){
			LOG.error("isLanDev({}) error ::: {}", interfaceDeviceName, e.getMessage());
		}
		return false;
	}

	public enum NetWireCd{
		WIRED
		,
		WIRELESS
		,
		NONE
		;
	}

}
