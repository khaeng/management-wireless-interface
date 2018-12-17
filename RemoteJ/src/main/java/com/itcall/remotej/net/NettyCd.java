package com.itcall.remotej.net;

import java.nio.charset.Charset;
import java.util.Arrays;

public enum NettyCd {

	OK("OK")
	,NOK("NOK")
	,RELOAD("RELOAD")
	,IMG("IMG")
	,IMGZIP("ZIP")
	,IMGBIT("IMGBIT")
	,IMG_BPS("BPS")
	, IMG_DEPTH("IMGDP")
	/**
	 * 이미지종류 : 기본(실패시) bmp, 이외 지정.
	 */
	, IMG_CD("IMGCD")
	, IMGZERO("IMGZERO")
	, IMG_CUT("IMGCUT") // 이미지위치시작점:이미지데이터
	, SCREEN_SIZE("SCR_SIZE")



	, SCR_VIEWER("VIEWER")
	, SCR_LOOKER("LOOKER")
	, SCR_SERVER("SERVER")


	, DELMITER("</END>")
	, BMP("BMP")
	, BMPZIP("BMPZIP")
	, SEND_TEXT ("S-TXT")
	, SEND ("SEND")
	, KEY_C ("KC")
	, KEY_D ("KD")
	, KEY_U ("KU")
	, MSL_C ("MLC")
	, MSL_DD ("MLDD")
	, MSL_D ("MLD")
	, MSL_U ("MLU")
	, MSC_C ("MCC")
	, MSC_DD ("MCDD")
	, MSC_D ("MCD")
	, MSC_U ("MCU")
	, MSR_C ("MRC")
	, MSR_DD ("MRDD")
	, MSR_D ("MRD")
	, MSR_U ("MRU")
	
	
	/**
	 * 1. VIEWER : DIR정보를 요청한다. DIR주소를 전송하지 않으면 현재(.) 디렉토리 정보를 전송한다.
	 * 2. SCREEN : 인수가 있으면 해당디렉토리 정보를... 없으면 현재(.) 디렉토리 정보를 전송해준다.
	 * 		전송정보는 각 "\n"로 구분하여 존재갯수만큼 보내준다.
	 * 		ROOT;C:\;Local Disk;100000000;210000
	 * 		ROOT;D:\;Local Disk;100000000;210000
	 * 		ROOT;L:\;CD Drive;0;0
	 * 		ROOT;/;null;100000000;210000 //리눅스의 경우
	 * 		MSG;전달하고픈 메시지...(결과전달용)
	 * 		.. // 이 값이 나오면 이후부터는 DIR, FILE 정보임.
	 * 		폴더명칭1;D
	 * 		폴더명칭2;D
	 * 		폴더명칭3;D
	 * 		파일명칭1;F;1024
	 * 		파일명칭2;F;1024
	 * 		파일명칭3;F;1024
	 * 			// 읽기/쓰기여부, 생성일, 수정일도 전송할지 고민중...
	 */
	, FTP_DIR_INFO("DIR")
	, FTP_MK_DIR("MK") // 전체경로를 전달하며, 받은 SCREEN은 FTP_DIR_INFO를 반환해준다.
	, FTP_DEL("RM") // 파일/폴더삭제 : 전체경로를 전달하며, 받은 SCREEN은 FTP_DIR_INFO를 반환해준다. (DIR이 비어있지 않으면 삭제불가)
	, FTP_RENAME("MV") // 변경전전체경로;변경후전체경로 전달하며 받은 SCREEN은 FTP_DIR_INFO를 반환해준다.(성공하든 실패하든...)
	, FTP_SEND("FTP_SEND") // 전체경로;압축된파일데이터 // 통으로 전달되기 때문에 용량은 실행된 VM에 따라 다르게 제한된다.
	, FTP_RECV("FTP_REQ") // 전체경로로 요청 > FTP_SEND로 전달해준다. 폴더인경우 폴더명으로 압축하여 전달한다.
	
	, OUT_OF_ORDER(null)
	
	;


	private String value;
	
