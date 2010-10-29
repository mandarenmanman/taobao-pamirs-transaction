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
	
	/**
	 * 事务管理器的线程变量，每个线程一个事务管理器
	 */
	private final static ThreadLocal<TBTransactionManager> s_transactionManager = new ThreadLocal<TBTransactionManager>() {
		public TBTransactionManager initialValue() {
			return new TBTransactionManagerImpl();
		}
	};
	
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
}
