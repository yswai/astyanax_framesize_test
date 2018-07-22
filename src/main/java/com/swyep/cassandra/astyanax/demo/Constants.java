package com.swyep.cassandra.astyanax.demo;

import org.apache.commons.lang3.StringUtils;

public interface Constants {

    String UUID_END = StringUtils.repeat("~", 32);

    String COUNTRY_END = "~~~";

    String TIMESTAMP_END = Long.valueOf(Long.MAX_VALUE).toString();

}
