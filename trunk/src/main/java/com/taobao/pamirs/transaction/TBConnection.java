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
 * 数据库连接的一个包装类 主要重载了三个方法：close,commit,rollback。如果该连接属于一个事务 close,commit命令被忽略
 * rollback：设置事务只能被回滚
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
			// 设置当前连接的服务器信息 和 链接发生的地址信息
			setDBMSConnectionInfo();
		} else {
			this.sessionId = "没有打开连接状态检查开关";
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

	// 设置当前连接的模块、action信息，如果设置出错，不影响系统正常运行
	private void setDBMSConnectionInfo() {
		// 如果不设置连接信息，则返回
		if (isSetConnectionInfo == false) {
			return;
		}
		try {
			// 设置当前连接的服务器信息 和 链接发生的地址信息
			String moduleName = "HJ"; // AIRootConfig.getInstance().getUniqueServerId();
			String actionName = "Web";// CallManager.getCallPath();

			// 如果不是oracle数据库，则不设置
			if ("oracle".equals(dbType.toLowerCase())) {
				String sql = "call DBMS_APPLICATION_INFO.SET_MODULE (?,?)";

				// 注意，这儿不能用重载后的方法，否则会把hasDDLOperator改变为true，
				PreparedStatement stmt = this.m_conn.prepareStatement(sql);
				stmt.setString(1, moduleName);
				stmt.setString(2, actionName);

				stmt.execute();
				stmt.close();
			}
		} catch (Exception e) {
			log.error("设置连接信息出错！不影响系统正常运行", e);
		}

	}

	public static String debuger(TBConnection conn) {
		StringBuffer sb = new StringBuffer();
		String startTime = "开始时间";
		String spendTime = "连接占用时间";
		String connHashCode = "连接的HashCodes";
		String connAddr = "连接发生的地址";
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
				String lineNumber = "行数";
				sb.append(stack[i].getClassName() + "." + stack[i].getMethodName() + "() " + lineNumber + ":"
						+ stack[i].getLineNumber() + "\n");
			}
		}
		return sb.toString();
	}

	/**
	 * 校验当前连接是否断开，通过向数据库执行一条命令，来校验连接可用性. oracle的数据库通过想
	 * 
	 * @throws Exception
	 */
	public void judgeConnAvailable() throws Exception {

		// 如果是oracle数据库，则设置其连接session的信息，否则
		if ("oracle".equals(dbType.toLowerCase())) {
			String sql = "call DBMS_APPLICATION_INFO.SET_MODULE (?,?)";
			PreparedStatement stmt = this.m_conn.prepareStatement(sql);
			stmt.setString(1, "");
			stmt.setString(2, "");
			stmt.execute();
			stmt.close();
		} else if ("mysql".equals(dbType.toLowerCase())) {
			// 注意，这儿不能用重载后的方法，否则会把hasDDLOperator改变为true，
			PreparedStatement stmt = this.m_conn.prepareStatement(validateSql4Mysql);

			stmt.execute();
			stmt.close();

		} else {
			throw new Exception("请提供其它数据库的校验方式");
		}
	}

	/**
	 * 在Statement中执行数据操作类函数的时候设置
	 */
	public void setHasDDLOperator() {
		this.hasDDLOperator = true;
	}

	/**
	 * 在Statement的构造函数中调用，用于在连接关闭的时候关闭所有的游标
	 * 
	 * @param statement
	 */
	public void addStatement(Statement statement) {
		this.m_statements.add(statement);
	}

	/**
	 * 在Statemt关闭的时候从连接中把移除自己
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
	 * 如果连接已经加入事务，则不能直接回滚
	 */
	public void commit() throws SQLException {
		if (m_session == null) {
			this.realCommit();
		}
	}

	/**
	 * 如果连接已经加入事务，则不能直接回滚，只能设置事务为只能回滚
	 */
	public void rollback() throws SQLException {
		if (m_session == null) {
			this.realRollback();
		} else {
			this.m_session.setRollbackOnly();
		}
	}

	/**
	 * 如果连接没有加入事务，直接关闭 如果连接只执行了查询操作，关闭，而且从事务环境中移除自己 否则不能关闭，在事务控制器中进行统一处理。
	 */
	public void close() throws SQLException {
		/**
		 * 关闭所有可能是由于程序书写不规范导致的未关闭游标
		 */
		Statement[] tmpList = m_statements.toArray(new Statement[0]);
		for (Statement item : tmpList) {
			item.close();
		}
		this.m_statements.clear();

		if (log.isDebugEnabled()) {
			log.debug("clean Statements:SESSION_ID=" + this.sessionId + ":" + this.m_conn);
		}

		if (m_session == null) {// 没有加入事务
			this.realClose();
		} else if (this.hasDDLOperator == false) {// 没有进行数据修改操作
			if (log.isDebugEnabled()) {
				log.debug("因为连接没有执行任何数据修改的操作，虽然加入了事务也直接关闭");
			}
			// 一定要从事务环境中清除连接
			this.m_session.removeConnection(this.dataSourceName);
			this.realClose();
		} else {
			if (log.isDebugEnabled()) {
				log.debug("已经执行了数据修改操作的加入事务conneciton，在事务提交的时候才执行关闭操作");
			}
		}
	}

	public void realCommit() throws SQLException {
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
		// 此方法会产生大量的数据库交互，因此屏蔽此方法的调用
		// m_conn.setTransactionIsolation(level);
	}

	public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
		m_conn.setTypeMap(map);
	}

	public void setHoldability(int holdability) throws SQLException {
		this.m_conn.setHoldability(holdability);
	}

	public Savepoint setSavepoint(String savePoint) throws SQLException {
		return this.m_conn.setSavepoint(savePoint);
	}

	public Savepoint setSavepoint() throws SQLException {
		return this.m_conn.setSavepoint();
	}

	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
		this.m_conn.releaseSavepoint(savepoint);
	}

	public int getHoldability() throws SQLException {
		return this.m_conn.getHoldability();
	}

	public void rollback(Savepoint savepoint) throws SQLException {
		this.m_conn.rollback(savepoint);
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
			result = "如果连接已经断开，需要重新连接";
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
