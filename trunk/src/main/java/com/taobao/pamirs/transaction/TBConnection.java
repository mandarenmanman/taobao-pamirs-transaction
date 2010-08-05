package com.taobao.pamirs.transaction;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * ���ݿ����ӵ�һ����װ�� ��Ҫ����������������close,commit,rollback���������������һ������ close,commit�������
 * rollback����������ֻ�ܱ��ع�
 * 
 * @author xuannan
 * 
 */
public class TBConnection implements java.sql.Connection {
	private static transient Log	log					= LogFactory.getLog(TBConnection.class);
	private static String			S_SESSION_QUERY		= "SELECT to_number(substr(dbms_session.unique_session_id,1,4),'xxxx') FROM dual";
	private String					validateSql4Mysql	= "SELECT 1+1";
	private static boolean			isSetConnectionInfo	= true;
	private String					dbType				= "";
	private Connection				m_conn;
	private String					sessionId;
	private TBTransactionImpl		m_session;
	private Exception				m_addr;
	private long					m_openTime;
	private int						m_queryTimeOut		= 0;
	private boolean					hasDDLOperator		= false;
	private String					dataSourceName;
	private List<Statement>			m_statements		= new ArrayList<Statement>();

	private TBConnection(String aDataSourceName, Connection conn, TBTransactionImpl session, int aQueryTimeOut)
			throws java.sql.SQLException {

		this.m_conn = conn;
		this.hasDDLOperator = false;
		this.dataSourceName = aDataSourceName;
		this.m_session = session;
		this.m_queryTimeOut = aQueryTimeOut;
		this.m_openTime = System.currentTimeMillis();
		this.m_addr = new Exception();
		this.dbType = this.m_conn.getMetaData().getDatabaseProductName();
		if (isSetConnectionInfo == true) {
			this.sessionId = this.queryDBSessionID();
			// ���õ�ǰ���ӵķ�������Ϣ �� ���ӷ����ĵ�ַ��Ϣ
			setDBMSConnectionInfo();
		} else {
			this.sessionId = "û�д�����״̬��鿪��";
		}
	}

	public static TBConnection wrap(String aDataSourceName, java.sql.Connection conn, TBTransactionImpl session,
			int aQueryTimeOut) throws java.sql.SQLException {
		return new TBConnection(aDataSourceName, conn, session, aQueryTimeOut);
	}

	public static TBConnection wrap(String aDataSourceName, java.sql.Connection conn, int aQueryTimeOut)
			throws java.sql.SQLException {
		return new TBConnection(aDataSourceName, conn, null, aQueryTimeOut);
	}

	public String toString() {
		if ("oracle".equals(dbType.toLowerCase())) {
			return this.getClass() + "@" + this.hashCode() + ":SESSION_ID=" + this.sessionId + ":" + this.m_conn;
		} else {
			return this.getClass() + "@" + this.hashCode() + ":" + this.m_conn;
		}
	}

	// ���õ�ǰ���ӵ�ģ�顢action��Ϣ��������ó�����Ӱ��ϵͳ��������
	private void setDBMSConnectionInfo() {
		// ���������������Ϣ���򷵻�
		if (isSetConnectionInfo == false) {
			return;
		}
		try {
			// ���õ�ǰ���ӵķ�������Ϣ �� ���ӷ����ĵ�ַ��Ϣ
			String moduleName = "HJ"; //��ȡϵͳ��Ϣ�������ݿ����ÿͻ�����Ϣ���������ݿ�˵�ϵͳ���
			String actionName = "Web";

			// �������oracle���ݿ⣬������
			if ("oracle".equals(dbType.toLowerCase())) {
				String sql = "call DBMS_APPLICATION_INFO.SET_MODULE (?,?)";

				// ע�⣬������������غ�ķ�����������hasDDLOperator�ı�Ϊtrue��
				PreparedStatement stmt = this.m_conn.prepareStatement(sql);
				stmt.setString(1, moduleName);
				stmt.setString(2, actionName);

				stmt.execute();
				stmt.close();
			}
		} catch (Exception e) {
			log.error("����������Ϣ������Ӱ��ϵͳ��������", e);
		}

	}

