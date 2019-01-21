package com.itcall.batch.jobs.sample.flatFileTest.reader;

import java.io.File;

import org.springframework.batch.core.StepExecution;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;

import com.itcall.batch.biz.vo.sample.flatFileTest.SvcAreaInfoVO;
import com.itcall.batch.config.batch.item.BaseFlatFileReader;

public class FlatFileTestJobReader extends BaseFlatFileReader<SvcAreaInfoVO> {

	@Value("#{batch['biz.batch.flatFileTestJob.fieldNames']?:'dongCd,addrNoType,startAddrNo,startAddrHo,endAddrNo,endAdddrHo,acptOfcCd,rssFlag'}") 
	private String fieldNames;

	@Override
	protected Class<SvcAreaInfoVO> initMapperRule() {
		/********************************************
		 * 파일의 빈줄을 넘어갈것인지 셋팅한다.
		 * 마지막 빈줄은 원래 넘어간다.(이외 에러남)
		 ********************************************/
		setEmptyLineSkip(true);

		/********************************************
		 * 필드별 구분자를 입력한다. 기본값 콤마(,)
		 ********************************************/
		// setFieldDelimiter("`");

		/********************************************
		 * 필드명을 셋팅한다. 기본값은 VO의 1차 멤버변수 필드명을 사용한다.(super는 제외함)
		 ********************************************/
		if(this.fieldNames!=null && !this.fieldNames.trim().isEmpty())
			setFieldNames(fieldNames.split(","));

		return SvcAreaInfoVO.class;
	}

	@Override
	protected void beforeStep(StepExecution stepExecution) {
		/********************************************
		 * 파라메터를 이용하여 어떤파일에서 Import할지 Job실행전에 셋팅한다.
		 ********************************************/
		LOG.debug("FlatFileTestJobReader.beforeStep");
		String fileNm = "D:\\itcall\\DEV\\workspaceitcall\\itcall\\MY-BOOT-BATCH\\fileFlatTest.src";
		LOG.debug("@@ Importing File {} ", fileNm);
		File file = new File(fileNm);
		if (!file.exists()) {
			LOG.info("@@ Import File Not Found !!!  " + fileNm);
		}
		this.setResource(new FileSystemResource(fileNm));
		this.setEncoding("EUC-KR");
		this.setStrict(false);
		this.setLinesToSkip(0);
	}

	@Override
	protected void afterStep(StepExecution stepExecution) {
		// TODO Auto-generated method stub
		LOG.debug("FlatFileTestJobReader.afterStep");
	}

}
