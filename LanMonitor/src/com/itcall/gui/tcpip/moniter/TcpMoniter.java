package com.itcall.gui.tcpip.moniter;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
//import jpcap.*;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import jpcap.JpcapCaptor;
import jpcap.JpcapSender;
import jpcap.NetworkInterface;
import jpcap.NetworkInterfaceAddress;
import jpcap.PacketReceiver;
import jpcap.packet.EthernetPacket;
import jpcap.packet.Packet;
import jpcap.packet.TCPPacket;

import com.itcall.util.GzipUtil;
import com.itcall.util.Util;
import com.itcall.util.chardet.CharDetector;

public class TcpMoniter implements PacketReceiver {
	
	static Executor executor = Executors.newSingleThreadExecutor(); // 8Thread의 1.5배
	
//	NetworkInterface[] devices = null;
	String inData = null;
	static JpcapSender sender = null;
//	String fileWriteTitleData = null;

	static int ConsoleFlag = 0;
	static int HexCodeFlag = 0;
	static CharSetFlag LangFlag = CharSetFlag.KSC5601;// = 0;
	static final int AUTO = 0;
	static final int KSC5601 = 1;
	static final int ISO_8859_1 = 2;
	static final int EUC_KR = 3;
	static final int MS949 = 4;
	static final int UTF8 = 5;

	

	TcpPacketListener listener;
	TcpMoniter instance;
	private String[] packetGetterSearchPortNumbers;

	private String[] ArrLocalAddr;

	private boolean IsSeparateLog;

	public TcpMoniter() {
		// TODO Auto-generated constructor stub
		this(null);
	}
	public TcpMoniter(TcpPacketListener listener) {
		// TODO Auto-generated constructor stub
		this.listener = listener;
		this.instance = this;
	}

	public enum CharSetFlag {
		AUTO(0), 
		KSC5601(1), 
		ISO_8859_1(2),
		EUC_KR(3),
		MS949(4),
		UTF8(5);
		int value;
		private CharSetFlag(int value) {
			// TODO Auto-generated constructor stub
			this.value = value;
		}
		public static CharSetFlag valueOf(int value){
			for (int i = 0; i < CharSetFlag.values().length; i++) {
				if(CharSetFlag.values()[i].getValue()==value) return CharSetFlag.values()[i];
			}
			throw new AssertionError("Unknown ENUM CharSetFlag: " + value);
		}
		public int getValue() {
			return value;
		}
	}

	public interface TcpPacketListener {
		/**
		 * 데이터 수신 시 1번째로 발생. 다음진행을 유지하고 싶으면 true를 반환해야 한다.
		 * @param packet
		 * @return
		 */
		public boolean receiveData(Packet packet);
		/**
		 * 데이터 수신 시 2번째로 발생. 다음진행을 유지하고 싶으면 true를 반환해야 한다.
		 * @param data
		 * @return
		 */
		public boolean receiveData(byte[] data);
		/**
		 * 데이터 수신 시 3번째로 발생. 다음진행을 유지하고 싶으면 true를 반환해야 한다.
		 * @param data
		 * @param charSet
		 * @return
		 */
		public boolean receiveData(String data, String charSet);
	}

