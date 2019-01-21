
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JTextField;
import java.awt.Rectangle;
import javax.swing.JTextArea;
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
import javax.swing.JComboBox;



//public class TestTcpSend implements PacketReceiver {
//NetworkInterface[] devices = null;
//String inData = null;
////static JpcapSender sender = null;
//////String fileWriteTitleData = null;
////
////static int ConsoleFlag = 0;
////static int HexCodeFlag = 0;
////static int LangFlag = 0;
////static final int KSC5601 = 0;
////static final int ISO_8859_1 = 1;
////static final int EUC_KR = 2;
////static final int MS949 = 3;
////static final int UTF_8 = 4;
////
public class GetR114 extends JFrame implements PacketReceiver {
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
	
	private static final long serialVersionUID = 1L;
	private JPanel jContentPane = null;
	private JButton jButtonRun = null;
	private JTextField jTextField0 = null;
	private JTextField jTextField1 = null;
	private JTextField jTextField2 = null;
	private JTextField jTextField3 = null;
	
	static JTextField jTextFieldFirst = null;
	static JTextField jTextFieldEnd = null;
	static JTextField jTextFieldDeviceNum = null;
	static JTextField jTextFieldPacketCount = null;
	static JComboBox jComboBoxIncode = null;
	static JComboBox jComboBoxLine = null;
	static JTextField jTextField4 = null;
	static JTextField jTextFieldPort = null;
	static JTextArea jTextAreaResult = null;
	
	/**
	 * This is the default constructor
	 */
	public GetR114() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(761, 680);
		this.setContentPane(getJContentPane());
		this.setTitle("JFrame");
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.add(getJTextFieldFirst(), null);
			jContentPane.add(getJTextFieldEnd(), null);
			jContentPane.add(getJButtonRun(), null);
			jContentPane.add(getJTextAreaResult(), null);
			jContentPane.add(getJTextField0(), null);
			jContentPane.add(getJTextFieldDeviceNum(), null);
			jContentPane.add(getJTextField1(), null);
			jContentPane.add(getJTextFieldPacketCount(), null);
			jContentPane.add(getJTextField2(), null);
			jContentPane.add(getJTextField3(), null);
			jContentPane.add(getJComboBoxIncode(), null);
			jContentPane.add(getJComboBoxLine(), null);
			jContentPane.add(getJTextField4(), null);
			jContentPane.add(getJTextFieldPort(), null);
		}
		return jContentPane;
	}

	/**
	 * This method initializes jTextFieldFirst	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJTextFieldFirst() {
		if (jTextFieldFirst == null) {
			jTextFieldFirst = new JTextField();
			jTextFieldFirst.setBounds(new Rectangle(562, 7, 180, 22));
			jTextFieldFirst.setText(";팩스:");
		}
		return jTextFieldFirst;
	}

	/**
	 * This method initializes jTextFieldEnd	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJTextFieldEnd() {
		if (jTextFieldEnd == null) {
			jTextFieldEnd = new JTextField();
			jTextFieldEnd.setBounds(new Rectangle(562, 36, 180, 22));
			jTextFieldEnd.setText("<br/>");
		}
		return jTextFieldEnd;
	}

	/**
	 * This method initializes jButtonRun	
	 * 	
	 * @return javax.swing.JButton	
	 */
