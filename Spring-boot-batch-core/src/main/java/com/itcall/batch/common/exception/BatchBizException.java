package com.itcall.batch.common.exception;

public class BatchBizException extends BaseException {
	private static final long serialVersionUID = 1L;

	public BatchBizException(Exception cause) {
		super(cause);
		LOG.error("{}",cause);
	}

	public BatchBizException(String message) {
		super(message);
		LOG.error("BatchBizException[{}]",message);
	}

	public BatchBizException(String message, Throwable cause) {
		super(message, cause);
		LOG.error("BatchBizException[{}]\n{}",message,cause);
	}

}
