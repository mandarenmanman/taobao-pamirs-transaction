package com.taobao.pamirs.transaction;

import java.util.List;

public interface IMonitor {
	/**
	 * 
	 * @param statement
	 * @param runTime
	 * @param finishTime
	 * @param parameter
	 * @param executeNum
	 */
	public void monitor(String statement, long runTime,
			long finishTime, List<Object> parameter, int executeNum);
	/**
	 * 
	 * @param statement
	 * @param runTime
	 * @param finishTime
	 * @param parameter
	 * @param executeNum
	 */
	public void monitor(String statement, long runTime,
			long finishTime, Object[] parameter, int executeNum);
}
