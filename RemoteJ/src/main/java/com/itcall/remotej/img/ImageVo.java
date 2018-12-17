package com.itcall.remotej.img;

import java.awt.AWTException;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;

import com.itcall.remotej.util.ReadRegistry;
import com.itcall.remotej.util.ReadRegistry.HKEY;
import com.itcall.remotej.util.Utils;
import com.itcall.remotej.util.WinRegistry;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter@Setter@ToString
public class ImageVo {

	private ByteBuf imageByteBuf;
	private byte[] imageBytes;
	private byte[] imageHeader;
	private byte[][][] imageBody;
	private ImageCd imageCd;
	private int cutCnt;

	private int imgSize; // 2 + 4
	private int offsetImg; // 10 + 4
	private int width; // 18 + 4
	private int height; // 22 + 4

	/**
	 * BMP이미지의 Body끝에는 0x00이 한 바이트 저장된다.
	 * BMP이미지의 Body는 LittleEndian방식으로 거꾸로 저장되며,
	 * 한Pixel은 3바이트의 RGB정보로 구성되며. 이또한 저장방식이 BGR순으로 저장된다.(LittleEndian)
	 * 한 Width가 끝나는 시점에 0x00byte가 존재한다.(따라서 가장마지막 바이트도 0x00이 된다. LineDelemiter라고 생각하면 될듯...)
	 * @param imageBytes
	 * @throws Exception
	 */
	public ImageVo(byte[] imageBytes) throws Exception {
		setImage(imageBytes);
	}

	/**
	 * 새로운 이미지를 셋팅한다.
	 * @param imageBytes
	 * @return
	 * @throws Exception
	 */
	public ImageVo setImage(byte[] imageBytes) throws Exception{
		this.imageBytes = imageBytes;
		this.imageCd = ImageCd.chkImg(imageBytes);
		this.imageByteBuf = PooledByteBufAllocator.DEFAULT.directBuffer().writeBytes(imageBytes);
		if(this.imageCd==ImageCd.BMP){
			this.imgSize 	= Utils.byteToInt(Arrays.copyOfRange(this.imageBytes, 2, 2 + 4), ByteOrder.LITTLE_ENDIAN);
			this.offsetImg 	= Utils.byteToInt(Arrays.copyOfRange(this.imageBytes, 10, 10 + 4), ByteOrder.LITTLE_ENDIAN);
			this.width 		= Utils.byteToInt(Arrays.copyOfRange(this.imageBytes, 18, 18 + 4), ByteOrder.LITTLE_ENDIAN);
			this.height 	= Utils.byteToInt(Arrays.copyOfRange(this.imageBytes, 22, 22 + 4), ByteOrder.LITTLE_ENDIAN);
			this.imageHeader = Arrays.copyOfRange(this.imageBytes, 0, this.offsetImg);
			// this.imageBody   = Arrays.copyOfRange(this.imageBytes, this.offsetImg, this.imgSize);
			this.imageBody = new byte[this.width][this.height][3]; // 마지막 3개는 RGB정보
			int cnt = this.offsetImg;
			for (int w = 0; w < this.width; w++) {
				for (int h = 0; h < this.height; h++) {
					this.imageBody[w][h][0]=this.imageBytes[cnt++]; // Blue
					this.imageBody[w][h][1]=this.imageBytes[cnt++]; // Green
					this.imageBody[w][h][2]=this.imageBytes[cnt++]; // Red
				}
				cnt++; // 한개의 열이 끝날때마다 0x00이 있으므로 제외한다.(복구할땐 체워넣어야 한다.)
			}
		}else{
			this.imgSize = this.imageBytes.length;
		}
		return this;
	}

	/**
	 * 변경된 이미지정보만 추출한다.[통신패턴에 맞춘다]
	 * @param newImageBytes
	 * @return
	 */
	public byte[] getChangeImage(byte[] newImageBytes){
		
		return null;
	}
	/**
	 * 변경된 이미지정보를 적용한다.[통신패턴에 맞춘 데이터를 받아서 셋팅해준다]
	 * @param changeBitsBytes
	 */
	public ImageVo setChangeImage(byte[] changeBitsBytes){
		
		return this;
	}









	public static void main(String[] args) throws IOException, AWTException, Exception {
		ImageVo imageVo = new ImageVo(getScreenCapture(0, ImageCd.BMP));
		BufferedOutputStream bo = new BufferedOutputStream(new FileOutputStream("TEST." + imageVo.imageCd));
		bo.write(imageVo.imageBytes);
		bo.flush();
		bo.close();
		
		System.out.println(imageVo.imageCd);
	}

