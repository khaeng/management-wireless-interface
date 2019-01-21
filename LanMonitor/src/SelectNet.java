import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
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


public class SelectNet implements PacketReceiver {
//	public class TestTcpSend implements PacketReceiver {
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

		String setText;
		setText = "usage: java TestTcpSend <select a number from the following>\n";
		for (int i = 0; i < devices.length; i++) {
			setText = setText + i + " :" + devices[i].name + "(" + devices[i].description + ")\n";
			setText = setText + "    data link:" + devices[i].datalink_name + "(" + devices[i].datalink_description + ")\n";
			setText = setText + "    MAC address:";
			for (byte b : devices[i].mac_address)
				setText = setText + Integer.toHexString(b & 0xff) + ":";
			setText = setText + "\n";
			for (NetworkInterfaceAddress a : devices[i].addresses)
				setText = setText + "    address:" + a.address + " " + a.subnet + " " + a.broadcast + "\n";
		}
		
		// BufferedReader br = null;
		int device_num, byte_num ; //, port_num;
		boolean AllORMe=false;
		//자동화로 바뀌면서 북마크함...

		if(GetR114.jTextFieldDeviceNum.getText().toString().trim().equals("")) {
			setText = "디바이스를 선택하지 않으셨습니다.\n\n" + setText;
			return;
		}else if(Integer.parseInt(GetR114.jTextFieldDeviceNum.getText().toString().trim())>=0 && Integer.parseInt(GetR114.jTextFieldDeviceNum.getText().toString().trim())<=devices.length){
			device_num = Integer.parseInt(GetR114.jTextFieldDeviceNum.getText().toString().trim());
		}else{
			setText = "올바른 디바이스를 선택하지 않으셨습니다.\n\n" + setText;
			return;
		}
		if(GetR114.jTextFieldPacketCount.getText().toString().trim().equals("")){
			setText = "올바른 패킷양을 지정하지 않으셨어요~.\n\n" + setText;
			return;
		}else if(Integer.parseInt(GetR114.jTextFieldPacketCount.getText().toString().trim())>=0){
			byte_num = Integer.parseInt(GetR114.jTextFieldPacketCount.getText().toString().trim()); // 128바이트가 기본임.
		}else{
			setText = "올바른 패킷양을 지정하지 않으셨어요~.\n\n" + setText;
			return;
		}

//		System.out.println("<== HexCode를 포함할까요? 구분자는 TAB Char(9)임. (기본 N) Y/N: ");	
//		br = new BufferedReader(new InputStreamReader(System.in));
//		inData = br.readLine();
//		if(inData.equals("y") || inData.equals("Y")) {HexCodeFlag = 1; } else {
			HexCodeFlag = 0;
//		} // 0이 기본임. 안함.
		
