package com.itcall.remotej.img;

import java.util.Arrays;

public enum ImageCd {

	BMP("42 4D"),
	JPG("FF D8"),
	PNG("89 50 4E 47 0D 0A 1A 0A"),
	GIF("47 49 46 38"),
	TIF("49 20 49")
	;
	
	String preFix;
	
	private ImageCd(String preFix) {
		this.preFix = preFix;
	}
	
	public byte[] getPreFixBytes(){
		String[] arrByteStr = this.preFix.split(" ");
		byte[] bts = new byte[arrByteStr.length];
		for (int i = 0; i < bts.length; i++) {
			bts[i] = (byte) Integer.parseInt(arrByteStr[i], 16);
		}
		return bts;
	}
	public boolean isImage(byte[] imgBytes){
		try{
			byte[] preFix = getPreFixBytes();
			return Arrays.equals(preFix, Arrays.copyOf(imgBytes, preFix.length));
		}catch(Exception e){
			return false;
		}
	}

	public static ImageCd chkImg(byte[] imgBytes) throws Exception{
		if(imgBytes==null) throw new Exception("this is not support (image)data : " + imgBytes);
		for (int i = 0; i < ImageCd.values().length; i++) {
			if(ImageCd.values()[i].isImage(imgBytes)){
				return ImageCd.values()[i];
			}
		}
		throw new Exception("this is not support (image)data : size " + imgBytes.length);
	}

//	BMP(42 4D),
//	JPG(FF D8),
//	PNG(89 50 4E 47 0D 0A 1A 0A),
//	GIF(47 49 46 38),
//	TIF(49 20 49)

}
