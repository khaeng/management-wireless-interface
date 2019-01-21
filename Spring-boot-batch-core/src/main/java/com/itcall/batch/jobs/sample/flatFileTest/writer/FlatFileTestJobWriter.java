package com.itcall.batch.jobs.sample.flatFileTest.writer;

import java.io.File;

import org.springframework.batch.core.StepExecution;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;

import com.itcall.batch.biz.vo.sample.flatFileTest.SvcAreaInfoVO;
import com.itcall.batch.config.batch.item.BaseFlatFileWriter;

public class FlatFileTestJobWriter extends BaseFlatFileWriter<SvcAreaInfoVO> {

	@Value("#{batch['biz.batch.flatFileTestJob.fieldNames']?:'dongCd,addrNoType,startAddrNo,startAddrHo,endAddrNo,endAdddrHo,acptOfcCd,rssFlag'}") 
	private String fieldNames;

	@Override
	protected Class<SvcAreaInfoVO> initMapperRule() {
		/********************************************
		 * 파일을 계속 추가할것인지 셋팅한다. 기본값 OverWrite
		 ********************************************/
		// setAppendAllowed(true);

		/********************************************
		 * 필드별 구분자를 입력한다. 기본값 콤마(,)
		 ********************************************/
		// setFieldDelimiter("`");

		/********************************************
		 * 필드명을 셋팅한다. 기본값은 VO의 1차 멤버변수 필드명을 사용한다.(super는 제외함)
		 ********************************************/
//		if(this.fieldNames!=null && !this.fieldNames.trim().isEmpty())
//			setFieldNames(fieldNames.split(","));

		return SvcAreaInfoVO.class;
	}

	@Override
	protected void beforeStep(StepExecution stepExecution) {
		/********************************************
		 * 파라메터를 이용하여 어떤파일로 Export할지 Job실행전에 셋팅한다.
		 ********************************************/
		LOG.debug("FlatFileTestJobWriter.beforeStep");
		String fileNm = "D:\\itcall\\DEV\\workspaceitcall\\itcall\\MY-BOOT-BATCH\\fileFlatTest.tgt";
		LOG.debug("@@ Exporting File {} ", fileNm);
		File file = new File(fileNm);
		if (!file.exists()) {
			LOG.info("@@ Export File Not Found !!!  " + fileNm);
		}
		this.setResource(new FileSystemResource(fileNm));
		this.setEncoding("EUC-KR");
	}

	@Override
	protected void afterStep(StepExecution stepExecution) {
		LOG.debug("FlatFileTestJobWriter.afterStep");
	}

}