	public static String debuger(TBConnection conn) {
		StringBuffer sb = new StringBuffer();
		String startTime = "��ʼʱ��";
		String spendTime = "����ռ��ʱ��";
		String connHashCode = "���ӵ�HashCodes";
		String connAddr = "���ӷ����ĵ�ַ";
		sb.append(startTime + ":" + new java.sql.Date(conn.m_openTime) + "\n");
		sb.append(spendTime + ":" + (System.currentTimeMillis() - conn.m_openTime) + "\n");
		sb.append(connHashCode + ":" + conn.hashCode() + "\n");
		sb.append(connAddr + ":" + getCallPath(conn.m_addr) + "\n");
		return sb.toString();
	}

	public static String getCallPath(Throwable e) {
		StringBuffer sb = new StringBuffer();
		StackTraceElement stack[] = e.getStackTrace();
		for (int i = 0; i < stack.length; i++) {
			if (stack[i].getClassName().indexOf("$jsp") >= 0 || stack[i].getClassName().indexOf(".dbconnmanager.") < 0
					&& stack[i].getClassName().indexOf("org.apache.") < 0
					&& stack[i].getClassName().indexOf("com.ai.appframe2.bo.") < 0
					&& stack[i].getClassName().indexOf("java.") < 0 && stack[i].getClassName().indexOf("javax.") < 0) {
				String lineNumber = "����";
				sb.append(stack[i].getClassName() + "." + stack[i].getMethodName() + "() " + lineNumber + ":"
						+ stack[i].getLineNumber() + "\n");
			}
		}
		return sb.toString();
	}

	/**
	 * У�鵱ǰ�����Ƿ�Ͽ���ͨ�������ݿ�ִ��һ�������У�����ӿ�����. oracle�����ݿ�ͨ����
	 * 
	 * @throws Exception
	 */
	public void judgeConnAvailable() throws Exception {

		// �����oracle���ݿ⣬������������session����Ϣ������
		if ("oracle".equals(dbType.toLowerCase())) {
			String sql = "call DBMS_APPLICATION_INFO.SET_MODULE (?,?)";
			PreparedStatement stmt = this.m_conn.prepareStatement(sql);
			stmt.setString(1, "");
			stmt.setString(2, "");
			stmt.execute();
			stmt.close();
		} else if ("mysql".equals(dbType.toLowerCase())) {
			// ע�⣬������������غ�ķ�����������hasDDLOperator�ı�Ϊtrue��
			PreparedStatement stmt = this.m_conn.prepareStatement(validateSql4Mysql);

			stmt.execute();
			stmt.close();

		} else {
			throw new Exception("���ṩ�������ݿ��У�鷽ʽ");
		}
	}

	/**
	 * ��Statement��ִ�����ݲ����ຯ����ʱ������
	 */
	public void setHasDDLOperator() {
		this.hasDDLOperator = true;
	}

	/**
	 * ��Statement�Ĺ��캯���е��ã����������ӹرյ�ʱ��ر����е��α�
	 * 
	 * @param statement
	 */
	public void addStatement(Statement statement) {
		this.m_statements.add(statement);
	}

	/**
	 * ��Statemt�رյ�ʱ��������а��Ƴ��Լ�
	 * 
	 * @param statement
	 */
	public void removeStatement(Statement statement) {
		this.m_statements.remove(statement);
	}

	public void clearWarnings() throws SQLException {
		m_conn.clearWarnings();
	}

	/**
	 * ��������Ѿ�������������ֱ�ӻع�
	 */
	public void commit() throws SQLException {
		if (m_session == null) {
			this.realCommit();
		}
	}

	/**
	 * ��������Ѿ�������������ֱ�ӻع���ֻ����������Ϊֻ�ܻع�
	 */
	public void rollback() throws SQLException {
		if (m_session == null) {
			this.realRollback();
		} else {
			this.m_session.setRollbackOnly();
		}
	}

