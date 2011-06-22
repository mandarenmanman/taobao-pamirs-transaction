package com.taobao.pamirs.transaction;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;


/**
 * 在开发环境，自动记录所有的SQL,注意剔重
 * 
 * @author xuannan
 * 
 */
public class SqlCheckMonitor implements InitializingBean{
	private static transient Log log = LogFactory.getLog(SqlCheckMonitor.class);
	public static String RUNMODE_WRITE ="开发";
	public static String RUNMODE_CHECK ="日常";
	public static String RUNMODE_PRODUCT ="生产";
	
	private DataSource dataSource;
	/**
	 * 产品线名称，例如：汇金
	 */
	private String productName;
	/**
	 * 项目或者日常名称。例如：道易，统一帐户，数据魔方
	 */
	private String projectName;
	
	/**
	 * 运行环境：开发，日常
	 */
	private String runMode; 
	
	/*
	 * 记录每个SQL是否被审核通过
	 */
	ConcurrentHashMap<String, Boolean> sqlMap = new ConcurrentHashMap<String, Boolean>();

	/**
	 * 在系统初始化的时候，装载系统的所有SQL
	 * @throws Throwable 
	 */
	public void afterPropertiesSet() throws Exception{
			Method method = SqlCheckMonitor.class.getDeclaredMethod("loadData",
					new Class[] {});
			TBMethodInvocation invocation = new TBMethodInvocation(method,
					this, null);
			try {
				TransactionManager.executeMethod(invocation, TBTransactionType.INDEPEND);
			} catch (Throwable e) {
				 throw new Exception(e);
			}
	}

	public void monitorBefore(String sqlText,String type,String parent){
		//
	}
	
	public void monitorAfter(String sqlText,String type, long runTime,
			long finishTime, Object[] parameter,Object returnValue,Throwable error, int executeNum,String parent){
		if(RUNMODE_WRITE.equals(this.runMode)){
			writeRuningSql(sqlText);
		}else if(RUNMODE_CHECK.equals(this.runMode)){
			checkSql(sqlText);
		}else if(RUNMODE_PRODUCT.equals(this.runMode)){
			//生产模式
		}else{
			throw new RuntimeException("请设置SQL的运行环境：\"开发\" 或者 \"日常\" 或者 \"生产\" ");
		}
	}
    public void checkSql(String sqlText){
    	Boolean isCheckOk = this.sqlMap.get(sqlText);
    	if(isCheckOk == null || isCheckOk.booleanValue() == false){
    		throw new RuntimeException("SQL没有通过审核，不能在测试环境运行，请联系 玄风: SQL= " + sqlText);
    	}
    }
	public void writeRuningSql(String sqlText){
		try {
			//此处可能存在并发重入的问题，但不影响系统目标的达成
			if (sqlMap.containsKey(sqlText) == false) {
				
				Method method = SqlCheckMonitor.class.getDeclaredMethod("save2DB",
						new Class[] {String.class,String.class});
				TBMethodInvocation invocation = new TBMethodInvocation(method,
						this, new Object[]{this.projectName, sqlText});
				TransactionManager.executeMethod(invocation, TBTransactionType.INDEPEND);
				
				sqlMap.put(sqlText, false);
			}
		} catch (Throwable e) {
			log.error(e.getMessage(),e);
		}
	}
	/**
	 * 从数据库中装载数据
	 * @throws SQLException
	 */
	public void loadData() throws SQLException{
		Connection conn = null;
		try {
			conn = dataSource.getConnection();
			String sql = "select ID,PRODUCT_NAME,PROJECT_NAME,SQL_TEXT,CHECK_OK  from PAMIRS_SQL_CHECK "
					+ " WHERE PRODUCT_NAME = ? ";
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, productName);
			ResultSet rs = statement.executeQuery();
			while(rs.next()){
				sqlMap.put(rs.getString("SQL_TEXT"),rs.getInt("CHECK_OK") == 1);
			}
			rs.close();
			statement.close();
		} catch (Throwable e) {
			log.error("装载数据错误，"+ e.getMessage(),e);
		} finally {
			if (conn != null) {
				conn.close();
			}
		}		
	}
	/**
	 * 持久化到数据库中
	 * 
	 * @param sql
	 * @throws SQLException
	 */
	public void save2DB(String projectName, String sqlText) throws SQLException {
		Connection conn = null;
		try {
			conn = dataSource.getConnection();
			String sysdateStr = TransactionManager.getDataBaseSysdateString(conn);
			String sql = "insert into PAMIRS_SQL_CHECK("
					+ "ID,PRODUCT_NAME,PROJECT_NAME,SQL_TEXT,CHECK_OK,GMT_CREATE,GMT_MODIFIED)"
					+ "VALUES(?,?,?,?,0," + sysdateStr + "," + sysdateStr + ")";
			if(sql.equals(sqlText)){
				return;
			}
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setLong(1, Math.abs(sqlText.hashCode()));
			statement.setString(2, productName);
			statement.setString(3, projectName);
			statement.setString(4, sqlText);
			statement.execute();
			statement.close();
			conn.commit();
		} catch (Throwable e) {
			//可能别的服务器已经提交了相同的SQL语句
			conn.rollback();
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}

	public void setDataSource(DataSource aDataSource) {
		dataSource = aDataSource;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	public void setProductName(String productName) {
		this.productName = productName;
	}

	public void setRunMode(String runMode) {
		this.runMode = runMode;
	}

	
}
