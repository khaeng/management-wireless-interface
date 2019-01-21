package com.itcall.batch.config.batch;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;

public enum JobParamCode {

	FIRST(1),
	SECOND(2),
	THIRD(3),
	FOURTH(4),
	FIFTH(5),
	SIXTH(6),
	SEVENTH(7),
	EIGHTH(8),
	NINTH(9),
	TENTH(10)
	;
	
	private int value;
	private boolean required;
	
	private JobParamCode(int value) {
		this.value = value;
	}

	public static JobParamCode valueOf(int value) {
		JobParamCode[] values = values();
		for (JobParamCode jobParamCode : values) {
			if(jobParamCode.getValue()==value)
				return jobParamCode;
		}
		throw new IllegalArgumentException("Do not matched Job Parameter key name.");
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public String getParameter(JobParameters params) {
		return params.getString(this.name());
	}

	public static String[] getParameters(JobParameters params) {
		String[] values = new String[values().length];
		for (int i = 0; i < values.length; i++) {
			if((values[i] = valueOf(i+1).getParameter(params))==null)
				return Arrays.copyOfRange(values, 0, i);
		}
		throw new ArrayIndexOutOfBoundsException("Do not support Job Parameter length more then 10");
	}

	public static JobParameters makeJobParameters(String[] values) {
		Map<String, JobParameter> paramMap = new HashMap<String, JobParameter>();
		paramMap.put("JobId", new JobParameter(String.valueOf(System.currentTimeMillis())));
		try {
			if(values==null) {
				return new JobParameters();
			}
			for (int i = 0; i < values.length; i++)
				paramMap.put(valueOf(i+1).name(), new JobParameter(values[i]));
			return new JobParameters(paramMap);
		}catch (Exception e) {
			throw new IllegalArgumentException("Do not support Job Parameter length more then 10");
		}
	}
}
