package com.itcall.util.lan;

import java.io.IOException;

/**
 * Hello world!
 *
 */
public class App {

	public static void main(String... args) throws IOException, InterruptedException {
		System.out.println("Hello World!");

		if (args != null && args.length == 1) {
			// Your Network interface Name ::: args[0]
			InterfaceUtil.reStartNetInterface(args[0]);
		} else if (args != null && args.length == 2) {
			// Your wireless Network interface Name ::: args[0] 
			// Your wireless Connected to ssid Name ::: args[1]
			InterfaceUtil.reStartNetInterface(args[0], args[1]);
		} else {
			System.out.println("Usage ::: parameters INTERFACE_NAME [SSID]");
		}
	}
}
