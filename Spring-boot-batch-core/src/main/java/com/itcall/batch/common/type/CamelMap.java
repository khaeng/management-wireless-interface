package com.itcall.batch.common.type;

import java.util.HashMap;

import com.google.common.base.CaseFormat;

/**
 * Mybatis resultType camelcase HashMap
 * written by chris.
 */
public class CamelMap extends HashMap {

    private String toCamelCase(String s) {
        return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, s.toUpperCase());
    }

    @Override
    public Object put(Object key, Object value) {
        return super.put(toCamelCase((String) key), value.toString());

    }

}
