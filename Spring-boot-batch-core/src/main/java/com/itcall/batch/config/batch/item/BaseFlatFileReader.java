package com.itcall.batch.config.batch.item;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.separator.RecordSeparatorPolicy;
import org.springframework.batch.item.file.separator.SimpleRecordSeparatorPolicy;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;

import com.itcall.batch.common.service.batchInfo.BatchInfoService;
import com.itcall.batch.common.vo.batchInfo.BatchInfoHstVo;
import com.itcall.batch.config.batch.JobParamCode;

public abstract class BaseFlatFileReader<I> extends FlatFileItemReader<I> {

	protected Logger LOG = LoggerFactory.getLogger(getClass());

	private StepExecution stepExecution;
	private String[] jobParameters;
	@Resource
	private BatchInfoService batchInfoService;

	private String fieldDelimiter;

	private String[] fieldNames;

	private boolean isEmptyLineSkip;

	private boolean isBizMsgWrited;

	private org.springframework.core.io.Resource resource;

	protected abstract Class<I> initMapperRule();
	protected abstract void beforeStep(StepExecution stepExecution);
	protected abstract void afterStep(StepExecution stepExecution);

	@PostConstruct
	public void initialize() {
		final Class<I> clazz = initMapperRule();
		setLineMapper(
				new DefaultLineMapper<I>() {{
					setLineTokenizer(new DelimitedLineTokenizer() {{
							if(fieldDelimiter!=null)
								setDelimiter(fieldDelimiter);
							if(fieldNames!=null) {
								setNames(fieldNames);
							}else {
								List<Field> fieldList = new ArrayList<Field>();
								fieldList.addAll(Arrays.asList(clazz.getDeclaredFields()));
								// 선언한 VO의 멤버변수만 취한다. (상속은 어찌될지 모르니...)
//								Class<?> superClass = clazz.getSuperclass();
//								if(superClass!=null) {
//									fieldList.addAll(Arrays.asList(superClass.getDeclaredFields()));
//								}
								StringBuffer sb = new StringBuffer();
								for (Field field : fieldList) {
									if(!field.toString().contains("static") && !field.toString().contains("final"))
										sb.append(sb.length()==0 ? field.getName() : "," + field.getName());
								}
								setNames(fieldNames = sb.toString().split(","));
							}
						}});
					setFieldSetMapper(new BeanWrapperFieldSetMapper<I>() {{
							setTargetType(clazz);
						}});
				}}
				);
		if(this.isEmptyLineSkip) {
			setRecordSeparatorPolicy(getEmptyLineSkipPolicy());
		}
	}

	private RecordSeparatorPolicy getEmptyLineSkipPolicy() {
		SimpleRecordSeparatorPolicy policy = new SimpleRecordSeparatorPolicy() {
			@Override
			public boolean isEndOfRecord(String line) {
				if(StringUtils.isEmpty(line)) {
					return false;
				}
				return line.trim().length() != 0 && super.isEndOfRecord(line);
			}
			@Override
			public String postProcess(String record) {
				if(record == null || record.trim().length() == 0)
					LOG.warn("Read file line[{}] is empty then pass...", getCurrentItemCount());
				return (record == null || record.trim().length() == 0) ? null : super.postProcess(record);
			}
		};
		return policy;
	}

	@BeforeStep
	public void readerBeforeStep(StepExecution stepExecution) {
		this.stepExecution = stepExecution;
		String[] params = JobParamCode.getParameters(stepExecution.getJobParameters());
		this.jobParameters = params;
		this.isBizMsgWrited = false;
		beforeStep(stepExecution);
	}

	@AfterStep
	public void readerAfterStep(StepExecution stepExecution) {
		afterStep(stepExecution);
	}

	@Override
	public void setResource(org.springframework.core.io.Resource resource) {
		super.setResource(this.resource = resource);
	}

	@Override
	public void close() {
		try {
			// 2018.12.05 읽은것이 없을때도 메시지를 써주는것으로 요청되어 처리함. if(!this.isBizMsgWrited && this.resource!=null && this.stepExecution!=null && this.stepExecution.getReadCount()>0)
			if(!this.isBizMsgWrited && this.stepExecution!=null) {
				addBizMsg(this.stepExecution.getReadCount(), new StringBuffer().append("ReadCount[").append(this.stepExecution.getReadCount()).append("], WriteCount[").append(this.stepExecution.getWriteCount()).append("], SkipRead[").append(this.stepExecution.getReadSkipCount()).append("], SkipWrite[").append(this.stepExecution.getWriteSkipCount()).append("]:" ).append(this.resource).toString());
			}
		} catch (Exception e) {
			LOG.error("addBizMsg add error : {}", e);
		}
		super.close();
	}

	protected void addBizMsg(int resultCnt, String resultMsg) throws Exception {
		this.isBizMsgWrited = true;
		JobExecution jobExecution = this.stepExecution.getJobExecution();
		BatchInfoHstVo batchInfoHst = new BatchInfoHstVo(jobExecution);
		batchInfoHst.setRestCnt(resultCnt);
		batchInfoHst.setRestMsg(resultMsg);
		batchInfoService.setBatchInfoHst(batchInfoHst);
	}

	public StepExecution getStepExecution() {
		return stepExecution;
	}

	public String[] getJobParameters() {
		return jobParameters;
	}

	public void setJobParameters(String[] jobParameters) {
		this.jobParameters = jobParameters;
	}

	protected void setFieldDelimiter(String fieldDelimiter) {
		this.fieldDelimiter = fieldDelimiter;
	}

	protected void setFieldNames(String[] fieldNames) {
		this.fieldNames = fieldNames;
	}

	public void setEmptyLineSkip(boolean isEmptyLineSkip) {
		this.isEmptyLineSkip = isEmptyLineSkip;
	}

}
