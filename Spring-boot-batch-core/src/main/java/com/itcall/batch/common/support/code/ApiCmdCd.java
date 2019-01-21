package com.itcall.batch.common.support.code;

public enum ApiCmdCd {

	LIST,
	GET,
	START,
	STOP,
	CANCLE,
	ADD,
	SAVE,
	REMOVE,
	ALL_VALUES()
	;

//	public static ApiTargetCd valueOf(String value) {
//		return 
//	}

	public static String[] getAllValues() {
		String[] values = new String[values().length];
		for (ApiCmdCd value : values()) {
			values[values.length] = value.name();
		}
		return values;
	}
}
