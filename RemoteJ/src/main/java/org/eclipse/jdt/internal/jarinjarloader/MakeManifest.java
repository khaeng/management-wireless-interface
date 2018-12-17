package org.eclipse.jdt.internal.jarinjarloader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Create to MANIFEST.MF file
 * @author khaeng@nate.com
 */
public class MakeManifest {

	private final static String MANIFEST_MF_FILE = "MANIFEST.MF";
	public static void main(String[] args) throws IOException {
		String fileName = MANIFEST_MF_FILE;
		String mainClass = args[0];
		try{
			fileName = args[1];
		}catch(Exception e){}
		BufferedWriter bw = new BufferedWriter(new FileWriter(fileName, false));
		bw.write(getManifestContents(mainClass));
		bw.flush();
		bw.close();
	}

	private static String getLibsList(){
		StringBuilder sb = new StringBuilder();
		File libs = new File("libs");
		String[] jarList = libs.list();
		for (int i = 0; i < jarList.length; i++) {
			if(jarList[i].endsWith(".jar") || jarList[i].endsWith(".war") ){
				sb.append(" libs/").append(jarList[i]);
			}
			if(i>0&&i%5==0){
				sb.append(" \n");
			}
		}
		return sb.toString().trim();
	}

	private final static String getManifestContents(String mainClass){
		return "Manifest-Version: 1.0"
			+ "\nBuilt-By: khaeng@nate.com"
			+ "\nBuild-Jdk: " + System.getProperty("java.version", "1.7.0_79")
			+ "\nRsrc-Class-Path: ./ " + getLibsList()
			+ "\nClass-Path: ."
			+ "\nCreated-By: Apache Maven 3.3.3"
			+ "\nRsrc-Main-Class: " + mainClass
			+ "\nMain-Class: org.eclipse.jdt.internal.jarinjarloader.JarRsrcLoader"
			+ "\nArchiver-Version: Plexus Archiver"
			+ "\n";
	}
}
