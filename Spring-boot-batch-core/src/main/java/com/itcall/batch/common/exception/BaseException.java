package com.itcall.batch.common.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	protected Logger LOG = LoggerFactory.getLogger(this.getClass());

	public BaseException(Exception cause) {
		super(cause);
	}

	public BaseException(String message) {
		super(message);
	}

	public BaseException(String message, Throwable cause) {
		super(message, cause);
	}
}
