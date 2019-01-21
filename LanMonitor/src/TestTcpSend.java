import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

import jpcap.JpcapCaptor;
import jpcap.JpcapSender;
import jpcap.NetworkInterface;
import jpcap.NetworkInterfaceAddress;
import jpcap.PacketReceiver;
import jpcap.packet.EthernetPacket;
import jpcap.packet.Packet;
import jpcap.packet.TCPPacket;
//import jpcap.*;





public class TestTcpSend implements PacketReceiver {
	NetworkInterface[] devices = null;
	String inData = null;
	static JpcapSender sender = null;
//	String fileWriteTitleData = null;

	static int ConsoleFlag = 0;
	static int HexCodeFlag = 0;
	static int LangFlag = 0;
	static final int KSC5601 = 0;
	static final int ISO_8859_1 = 1;
	static final int EUC_KR = 2;
	static final int MS949 = 3;
	static final int UTF_8 = 4;
	
	
	public void getInterface() throws Exception {
		devices = JpcapCaptor.getDeviceList();

		System.out.println("usage: java TestTcpSend <select a number from the following>");
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
		int device_num, byte_num ; //, port_num;
		boolean AllORMe=false;
		//자동화로 바뀌면서 북마크함...

		StringBuffer buf = new StringBuffer();
		buf.append("===============================\n\n");
		buf.append("<== 원하는 Netwokr Card 번호 입력 : ");
		System.out.println(buf.toString());
//////////////////////////////////////////////////////////////////////////////////////////
		br = new BufferedReader(new InputStreamReader(System.in));
		inData = br.readLine();
		if (inData.length()<1){
			System.out.println("\n디바이스를 선택하지 않으셨습니다.");
			return;
		}
		device_num = Integer.parseInt(inData);
		System.out.println("<== 수집할 패킷양(Byte) 입력 (기본 64): ");	br = new BufferedReader(new InputStreamReader(System.in));	inData = br.readLine();
		byte_num = Integer.parseInt(inData); // 64바이트가 기본임.
		
		System.out.println("<== HexCode를 포함할까요? 구분자는 TAB Char(9)임. (기본 N) Y/N: ");	
		br = new BufferedReader(new InputStreamReader(System.in));
		inData = br.readLine();
		if(inData.equals("y") || inData.equals("Y")) {HexCodeFlag = 1; } else {HexCodeFlag = 0;} // 0이 기본임. 안함.
		
		System.out.println("<== 글자 인코딩은 뭐로할까요? (기본  KSC5601 = 0, ISO-8859-1 = 1, EUC-KR = 2, MS949 = 3, UTF-8 = 4 ) : ");
		br = new BufferedReader(new InputStreamReader(System.in));
		inData = br.readLine();
		if (inData == null || inData.length() < 1 ) inData = Integer.toString(KSC5601);
		switch (Integer.parseInt(inData)) {
		case UTF_8:
			LangFlag = UTF_8;
			break;
		case MS949:
			LangFlag = MS949;
			break;
		case EUC_KR:
			LangFlag = EUC_KR;
			break;
		case ISO_8859_1:
			LangFlag = ISO_8859_1;
			break;
		default:
			LangFlag = KSC5601;
			break;
		}
		System.out.println("<== 결과를 화면에 출력할까요? (기본 N) Y/N: ");	
		br = new BufferedReader(new InputStreamReader(System.in));
		inData = br.readLine();
		if(inData.equals("y") || inData.equals("Y")) {ConsoleFlag = 1; } else {ConsoleFlag = 0;} // 0이 기본임. 안함.
		
		System.out.println("<== 모든회선에 대한검색(1), 내PC가 호스트인패킷만 검색(0) 선택 (기본은 0)");	br = new BufferedReader(new InputStreamReader(System.in));	inData = br.readLine();
		if (inData != null || Integer.parseInt(inData) == 1){AllORMe=true;}
		//무선네트워크를 선택할땐 0으로 선택해야한다.
/////////////////////////////////////////////////////////////////////////////////////////////
//		System.out.println("<== 수집할 포트번호 (기본 80): ");	br = new BufferedReader(new InputStreamReader(System.in));	inData = br.readLine();
//		port_num = Integer.parseInt(inData); 

//		device_num = 5;byte_num = 128;AllORMe = true; //물어보지 않고 강제로 지정하기... 그러면 위에를 리마크해야한다...
		System.out.println("\n입력한 내용 : 선택한디바이스( " + device_num + " ), 수집할패킷양( " + byte_num + " ), 모든회선 선택 true or false ( " + AllORMe + " )\n\n" );

		// interface, 패킷양(byte), true(모든 지나가는 회선 패킷) false(내 PC가 호스트인 패킷),
		// timeout(ms)
		JpcapCaptor jpcap = JpcapCaptor.openDevice(devices[device_num], byte_num, AllORMe, -1);
		sender = JpcapSender.openDevice(devices[device_num]);

//		jpcap.setFilter("port " + port_num, true); // 포트를 지정할때... ""는 모든포트이다....
		jpcap.setFilter("", true); // 포트가 아니라 전체 페킷은 어케 설정하지???
		// int 출력할 패킷수, -1 은 무한데
		jpcap.loopPacket(-1, new TestTcpSend());
		
	}

