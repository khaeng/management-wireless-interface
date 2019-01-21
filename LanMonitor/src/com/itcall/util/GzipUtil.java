package com.itcall.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GzipUtil {

	public static byte[] compress(String value) throws Exception{
		return compress(value.getBytes());
	}
	public static byte[] compress(byte[] value) throws Exception{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		GZIPOutputStream gzipOut = new GZIPOutputStream(new BufferedOutputStream(out));
		gzipOut.write(value);
		gzipOut.finish();
		gzipOut.close();
		return out.toByteArray();
	}

	public static String decompressStr(byte[] value) throws Exception {
		return new String(decompress(value));
	}

	public static byte[] decompress(byte[] value) throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		GZIPInputStream gzipIn = new GZIPInputStream(new BufferedInputStream(new ByteArrayInputStream(value)));
		int size = 0;
		byte[] buffer = new byte[1024];
		while ((size = gzipIn.read(buffer)) > 0) {
			out.write(buffer, 0, size);
		}
		out.flush();
		out.close();
		return out.toByteArray();
	}
}
