package com.taobao.pamirs.transaction;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import javax.sql.DataSource;

import org.aopalliance.intercept.MethodInvocation;

/**
 * 
 * @author xuannan
 *
 */
public class TransactionManager{
	
	private static Map<String,DataSource> dataSourceMap;	
	private static int queryTimeOut = -1;
	private static int warnTimeOut = 1000;
	/**
	 * 事务管理器的线程变量，每个线程一个事务管理器
	 */
	private final static ThreadLocal<TBTransactionManager> s_transactionManager = new ThreadLocal<TBTransactionManager>() {
		public TBTransactionManager initialValue() {
			return new TBTransactionManagerImpl(queryTimeOut,warnTimeOut);
		}
	};
	
	public void setQueryTimeOut(int aQueryTimeOut) {
		queryTimeOut = aQueryTimeOut;
	}

	public void setWarnTimeOut(int aWarnTimeOut) {
		warnTimeOut = aWarnTimeOut;
	}


	@SuppressWarnings("static-access")
	public void setDataSourceMap(Map<String, DataSource> dataSourceMap) {
		this.dataSourceMap = dataSourceMap;
	}
	

    protected static DataSource getDataSource(String dataSourceName){
    	return dataSourceMap.get(dataSourceName);
    }
    
    public static Connection getConnection(String dataSourceName) throws SQLException{
    	return getDataSource(dataSourceName).getConnection();
    }
    
    
	public static TBTransactionManager getTransactionManager() {
       return s_transactionManager.get();
	}
	
	
	public static Object executeMethod(TBMethodAction invocation,TBTransactionType transactionType) throws Throwable {
		return TransactionRoundAdvice.invokeInner(invocation, transactionType);
	}

	public static Object executeMethod(MethodInvocation invocation,
			TBTransactionType transactionType) throws Throwable {
		return TransactionRoundAdvice.invokeInner(invocation, transactionType);
	}
    /**
     * 用指定的事务方式来执行一个方法
     * @param aMethod
     * @param aRunObject
     * @param aArguments
     * @param transactionType
     * @return
     * @throws Throwable
     */
	public static Object executeMethod(Method aMethod, Object aRunObject,
			Object[] aArguments, TBTransactionType transactionType)
			throws Throwable {
		return TransactionRoundAdvice.invokeInner(new TBMethodInvocation(
				aMethod, aRunObject, aArguments), transactionType);
	}

	/**
	 * 根据连接获取数据库类型
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	public static String getDataBaseType(Connection conn) throws SQLException {
		String result;
		if (conn instanceof TBConnection) {
			result = ((TBConnection) conn).getDBType();
		} else {
			result = conn.getMetaData().getDatabaseProductName();
			if ("oracle".equalsIgnoreCase(result) == false
					&& "mysql".equalsIgnoreCase(result) == false) {
				throw new SQLException("不支持的数据库类型：" + result);
			}
		}
		return result;
	}
	/**
	 * 获取不同数据库获取系统时间的方法字符串
	 * 
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	public static String getDataBaseSysdateString(Connection conn) throws Exception {
		String type = getDataBaseType(conn);
		if ("oracle".equalsIgnoreCase(type)) {
			return "sysdate";
		} else if ("mysql".equalsIgnoreCase(type)) {
			return "now()";
		} else {
			throw new Exception("不支持的数据库类型：" + type);
		}
	}
}
