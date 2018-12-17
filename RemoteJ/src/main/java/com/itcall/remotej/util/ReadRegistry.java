package com.itcall.remotej.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

public class ReadRegistry {
	public enum HKEY {
		LOCAL_MACHINE, CURRENT_USER;
		public String getPath(String path) throws Exception {
			if (path != null && path.startsWith("\\")) {
				path = path.substring(1);
			}
			switch (this) {
			case LOCAL_MACHINE:
				return "HKLM\\" + path;
			case CURRENT_USER:
				return "HKCU\\" + path;
			}
			throw new Exception("No supported...");
		}
	}

	public static final List<Map<String, String>> readRegistryList(HKEY root, String path){
//		try{
//			// Control Panel\Desktop\PermonitorSettings
//			String cmd = "reg query " + '"' + root.getPath(path) + "\" /s";
//			Process process = Runtime.getRuntime().exec(cmd);
//	
//			StreamReader reader = new StreamReader(process.getInputStream());
//			reader.start();
//			process.waitFor();
//			reader.join();
//	
//			// Parse out the value
//			// String[] parsed = reader.getResult().split("\\s+");
//	
//			String s1[];
//			try {
//				s1 = reader.getResult().split("REG_SZ|REG_DWORD|REG_QWORD|REG_EXPAND_SZ|REG_MULTI_SZ|REG_BINARY");
//			} catch (Exception e) {
//				return " ";
//			}
//			// MK System.out.println(s1[1].trim());
//	
//			return s1[1].trim();
//		} catch (Exception e) {
//		}

		return null;
	}
	public static final String readRegistry(HKEY root, String path, String key) {
		try {
			// Run reg query, then read output with StreamReader (internal
			// class)
			String cmd = "reg query " + '"' + root.getPath(path) + "\" /v " + key;
			Process process = Runtime.getRuntime().exec(cmd);

			StreamReader reader = new StreamReader(process.getInputStream());
			reader.start();
			process.waitFor();
			reader.join();

			// Parse out the value
			// String[] parsed = reader.getResult().split("\\s+");

			String s1[];
			try {
				s1 = reader.getResult().split("REG_SZ|REG_DWORD|REG_QWORD|REG_EXPAND_SZ|REG_MULTI_SZ|REG_BINARY");
			} catch (Exception e) {
				return " ";
			}
			// MK System.out.println(s1[1].trim());

			return s1[1].trim();
		} catch (Exception e) {
		}

		return null;
	}

	static class StreamReader extends Thread {
		private InputStream is;
		private StringWriter sw = new StringWriter();

		public StreamReader(InputStream is) {
			this.is = is;
		}

		public void run() {
			try {
				int c;
				while ((c = is.read()) != -1)
					// System.out.println(c);
					sw.write(c);
			} catch (IOException e) {
			}
		}

		public String getResult() {
			return sw.toString();
		}
	}
}