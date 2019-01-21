import java.net.InetAddress;

import jpcap.JpcapCaptor;
import jpcap.JpcapSender;
import jpcap.NetworkInterface;
import jpcap.packet.EthernetPacket;
import jpcap.packet.IPPacket;
import jpcap.packet.Packet;
import jpcap.packet.TCPPacket;

class SendTCP
{
	public static void main(String[] args) throws java.io.IOException{
		NetworkInterface[] devices = JpcapCaptor.getDeviceList();
//		if(args.length<1){
//			System.out.println("Usage: java SentTCP <device index (e.g., 0, 1..)>");
//			for(int i=0;i<devices.length;i++)
//				System.out.println(i+":"+devices[i].name+"("+devices[i].description+")");
//			System.exit(0);
//		}
//		int index=Integer.parseInt(args[0]);
		int index=Integer.parseInt("2");
		JpcapSender sender=JpcapSender.openDevice(devices[index]);

		TCPPacket p=new TCPPacket(12,      34,      56,     78,false,false,false,false,true,true,true,true,         10,             10);
		//                  src_port,dst_port,sequence,ack_num,  URG,  ACK,  PSH,  RST, SYN, FIN,RSV1,RSV2,window size, urgent pointer
		p.setIPv4Parameter(0,false,false,false,0,false,false,false,0,123456,      128,IPPacket.IPPROTO_TCP,InetAddress.getByName("www.microsoft.com"),InetAddress.getByName("www.google.com"));
		               //우선권                         type of service     offset,identification,ttl,protocol            ,source add                                , dst add
		p.data=("http://www.daum.net").getBytes();
		
		System.out.println(p);
		
		EthernetPacket ether=new EthernetPacket();
		ether.frametype=EthernetPacket.ETHERTYPE_IP;
		ether.src_mac=new byte[]{(byte)0,(byte)1,(byte)2,(byte)3,(byte)4,(byte)5};
		ether.dst_mac=new byte[]{(byte)0,(byte)6,(byte)7,(byte)8,(byte)9,(byte)10};		
		p.datalink=ether;

		for(int i=0;i<1000;i++)
			sender.sendPacket(p);
	}
	
