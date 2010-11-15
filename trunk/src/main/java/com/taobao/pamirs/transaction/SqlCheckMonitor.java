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
 * �ڿ����������Զ���¼���е�SQL,ע������
 * 
 * @author xuannan
 * 
 */
public class SqlCheckMonitor implements InitializingBean{
	private static transient Log log = LogFactory.getLog(SqlCheckMonitor.class);
	public static String RUNMODE_WRITE ="����";
	public static String RUNMODE_CHECK ="�ճ�";
	public static String RUNMODE_PRODUCT ="����";
	
	private DataSource dataSource;
	/**
	 * ��Ʒ�����ƣ����磺���
	 */
	private String productName;
	/**
	 * ��Ŀ�����ճ����ơ����磺���ף�ͳһ�ʻ�������ħ��
	 */
	private String projectName;
	
	/**
	 * ���л������������ճ�
	 */
	private String runMode; 
	
	/*
	 * ��¼ÿ��SQL�Ƿ����ͨ��
	 */
	ConcurrentHashMap<String, Boolean> sqlMap = new ConcurrentHashMap<String, Boolean>();

	/**
	 * ��ϵͳ��ʼ����ʱ��װ��ϵͳ������SQL
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
	
	public void monitor(String sqlText,String type, long runTime,
			long finishTime, Object[] parameter, int executeNum,String parent){
		if(RUNMODE_WRITE.equals(this.runMode)){
			writeRuningSql(sqlText);
		}else if(RUNMODE_CHECK.equals(this.runMode)){
			checkSql(sqlText);
		}else if(RUNMODE_PRODUCT.equals(this.runMode)){
			//����ģʽ
		}else{
			throw new RuntimeException("������SQL�����л�����\"����\" ���� \"�ճ�\" ���� \"����\" ");
		}
	}
    public void checkSql(String sqlText){
    	Boolean isCheckOk = this.sqlMap.get(sqlText);
    	if(isCheckOk == null || isCheckOk.booleanValue() == false){
    		throw new RuntimeException("SQLû��ͨ����ˣ������ڲ��Ի������У�����ϵ ����: SQL= " + sqlText);
    	}
    }
	public void writeRuningSql(String sqlText){
		try {
			//�˴����ܴ��ڲ�����������⣬����Ӱ��ϵͳĿ��Ĵ��
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
	 * �����ݿ���װ������
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
			log.error("װ�����ݴ���"+ e.getMessage(),e);
		} finally {
			if (conn != null) {
				conn.close();
			}
		}		
	}
	/**
	 * �־û������ݿ���
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
			log.error("���ܱ�ķ������Ѿ��ύ����ͬ��SQL���:" + sqlText);
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