	public static ByteBuffer imageToByteBuffer(BufferedImage bi, ImageCd imageCd) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ImageIO.write(bi, imageCd.name(), out);
		out.flush();
		out.close();
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(out.size());
		byteBuffer.put(out.toByteArray(), 0, out.size());
		return byteBuffer;
	}

	public static ByteBuf imageToByteBuf(BufferedImage bi, ImageCd imageCd) throws IOException {
		ByteBuf byteBuf = PooledByteBufAllocator.DEFAULT.directBuffer(); // ByteBuf.allocateDirect(out.size());
		return imageToByteBuf(bi, imageCd, byteBuf);
	}
	public static ByteBuf imageToByteBuf(BufferedImage bi, ImageCd imageCd, ByteBuf byteBuf) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ImageIO.write(bi, imageCd.name(), out);
		out.flush();
		out.close();
		byteBuf.writeBytes(out.toByteArray(), 0, out.size());
		return byteBuf;
	}

	public static byte[] getScreenCapture() throws AWTException, IOException{return getScreenCapture(0, ImageCd.BMP);}
	public static byte[] getScreenCapture(int screenNum) throws AWTException, IOException{return getScreenCapture(screenNum, ImageCd.BMP);}
	public static byte[] getScreenCapture(ImageCd imageCd) throws AWTException, IOException{return getScreenCapture(0, imageCd);}
	public static byte[] getScreenCapture(int screenNum, ImageCd imageCd) throws AWTException, IOException{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Rectangle screenRect = null;
		try{
			// screenRect = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[screenNum].getFullScreenWindow().getBounds();
			screenRect = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[screenNum].getDefaultConfiguration().getBounds();
//			GraphicsConfiguration[] gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[screenNum].getConfigurations();
//			for (GraphicsConfiguration curGc : gc) {
//				Rectangle bounds = curGc.getBounds();
//				// System.out.println(bounds.getX() + "," + bounds.getY() + " " + bounds.getWidth() + "x" + bounds.getHeight());
//				if (screenRect.getX() > bounds.getX()) screenRect.setLocation((int) bounds.getX()    , (int) screenRect.getY());
//				if (screenRect.getY() > bounds.getY()) screenRect.setLocation((int) screenRect.getX(), (int) bounds.getY());
//				if (screenRect.getWidth()  < bounds.getWidth()) screenRect.setSize((int)bounds.getWidth()    , (int)screenRect.getHeight());
//				if (screenRect.getHeight() < bounds.getHeight())screenRect.setSize((int)screenRect.getWidth(), (int)bounds.getHeight());
//			}
		}catch(Exception e){
			screenRect = new Rectangle();
		}
		BufferedImage capture = new Robot().createScreenCapture(screenRect);
		ImageIO.write(capture, imageCd.name(), baos);
		baos.flush();baos.close();
		return baos.toByteArray();
	}

	public byte[] getImageBytes() {
		return this.imageBytes;
	}

	public ImageCd getImageCd() {
		return this.imageCd;
	}

/**
 * Windows7 ~ 10
 * HKEY_CURRENT_USER\\Control Panel\\Desktop		Win8DpiScaling 
 * Value data	Description		0	1
 * 0 :	Enter 0 if you used 96 in step 5 for no custom DPI scaling.
 * 1 :	Enter 1 if you used any other size in step 5 for custom DPI scaling.
 * 
 * Check Registry Infomation
 * HKEY_CURRENT_USER\\Control Panel\\Desktop		LogPixels 
 * DPI Scaling 	Level	Value data
 * Smaller 		100% 	(default)	96
 * Medium 		125%	120
 * Larger 		150%	144
 * Extra Large 	200%	192
 * Custom 		250%	240
 * Custom 		300%	288
 * Custom 		400%	384
 * Custom 		500%	480
 * 
 * 
 */
	public static String regPrint(){
		String value = null;
		try {
			value = WinRegistry.getRegKey(
				    WinRegistry.HKEY_LOCAL_MACHINE,                             //HKEY
				   "SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion",           //Key
				   "ProductName");                                              //ValueName
			System.out.println("Windows Distribution = " + value);
			value = WinRegistry.getRegKey(
				    WinRegistry.HKEY_LOCAL_MACHINE,                             //HKEY
				   "SYSTEM\\CurrentControlSet\\Control\\Windows",           //Key
				   "ShutdownStopTimePerfCounter");                                              //ValueName
			System.out.println("Windows Distribution = " + value);
			value = WinRegistry.getRegKey(
				    WinRegistry.HKEY_CURRENT_USER,
				   "Control Panel\\Desktop",
				   "Win8DpiScaling");
			System.out.println("Win8DpiScaling = " + value);
			value = WinRegistry.getRegKey(
				    WinRegistry.HKEY_CURRENT_USER,
				   "Control Panel\\Desktop",
				   "LogPixels");
			System.out.println("LogPixels = " + value);
			value = WinRegistry.getRegKey(
				    WinRegistry.HKEY_CURRENT_USER,
				   "Control Panel\\Desktop",
				   "TranscodedImageCache");
			System.out.println("TranscodedImageCache = " + value);
			value = WinRegistry.getRegKey(
				    WinRegistry.HKEY_CURRENT_USER,
				   "Control Panel\\Desktop",
				   "TranscodedImageCount");
			System.out.println("TranscodedImageCount = " + value);
			value = WinRegistry.getRegKey(
				    WinRegistry.HKEY_CURRENT_USER,
				   "Control Panel\\Desktop",
				   "WallPaper");
			System.out.println("WallPaper = " + value);
			value = WinRegistry.getRegKey(
				    WinRegistry.HKEY_CURRENT_USER,
				   "Control Panel\\Desktop",
				   "PreferredUILanguages");
			System.out.println("PreferredUILanguages = " + value);
			
			
			// Write Operation
//			Preferences p = Preferences.userRoot();
//			p.put("key","value"); 
//			// also there are various other methods such as putByteArray(), putDouble() etc.
//			p.flush();
			//Read Operation
			Preferences userRoot = Preferences.userRoot();
			Preferences systemRoot = Preferences.systemRoot();
			String v = userRoot.get("HKCU\\Control Panel\\Desktop\\PreferredUILanguages", "null");
			int i = userRoot.getInt("HKCU\\Control Panel\\Desktop\\PreferredUILanguages", -1);
			System.out.println(
					"PreferredUILanguages = " + 
			ReadRegistry.readRegistry(HKEY.CURRENT_USER, "Control Panel\\Desktop", "PreferredUILanguages")
			);
			
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return value;
	}
}
