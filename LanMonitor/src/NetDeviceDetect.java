/*
 * jpcap을 이용한 랜카드 디바이스 정보를 출력하기...
 * jpcap 설치 : http://netresearch.ics.uci.edu/kfujii/Jpcap/doc/download.html
 * 운영체제 종류별로 받아서 설치하면... jpcap.jar를 외부 라이브러리로 등록해준다.
 * Win32 : C:\WINDOWS\Sun\Java\lib\ext
 * 리눅스 : /usr/java/packegs/lib/ext
 * 
 * 윈도우일경우에는 WinPcap_4_1_2 설치, JpcapSetup-0.7 설치...
 */

import jpcap.*;
import jpcap.packet.*;

public class NetDeviceDetect {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//for save each interface information
		NetworkInterface[] devices = JpcapCaptor.getDeviceList();
		//for each network interface
		for(int i = 0; i<devices.length;i++) {
			//print out its name and description
			System.out.println(i+": "+devices[i].name + "(" + devices[i].datalink_description+")");
			//print out its datalink name and description
			System.out.println(" datalink: "+devices[i].datalink_name + "(" + devices[i].datalink_description+")");
			//print out its MAC address
			System.out.print(" MAC address:");
			
			for(byte b : devices[i].mac_address)
				System.out.print(Integer.toHexString(b&0xff) + ":");
			
			System.out.println();
			
			//print out its IP address, subnet mask and broadcast address
			for(NetworkInterfaceAddress a : devices[i].addresses)
				System.out.println(" address:"+a.address + " " + a.subnet + " " + a.broadcast);
			
		}

	}

}