//public static void main(String[] args) throws Exception {
////if (args.length == 1){
////	fileName = args[0];			
////} else {
////	fileName = 
////}
//TestTcpSend testTcpSend = new TestTcpSend();
//testTcpSend.getInterface();
//}
	private JButton getJButtonRun() {
		if (jButtonRun == null) {
			jButtonRun = new JButton();
			jButtonRun.setBounds(new Rectangle(562, 66, 180, 26));
			jButtonRun.setText("실행하기");
			jButtonRun.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					JOptionPane.showMessageDialog(null, "개더링을 시작합니다.");// System.out.println("actionPerformed()"); // TODO Auto-generated Event stub actionPerformed()
////					TestTcpSend testTcpSend = new TestTcpSend();
////					testTcpSend.getInterface();
//					try {
//						SelectNet.main(null);
//
////						GetR114 testTcpSend = new GetR114();
////						testTcpSend.getInterface();
//					} catch (Exception e1) {
//						// TODO Auto-generated catch block
//						e1.printStackTrace();
//						jTextAreaResult.setText("에러:"+e1+"\n\n"+jTextAreaResult.getText().toString());
//					}
					new Thread(new Runnable() {
					    @Override
					    public void run() {
							try {
								SelectNet.GetNet();
//								TestTcpSend testTcpSend = new TestTcpSend();
//								testTcpSend.getInterface();
							} catch (Exception e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
								jTextAreaResult.setText("에러:"+e1+"\n\n"+jTextAreaResult.getText().toString());
							}
					    }
					}).start();
					
				}
			});
		}
		return jButtonRun;
	}

	/**
	 * This method initializes jTextAreaResult	
	 * 	
	 * @return javax.swing.JTextArea	
	 */
	private JTextArea getJTextAreaResult() {
		if (jTextAreaResult == null) {
			jTextAreaResult = new JTextArea();
			jTextAreaResult.setBounds(new Rectangle(3, 103, 747, 549));
		}
		return jTextAreaResult;
	}

	
	
	
	
	



	/**
	 * This method initializes jTextField0	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJTextField0() {
		if (jTextField0 == null) {
			jTextField0 = new JTextField();
			jTextField0.setBounds(new Rectangle(13, 6, 56, 22));
			jTextField0.setText("장치번호");
		}
		return jTextField0;
	}

	/**
	 * This method initializes jTextFieldDeviceNum	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJTextFieldDeviceNum() {
		if (jTextFieldDeviceNum == null) {
			jTextFieldDeviceNum = new JTextField();
			jTextFieldDeviceNum.setBounds(new Rectangle(77, 6, 36, 22));
			jTextFieldDeviceNum.setText("1");
		}
		return jTextFieldDeviceNum;
	}

	/**
	 * This method initializes jTextField1	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJTextField1() {
		if (jTextField1 == null) {
			jTextField1 = new JTextField();
			jTextField1.setBounds(new Rectangle(13, 30, 56, 22));
			jTextField1.setText("패킷양");
		}
		return jTextField1;
	}

	/**
	 * This method initializes jTextFieldPacketCount	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJTextFieldPacketCount() {
		if (jTextFieldPacketCount == null) {
			jTextFieldPacketCount = new JTextField();
			jTextFieldPacketCount.setBounds(new Rectangle(77, 30, 36, 22));
			jTextFieldPacketCount.setText("128");
		}
		return jTextFieldPacketCount;
	}

	/**
	 * This method initializes jTextField2	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJTextField2() {
		if (jTextField2 == null) {
			jTextField2 = new JTextField();
			jTextField2.setBounds(new Rectangle(13, 55, 56, 22));
			jTextField2.setText("인코딩");
		}
		return jTextField2;
	}

	/**
	 * This method initializes jTextField3	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJTextField3() {
		if (jTextField3 == null) {
			jTextField3 = new JTextField();
			jTextField3.setBounds(new Rectangle(13, 80, 56, 22));
			jTextField3.setText("회선");
		}
		return jTextField3;
	}

	/**
	 * This method initializes jComboBoxIncode	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getJComboBoxIncode() {
		if (jComboBoxIncode == null) {
			jComboBoxIncode = new JComboBox();
			jComboBoxIncode.setBounds(new Rectangle(77, 55, 87, 22));
			jComboBoxIncode.addItem("KSC5601"); //기본  KSC5601 = 0, ISO-8859-1 = 1, EUC-KR = 2, MS949 = 3, UTF-8 = 4
			jComboBoxIncode.addItem("ISO-8859-1");
			jComboBoxIncode.addItem("EUC-KR");
			jComboBoxIncode.addItem("MS949");
			jComboBoxIncode.addItem("UTF-8");
			jComboBoxIncode.setSelectedItem("KSC5601"); //Name("KSC5601");
			jComboBoxIncode.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					//JOptionPane.showMessageDialog(null, "현재 선택된 아이템은 " + jComboBoxIncode.getSelectedIndex() + "번, " + jComboBoxIncode.getSelectedItem().toString());//System.out.println("itemStateChanged()"); // TODO Auto-generated Event stub itemStateChanged()
				}
			});
//			jComboBoxIncode.addItemListener(new java.awt.event.ItemListener() {
//				public void itemStateChanged(java.awt.event.ItemEvent e) {
//					System.out.println("itemStateChanged()"); // TODO Auto-generated Event stub itemStateChanged()
//				}
//			});
		}
		return jComboBoxIncode;
	}

	/**
	 * This method initializes jComboBoxLine	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getJComboBoxLine() {
		if (jComboBoxLine == null) {
			jComboBoxLine = new JComboBox();
			jComboBoxLine.setBounds(new Rectangle(77, 80, 87, 22));
			jComboBoxLine.addItem("내회선만");
			jComboBoxLine.addItem("전체회선");
			jComboBoxLine.setSelectedItem("0");
			jComboBoxLine.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					//JOptionPane.showMessageDialog(null, "현재 선택된 아이템은 " + jComboBoxLine.getSelectedIndex() + "번, " + jComboBoxLine.getSelectedItem().toString());//System.out.println("itemStateChanged()"); // TODO Auto-generated Event stub itemStateChanged()

				}
			});
		}
		return jComboBoxLine;
	}

	/**
	 * This method initializes jTextField4	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJTextField4() {
		if (jTextField4 == null) {
			jTextField4 = new JTextField();
			jTextField4.setBounds(new Rectangle(261, 9, 150, 22));
			jTextField4.setText("감시할 포트(없으면 전체)");
		}
		return jTextField4;
	}

	/**
	 * This method initializes jTextFieldPort	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJTextFieldPort() {
		if (jTextFieldPort == null) {
			jTextFieldPort = new JTextField();
			jTextFieldPort.setBounds(new Rectangle(420, 9, 45, 22));
			jTextFieldPort.setText("");
		}
		return jTextFieldPort;
	}

	
	
	
	
	
//	public class TestTcpSend implements PacketReceiver {
//		NetworkInterface[] devices = null;
//		String inData = null;
////		static JpcapSender sender = null;
//////		String fileWriteTitleData = null;
////
////		static int ConsoleFlag = 0;
////		static int HexCodeFlag = 0;
////		static int LangFlag = 0;
////		static final int KSC5601 = 0;
////		static final int ISO_8859_1 = 1;
////		static final int EUC_KR = 2;
////		static final int MS949 = 3;
////		static final int UTF_8 = 4;
////		
		
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

			if(jTextFieldDeviceNum.getText().toString().trim().equals("")) {
				setText = "디바이스를 선택하지 않으셨습니다.\n\n" + setText;
				return;
			}else if(Integer.parseInt(jTextFieldDeviceNum.getText().toString().trim())>=0 && Integer.parseInt(jTextFieldDeviceNum.getText().toString().trim())<=devices.length){
				device_num = Integer.parseInt(jTextFieldDeviceNum.getText().toString().trim());
			}else{
				setText = "올바른 디바이스를 선택하지 않으셨습니다.\n\n" + setText;
				return;
			}
			if(jTextFieldPacketCount.getText().toString().trim().equals("")){
				setText = "올바른 패킷양을 지정하지 않으셨어요~.\n\n" + setText;
				return;
			}else if(Integer.parseInt(jTextFieldPacketCount.getText().toString().trim())>=0){
				byte_num = Integer.parseInt(jTextFieldPacketCount.getText().toString().trim()); // 128바이트가 기본임.
			}else{
				setText = "올바른 패킷양을 지정하지 않으셨어요~.\n\n" + setText;
				return;
			}

//			System.out.println("<== HexCode를 포함할까요? 구분자는 TAB Char(9)임. (기본 N) Y/N: ");	
//			br = new BufferedReader(new InputStreamReader(System.in));
//			inData = br.readLine();
//			if(inData.equals("y") || inData.equals("Y")) {HexCodeFlag = 1; } else {
				HexCodeFlag = 0;
//			} // 0이 기본임. 안함.
			
			//System.out.println("<== 글자 인코딩은 뭐로할까요? (기본  KSC5601 = 0, ISO-8859-1 = 1, EUC-KR = 2, MS949 = 3, UTF-8 = 4 ) : ");
			//br = new BufferedReader(new InputStreamReader(System.in));
			//inData = br.readLine();
			//if (inData == null || inData.length() < 1 ) inData = Integer.toString(KSC5601);
			//switch (Integer.parseInt(inData)) {
			switch(jComboBoxIncode.getSelectedIndex()){
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
//			System.out.println("<== 결과를 화면에 출력할까요? (기본 N) Y/N: ");	
//			br = new BufferedReader(new InputStreamReader(System.in));
//			inData = br.readLine();
//			if(inData.equals("y") || inData.equals("Y")) {ConsoleFlag = 1; } else {
			ConsoleFlag = 0;
//			} // 0이 기본임. 안함.
			
//			System.out.println("<== 모든회선에 대한검색(1), 내PC가 호스트인패킷만 검색(0) 선택 (기본은 0)");	br = new BufferedReader(new InputStreamReader(System.in));	inData = br.readLine();
//			if (Integer.parseInt(inData) == 1){AllORMe=true;}
			switch(jComboBoxLine.getSelectedIndex()){
			case 1:
				AllORMe=true;
				break;
			default:
				AllORMe=false;
				break;
			}
			//무선네트워크를 선택할땐 0으로 선택해야한다.
	/////////////////////////////////////////////////////////////////////////////////////////////
//			System.out.println("<== 수집할 포트번호 (기본 80): ");	br = new BufferedReader(new InputStreamReader(System.in));	inData = br.readLine();
//			port_num = Integer.parseInt(inData); 

//			device_num = 5;byte_num = 128;AllORMe = true; //물어보지 않고 강제로 지정하기... 그러면 위에를 리마크해야한다...
//			System.out.println("\n입력한 내용 : 선택한디바이스( " + device_num + " ), 수집할패킷양( " + byte_num + " ), 모든회선 선택 true or false ( " + AllORMe + " )\n\n" );
			setText = setText + "\n입력한 내용 : 선택한디바이스( " + device_num + " ), 수집할패킷양( " + byte_num + " ), 모든회선 선택 true or false ( " + AllORMe + " )\n\n";
			// interface, 패킷양(byte), true(모든 지나가는 회선 패킷) false(내 PC가 호스트인 패킷),
			// timeout(ms)
			
			jTextAreaResult.setText(setText);
			
						JpcapCaptor jpcap = JpcapCaptor.openDevice(devices[device_num], byte_num, AllORMe, -1);
			sender = JpcapSender.openDevice(devices[device_num]);

			if(Integer.parseInt(jTextFieldPort.getText().trim())>0){
				jpcap.setFilter("port " + Integer.parseInt(jTextFieldPort.getText().trim()), true); // 포트를 지정할때... ""는 모든포트이다....
			}else {
				jpcap.setFilter("", true); // 포트가 아니라 전체 페킷은 어케 설정하지???
			}
			jpcap.loopPacket(-1, new TestTcpSend());
			
		}

		public void receivePacket(Packet packet) {
			//TCPPacket tcpPacket = (TCPPacket) packet;
			if (packet instanceof TCPPacket) {
//				System.out.println("센더 : " + sender.toString());
//				System.out.println("패킷 : " + packet.toString());
				
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
					
//					//isoData = new String(data, "ISO-8859-1");
//					//isoData = new String(data.toString().getBytes("KSC5601"),"EUC-KR");
//					isoData = new String(data, "KSC5601");
//					//isoData = new String(data, "ISO-8859-1") + "	" + Util.getHexa(data) ;
//					//안된다//isoData = new String(data, "UTF-8") + "	" + Util.getHexa(data) ;
//					//안된다//isoData = new String(data, "EUC-KR") + "	" + Util.getHexa(data) ;
//					//안된다//isoData = new String(data, "MS949") + "	" + Util.getHexa(data) ;

				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				
				Date today = new Date(); 
	            SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd"); 
	            date.format(today).toString();  

				try {
					String str = GetData(isoData);
					if (str!=null && str.length()>0){
						jTextAreaResult.setText(str+"\n"+jTextAreaResult.getText().toString());
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
			aStr=isoData.split(jTextFieldFirst.getText().toString());
			if (aStr[1]==null) return "";
			aStr[0]=aStr[1];
			aStr[1]=null;
			aStr=aStr[1].split(jTextFieldEnd.getText().toString());
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

//		public static void main(String[] args) throws Exception {
////			if (args.length == 1){
////				fileName = args[0];			
////			} else {
////				fileName = 
////			}
//			TestTcpSend testTcpSend = new TestTcpSend();
//			testTcpSend.getInterface();
//		}
//	}




//	public static void main(String[] args) throws Exception {
////		if (args.length == 1){
////			fileName = args[0];			
////		} else {
////			fileName = 
////		}
//		TestTcpSend testTcpSend = new TestTcpSend();
//		testTcpSend.getInterface();
//	}
//}


	
	

}  //  @jve:decl-index=0:visual-constraint="10,10"