	public void receivePacket(Packet packet) {
		//TCPPacket tcpPacket = (TCPPacket) packet;
		if (packet instanceof TCPPacket) {
//			System.out.println("센더 : " + sender.toString());
//			System.out.println("패킷 : " + packet.toString());
			
			TCPPacket tcpPacket = (TCPPacket)packet;
			byte[] data = tcpPacket.data;
			String srcHost = tcpPacket.src_ip + ":" + tcpPacket.src_port ;
			String dstHost = tcpPacket.dst_ip + ":" + tcpPacket.dst_port ;
			String isoData = null;
			try {
				switch (LangFlag) {
				case UTF_8:
					isoData = new String(data, "UTF-8");
					break;
				case MS949:
					isoData = new String(data, "MS949");
					break;
				case EUC_KR:
					isoData = new String(data, "EUC-KR");
					break;
				case ISO_8859_1:
					isoData = new String(data, "ISO-8859-1");
					break;
				default:
					isoData = new String(data, "KSC5601");
					break;
				}
				if(HexCodeFlag==1)isoData = isoData + "	" + Util.getHexa(data) ;
				
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
			
			Date today = new Date(); 
            SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd"); 
            date.format(today).toString();  

            String str=isoData; // GetData(isoData);
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(date.format(today).toString() + ".txt",true)); //파일이 있으면 덮어쓰지않고 이어쓴다.
				out.write(srcHost+" -> " + dstHost + "=(DATA+HEX)" + str); out.newLine();
				//out.write(srcHost+" -> " + dstHost + "=(DATA+HEX)" + isoData); out.newLine();
				out.close();
			} catch (IOException e) {
			    System.err.println(e); // 에러가 있다면 메시지 출력
			    //System.exit(1);
			}

			if (ConsoleFlag == 1) System.out.println(srcHost+" -> " + dstHost + "=(DATA+HEX)" + str); //화면에 결과를 뿌린다...              
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
//		String javaLibraryPath = System.getProperty("java.library.path");
//		if(javaLibraryPath.indexOf("C:\\Windows\\Sun\\Java\\bin")>-1)
//			System.setProperty("java.library.path", "C:\\Windows\\Sun\\Java\\lib\\ext;" + javaLibraryPath);
//		System.out.println(System.getProperty("java.library.path"));
//		System.out.println(System.getProperty("os.version"));
//		System.out.println(System.getProperty("java.vm.version"));
//		System.out.println(System.getProperty("java.vm.vendor"));
//		System.out.println(System.getProperty("java.vm.name")); //JRockit
//		System.out.println(System.getProperty("java.ext.dirs"));
//		System.out.println(System.getProperty("java.version"));
//		System.out.println(System.getProperty("java.vendor"));
//		System.out.println(System.getProperty("java.vendor.url"));
//		System.out.println(System.getProperty("java.vm.specification.version"));
//		System.out.println(System.getProperty("java.vm.specification.vendor"));
//		System.out.println(System.getProperty("java.vm.specification.name"));
//		System.out.println(System.getProperty("java.specification.version"));
//		System.out.println(System.getProperty("java.specification.vendor"));
//		System.out.println(System.getProperty("java.specification.name"));
//		System.out.println(System.getProperty("java.class.version"));

		boolean isRun = false;
		if(System.getProperty("file.separator").equals("\\")
				&& System.getProperty("java.vm.version").indexOf("64")==-1
				&& System.getProperty("java.vm.name").indexOf("64")==-1) {
			String[] extPaths = System.getProperty("java.ext.dirs").split(";");
			for (int i = 0; i < extPaths.length; i++) {
				if(new File(extPaths[i] + "\\jpcap.jar").exists()){
					isRun=true;
					break;
				}
			}
		}
		
		if(isRun)
			new TestTcpSend().getInterface();
		else
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
	}
}
