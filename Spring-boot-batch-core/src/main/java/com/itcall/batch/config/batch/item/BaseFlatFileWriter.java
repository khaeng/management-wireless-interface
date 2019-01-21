package com.itcall.batch.config.batch.item;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;

import com.itcall.batch.common.service.batchInfo.BatchInfoService;
import com.itcall.batch.config.batch.JobParamCode;

public abstract class BaseFlatFileWriter<O> extends FlatFileItemWriter<O> {
	protected Logger LOG = LoggerFactory.getLogger(getClass());

	private StepExecution stepExecution;
	private String[] jobParameters;
	@Resource
	private BatchInfoService batchInfoService;

	private String fieldDelimiter;

	private String[] fieldNames;

	protected abstract Class<O> initMapperRule();
	protected abstract void beforeStep(StepExecution stepExecution);
	protected abstract void afterStep(StepExecution stepExecution);

	@PostConstruct
	public void initialize() {
		final Class<O> clazz = initMapperRule();
		setLineAggregator(
				new DelimitedLineAggregator<O>() {{
					if(fieldDelimiter!=null)
						setDelimiter(fieldDelimiter);
					setFieldExtractor(new BeanWrapperFieldExtractor<O>() {{
						if(fieldNames!=null) {
							setNames(fieldNames);
						}else {
							List<Field> fieldList = new ArrayList<Field>();
							fieldList.addAll(Arrays.asList(clazz.getDeclaredFields()));
							// 선언한 VO의 멤버변수만 취한다. (상속은 어찌될지 모르니...)
//							Class<?> superClass = clazz.getSuperclass();
//							if(superClass!=null) {
//								fieldList.addAll(Arrays.asList(superClass.getDeclaredFields()));
//							}
							StringBuffer sb = new StringBuffer();
							for (Field field : fieldList) {
								if(!field.toString().contains("static") && !field.toString().contains("final"))
									sb.append(sb.length()==0 ? field.getName() : "," + field.getName());
							}
							setNames(fieldNames = sb.toString().split(","));
						}
					}});
				}});
	}

	@BeforeStep
	public void readerBeforeStep(StepExecution stepExecution) {
		this.stepExecution = stepExecution;
		String[] params = JobParamCode.getParameters(stepExecution.getJobParameters());
		this.jobParameters = params;
		beforeStep(stepExecution);
	}

	@AfterStep
	public void readerAfterStep(StepExecution stepExecution) {
		afterStep(stepExecution);
	}

	protected void setFieldDelimiter(String fieldDelimiter) {
		this.fieldDelimiter = fieldDelimiter;
	}

	protected void setFieldNames(String[] fieldNames) {
		this.fieldNames = fieldNames;
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

}
