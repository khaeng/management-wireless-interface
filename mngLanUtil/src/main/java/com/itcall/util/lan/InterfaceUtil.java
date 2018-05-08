package com.itcall.util.lan;

import java.io.IOException;

public class InterfaceUtil{

	/**
	 * restart Network interface
	 * @param interfaceName
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static final void reStartNetInterface(String interfaceName) throws IOException, InterruptedException {
		reStartNetInterface(interfaceName, null);
	}
	/**
	 * restart and reconnect to Network interface
	 * @param interfaceName
	 * @param ssid
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static final void reStartNetInterface(String interfaceName, String ssid) throws IOException, InterruptedException {
		System.out.print(String.format("Network is Restart... Try to \"%s\" interface", interfaceName));
		final Runtime runtime = Runtime.getRuntime();
		
		// disconnect wireless lan (wireless case)
		String cmd = "cmd /c netsh wlan disconnect";
		Process process = runtime.exec(cmd);
		process.waitFor();
		Thread.sleep(1 * 1000);

		// network interface is disabled...
		cmd = String.format("cmd /c netsh interface set interface \"%s\" DISABLED", interfaceName);
		process = runtime.exec(cmd);
		process.waitFor();
		Thread.sleep(1 * 1000);

		// network interface is enabled...
		cmd = String.format("cmd /c netsh interface set interface \"%s\" ENABLED", interfaceName);
		process = runtime.exec(cmd);
		process.waitFor();
		Thread.sleep(3 * 1000);


		if(ssid!=null && !ssid.isEmpty()){
			// connect wireless lan (wireless case)
			cmd = String.format("cmd /c netsh wlan name=\"%s\" ssid=\"%s\" interface=\"%s\"", ssid, ssid, interfaceName);
			process = runtime.exec(cmd);
			process.waitFor();
			Thread.sleep(1 * 1000);
		}

	}
}