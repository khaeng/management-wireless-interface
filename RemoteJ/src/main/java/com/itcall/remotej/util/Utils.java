package com.itcall.remotej.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Utils {

	public final static int MAX_WORK_MULTI_CNT = 10;
	public final static Executor ALL_EXECUTOR_POOL = Executors.newFixedThreadPool(MAX_WORK_MULTI_CNT);
	public final static Executor ALL_EXECUTOR_SINGLE_POOL = Executors.newSingleThreadExecutor();

	/**
	 * ㅇ. ByteOrder.BIG_ENDIAN (inJava)
	 * ㅇ. ByteOrder.LITTLE_ENDIAN (c or file)
	 * Write Number to 2bytes
	 */
	public static byte[] numTobyte(short shortNum, ByteOrder order) {
		ByteBuffer buff = ByteBuffer.allocate(Short.SIZE/8);
		buff.order(order);
		buff.putShort(shortNum);
		return buff.array();
	}
	/**
	 * ㅇ. ByteOrder.BIG_ENDIAN (inJava)
	 * ㅇ. ByteOrder.LITTLE_ENDIAN (c or file)
	 * Write Number to 4bytes
	 */
	public static byte[] numTobyte(int integer, ByteOrder order) {
		ByteBuffer buff = ByteBuffer.allocate(Integer.SIZE/8);
		buff.order(order);
		buff.putInt(integer);
		return buff.array();
	}
	/**
	 * ㅇ. ByteOrder.BIG_ENDIAN (inJava)
	 * ㅇ. ByteOrder.LITTLE_ENDIAN (c or file)
	 * Write Number to 8bytes
	 */
	public static byte[] numTobyte(long longNum, ByteOrder order) {
		ByteBuffer buff = ByteBuffer.allocate(Long.SIZE/8);
		buff.order(order);
		buff.putLong(longNum);
		return buff.array();
	}
	/**
	 * read Number from 2bytes
	 */
 	public static short byteToShot(byte[] bytes, ByteOrder order) {
		ByteBuffer buff = ByteBuffer.allocate(Short.SIZE/8);
		buff.order(order);
		buff.put(bytes);
		buff.flip();
		return buff.getShort();
	}
	/**
	 * read Number from 4bytes
	 */
	public static int byteToInt(byte[] bytes, ByteOrder order) {
		ByteBuffer buff = ByteBuffer.allocate(Integer.SIZE/8);
		buff.order(order);
		buff.put(bytes);
		buff.flip();
		return buff.getInt();
	}
	/**
	 * read Number from 8bytes
	 */
	public static long byteToLong(byte[] bytes, ByteOrder order) {
		ByteBuffer buff = ByteBuffer.allocate(Long.SIZE/8);
		buff.order(order);
		buff.put(bytes);
		buff.flip();
		return buff.getLong();
	}

}
