package com.itcall.remotej.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.itcall.remotej.net.NettyCd;

public class ArchiveUtil {

	private static ByteArrayOutputStream byteArrOutStrm = new ByteArrayOutputStream();
	private static ByteArrayOutputStream[] arrByteArrOutStrm ;
	static {
		arrByteArrOutStrm = new ByteArrayOutputStream[NettyCd.MAX_WORK_MULTI_CNT];
		for (int i = 0; i < arrByteArrOutStrm.length; i++) {
			arrByteArrOutStrm[i] = new ByteArrayOutputStream();
		}
	}


	public static byte[] zipData(byte[] srcBts) throws IOException{
		ByteArrayOutputStream baos  = new ByteArrayOutputStream();
		ZipOutputStream zipOut = new ZipOutputStream(baos, NettyCd.DEF_CHARSET);
		zipOut.setLevel(9); // 기본은 8, 최대 9
		zipOut.putNextEntry(new ZipEntry(NettyCd.DEF_ZIP_ENTRY_NAME));
		zipOut.write(srcBts);
		zipOut.flush();
		zipOut.finish();
		zipOut.close();
		baos.close();
		return baos.toByteArray(); // 		System.out.println("BMP 변환후 압축 싸이즈 : " + baos .size());
	}
	public static byte[] unZipData(byte[] zipBts) throws IOException{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipInputStream unZipIn = new ZipInputStream(new ByteArrayInputStream(zipBts));
		unZipIn.getNextEntry(); // 데이터압축시 Entry는 꼭 하나로 생성해야 한다.(위의 temp.tmp처럼)
		byte[] buffer = new byte[NettyCd.DATA_LENTGH];
		int length = -1;
		while ((length=unZipIn.read(buffer))>-1)
			baos.write(buffer, 0, length);
		unZipIn.close();
		baos.flush();
		baos.close();
		return baos.toByteArray(); // 		System.out.println("BMP 변환후 압축 후 해제싸이즈 : " + baos.size());
	}

	/**
	 * 바뀐 이미지부분만 추출하여 압축
	 * @param oldBts
	 * @param newBts
	 * @return
	 * @throws IOException
	 */
	public static byte[] getCutsImgMulti(final byte[] oldBts, final byte[] newBts) throws IOException {
		Thread[] thread = new Thread[NettyCd.MAX_WORK_MULTI_CNT];
		byteArrOutStrm.reset();
		for (int i = 0; i < NettyCd.MAX_WORK_MULTI_CNT; i++) {
			final int idx = i;
			final int start = newBts.length/NettyCd.MAX_WORK_MULTI_CNT*i;
			final int end = (i+1==NettyCd.MAX_WORK_MULTI_CNT) ? (newBts.length/NettyCd.MAX_WORK_MULTI_CNT*(i+1))
					: (newBts.length/NettyCd.MAX_WORK_MULTI_CNT*(i+1) + newBts.length%NettyCd.MAX_WORK_MULTI_CNT) ;
			thread[i] = new Thread(new Runnable() {
				@Override public void run() {
					getCutsImgMulti(idx, start, Arrays.copyOfRange(oldBts, start, end), Arrays.copyOfRange(newBts, start, end));
				}
			});
			Utils.ALL_EXECUTOR_POOL.execute(thread[i]);
		}
		for (int i = 0; i < NettyCd.MAX_WORK_MULTI_CNT; i++) {
			try {
				thread[i].join();
				thread[i] = null;
			} catch (InterruptedException e) {e.printStackTrace();}
			byteArrOutStrm.write(arrByteArrOutStrm[i].toByteArray());
		}
		byteArrOutStrm.flush();
		byteArrOutStrm.close();
		return zipData(byteArrOutStrm.toByteArray());
	}
	public static void getCutsImgMulti(int idx, int pos, byte[] oldBts, byte[] newBts) {
		try{
			arrByteArrOutStrm[idx].reset();
			int start = 0, end = 0, sameCnt = 0;
			for (int i = 0; i < newBts.length; i++) {
				if(oldBts[i]!=newBts[i]) {
					start = i;
					for (++i; i < newBts.length; i++) {
						if(oldBts[i]==newBts[i] && ++sameCnt > NettyCd.IMG_CUT_LIMIT_SIZE) {
							end = i - NettyCd.IMG_CUT_LIMIT_SIZE;
							break;
						}else if(oldBts[i]!=newBts[i]){
							sameCnt=0;
						}
					}
					if(end<=0) end = newBts.length;
					Arrays.copyOfRange(newBts, start, end);
					arrByteArrOutStrm[idx].write(String.format("%010d", start+pos).getBytes(), 0, 10);
					arrByteArrOutStrm[idx].write(String.format("%010d",   end+pos).getBytes(), 0, 10);
					arrByteArrOutStrm[idx].write(Arrays.copyOfRange(newBts, start, end), 0, end-start);
					start=end=sameCnt=0;
				}
			}
			arrByteArrOutStrm[idx].flush();
			arrByteArrOutStrm[idx].close();
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 바뀐 이미지 부분을 복구합니다.
	 * @param oldBts
	 * @param newBts
	 * @return
	 * @throws IOException
	 */
	public static byte[] restoreCutsImg(byte[] oldBts, byte[] newBts) throws IOException {
		int start = 0, end = 0;
		newBts = unZipData(newBts);
		while (true) {
			try {
				start = Integer.parseInt(new String(Arrays.copyOfRange(newBts,  0, 10)));
				end   = Integer.parseInt(new String(Arrays.copyOfRange(newBts, 10, 20)));
				System.arraycopy(newBts, 20, oldBts, start, end-start); // 교체
				newBts = Arrays.copyOfRange(newBts, end-start+20, newBts.length);
				if(newBts.length<=0)
					break;
			}catch (Exception e) {
				e.printStackTrace();
				break;
			}
		}
		return oldBts;
	}

	public static long getKbytes(long size){
		return size/1024;
	}

	public static long bytesToLong(byte[] bts){
		return new BigInteger(bts).longValue();
	}
	public static int bytesToInt(byte[] bts){
		return new BigInteger(bts).intValue();
	}
	public static byte[] bytesFromLong(long l){
		return BigInteger.valueOf(l).toByteArray();
	}
	public static byte[] bytesFromLong(String s){
		return BigInteger.valueOf(Long.parseLong(s)).toByteArray();
	}

}