	public NetworkInterface[] getInterfaceInfo(){
		NetworkInterface[] devices = JpcapCaptor.getDeviceList();

//		System.out.println("usage: java TestTcpSend <select a number from the following>");
//		fileWriteTitleData = "usage: java TestTcpSend <select a number from the following>";

		for (int i = 0; i < devices.length; i++) {
			System.out.println(i + " :" + devices[i].name + "(" + devices[i].description + ")");
			System.out.println("    data link:" + devices[i].datalink_name + "(" + devices[i].datalink_description + ")");
			System.out.print("    MAC address:");
			for (byte b : devices[i].mac_address)
				System.out.print(Integer.toHexString(b & 0xff) + ":");
			System.out.println();
			for (NetworkInterfaceAddress a : devices[i].addresses)
				System.out.println("    address:" + a.address + " " + a.subnet + " " + a.broadcast);
		}
		return devices;
	}
	public void runInterfaceLog(int deviceNum, boolean isAllLine, int packetSize, boolean isHex, String charSet, boolean isConsole) throws IOException{
		LangFlag = CharSetFlag.valueOf(charSet);
		NetworkInterface[] devices = getInterfaceInfo();
		ConsoleFlag = isConsole?1:0; // 0이 기본임. 안함.

		System.out.println("\n입력한 내용 : 선택한디바이스( " + deviceNum + " ), 수집할패킷양( " + packetSize + " ), 모든회선 선택 true or false ( " + isAllLine + " )\n\n" );

		// interface, 패킷양(byte), true(모든 지나가는 회선 패킷) false(내 PC가 호스트인 패킷),
		// timeout(ms)
		JpcapCaptor jpcap = JpcapCaptor.openDevice(devices[deviceNum], packetSize, isAllLine, -1);
		sender = JpcapSender.openDevice(devices[deviceNum]);

//		jpcap.setFilter("port " + port_num, true); // 포트를 지정할때... ""는 모든포트이다....
		jpcap.setFilter("", true); // 포트가 아니라 전체 페킷은 어케 설정하지???
		// int 출력할 패킷수, -1 은 무한데
//		jpcap.loopPacket(-1, new TcpMoniter());
		jpcap.loopPacket(-1, instance);
	}
	public void getInterfaceCmd() throws Exception {

		System.out.println("usage: java TestTcpSend <select a number from the following>");
		NetworkInterface[] devices = getInterfaceInfo();
		
		// 무선네트워크는 기본적으로 패킷을 자기걸제외하고 모두 버리는 구조(Non-Promiscuous Mode)이므로 이것을 강제적으로 수집토록 바꿔야한다.
		// To work around this behavior, force the adapter into Compatibility mode:
//		At a command prompt, type netsh bridge show adapter .
//		Locate the identification number of the NIC that is not responding.
//		If the NIC is not in Compatibility mode, you can change it manually if you type the following command, where 1 is the number of the NIC that is displayed in the first step:
//		netsh bridge set adapter 1 forcecompatmode=enable
//		Run the netsh bridge show adapter command again to verify that the ForceCompatabilityMode field for the NIC is displayed as Enabled .
		//명령행모드에서 netsh bridge show adapter
		// netsh bridge set adapter 1 forcecompatmode=enable // 1번 어뎁터가 무선일경우...
//		//밑의 그냥 출력한번인데... 이거 안하면 무선네트워크 0번 선택시 에러난다... 왜일까?
//		//유선네트워크인 1번은 밑에꺼 안해도 에러안난다...
//		// Windows에서 lookup되어진 Device Name을 그대로 다 출력해 보기
//		System.out.println("*** All Devices name without fix*******************");
//		for (int i = 0; i < devices.length; i++) {
//		System.out.println("[" + i + "] " + devices[i]);
//		}
//		System.out.println("");
//		// Windows에서 lookup되어진 Device Nameing문제 해결 - 예전꺼라 상관없는듯 하다...
//		Util.fixWindowsNameingProblem(devices.toString());
//		System.out.println("*** All Devices name fixed*************************");
//		for (int i = 0; i < devices.length; i++) {
//		System.out.println("[" + i + "] " + devices[i]);
//		}
		
		

		BufferedReader br = null;
		int device_num, byte_num = 2048 ; //, port_num;
		boolean AllORMe=false;
		//자동화로 바뀌면서 북마크함...

		StringBuffer buf = new StringBuffer();
		buf.append("===============================\n\n");
		buf.append("<== 원하는 Netwokr Card 번호 입력 : ");
		System.out.println(buf.toString());
//////////////////////////////////////////////////////////////////////////////////////////
		try {
			br = new BufferedReader(new InputStreamReader(System.in));
			inData = br.readLine();
			device_num = Integer.parseInt(inData);
			ArrLocalAddr = new String[devices[device_num].addresses.length];
			for (int i=0;i<devices[device_num].addresses.length;++i)
				ArrLocalAddr[i] = devices[device_num].addresses[i].address + "";
		} catch (Exception e) {
			System.out.println("\n디바이스를 선택하셔야 진행하실 수 있습니다.");
			return;
		}

		System.out.println("<== 수집할 패킷양(Byte) 입력 (기본 2048): ");
		try {
			br = new BufferedReader(new InputStreamReader(System.in));	inData = br.readLine();
			byte_num = Integer.parseInt(inData); // 2048바이트가 기본임.
		} catch (Exception e) {
			byte_num = 2048;
		}
		
		System.out.println("<== HexCode를 포함할까요? 구분자는 TAB Char(9)임. (기본 N) Y/N: ");
		try {
			br = new BufferedReader(new InputStreamReader(System.in));inData = br.readLine();
			if(inData.toUpperCase().equals("Y")) HexCodeFlag = 1;
		} catch (Exception e) {
			HexCodeFlag = 0; // 0이 기본임. 안함.
		}
		
		System.out.println("<== 글자 인코딩은 뭐로할까요? ( 자동 = 0(기본), KSC5601 = 1, ISO-8859-1 = 2, EUC-KR = 3, MS949 = 4, UTF-8 = 5 ) : ");
		try {
			br = new BufferedReader(new InputStreamReader(System.in));inData = br.readLine();
			LangFlag = CharSetFlag.valueOf(Integer.parseInt(inData));
		} catch (Exception e) {
			inData = Integer.toString(AUTO);
		}
		
//		switch (Integer.parseInt(inData)) {
//		case UTF_8:
//			LangFlag = UTF_8;
//			break;
//		case MS949:
//			LangFlag = MS949;
//			break;
//		case EUC_KR:
//			LangFlag = EUC_KR;
//			break;
//		case ISO_8859_1:
//			LangFlag = ISO_8859_1;
//			break;
//		default:
//			LangFlag = KSC5601;
//			break;
//		}
		System.out.println("<== 결과를 화면에 출력할까요? (기본 N) Y/N: ");	
		try {
			br = new BufferedReader(new InputStreamReader(System.in));inData = br.readLine();
			if(inData.toUpperCase().equals("Y")) ConsoleFlag = 1;
		} catch (Exception e) {
			ConsoleFlag = 0; // 0이 기본임. 안함.
		}
		
		System.out.println("<== 모든회선에 대한검색(1), 내PC가 호스트인패킷만 검색(0) 선택 (기본은 0)");
		try {
			br = new BufferedReader(new InputStreamReader(System.in));	inData = br.readLine();
			if (Integer.parseInt(inData) == 1) AllORMe=true;
		} catch (Exception e) {
			AllORMe=false;
		}
		//무선네트워크를 선택할땐 0으로 선택해야한다.
/////////////////////////////////////////////////////////////////////////////////////////////
		System.out.println("<== 수집할 포트번호 (기본 모두 0 또는 포트번호를 콤마{,}로 구분하여 입력) ");
		try {
			br = new BufferedReader(new InputStreamReader(System.in));	inData = br.readLine();
			packetGetterSearchPortNumbers = inData.replaceAll(" ", "").split(",");
			if(packetGetterSearchPortNumbers==null || packetGetterSearchPortNumbers[0].replaceAll(" ", "").equals(""))
				packetGetterSearchPortNumbers = new String[]{"0"}; 
		} catch (Exception e) {
			packetGetterSearchPortNumbers = new String[]{"0"}; 
		}

		System.out.println("<== 수신과 송신을 구분하여 로그를 저장할까요?(기본값:N)\n\t(포트를 지정한 경우 주소:포트 기준이며, 이외는 송수신 주소기준임. 전체로그는 별도로 저장됨.)");	
		try {
			br = new BufferedReader(new InputStreamReader(System.in));inData = br.readLine();
			if(inData.toUpperCase().equals("Y")) IsSeparateLog = true;
		} catch (Exception e) {}

//		device_num = 5;byte_num = 128;AllORMe = true; //물어보지 않고 강제로 지정하기... 그러면 위에를 리마크해야한다...
		System.out.print("입력한 내용 : 선택한디바이스( " + device_num + " ), 수집할패킷양( " + byte_num + " ), 모든회선 선택여부( " + AllORMe + " ), 모니터링할 포트(0값 존재 시 전체) : " );
		for (String strPort : packetGetterSearchPortNumbers) {
			System.out.print(strPort + ", ");
		}
		System.out.print('\b');
		System.out.print('\b');
		System.out.print('\n');

		// interface, 패킷양(byte), true(모든 지나가는 회선 패킷) false(내 PC가 호스트인 패킷),
		// timeout(ms)
		JpcapCaptor jpcap = JpcapCaptor.openDevice(devices[device_num], byte_num, AllORMe, -1);
		sender = JpcapSender.openDevice(devices[device_num]);

//		jpcap.setFilter("port " + port_num, true); // 포트를 지정할때... ""는 모든포트이다....
		jpcap.setFilter("", true); // 포트가 아니라 전체 페킷은 어케 설정하지???
		// int 출력할 패킷수(수집횟수임. 들어오는 바이트 제한수가 아니라...), -1 은 무한데
//		jpcap.loopPacket(-1, new TcpMoniter());
		jpcap.loopPacket(-1, instance);
//		jpcap.processPacket(-1, instance);
		
	}

//	public void receivePacket(Packet packet) {receivePacket(packet, null);}
	@Override
	public void receivePacket(final Packet packet/*, TcpPacketListener listener*/) {
		//TCPPacket tcpPacket = (TCPPacket) packet;
		if (packet instanceof TCPPacket) {
//			final TcpPacketListener listener = this.listener;
			final Date today = new Date();
//executor.execute(new Thread(new Runnable() {
//	@Override
//	public void run() {
//			System.out.println("센더 : " + sender.toString());
//			System.out.println("패킷 : " + packet.toString());
			
			TCPPacket tcpPacket = (TCPPacket)packet;
			if(listener!=null && 
					!listener.receiveData(tcpPacket))
				return;
			byte[] data = tcpPacket.data;
			if(listener!=null && 
					!listener.receiveData(data))
				return;
			String srcHost = tcpPacket.src_ip + ":" + tcpPacket.src_port ;
			String dstHost = tcpPacket.dst_ip + ":" + tcpPacket.dst_port ;
			if(packetGetterSearchPortNumbers!=null && packetGetterSearchPortNumbers.length>0 && !packetGetterSearchPortNumbers[0].equals("0")){
				for(int i=0;i<packetGetterSearchPortNumbers.length;i++){
					if(packetGetterSearchPortNumbers[i]!=null && (packetGetterSearchPortNumbers[i].equals(tcpPacket.src_port + "") || packetGetterSearchPortNumbers[i].equals(tcpPacket.dst_port + "")))
						break; // 수집 한다.
					if(i>=packetGetterSearchPortNumbers.length-1)
						return; // 수집 안한다.
				}
			}else if(packetGetterSearchPortNumbers==null || packetGetterSearchPortNumbers.length!=1 || !packetGetterSearchPortNumbers[0].equals("0"))
				return; // 수집 안한다. (모두 수집하는 0의 조건일때...)
			String isoData = null;
			try {
				String charSet =  LangFlag.name();
				if(CharSetFlag.AUTO == LangFlag){
					charSet = CharDetector.detectFileCharset(data, Charset.defaultCharset().name());
				}
//				else
//					CharDetector.detectFileCharset(data, LangFlag.name());
				isoData = new String(data, charSet);
				if(listener!=null && 
						!listener.receiveData(isoData, charSet))
					return;
//				switch (LangFlag) {
//				case UTF_8:
//					isoData = new String(data, "UTF-8");
//					break;
//				case MS949:
//					isoData = new String(data, "MS949");
//					break;
//				case EUC_KR:
//					isoData = new String(data, "EUC-KR");
//					break;
//				case ISO_8859_1:
//					isoData = new String(data, "ISO-8859-1");
//					break;
//				default:
//					isoData = new String(data, "KSC5601");
//					break;
//				}
				if(HexCodeFlag==1)isoData = "\t(DATA+HEX)("+charSet+")\n"+isoData + "\n" + Util.getHexa(data) ;
				else isoData = "\t(DATA)("+charSet+")\n"+isoData;
				
//				//isoData = new String(data, "ISO-8859-1");
//				//isoData = new String(data.toString().getBytes("KSC5601"),"EUC-KR");
//				isoData = new String(data, "KSC5601");
//				//isoData = new String(data, "ISO-8859-1") + "	" + Util.getHexa(data) ;
//				//안된다//isoData = new String(data, "UTF-8") + "	" + Util.getHexa(data) ;
//				//안된다//isoData = new String(data, "EUC-KR") + "	" + Util.getHexa(data) ;
//				//안된다//isoData = new String(data, "MS949") + "	" + Util.getHexa(data) ;

			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			
//			String fileData = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss :::").format(today) + " -> " + dstHost + isoData;
//			String consoleData = new SimpleDateFormat("hh:mm:ss :::").format(today) + srcHost+" -> " + dstHost + isoData; // GetData(isoData);

			String yyyymmddhhmmss = new SimpleDateFormat("[yyyy-MM-dd hh:mm:ss.SSS] ::: ").format(today).toString();
			String yyyymmdd = yyyymmddhhmmss.substring(1,11); // new SimpleDateFormat("yyyy-MM-dd").format(today).toString();
			String hhmmss = "["+yyyymmddhhmmss.substring(12, 20)+" ::: "; // new SimpleDateFormat("[hh:mm:ss] ::: ").format(today).toString();
			String fileName=(yyyymmdd + "(" + srcHost + ")to(" + dstHost + ").log").replaceAll(":", "_").replaceAll("/", "_") ;
			String svrLogName=null;
			boolean isTarget=true;
			try {
				if(packetGetterSearchPortNumbers!=null && packetGetterSearchPortNumbers.length>0 && !packetGetterSearchPortNumbers[0].equals("0")){
					if(IsSeparateLog)
						for(int i=0;i<packetGetterSearchPortNumbers.length;i++){
							if(packetGetterSearchPortNumbers[i]!=null && packetGetterSearchPortNumbers[i].equals(tcpPacket.src_port + "")){
								fileName = (yyyymmdd + "(" + srcHost + ")to(~).log").replaceAll(":", "_").replaceAll("/", "_") ;
								break;
							}else if(packetGetterSearchPortNumbers[i]!=null && packetGetterSearchPortNumbers[i].equals(tcpPacket.dst_port + "")){
								fileName = (yyyymmdd + "(~)to(" + dstHost + ").log").replaceAll(":", "_").replaceAll("/", "_") ;
								break;
							}
						}
					else{
						for(int i=0;i<ArrLocalAddr.length;++i)
							if(srcHost.split(":")[0].equals(ArrLocalAddr[i]))
								isTarget&=false; // svrLogName=srcHost;
						if(isTarget) svrLogName=srcHost;
						isTarget=true;
						for(int i=0;i<ArrLocalAddr.length;++i)
							if(dstHost.split(":")[0].equals(ArrLocalAddr[i]))
								isTarget&=false; // svrLogName=dstHost;
						if(isTarget) svrLogName=dstHost;
						isTarget=true;
						for(int i=0;i<packetGetterSearchPortNumbers.length;i++){
							if(packetGetterSearchPortNumbers[i]!=null && packetGetterSearchPortNumbers[i].equals(tcpPacket.src_port + "")){
								fileName = (yyyymmdd + "(" + svrLogName + ").log").replaceAll(":", "_").replaceAll("/", "_") ;
								break;
							}else if(packetGetterSearchPortNumbers[i]!=null && packetGetterSearchPortNumbers[i].equals(tcpPacket.dst_port + "")){
								fileName = (yyyymmdd + "(" + svrLogName + ").log").replaceAll(":", "_").replaceAll("/", "_") ;
								break;
							}
						}
					}
				}else if(packetGetterSearchPortNumbers!=null && packetGetterSearchPortNumbers.length==1 && packetGetterSearchPortNumbers[0].equals("0")){
					if(IsSeparateLog)
						fileName = (yyyymmdd + "(" + srcHost.split(":")[0] + ")to(" + dstHost.split(":")[0] + ").log").replaceAll(":", "_").replaceAll("/", "_") ;
					else{
						for(int i=0;i<ArrLocalAddr.length;++i)
							if(srcHost.split(":")[0].equals(ArrLocalAddr[i]))
								isTarget&=false; // svrLogName=srcHost.split(":")[0];
						if(isTarget) svrLogName=srcHost.split(":")[0];
						isTarget=true;
						for(int i=0;i<ArrLocalAddr.length;++i)
							if(dstHost.split(":")[0].equals(ArrLocalAddr[i]))
								isTarget&=false; // svrLogName=dstHost.split(":")[0];
						if(isTarget) svrLogName=dstHost.split(":")[0];
						isTarget=true;
						fileName = (yyyymmdd + "(" + svrLogName + ").log").replaceAll(":", "_").replaceAll("/", "_") ;
					}
				}
				saveLogFile(fileName, "\n" + yyyymmddhhmmss + srcHost+" -> " + dstHost + isoData/* + "\n"*/);
				saveLogFile((yyyymmdd + "(" + srcHost.split(":")[0] + ")to(" + dstHost.split(":")[0] + ").bytes.log").replaceAll(":", "_").replaceAll("/", "_"), getSaveByte(data));
//				BufferedOutputStream bw = new BufferedOutputStream(new FileOutputStream(fileName, true));
//				bw.write((yyyymmddhhmmss + srcHost+" -> " + dstHost + isoData + "\n").getBytes()); bw.flush();
//				bw.close();

//				BufferedWriter out = new BufferedWriter(new FileWriter(yyyymmdd + ".log",true)); //파일이 있으면 덮어쓰지않고 이어쓴다.
//				out.write(new SimpleDateFormat("[yyyy-MM-dd hh:mm:ss] ::: ").format(today) + srcHost+" -> " + dstHost + isoData); out.newLine();
//				//out.write(srcHost+" -> " + dstHost + isoData); out.newLine();
//				out.close();

			} catch (IOException e) {
				System.out.println(hhmmss + srcHost+" -> " + dstHost + isoData);
			    System.err.println(e); // 에러가 있다면 메시지 출력
			    //System.exit(1);
			}

			if (ConsoleFlag == 1)
				System.out.println(hhmmss + srcHost+" -> " + dstHost + isoData); //화면에 결과를 뿌린다...
			else
				System.out.println(hhmmss + srcHost+" -> " + dstHost + " ::: " + isoData.substring(7, (isoData.length()>50?50:isoData.length())).replaceAll("\n", "\\\\n").replaceAll("\r", "\\\\r").replaceAll("\t", "\\\\t") + "... ::: " + isoData.length() + "bytes"); //화면에 새로운 수집을 알린다...
			
//		}
//	}));
		}

/*
		// GET 값을 구한다
		if(tcpPacket.ack&&tcpPacket.psh&&!tcpPacket.dst_ip.toString().equals("/211.45.156.89")){
			byte[] bBytes = new byte[3];
			System.arraycopy(tcpPacket.data, 0, bBytes, 0, 3);
	
			// GET일경우만 SEND한다.
			if (new String(bBytes).equals("GET")) {
				sendPacket(sender, packet); // 이걸여니 모든 요청을 http://www.aromit.com만 가게한다.
			}
		}
		*/

	}
	
	private byte[] getSaveByte(byte[] data) {
		try {
			byte[] result=null;
			Scanner scanner = new Scanner(new String(data));
			boolean isGzip = false;
			int gzipSize = 0;
			while(scanner.hasNextLine()){
				String text = scanner.nextLine();
				// System.out.println("'" + sb.toString().split("\n")[0] + "'\n'" + saveText + "'");
				if(text.startsWith("Content-Encoding: gzip"))
					isGzip = true;
				if(text.startsWith("Accept-Encoding: gzip"))
					isGzip = true;
				if(text.startsWith("Content-Length: "))
					gzipSize = Integer.parseInt(text.split(":")[1].trim());
				if(text=="" || text.equals(""))
					break;
			}
			if(isGzip){
				for (int i = 4; i < data.length; i++) {
					if((data[i-1]==13 && data[i-2]==10 && data[i-3]==13 && data[i-4]==10)
							|| (data[i-1]==13 && data[i-2]==13)
							|| (data[i-1]==10 && data[i-2]==10)
							|| (data[i-1]==10 && data[i-2]==13 && data[i-3]==10 && data[i-4]==13)){
						result = new byte[gzipSize>0 ? gzipSize : data.length - i];
						System.arraycopy(data, i, result, 0, result.length);
						break;
					}
				}
				if(result!=null && result.length>0){
					result = GzipUtil.decompress(result);
					byte[] data2 = new byte[data.length + result.length + 2];
					System.arraycopy(data, 0, data2, 0, data.length);
					System.arraycopy(result, 0, data2, data.length+2, result.length);
					data2[data.length] = 13;
					data2[data.length+1] = 10;
					data=data2;
				}
			}
			if(data.length>5 && (data[0]==72 && data[1]==84 && data[2]==84 && data[3]==80)
					|| (data[1]==72 && data[2]==84 && data[3]==84 && data[4]==80)){
				result = new byte[data.length + 1];
				System.arraycopy(data, 0, result, 1, data.length);
				result[0] = 10;
				data = result;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return data;
	}
	
	synchronized private void saveLogFile(String fileName, String data) throws IOException{
		BufferedOutputStream bw = new BufferedOutputStream(new FileOutputStream(fileName, true));
		bw.write(data.getBytes("UTF-8")); bw.flush();
		bw.close();
		bw = new BufferedOutputStream(new FileOutputStream(fileName.substring(0,10)+".log", true));
		bw.write(data.getBytes("UtF-8")); bw.flush();
		bw.close();
	}
	
	synchronized private void saveLogFile(String fileName, byte[] data) throws IOException{
		BufferedOutputStream bw = new BufferedOutputStream(new FileOutputStream(fileName, true));
		bw.write(data); bw.flush();
		bw.close();
	}
	
	private String GetData(String isoData) {
		// TODO Auto-generated method stub
		String [] aStr = null;
		aStr=isoData.split(";팩스:");
		if (aStr[1]==null) return "";
		aStr[0]=aStr[1];
		aStr[1]=null;
		aStr=aStr[1].split("<br/>");
		if (aStr[1]==null || aStr[0].length()>15) return aStr[0].substring(0, 15);
		return aStr[0];
	}
	
	
	
	public void sendPacket(JpcapSender sender, Packet packet){
		//패킷 내용을 변경하거나, 조작, 출발/도착지 아이피를 변경하여 다시 보낼수 있다.
		try {
			TCPPacket tcpPacket = (TCPPacket)packet;
			
			EthernetPacket ethernet = (EthernetPacket)packet.datalink;
			
			EthernetPacket ether=new EthernetPacket();
			ether.frametype=EthernetPacket.ETHERTYPE_IP;
			ether.src_mac=ethernet.dst_mac;
			ether.dst_mac=ethernet.src_mac;
			
			TCPPacket p=new TCPPacket(tcpPacket.dst_port,tcpPacket.src_port,tcpPacket.ack_num,tcpPacket.sequence+206,
					false, true,true,false,false,true,false,false,0,tcpPacket.urgent_pointer);
			p.setIPv4Parameter(tcpPacket.priority,tcpPacket.d_flag,tcpPacket.t_flag,tcpPacket.r_flag,tcpPacket.rsv_tos,false,
					false,false,tcpPacket.offset,tcpPacket.ident+23187,226,tcpPacket.protocol,tcpPacket.dst_ip,tcpPacket.src_ip);
			p.data=("HTTP/1.0 302 Redirect\r\nLocation: http://www.aromit.com\r\n\r\n").getBytes();
			p.datalink=ether;
			sender.sendPacket(p);
			
		} catch (Exception e) {
			System.out.println("sendPacketException : " + e.getMessage());
		}
	}

	public static void main(String[] args) throws Exception {
//		if (args.length == 1){
//			fileName = args[0];			
//		} else {
//			fileName = 
//		}
		boolean isRun = false;
		if(System.getProperty("file.separator").equals("\\")
				&& System.getProperty("java.vm.version").indexOf("64")==-1
				&& System.getProperty("java.vm.name").indexOf("64")==-1) {
//			String libpath = System.getProperty("java.ext.dirs"); // System.getProperty("java.library.path");
//			libpath = System.getProperty("user.dir") + ";"+ libpath;
//			System.setProperty("java.ext.dirs",libpath); // System.setProperty("java.library.path",libpath);
//			libpath = System.getProperty("java.library.path");
//			libpath = "C:\\Windows\\Sun\\Java\\lib\\ext" + ";" + libpath + ";"+ System.getProperty("user.dir");
//			System.setProperty("java.library.path",libpath);
//
//			Runtime.getRuntime().exec("cmd /C \"SET PATH=" + libpath + "\"");

//			System.loadLibrary("jpcap");

			String[] extPaths = System.getProperty("java.ext.dirs").split(";");
			for (int i = 0; i < extPaths.length; i++) {
				if(new File(extPaths[i] + "\\jpcap.jar").exists()){
					isRun=true;
					break;
				}
			}
		}
		
		if(isRun)
			new TcpMoniter().getInterfaceCmd();
		else{
			System.out.println(
					new StringBuilder()
					.append("1. 실행은 JDK 32비트로 하여야 한다.(Jrocket안됨)\n")
					.append("\n")
					.append("2. jpcapSetup-0.7.exe를 설치하여 해당 폴더에 jpcap.jar이 있어야 한다.\n")
					.append("   jpcap.jar은 윈도우폴더밑에 Sun\\Java\\lib\\ext 안에 있어야 한다.\n")
					.append("   또는 java.ext.dirs 환경경로에 복사되어 있어야 한다. 이경우 Jrocket가능\n")
					.append("\n")
					.append("3. WinPcap_4_1_2.exe를 설치하며, 랜모듈를 모니터링 할 수 있게\n")
					.append("   실행시 로드할 수 있는부분에 체크하여야 한다.(설치시 물어봄)\n")
					.append("\n")
					.append("=== 64비트 윈도우의 경우에도 JDK 32비트를 설치하여 직접 지정하여 실행하면 된다.\n")
					.append("\n")
					.toString());
			new Throwable("jdk32bit Only and Need jpcap.jar in java_ext folder");
		}
	}
}
