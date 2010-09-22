package com.taobao.pamirs.transaction;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 系统时间管理，为了避免主机间时间不一致导致的问题，统一获取数据库时间
 * 
 * @author xuannan
 * 
 */
public class TBSysdateManager {
	private static transient Log log = LogFactory
			.getLog(TBSysdateManager.class);
	/**
	 * 系统加载时候的数据库时间
	 */
	private static long initialDataBaseTime = -2;
	/**
	 * 系统加载时候的当前主机时间
	 */
	private static long initialLocalServerBaseTime = -1;

	private static DataSource dataSource;

	public void setDataSource(DataSource aDataSource) {
		dataSource = aDataSource;
	}

	public static long getCurrentTimeMillis() {
		 if(initialDataBaseTime == -2){
			 initial();
		 }
		if (initialDataBaseTime > 0) {
			return System.currentTimeMillis() - initialLocalServerBaseTime
					+ initialDataBaseTime;
		} else {
			// 没有在Spring中配置相关信息，只能取当前主机时间
			return System.currentTimeMillis();
		}
	}

	private static void initial() {
		try {
			Method method = TBSysdateManager.class.getDeclaredMethod("initialInner",
					new Class[] {});
			TBMethodInvocation invocation = new TBMethodInvocation(method,
					null, null);
			TransactionManager.executeMethod(invocation, TBTransactionType.JOIN);
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
		}

	}
	
	public static void initialInner(){
		if (dataSource == null) {
			initialDataBaseTime = -1;
			return;
		}
		Connection conn = null;
		try {
			conn = dataSource.getConnection();
			String sql = "";
			String dbType = conn.getMetaData().getDatabaseProductName();
			if ("oracle".equalsIgnoreCase(dbType)) {
				sql = "select sysdate from dual";
			} else if ("mysql".equalsIgnoreCase(dbType)) {
				sql = "select now()";
			}
			Statement statement = conn.createStatement();
			// 同步初始化本地服务器的时间基线
			initialLocalServerBaseTime = System.currentTimeMillis();
			ResultSet resultSet = statement.executeQuery(sql);
			if (resultSet.next()) {
				initialDataBaseTime = resultSet.getTimestamp(1).getTime();
			} else {
				throw new SQLException("取系统时间错误");
			}
			resultSet.close();
			statement.close();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					log.error(e.getMessage(), e);
				}
			}
		}
		log.info("初始化系统时间成功,数据库初始时间：" + initialDataBaseTime + " 当前主机时间：" + initialLocalServerBaseTime);
	}
}
