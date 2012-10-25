package com.taobao.pamirs.transaction;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author xuannan
 *
 */
public class TBStatement implements Statement {
	private static transient Log log = LogFactory.getLog(TBConnection.class);

	protected Statement m_statement = null;
	protected String m_sql;
	TBConnection m_conn;
	private int queryTimeOut = 0;
	
	public TBStatement(TBConnection conn, Statement statement, int aQueryTimeOut)
			throws SQLException {
		this.m_conn = conn;
		this.m_statement = statement;
		this.queryTimeOut = aQueryTimeOut;
		if (this.queryTimeOut > 0) {
			statement.setQueryTimeout(this.queryTimeOut);
		}
		conn.addStatement(this);
	}

	public ResultSet executeQuery(String sql) throws SQLException {
		try {
			long startTime = System.nanoTime();
			ResultSet result = this.m_statement.executeQuery(sql);
			SqlMonitor.monitorSQL(sql, startTime, System
					.nanoTime(), null, -1);
			return result;
		} catch (SQLException e) {
			dealWithQueryTimeOutException(e, sql);
			return null;
		}
	}

	public void close() throws SQLException {
		if (this.m_statement != null) {
			this.m_statement.close();
		}
		this.m_conn.removeStatement(this);
		m_statement = null;
	}

	public int executeUpdate(String sql) throws SQLException {
		this.m_conn.preExecuteDDLStatement();
		try {
			long startTime = System.nanoTime();
			int result = this.m_statement.executeUpdate(sql);
			SqlMonitor.monitorSQL(sql, startTime, System
					.nanoTime(), null, result);
			return result;
		} catch (SQLException e) {
			dealWithQueryTimeOutException(e, sql);
			return 0;
		}

	}

	public int getMaxFieldSize() throws SQLException {
		return this.m_statement.getMaxFieldSize();
	}

	public void setMaxFieldSize(int max) throws SQLException {
		this.m_statement.setMaxFieldSize(max);
	}

	public int getMaxRows() throws SQLException {
		return this.m_statement.getMaxRows();
	}

	public void setMaxRows(int max) throws SQLException {
		this.m_statement.setMaxRows(max);
	}

	public void setEscapeProcessing(boolean enable) throws SQLException {
		this.m_statement.setEscapeProcessing(enable);
	}

	public int getQueryTimeout() throws SQLException {
		return this.m_statement.getQueryTimeout();
	}

	public void setQueryTimeout(int seconds) throws SQLException {
		this.m_statement.setQueryTimeout(seconds);
	}

	public void cancel() throws SQLException {
		this.m_statement.cancel();
	}

	public SQLWarning getWarnings() throws SQLException {
		return this.m_statement.getWarnings();
	}

	public void clearWarnings() throws SQLException {
		this.m_statement.clearWarnings();
	}

	public void setCursorName(String name) throws SQLException {
		this.m_statement.setCursorName(name);
	}

	public boolean execute(String sql) throws SQLException {
		this.m_conn.preExecuteDDLStatement();
		try {
			long startTime = System.nanoTime();
			boolean result = this.m_statement.execute(sql);
			SqlMonitor.monitorSQL(sql, startTime, System
					.nanoTime(), null, -1);
			return result;

		} catch (SQLException e) {
			dealWithQueryTimeOutException(e, sql);
			return false;
		}
	}

	public ResultSet getResultSet() throws SQLException {
		return this.m_statement.getResultSet();
	}

	public int getUpdateCount() throws SQLException {
		return this.m_statement.getUpdateCount();
	}

	public boolean getMoreResults() throws SQLException {
		return this.m_statement.getMoreResults();
	}

	public void setFetchDirection(int direction) throws SQLException {
		this.m_statement.setFetchDirection(direction);
	}

	public int getFetchDirection() throws SQLException {
		return this.m_statement.getFetchDirection();
	}

	public void setFetchSize(int rows) throws SQLException {
		this.m_statement.setFetchSize(rows);
	}

	public int getFetchSize() throws SQLException {
		return this.m_statement.getFetchSize();
	}

	public int getResultSetConcurrency() throws SQLException {
		return this.m_statement.getResultSetConcurrency();
	}

	public int getResultSetType() throws SQLException {
		return this.m_statement.getResultSetType();
	}

	public void addBatch(String sql) throws SQLException {
		this.m_sql = this.m_sql + sql + "\n";
		this.m_statement.addBatch(sql);
	}

	public void clearBatch() throws SQLException {
		this.m_sql = "";
		this.m_statement.clearBatch();
	}

	public int[] executeBatch() throws SQLException {
		this.m_conn.preExecuteDDLStatement();
		try {
			long startTime = System.nanoTime();
			int[] result = this.m_statement.executeBatch();
			SqlMonitor.monitorSQL(this.m_sql, startTime, System
					.nanoTime(), null, -1);
			return result;
		} catch (SQLException e) {
			dealWithQueryTimeOutException(e, this.m_sql);
			return null;
		}

	}

