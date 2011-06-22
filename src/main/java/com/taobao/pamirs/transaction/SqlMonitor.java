package com.taobao.pamirs.transaction;


import java.lang.reflect.Method;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SqlMonitor {
	private static transient Log log = LogFactory.getLog(SqlMonitor.class);
	private static Object[] monitorObjects;
	private static Method[] methods;
	private static boolean isFirstOutputErrorMessage = true;
	public static void monitorSQL(String statement, long runTime,
			long finishTime, List<Object> parameter, int executeNum){
		try {
			if (log.isDebugEnabled()) {
				StringBuilder builder = new StringBuilder();
				builder.append(Thread.currentThread() + "执行SQL "
						+ ((finishTime - runTime) / 1000) + " 微秒 :" + statement);
				if (parameter != null) {
					for (int i = 0; i < parameter.size(); i++) {
						builder.append(" ").append(
								"P" + (i + 1) + "{" + parameter.get(i) + "}");
					}
				}
				log.debug(builder.toString());
			}
			if (monitorObjects != null) {
				for (int i = 0; i < monitorObjects.length; i++) {
					if (parameter != null) {
						methods[i].invoke(monitorObjects[i], new Object[] {
								statement, "SQL", runTime, finishTime,
								parameter.toArray(),null,null, executeNum, null });
					} else {
						methods[i].invoke(monitorObjects[i], new Object[] {
								statement, "SQL", runTime, finishTime,
								new Object[0],null,null, executeNum, null });
					}
				}
			}
		} catch (Throwable e) {
			if (isFirstOutputErrorMessage == true) {
				log.error("日志信息输出失败" + e);
				isFirstOutputErrorMessage = false;
			}
		}
	}

	public void setMonitors(List<Object> aMonitors) throws SecurityException, NoSuchMethodException {
		methods = new Method[aMonitors.size()];
		monitorObjects = new Object[aMonitors.size()];
		for (int i = 0; i < aMonitors.size(); i++) {
			monitorObjects[i] = aMonitors.get(i);
			methods[i] = monitorObjects[i].getClass()
					.getMethod(
							"monitorAfter",
							new Class[] { String.class,String.class,long.class, long.class,
									Object[].class,Object.class,Throwable.class,int.class,String.class });
		}
	}
}