		//System.out.println("<== 글자 인코딩은 뭐로할까요? (기본  KSC5601 = 0, ISO-8859-1 = 1, EUC-KR = 2, MS949 = 3, UTF-8 = 4 ) : ");
		//br = new BufferedReader(new InputStreamReader(System.in));
		//inData = br.readLine();
		//if (inData == null || inData.length() < 1 ) inData = Integer.toString(KSC5601);
		//switch (Integer.parseInt(inData)) {
		switch(GetR114.jComboBoxIncode.getSelectedIndex()){
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
//		System.out.println("<== 결과를 화면에 출력할까요? (기본 N) Y/N: ");	
//		br = new BufferedReader(new InputStreamReader(System.in));
//		inData = br.readLine();
//		if(inData.equals("y") || inData.equals("Y")) {ConsoleFlag = 1; } else {
		ConsoleFlag = 1;
//		} // 0이 기본임. 안함.
		
//		System.out.println("<== 모든회선에 대한검색(1), 내PC가 호스트인패킷만 검색(0) 선택 (기본은 0)");	br = new BufferedReader(new InputStreamReader(System.in));	inData = br.readLine();
//		if (Integer.parseInt(inData) == 1){AllORMe=true;}
		switch(GetR114.jComboBoxLine.getSelectedIndex()){
		case 1:
			AllORMe=true;
			break;
		default:
			AllORMe=false;
			break;
		}
		//무선네트워크를 선택할땐 0으로 선택해야한다.
/////////////////////////////////////////////////////////////////////////////////////////////
//		System.out.println("<== 수집할 포트번호 (기본 80): ");	br = new BufferedReader(new InputStreamReader(System.in));	inData = br.readLine();
//		port_num = Integer.parseInt(inData); 

//		device_num = 5;byte_num = 128;AllORMe = true; //물어보지 않고 강제로 지정하기... 그러면 위에를 리마크해야한다...
//		System.out.println("\n입력한 내용 : 선택한디바이스( " + device_num + " ), 수집할패킷양( " + byte_num + " ), 모든회선 선택 true or false ( " + AllORMe + " )\n\n" );
		setText = setText + "\n입력한 내용 : 선택한디바이스( " + device_num + " ), 수집할패킷양( " + byte_num + " ), 모든회선 선택 true or false ( " + AllORMe + " )\n\n";
		// interface, 패킷양(byte), true(모든 지나가는 회선 패킷) false(내 PC가 호스트인 패킷),
		// timeout(ms)
		
		GetR114.jTextAreaResult.setText(setText);
		
					JpcapCaptor jpcap = JpcapCaptor.openDevice(devices[device_num], byte_num, AllORMe, -1);
		sender = JpcapSender.openDevice(devices[device_num]);

		if( !GetR114.jTextFieldPort.getText().trim().equals("") && Integer.parseInt(GetR114.jTextFieldPort.getText().trim())>0){
			jpcap.setFilter("port " + Integer.parseInt(GetR114.jTextFieldPort.getText().trim()), true); // 포트를 지정할때... ""는 모든포트이다....
		}else {
			jpcap.setFilter("", true); // 포트가 아니라 전체 페킷은 어케 설정하지???
		}
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

			try {
				String str = isoData; //GetData(isoData);
				if (str!=null && str.length()>0){
					GetR114.jTextAreaResult.setText(str+"\n"+GetR114.jTextAreaResult.getText().toString());
				}
				BufferedWriter out = new BufferedWriter(new FileWriter(date.format(today).toString() + ".txt",true)); //파일이 있으면 덮어쓰지않고 이어쓴다.
				out.write(srcHost+" -> " + dstHost + "=(DATA+HEX)" + isoData); out.newLine();
				//out.write(srcHost+" -> " + dstHost + "=(DATA+HEX)" + isoData); out.newLine();
				out.close();
			} catch (IOException e) {
			    System.err.println(e); // 에러가 있다면 메시지 출력
			    //System.exit(1);
			}

			if (ConsoleFlag == 1) System.out.println(srcHost+" -> " + dstHost + "=(DATA+HEX)" + isoData); //화면에 결과를 뿌린다...              
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
		aStr=isoData.split(GetR114.jTextFieldFirst.getText().toString());
		if (aStr[1]==null) return "";
		aStr[0]=aStr[1];
		aStr[1]=null;
		aStr=aStr[1].split(GetR114.jTextFieldEnd.getText().toString());
		if (aStr[1]==null || aStr[0].length()>15) return aStr[0].substring(0, 15);
		return aStr[0];
	}

//	public void sendPacket(JpcapSender sender, Packet packet){
//		//패킷 내용을 변경하거나, 조작, 출발/도착지 아이피를 변경하여 다시 보낼수 있다.
//		try {
//			TCPPacket tcpPacket = (TCPPacket)packet;
//			
//			EthernetPacket ethernet = (EthernetPacket)packet.datalink;
//			
//			EthernetPacket ether=new EthernetPacket();
//			ether.frametype=EthernetPacket.ETHERTYPE_IP;
//			ether.src_mac=ethernet.dst_mac;
//			ether.dst_mac=ethernet.src_mac;
//			
//			TCPPacket p=new TCPPacket(tcpPacket.dst_port,tcpPacket.src_port,tcpPacket.ack_num,tcpPacket.sequence+206,
//					false, true,true,false,false,true,false,false,0,tcpPacket.urgent_pointer);
//			p.setIPv4Parameter(tcpPacket.priority,tcpPacket.d_flag,tcpPacket.t_flag,tcpPacket.r_flag,tcpPacket.rsv_tos,false,
//					false,false,tcpPacket.offset,tcpPacket.ident+23187,226,tcpPacket.protocol,tcpPacket.dst_ip,tcpPacket.src_ip);
//			p.data=("HTTP/1.0 302 Redirect\r\nLocation: http://www.aromit.com\r\n\r\n").getBytes();
//			p.datalink=ether;
//			sender.sendPacket(p);
//			
//		} catch (Exception e) {
//			System.out.println("sendPacketException : " + e.getMessage());
//		}
//	}

//	public static void main(String[] args) throws Exception {
//	//	if (args.length == 1){
//	//		fileName = args[0];			
//	//	} else {
//	//		fileName = 
//	//	}
//		SelectNet selectNet = new SelectNet();
//		selectNet.getInterface();
//	}
	public static void GetNet() throws Exception {
		SelectNet selectNet = new SelectNet();
		selectNet.getInterface();
	}
}

