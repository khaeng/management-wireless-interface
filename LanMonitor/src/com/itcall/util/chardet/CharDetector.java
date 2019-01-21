package com.itcall.util.chardet;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import com.itcall.util.chardet.UniversalDetector;

public class CharDetector {

	/**
	 * 실행되는 VM SYSTEM의 기본적인 Charater SET이 저장되어 있음.
	 */
	public static final Charset DEF_CHARSET = Charset.defaultCharset();

	public static void main(String[] args) throws java.io.IOException {
//		if (args.length != 1) {
//			System.err.println("Usage: java CharDetector FILENAME");
//			System.exit(1);
//		}
		Charset charset;
		
		charset = Charset.forName(detectFileCharset("D:\\KTDS\\works_x64\\egi-stub\\egi-stub-core\\FileBatchRecv\\CyKTOA\\20130422\\test.ansi"));
		detectFileCharset("D:\\KTDS\\works_x64\\egi-stub\\egi-stub-core\\FileBatchRecv\\CyKTOA\\20130422\\test.utf8");
		detectFileCharset("D:\\KTDS\\works_x64\\egi-stub\\egi-stub-core\\FileBatchSend\\CyKTOA\\test.utf8");
		detectFileCharset("D:\\KTDS\\works_x64\\egi-stub\\egi-stub-core\\FileBatchRecv\\CyKTOA\\20130422\\test.ansi");
		detectFileCharset("D:\\KTDS\\works_x64\\egi-stub\\egi-stub-core\\FileBatchRecv\\CyKTOA\\20130422\\test.utf8");
		detectFileCharset("D:\\KTDS\\works_x64\\egi-stub\\egi-stub-core\\FileBatchSend\\CyKTOA\\test.utf8");
		detectFileCharset("D:\\KTDS\\works_x64\\egi-stub\\egi-stub-core\\FileBatchRecv\\CyKTOA\\20130422\\test.ansi");
		detectFileCharset("D:\\KTDS\\works_x64\\egi-stub\\egi-stub-core\\FileBatchRecv\\CyKTOA\\20130422\\test.utf8");
		detectFileCharset("D:\\KTDS\\works_x64\\egi-stub\\egi-stub-core\\FileBatchSend\\CyKTOA\\test.utf8");
	}

	public static String detectFileCharset(String fileName) {
		return detectFileCharset(fileName, DEF_CHARSET.name());
	}
	public static String detectFileCharset(String fileName, String defCharset) {// throws IOException{
		String encoding = defCharset;
		try {
			byte[] buf = new byte[4096];
	//		String fileName = args[0];
			java.io.FileInputStream fis = new java.io.FileInputStream(fileName);
	
			// (1)
			UniversalDetector detector = new UniversalDetector(null);
	
			// (2)
			int nread;
			while ((nread = fis.read(buf)) > 0 && !detector.isDone()) {
				detector.handleData(buf, 0, nread);
			}
			// (3)
			detector.dataEnd();
	
			// (4)
			encoding = detector.getDetectedCharset();
			if (encoding != null) {
				System.out.println(fileName + " fils's Detected encoding = " + encoding);
			} else {
				encoding = defCharset;
				System.out.println(fileName + " fils's No encoding detected. using default encoding = " + defCharset);
			}
	
			// (5)
			detector.reset();
		} catch (IOException e) {
			encoding = defCharset;
			System.out.println(fileName + " fils's Encoding detected. IOException : " + e.getMessage() + " using default encoding = " + defCharset);
		}
		
		return encoding;
	}
	public static String detectFileCharset(InputStream fis, String defCharset) {// throws IOException{
		String encoding = defCharset;
		try {
			byte[] buf = new byte[4096];
	//		String fileName = args[0];
//			java.io.FileInputStream fis = new java.io.FileInputStream(fileName);
	
			// (1)
			UniversalDetector detector = new UniversalDetector(null);
	
			// (2)
			int nread;
			while ((nread = fis.read(buf)) > 0 && !detector.isDone()) {
				detector.handleData(buf, 0, nread);
			}
			// (3)
			detector.dataEnd();
	
			// (4)
			encoding = detector.getDetectedCharset();
			if (encoding != null) {
//				System.out.println(fileName + " fils's Detected encoding = " + encoding);
			} else {
				encoding = defCharset;
//				System.out.println(fileName + " fils's No encoding detected. using default encoding = " + defCharset);
			}
	
			// (5)
			detector.reset();
		} catch (IOException e) {
			encoding = defCharset;
//			System.out.println(fileName + " fils's Encoding detected. IOException : " + e.getMessage() + " using default encoding = " + defCharset);
		}
		
		return encoding;
	}
	public static String detectFileCharset(byte[] buf, String defCharset) {// throws IOException{
		String encoding = defCharset;
		// (1)
		UniversalDetector detector = new UniversalDetector(null);

		// (2)
//			int nread;
//			while ((nread = fis.read(buf)) > 0 && !detector.isDone()) {
//				detector.handleData(buf, 0, nread);
//			}
		detector.handleData(buf, 0, buf.length);
		// (3)
		detector.dataEnd();

		// (4)
		encoding = detector.getDetectedCharset();
		if (encoding != null) {
//				System.out.println(fileName + " fils's Detected encoding = " + encoding);
		} else {
			encoding = defCharset;
//				System.out.println(fileName + " fils's No encoding detected. using default encoding = " + defCharset);
		}

		// (5)
		detector.reset();
		
		return encoding;
	}
}