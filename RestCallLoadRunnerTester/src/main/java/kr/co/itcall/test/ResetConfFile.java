package kr.co.itcall.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.Arrays;

public class ResetConfFile {

	private String confFile;
	private StringBuffer fileBody;
	
	public ResetConfFile(String confFile) {
		this.confFile = confFile;
		this.fileBody = new StringBuffer();
	}

	public static void main(String[] args) throws IOException {
//		System.out.println("test.0.name=로".matches("test[.]{1}[0-9]{1,5}[.]{1}name=.*")); // 아무한문자(.), 모든문자(.*)
//		System.out.println("test.0.file.0.pathasgdfasdf".matches("test[.]{1}[0-9]{1,5}[.]{1}file[.]{1}[0-9]{1,5}.path.*"));
		
		/*** 일반식은 Exception을 부모에게 넘길 수 있다. 여러파일 처리 중 에러발생하면 전체를 종료할 수 있다. ***/
//		for (String confFile : TestCall.choiceAndRunTestCaseConfFile("").split(",")) {
//			new ResetConfFile(confFile).resetConfFile();
//		}
		
		/*** stream + 람다식은 interface의 신규생성을 이용하는 것이라 부모에게 Exception을 넘길 수 없다. 따라서 여러파일 처리 중 에러발생해도 전체를 종료할 수 없다. ***/
		Arrays.asList(TestCall.choiceAndRunTestCaseConfFile("").split(",")).forEach(confFile -> {
			try {
				new ResetConfFile(confFile).resetConfFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	private void resetConfFile() throws IOException {
		
		boolean checkTestTerm = false;
		int index = -1;
		BufferedReader br = null;
		br = new BufferedReader(new InputStreamReader(new FileInputStream(this.confFile), Charset.forName("UTF-8")));
		while (true) {
			String readLine = br.readLine();
			if(readLine==null)
				break;
			String[] tests = readLine.split("[.]", 3);
			if(tests.length==3 && readLine.matches(".*test[.]{1}[0-9]{1,5}[.]{1}.*")) {
				try {
					if(readLine.matches("test[.]{1}[0-9]{1,5}[.]{1}name=.*")) { // 아무한문자(.), 모든문자(.*)
						++index;
					}
					this.fileBody.append(tests[0]).append(".").append(index).append(".").append(tests[2]).append("\n");
				} catch (Exception e) {
					this.fileBody.append(readLine).append("\n");
				}
			} else {
				this.fileBody.append(readLine).append("\n");
			}
		}
		
		br.close();
		
		System.out.println(this.fileBody.toString());
		
		System.out.println("\n======================================================================================");
		System.out.print  ("저장할까요?(YES/NO) ==>");
		
		br = new BufferedReader(new InputStreamReader(System.in));
		String inputUserData = br.readLine().trim();
		if(!inputUserData.toUpperCase().contains("Y")) {
			System.out.println("저장하지 않았습니다.");
			return;
		}
		
		
		BufferedWriter bw = null;
		bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.confFile+".reset"), Charset.forName("UTF-8")));
		bw.write(fileBody.toString());
		bw.flush();
		bw.close();
		System.out.println("파일이 갱신되었습니다.");
	}
}
