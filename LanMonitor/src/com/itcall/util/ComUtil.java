package com.itcall.util;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ComUtil {

	synchronized public static void saveLogFile(String fileName, String data, boolean isAppend) throws IOException{
		BufferedOutputStream bw = new BufferedOutputStream(new FileOutputStream(fileName, isAppend));
		bw.write(data.getBytes()); bw.flush();
		bw.close();
	}
}
