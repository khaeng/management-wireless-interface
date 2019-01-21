package com.itcall.batch.common.support.editor;

import java.beans.PropertyEditorSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itcall.batch.common.support.code.ApiCmdCd;
import com.itcall.batch.common.support.code.ApiTargetCd;

public class ApiParamEnumEditor extends PropertyEditorSupport {

	private static Logger LOG = LoggerFactory.getLogger(ApiParamEnumEditor.class);

	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		try {
			setValue(ApiTargetCd.valueOf(text.toUpperCase()));
			return;
		} catch (IllegalArgumentException e) {
			LOG.debug("Param is not ApiTargetCd.class data: {}", e.getMessage());
		}
		try {
			setValue(ApiCmdCd.valueOf(text.toUpperCase()));
			return;
		} catch (IllegalArgumentException e) {
			LOG.debug("Param is not ApiCmdCd.class data: {}", e.getCause());
		}
		// super.setAsText(text);
		setValue(text);
	}
}
