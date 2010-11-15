package com.taobao.pamirs.transaction;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SqlMonitor {
	private static transient Log log = LogFactory.getLog(SqlMonitor.class);
	private static Method monitorThreadLocalPeepCaller;
	private static Object[] monitorObjects;
	private static Method[] methods;

	public static String peepParent() {
		if (monitorThreadLocalPeepCaller != null) {
			try {
				return (String) monitorThreadLocalPeepCaller.invoke(null,
						new Object[0]);
			} catch (Exception e) {
                return null;
			}
		} else {
			return null;
		}
	}
	public static void monitorSQL(String statement, long runTime,
			long finishTime, List<Object> parameter, int executeNum){
		String  parent = peepParent();
		if (log.isDebugEnabled()) {
			StringBuilder builder = new StringBuilder();
			builder.append(Thread.currentThread() + "执行SQL " + ((finishTime - runTime)/1000) +" 毫秒 :" + statement);
			if (parameter != null) {
				for (int i = 0; i < parameter.size(); i++) {
					builder.append(" ").append(
							"P" + (i + 1) + "{" + parameter.get(i) + "}");
				}
			}
			log.debug(builder.toString());
		}
		if (monitorObjects != null) {
			try{
			for (int i =0;i<monitorObjects.length;i++ ) {
				if(parameter != null){
				methods[i].invoke(monitorObjects[i],new Object[]{statement,"SQL", runTime, finishTime, parameter.toArray(),
						executeNum,parent});
				}else{
					methods[i].invoke(monitorObjects[i],new Object[]{statement,"SQL", runTime, finishTime,new Object[0],
							executeNum,parent});					
				}
			}
			}catch(Exception e){
				throw new RuntimeException(e);
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
							"monitor",
							new Class[] { String.class,String.class,long.class, long.class,
									Object[].class, int.class,String.class });
		}
		try{
			Class monitorThreadLocal =Class.forName("com.taobao.pamirs.stat.MonitorThreadLocal");
			monitorThreadLocalPeepCaller =monitorThreadLocal.getDeclaredMethod("peepCaller",new Class[0]);
		}catch(Exception e){
			log.warn(e.getMessage() +" 不能进行调用堆栈相关的细化分析");
		}
	}
}