	/**
	 * �������û�м�������ֱ�ӹر� �������ִֻ���˲�ѯ�������رգ����Ҵ����񻷾����Ƴ��Լ� �����ܹرգ�������������н���ͳһ����
	 */
	public void close() throws SQLException {
		/**
		 * �ر����п��������ڳ�����д���淶���µ�δ�ر��α�
		 */
		Statement[] tmpList = m_statements.toArray(new Statement[0]);
		for (Statement item : tmpList) {
			item.close();
		}
		this.m_statements.clear();

		if (log.isDebugEnabled()) {
			log.debug("clean Statements:SESSION_ID=" + this.sessionId + ":" + this.m_conn);
		}

		if (m_session == null) {// û�м�������
			this.realClose();
		} else if (this.hasDDLOperator == false) {// û�н��������޸Ĳ���
			if (log.isDebugEnabled()) {
				log.debug("��Ϊ����û��ִ���κ������޸ĵĲ�������Ȼ����������Ҳֱ�ӹر�");
			}
			// һ��Ҫ�����񻷾����������
			this.m_session.removeConnection(this.dataSourceName);
			this.realClose();
		} else {
			if (log.isDebugEnabled()) {
				log.debug("�Ѿ�ִ���������޸Ĳ����ļ�������conneciton���������ύ��ʱ���ִ�йرղ���");
			}
		}
	}

	public void realCommit() throws SQLException {
		if(this.m_session == null && this.hasDDLOperator == true){
			throw new SQLException("û�м�����������Ӳ���ִ�������޸Ĳ���");
		}
		m_conn.commit();
		if (log.isDebugEnabled()) {
			log.debug("commit Connection:SESSION_ID=" + this.sessionId + ":" + this.m_conn);
		}
	}

	public void realRollback() throws SQLException {
		m_conn.rollback();
		if (log.isDebugEnabled()) {
			log.debug("rollback Connection:SESSION_ID=" + this.sessionId + ":" + this.m_conn);
		}
	}

	public void realClose() throws SQLException {
		this.m_session = null;
		m_conn.close();
		if (log.isDebugEnabled()) {
			log.debug("close Connection:SESSION_ID=" + this.sessionId + ":" + this.m_conn);
		}
	}

	protected void finalize() throws Throwable {
		try {
			m_conn.close();
		} finally {
			super.finalize();
		}
	}

	public Statement createStatement() throws SQLException {
		return new TBStatement(this, m_conn.createStatement(), m_queryTimeOut);
	}