	public Connection getConnection() throws SQLException {
		return this.m_conn;
	}

	public boolean getMoreResults(int current) throws SQLException {
		return this.m_statement.getMoreResults(current);
	}

	public ResultSet getGeneratedKeys() throws SQLException {
		return this.m_statement.getGeneratedKeys();
	}

	public int executeUpdate(String sql, int autoGeneratedKeys)
			throws SQLException {
		this.m_conn.preExecuteDDLStatement();
		try {
			long startTime = System.nanoTime();
			int result = this.m_statement.executeUpdate(sql, autoGeneratedKeys);
			SqlMonitor.monitorSQL(sql, startTime, System
					.nanoTime(), null, result);
			return result;
		} catch (SQLException e) {
			dealWithQueryTimeOutException(e, sql);
			return 0;
		}

	}

	public int executeUpdate(String sql, int columnIndexes[])
			throws SQLException {
		this.m_conn.preExecuteDDLStatement();
		try {
			long startTime = System.nanoTime();
			int result = this.m_statement.executeUpdate(sql, columnIndexes);
			SqlMonitor.monitorSQL(sql, startTime, System
					.nanoTime(), null, result);
			return result;
		} catch (SQLException e) {
			dealWithQueryTimeOutException(e, sql);
			return 0;
		}

	}

	public int executeUpdate(String sql, String columnNames[])
			throws SQLException {
		this.m_conn.preExecuteDDLStatement();
		try {
			long startTime = System.nanoTime();
			int result = this.m_statement.executeUpdate(sql, columnNames);
			SqlMonitor.monitorSQL(sql, startTime, System
					.nanoTime(), null, result);
			return result;
		} catch (SQLException e) {
			dealWithQueryTimeOutException(e, sql);
			return 0;
		}
	}

	public boolean execute(String sql, int autoGeneratedKeys)
			throws SQLException {
		this.m_conn.preExecuteDDLStatement();
		try {
			long startTime = System.nanoTime();
			boolean result = this.m_statement.execute(sql, autoGeneratedKeys);
			SqlMonitor.monitorSQL(sql, startTime, System
					.nanoTime(), null, -1);

			return result;
		} catch (SQLException e) {
			dealWithQueryTimeOutException(e, sql);
			return false;
		}

	}

	public boolean execute(String sql, int columnIndexes[]) throws SQLException {
		this.m_conn.preExecuteDDLStatement();
		try {
			long startTime = System.nanoTime();
			boolean result = this.m_statement.execute(sql, columnIndexes);
			SqlMonitor.monitorSQL(sql, startTime, System
					.nanoTime(), null, -1);

			return result;
		} catch (SQLException e) {
			dealWithQueryTimeOutException(e, sql);
			return false;
		}
	}

	public boolean execute(String sql, String columnNames[])
			throws SQLException {
		this.m_conn.preExecuteDDLStatement();
		try {
			long startTime = System.nanoTime();
			boolean result = this.m_statement.execute(sql, columnNames);
			SqlMonitor.monitorSQL(sql, startTime, System
					.nanoTime(), null, -1);

			return result;
		} catch (SQLException e) {
			dealWithQueryTimeOutException(e, sql);
			return false;
		}
	}

	public int getResultSetHoldability() throws SQLException {
		return this.m_statement.getResultSetHoldability();
	}

	/**
	 * 处理超时异常
	 * 
	 * @param e
	 * @param sql
	 * @throws SQLException
	 */
	protected void dealWithQueryTimeOutException(SQLException e, String sql)
			throws SQLException {
		if (e.getMessage() != null && e.getMessage().indexOf("ORA-01013") > -1) {
			log
					.error(
							"The time-cost of executing the sql have been over the max executable time.The max time-cost is "
									+ this.getQueryTimeout()
									+ " seconds and the sql statement is : "
									+ sql, e);// 执行SQL超时最大执行时间 秒；超时SQL
			String msg = "执行SQL超时最大执行时间" + this.getQueryTimeout() + "秒；超时SQL:"
					+ this.m_sql;
			throw new SQLException(msg);
		} else {
			throw e;
		}
	}

	public boolean isClosed() throws SQLException {
		return this.m_statement.isClosed();
	}

	public boolean isPoolable() throws SQLException {
		return this.m_statement.isPoolable();
	}

	public void setPoolable(boolean poolable) throws SQLException {
		this.m_statement.setPoolable(poolable);

	}

	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return this.m_statement.isWrapperFor(iface);
	}

	public <T> T unwrap(Class<T> iface) throws SQLException {
		return this.m_statement.unwrap(iface);
	}
}
