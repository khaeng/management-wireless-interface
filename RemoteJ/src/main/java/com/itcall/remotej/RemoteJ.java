package com.itcall.remotej;

import java.awt.AWTException;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itcall.remotej.img.ImageCd;
import com.itcall.remotej.img.ImageVo;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;

public class RemoteJ extends JFrame {

	private static final long serialVersionUID = -5233605061620193596L;
	private final static Logger LOG = LoggerFactory.getLogger(RemoteJ.class); 

	public static void main(String ...args) throws AWTException, IOException, Exception {
		new RemoteJ().initRepeaterJ(args);
		new RemoteJ().initScreenJ(args);
		new RemoteJ().initControlJ(args);

		ImageVo imageVo = new ImageVo(ImageVo.getScreenCapture(0, ImageCd.BMP));
		BufferedOutputStream bo = new BufferedOutputStream(new FileOutputStream("TEST." + imageVo.getImageCd()));
		bo.write(imageVo.getImageBytes());
		bo.flush();
		bo.close();
		
		System.out.println(imageVo.getImageCd());
		
		ByteBuf byteBuf = PooledByteBufAllocator.DEFAULT.directBuffer();
		byteBuf.writeBytes(ImageVo.getScreenCapture());
		LOG.debug(".....{}", byteBuf.readableBytes());
		LOG.info(".....{}", byteBuf.readableBytes());
		LOG.warn(".....{}", byteBuf.readableBytes());
		LOG.error(".....{}", byteBuf.readableBytes());
		
		ImageVo.regPrint();
	}

	private void initRepeaterJ(String ...args){
		
	}

	private void initScreenJ(String ...args){
		
	}

	private void initControlJ(String ...args){
		
	}

	@Override
	public void repaint() {
		super.repaint();
	}

}