	public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
		return new TBStatement(this, m_conn.createStatement(resultSetType, resultSetConcurrency), this.m_queryTimeOut);
	}

	public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		return new TBStatement(this, this.m_conn.createStatement(resultSetType, resultSetConcurrency,
				resultSetHoldability), this.m_queryTimeOut);
	}

	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency,
			int resultSetHoldability) throws SQLException {
		return new TBPreparedStatement(this, this.m_conn.prepareStatement(sql, resultSetType, resultSetConcurrency,
				resultSetHoldability), sql, this.m_queryTimeOut);

	}

	public PreparedStatement prepareStatement(String sql, int columnIndexes[]) throws SQLException {
		return new TBPreparedStatement(this, this.m_conn.prepareStatement(sql, columnIndexes), sql, this.m_queryTimeOut);
	}

	public PreparedStatement prepareStatement(String sql, String columnNames[]) throws SQLException {
		return new TBPreparedStatement(this, this.m_conn.prepareStatement(sql, columnNames), sql, this.m_queryTimeOut);
	}

	public boolean getAutoCommit() throws SQLException {
		return m_conn.getAutoCommit();
	}

	public String getCatalog() throws SQLException {
		return m_conn.getCatalog();
	}

	public java.sql.DatabaseMetaData getMetaData() throws SQLException {
		return m_conn.getMetaData();
	}

	public int getTransactionIsolation() throws SQLException {
		return m_conn.getTransactionIsolation();
	}

	public Map<String, Class<?>> getTypeMap() throws SQLException {
		return m_conn.getTypeMap();
	}

	public SQLWarning getWarnings() throws SQLException {
		return m_conn.getWarnings();
	}

	public boolean isClosed() throws SQLException {
		return m_conn.isClosed();
	}

	public boolean isReadOnly() throws SQLException {
		return m_conn.isReadOnly();
	}

	public String nativeSQL(String sql) throws SQLException {
		return m_conn.nativeSQL(sql);
	}

	public java.sql.CallableStatement prepareCall(String sql) throws SQLException {
		return new TBCallableStatement(this, m_conn.prepareCall(sql), sql, this.m_queryTimeOut);

	}

	public java.sql.CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency)
			throws SQLException {
		return new TBCallableStatement(this, m_conn.prepareCall(sql, resultSetType, resultSetConcurrency), sql,
				this.m_queryTimeOut);
	}

	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency,
			int resultSetHoldability) throws SQLException {
		return new TBCallableStatement(this, this.m_conn.prepareCall(sql, resultSetType, resultSetConcurrency,
				resultSetHoldability), sql, this.m_queryTimeOut);
	}

	public java.sql.PreparedStatement prepareStatement(String sql) throws SQLException {
		return new TBPreparedStatement(this, m_conn.prepareStatement(sql), sql, this.m_queryTimeOut);
	}

	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
		return new TBPreparedStatement(this, this.m_conn.prepareStatement(sql, autoGeneratedKeys), sql,
				this.m_queryTimeOut);
	}

	public java.sql.PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)
			throws SQLException {
		return new TBPreparedStatement(this, m_conn.prepareStatement(sql, resultSetType, resultSetConcurrency), sql,
				this.m_queryTimeOut);
	}

	public void setAutoCommit(boolean autoCommit) throws SQLException {
		m_conn.setAutoCommit(autoCommit);
	}

	public void setCatalog(String catalog) throws SQLException {
		m_conn.setCatalog(catalog);
	}

	public void setReadOnly(boolean readOnly) throws SQLException {
		m_conn.setReadOnly(readOnly);
	}

	public void setTransactionIsolation(int level) throws SQLException {
		// �˷�����������������ݿ⽻����������δ˷����ĵ���
		// m_conn.setTransactionIsolation(level);
	}

	public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
		m_conn.setTypeMap(map);
	}

	public void setHoldability(int holdability) throws SQLException {
		this.m_conn.setHoldability(holdability);
	}

	public Savepoint setSavepoint(String savePoint) throws SQLException {
		throw new SQLException("��֧�ֵķ���");
	}

	public Savepoint setSavepoint() throws SQLException {
		throw new SQLException("��֧�ֵķ���");
	}

	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
		throw new SQLException("��֧�ֵķ���");
	}

	public int getHoldability() throws SQLException {
		return this.m_conn.getHoldability();
	}

	public void rollback(Savepoint savepoint) throws SQLException {
		throw new SQLException("��֧�ֵķ���");
	}

	public Connection getRealConnection() {
		return this.m_conn;
	}

	public long getOpenTime() {
		return m_openTime;
	}

	public String getDBSessionID() {
		return this.sessionId;
	}

	protected String queryDBSessionID() {
		if ("oracle".equals(dbType.toLowerCase())) {
			return "Only ORACLE has SESSION_ID";
		}
		String result = "";
		try {
			Statement stmt = this.m_conn.createStatement();
			ResultSet set = stmt.executeQuery(S_SESSION_QUERY);
			set.next();
			result = set.getString(1);
			set.close();
			stmt.close();
		} catch (Throwable ex) {
			result = "��������Ѿ��Ͽ�����Ҫ��������";
		}
		return result;
	}

	public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
		return this.m_conn.createArrayOf(typeName, elements);
	}

	public Blob createBlob() throws SQLException {
		return this.m_conn.createBlob();
	}

	public Clob createClob() throws SQLException {
		return this.m_conn.createClob();
	}

	public NClob createNClob() throws SQLException {
		return this.m_conn.createNClob();
	}

	public SQLXML createSQLXML() throws SQLException {
		return this.m_conn.createSQLXML();
	}

	public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
		return this.m_conn.createStruct(typeName, attributes);
	}

	public Properties getClientInfo() throws SQLException {
		return this.m_conn.getClientInfo();
	}

	public String getClientInfo(String name) throws SQLException {
		return this.m_conn.getClientInfo(name);
	}

	public boolean isValid(int timeout) throws SQLException {
		return this.m_conn.isValid(timeout);
	}

	public void setClientInfo(Properties properties) throws SQLClientInfoException {
		this.m_conn.setClientInfo(properties);

	}

	public void setClientInfo(String name, String value) throws SQLClientInfoException {
		this.m_conn.setClientInfo(name, value);

	}

	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return this.m_conn.isWrapperFor(iface);
	}

	public <T> T unwrap(Class<T> iface) throws SQLException {
		return this.m_conn.unwrap(iface);
	}

}
