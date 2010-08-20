package com.taobao.pamirs.transaction;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SqlMonitor {
	private static transient Log log = LogFactory
			.getLog(SqlMonitor.class);

	@SuppressWarnings({ "rawtypes" })
	public static void monitorSQL(String statement, long runTime,
			long finishTime, List parameter, int executeNum) {
		if (log.isDebugEnabled()) {
			log.debug(Thread.currentThread() + "Ö´ÐÐSQL :" + statement);
		}
	}

}