	private NettyCd(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public boolean startsWith(byte[] bts) {
		try{
			byte[] srcBts = this.value.getBytes(DEF_CHARSET);
			return Arrays.equals(srcBts, Arrays.copyOfRange(bts, 0, srcBts.length));
		}catch(Exception e){
			return false;
		}
	}

	public static NettyCd getNettyCd(String value){
		try{
			for (int i = 0; i < NettyCd.values().length; i++) {
				if(NettyCd.values()[i].getValue().equals(value)){
					return NettyCd.values()[i];
				}
			}
		}catch(Exception e){}
		return NettyCd.OUT_OF_ORDER;
	}
	public static NettyCd getCmd(byte[] bts){
		try{
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < bts.length; i++) {
				if(bts[i]==GAP_OF_TR){
					return getNettyCd(sb.toString());
				}
				sb.append((char)bts[i]);
			}
		}catch(Exception e){}
		return NettyCd.OUT_OF_ORDER;
	}
	public static byte[] getDataByte(byte[] bts){
		try{
			for (int i = 0; i < bts.length; i++) {
				if(bts[i]==GAP_OF_TR){
					return Arrays.copyOfRange(bts, i+1, bts.length);
				}
			}
		}catch(Exception e){}
		return bts;
	}
	public static String getDataString(byte[] bts){
		return new String(getDataByte(bts),DEF_CHARSET);
	}

public static void main(String[] args) {
	byte[] src = {'O','K','!','~'};
	System.out.println(NettyCd.OK.startsWith(src));
	int idx = 3;
	switch (idx) {
	case 1:{
		System.out.println(idx);
	}case 2:{
		System.out.println(idx);
	}case 3:{
		System.out.println(idx);
	}case 4:{
		System.out.println(idx);
	}

	default:
		System.out.println("defalut");
		break;
	}
}



	public static final int NIO_THREAD_COUNT = 5;
	public static final int MAX_BUFF_SIZE = 9999999;
	public static final int CONNECTION_TIMEOUT = 5 * 1000;
	public static final long READ_TIMEOUT = 5 * 60 * 1000;
	public static final long WRITE_TIMEOUT = 5 * 60 * 1000;
	public static final Charset DEF_CHARSET = Charset.forName("UTF-8");
	public static final String END_OF_TR = "</END>";
	public static final char SPLIT_OF_TR = ';';
	public static final char GAP_OF_TR = ':';
	public static final String DEF_KEY = "1234";
	public static final String DEF_ZIP_ENTRY_NAME = "$REMOTE_CONTROL.TMP";
	public static final String DEF_IMAGE_CD = "BMP";
	public static final String DEF_IMAGE_DEPTH = "원본";

	public static final int MAX_KEEP_BUFFER_SIZE = 10*1024*1024; // 버퍼에 담아둘 데이터를 10메가로 제한하며 초과시에는 버린다.
	public static final int DATA_LENTGH = 66536; // 300000; // 66536;
	public static final int SOCKET_TIMEOUT = 10 * 60 * 1000; // 10분을 기본 대기시간으로 셋팅한다.
	public static final int LOOP_SHOT_DELAY = 50;

	public static final String TCP_REPEATER_HOST = "localhost"; // "192.168.0.46"; //"localhost";
	public static final String TCP_REPEATER_PORT = "8080";
	public static final int WAIT_COUNT = 500; // 60; // 초당 하나씩 전송한다.
	public static final int LOOP_DELAY = 1000;

	public static final int KEY_DELAY = 30;
	public static final byte NO_CHANGE_BYTE_FLAG = (byte) -1; // 0xFF; // 0
	public static final int IMG_CUT_LIMIT_SIZE = 50; // 해당크기만큼 똑같으면 커팅한다.
	public static final int MAX_WORK_MULTI_CNT = 5;

	public static final String[] VIEWER_SCREEN_SIZE    = {"0.3X","0.5X","0.7X","0.8X","0.9X","1.0X","1.5X","2.0X","2.5X","3.0X","4.0X","5.0X","7.0X","10.0X"};
	public static final int      VIEWER_SCREEN_DEF_IDX = 5;   
	public static final float[]  VIEWER_SCREEN_GO_SIZE = {  0.3f,  0.5f,  0.7f,  0.8f,  0.9f,   1f ,  1.5f,  2.0f,  2.5f,  3.0f,  4.0f,  5.0f,  7.0f,  10.0f}; // 변환배율을 지정한다.
	public static final float[]  VIEWER_SCREEN_RE_SIZE = {1/0.3f,1/0.5f,1/0.7f,1/0.8f,1/0.9f,   1f ,1/1.5f,1/2.0f,1/2.5f,1/3.0f,1/4.0f,1/5.0f,1/7.0f,1/10.0f}; // 원복값은 "1/배율"로 하면 된다.





}