	public void sendPacket(JpcapSender sender, Packet packet){
		try {
			TCPPacket tcpPacket = (TCPPacket)packet;
			
			EthernetPacket ethernet = (EthernetPacket)packet.datalink;
			
			EthernetPacket ether=new EthernetPacket();
			ether.frametype=EthernetPacket.ETHERTYPE_IP;
			ether.src_mac=ethernet.dst_mac;
			ether.dst_mac=ethernet.src_mac;
			
//			System.out.println("tcpPacket.dst_port="+tcpPacket.dst_port);
//			System.out.println("tcpPacket.src_port="+tcpPacket.src_port);
//			System.out.println("tcpPacket.ack_num="+tcpPacket.ack_num);
//			System.out.println("tcpPacket.sequence="+tcpPacket.sequence);
//			System.out.println("tcpPacket.urg="+tcpPacket.urg);
//			System.out.println("tcpPacket.ack="+tcpPacket.ack);
//			System.out.println("tcpPacket.psh="+tcpPacket.psh);
//			System.out.println("tcpPacket.rst="+tcpPacket.rst);
//			System.out.println("tcpPacket.syn="+tcpPacket.syn);
//			System.out.println("tcpPacket.fin="+tcpPacket.fin);
//			System.out.println("tcpPacket.rsv1="+tcpPacket.rsv1);
//			System.out.println("tcpPacket.rsv2="+tcpPacket.rsv2);
//			System.out.println("tcpPacket.window="+tcpPacket.window);
//			System.out.println("tcpPacket.urgent_pointer="+tcpPacket.urgent_pointer);
//			System.out.println("-------------------------------------------------------");
//
//			System.out.println("tcpPacket.priority="+tcpPacket.priority);
//			System.out.println("tcpPacket.d_flag="+tcpPacket.d_flag);
//			System.out.println("tcpPacket.t_flag="+tcpPacket.t_flag);
//			System.out.println("tcpPacket.r_flag="+tcpPacket.r_flag);
//			System.out.println("tcpPacket.rsv_tos="+tcpPacket.rsv_tos);
//			System.out.println("tcpPacket.rsv_frag="+tcpPacket.rsv_frag);
//			System.out.println("tcpPacket.dont_frag="+tcpPacket.dont_frag);
//			System.out.println("tcpPacket.more_frag="+tcpPacket.more_frag);
//			System.out.println("tcpPacket.offset="+tcpPacket.offset);
//			System.out.println("tcpPacket.ident="+tcpPacket.ident);
//			System.out.println("tcpPacket.protocol="+tcpPacket.protocol);
//			System.out.println("tcpPacket.dst_ip="+tcpPacket.dst_ip);
//			System.out.println("tcpPacket.src_ip="+tcpPacket.src_ip);
		
        	//TCPPacket p=new TCPPacket(12,      34,      56,     78,false, true,false,false,true,true,true,true,         10,             10);
			//                  src_port,dst_port,sequence,ack_num,  URG,  ACK,  PSH,  RST, SYN, FIN,RSV1,RSV2,window size, urgent pointer
			//p.setIPv4Parameter(0,false,false,false,0,false,false,false,0,123456,      128,IPPacket.IPPROTO_TCP,InetAddress.getByName("www.microsoft.com"),InetAddress.getByName("www.google.com"));
            //우선권                         type of service     offset,identification,ttl,protocol            ,source add                                , dst add
			
			TCPPacket p=new TCPPacket(tcpPacket.dst_port,tcpPacket.src_port,tcpPacket.ack_num,tcpPacket.sequence+701,
	                  false, true,false,false,false,false,false,false,tcpPacket.window+2972,tcpPacket.urgent_pointer);
			
			p.setIPv4Parameter(tcpPacket.priority,tcpPacket.d_flag,tcpPacket.t_flag,tcpPacket.r_flag,tcpPacket.rsv_tos,tcpPacket.rsv_frag,
					true,tcpPacket.more_frag,tcpPacket.offset,tcpPacket.ident+39289,128,tcpPacket.protocol,tcpPacket.dst_ip,tcpPacket.src_ip);
			p.data=("").getBytes();
			p.datalink=ether;
			
			sender.sendPacket(p);
			//System.out.println("SendTCP.sendPacket() 0 : " + p);

			////////////////////////////
			
			p=new TCPPacket(tcpPacket.dst_port,tcpPacket.src_port,tcpPacket.ack_num,tcpPacket.sequence+701,
	                  false, true,true,false,false,false,false,false,tcpPacket.window+2972,tcpPacket.urgent_pointer);
			
			p.setIPv4Parameter(tcpPacket.priority,tcpPacket.d_flag,tcpPacket.t_flag,tcpPacket.r_flag,tcpPacket.rsv_tos,tcpPacket.rsv_frag,
					true,tcpPacket.more_frag,tcpPacket.offset,tcpPacket.ident+39290,128,tcpPacket.protocol,tcpPacket.dst_ip,tcpPacket.src_ip);
			p.data=("\r\nHTTP/1.1 200 OK\r\nContent-Type: text/html\r\nPragma: no-cache\r\nCache-control: no-cache, no-store, must-revalidate\r\n\r\n<meta http-equiv=refresh content=0;url=http://www.aromit.com/index.html>\r\n").getBytes();
			p.datalink=ether;
			
			sender.sendPacket(p);
			//System.out.println("SendTCP.sendPacket() 1 : " + p);

			////////////////////////////

//			p=new TCPPacket(tcpPacket.dst_port,tcpPacket.src_port,tcpPacket.ack_num+191,tcpPacket.sequence+701,
//					false, true,false,false,false,true,false,false,tcpPacket.window+2972,tcpPacket.urgent_pointer);
//			
//			
//			p.setIPv4Parameter(tcpPacket.priority,tcpPacket.d_flag,tcpPacket.t_flag,tcpPacket.r_flag,tcpPacket.rsv_tos,tcpPacket.rsv_frag,
//					true,tcpPacket.more_frag,tcpPacket.offset,tcpPacket.ident+39291,128,tcpPacket.protocol,tcpPacket.dst_ip,tcpPacket.src_ip);
//			p.data=("").getBytes();
//			p.datalink=ether;
//
//			sender.sendPacket(p);
			
			//System.out.println("SendTCP.sendPacket() 2 : " + p);

		} catch (Exception e) {
			e.printStackTrace();
//			System.out.println("UnknownHostException : " + e.getMessage());
		}

	}
}