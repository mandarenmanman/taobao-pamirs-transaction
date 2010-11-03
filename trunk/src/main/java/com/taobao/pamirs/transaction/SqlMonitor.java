package com.taobao.pamirs.transaction;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SqlMonitor {
	private static transient Log log = LogFactory
			.getLog(SqlMonitor.class);
    static List<IMonitor> monitors;
     
	public static void monitorSQL(String statement, long runTime,
			long finishTime, List<Object> parameter, int executeNum) {
		if (log.isDebugEnabled()) {
			StringBuilder builder = new StringBuilder();
			builder.append(Thread.currentThread() + "Ö´ÐÐSQL :" + statement);
			if(parameter != null){
				for(int i=0;i<parameter.size();i++){
					builder.append(" ").append("P" + (i+1) +"{" + parameter.get(i)+"}");
				}
			}			
			log.debug(builder.toString());
		}
		if(monitors!=null){
			for(IMonitor monitor:monitors){
				monitor.monitor(statement, runTime, finishTime, parameter, executeNum);
			}
		}
	}

	public void setMonitors(List<IMonitor> aMonitors) {
		monitors = aMonitors;
	}

}
