package com.taobao.pamirs.transaction;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.util.Calendar;

import java.util.List;
import java.util.ArrayList;

/**
 * 
 * @author xuannan
 *
 */
public class TBPreparedStatement extends TBStatement implements
		PreparedStatement {
	private List<Object> m_parameters = new ArrayList<Object>();

	public TBPreparedStatement(TBConnection conn, PreparedStatement statement,
			String sql, int aQueryTimeOut) throws SQLException {
		super(conn, statement, aQueryTimeOut);
		this.m_sql = sql;
	}

	public void close() throws SQLException {
		this.m_parameters.clear();
		super.close();
	}

	public ResultSet executeQuery() throws SQLException {
		try {
			long startTime = System.nanoTime();
			ResultSet result = ((PreparedStatement) this.m_statement)
					.executeQuery();
			SqlMonitor.monitorSQL(this.m_sql, startTime, System
					.nanoTime(), this.m_parameters, -1);
			return result;
		} catch (SQLException e) {
			dealWithQueryTimeOutException(e, m_sql);
			return null;
		}
	}

	public int executeUpdate() throws SQLException {
		this.m_conn.preExecuteDDLStatement();
		try {
			long startTime = System.nanoTime();
			int result = ((PreparedStatement) this.m_statement).executeUpdate();
			SqlMonitor.monitorSQL(this.m_sql, startTime, System
					.nanoTime(), this.m_parameters, -1);
			return result;
		} catch (SQLException e) {
			dealWithQueryTimeOutException(e, m_sql);
			return 0;
		}

	}

	public void setNull(int parameterIndex, int sqlType) throws SQLException {
		((PreparedStatement) this.m_statement).setNull(parameterIndex, sqlType);
		this.m_parameters.add(parameterIndex - 1, "NULL");
	}

	public void setBoolean(int parameterIndex, boolean x) throws SQLException {
		((PreparedStatement) this.m_statement).setBoolean(parameterIndex, x);
		this.m_parameters.add(parameterIndex - 1, Boolean.valueOf(x));
	}

	public void setByte(int parameterIndex, byte x) throws SQLException {
		((PreparedStatement) this.m_statement).setByte(parameterIndex, x);
		this.m_parameters.add(parameterIndex - 1, Byte.valueOf(x));
	}

	public void setShort(int parameterIndex, short x) throws SQLException {
		((PreparedStatement) this.m_statement).setShort(parameterIndex, x);
		this.m_parameters.add(parameterIndex - 1, Short.valueOf(x));

	}

	public void setInt(int parameterIndex, int x) throws SQLException {
		((PreparedStatement) this.m_statement).setInt(parameterIndex, x);
		this.m_parameters.add(parameterIndex - 1, Integer.valueOf(x));
	}

	public void setLong(int parameterIndex, long x) throws SQLException {
		((PreparedStatement) this.m_statement).setLong(parameterIndex, x);
		this.m_parameters.add(parameterIndex - 1, Long.valueOf(x));

	}

	public void setFloat(int parameterIndex, float x) throws SQLException {
		((PreparedStatement) this.m_statement).setFloat(parameterIndex, x);
		this.m_parameters.add(parameterIndex - 1, new Float(x));

	}

	public void setDouble(int parameterIndex, double x) throws SQLException {
		((PreparedStatement) this.m_statement).setDouble(parameterIndex, x);
		this.m_parameters.add(parameterIndex - 1, new Double(x));

	}

	public void setBigDecimal(int parameterIndex, BigDecimal x)
			throws SQLException {
		((PreparedStatement) this.m_statement).setBigDecimal(parameterIndex, x);
		this.m_parameters.add(parameterIndex - 1, x);
	}

	public void setString(int parameterIndex, String x) throws SQLException {
		((PreparedStatement) this.m_statement).setString(parameterIndex, x);
		this.m_parameters.add(parameterIndex - 1, x);
	}

	public void setBytes(int parameterIndex, byte x[]) throws SQLException {
		((PreparedStatement) this.m_statement).setBytes(parameterIndex, x);
		this.m_parameters.add(parameterIndex - 1, x);
	}

	public void setDate(int parameterIndex, java.sql.Date x)
			throws SQLException {
		((PreparedStatement) this.m_statement).setDate(parameterIndex, x);
		this.m_parameters.add(parameterIndex - 1, x);
	}

	public void setTime(int parameterIndex, java.sql.Time x)
			throws SQLException {
		((PreparedStatement) this.m_statement).setTime(parameterIndex, x);
		this.m_parameters.add(parameterIndex - 1, x);
	}

	public void setTimestamp(int parameterIndex, java.sql.Timestamp x)
			throws SQLException {
		((PreparedStatement) this.m_statement).setTimestamp(parameterIndex, x);
		this.m_parameters.add(parameterIndex - 1, x);
	}

	public void setAsciiStream(int parameterIndex, java.io.InputStream x,
			int length) throws SQLException {
		((PreparedStatement) this.m_statement).setAsciiStream(parameterIndex,
				x, length);
		String msg = "AsciiStream不能导出";
		this.m_parameters.add(parameterIndex - 1, msg);
	}

	@SuppressWarnings("deprecation")
	public void setUnicodeStream(int parameterIndex, java.io.InputStream x,
			int length) throws SQLException {
		((PreparedStatement) this.m_statement).setUnicodeStream(parameterIndex,
				x, length);
		String msg = "UnicodeStream不能导出";
		this.m_parameters.add(parameterIndex - 1, msg);
	}

	public void setBinaryStream(int parameterIndex, java.io.InputStream x,
			int length) throws SQLException {
		((PreparedStatement) this.m_statement).setBinaryStream(parameterIndex,
				x, length);
		String msg = "BinaryStream不能导出";
		this.m_parameters.add(parameterIndex - 1, msg);
	}

	public void clearParameters() throws SQLException {
		this.m_parameters.clear();
		((PreparedStatement) this.m_statement).clearParameters();
	}

	public void setObject(int parameterIndex, Object x, int targetSqlType,
			int scale) throws SQLException {
		((PreparedStatement) this.m_statement).setObject(parameterIndex, x,
				targetSqlType, scale);
		this.m_parameters.add(parameterIndex - 1, x);
	}

	public void setObject(int parameterIndex, Object x, int targetSqlType)
			throws SQLException {
		((PreparedStatement) this.m_statement).setObject(parameterIndex, x,
				targetSqlType);
		this.m_parameters.add(parameterIndex - 1, x);
	}

	public void setObject(int parameterIndex, Object x) throws SQLException {
		((PreparedStatement) this.m_statement).setObject(parameterIndex, x);
		this.m_parameters.add(parameterIndex - 1, x);
	}

	public boolean execute() throws SQLException {
		// ibatis的SqlExecutor永远只调用PreparedStatement的execute方法 无视executeQuery
		// 所以要在这里判断下sql是不是select
		boolean isSelect = false;
		if (this.m_sql.trim().toLowerCase().startsWith("select")) {
			isSelect = true;
		}
		if(isSelect == false){
			this.m_conn.preExecuteDDLStatement();
		}
		try {
			long startTime = System.nanoTime();
			boolean result = ((PreparedStatement) this.m_statement).execute();
			SqlMonitor.monitorSQL(this.m_sql, startTime, System
					.nanoTime(), this.m_parameters, -1);
			return result;
		} catch (SQLException e) {
			dealWithQueryTimeOutException(e, m_sql);
			return false;
		}

	}

	public void addBatch() throws SQLException {
		((PreparedStatement) this.m_statement).addBatch();
	}

	public void setCharacterStream(int parameterIndex, java.io.Reader reader,
			int length) throws SQLException {
		((PreparedStatement) this.m_statement).setCharacterStream(
				parameterIndex, reader, length);
		String msg = "CharacterStream不能导出";
		this.m_parameters.add(parameterIndex - 1, msg);
	}

	public void setRef(int parameterIndex, Ref x) throws SQLException {
		((PreparedStatement) this.m_statement).setRef(parameterIndex, x);
		String msg = "Ref引用不能导出";
		this.m_parameters.add(parameterIndex - 1, msg);
	}

	public void setBlob(int parameterIndex, Blob x) throws SQLException {
		((PreparedStatement) this.m_statement).setBlob(parameterIndex, x);
		String msg = "Blob不能导出";
		this.m_parameters.add(parameterIndex - 1, msg);
	}

	public void setClob(int parameterIndex, Clob x) throws SQLException {
		((PreparedStatement) this.m_statement).setClob(parameterIndex, x);
		String msg = "Clob不能导出";
		this.m_parameters.add(parameterIndex - 1, msg);
	}

	public void setArray(int parameterIndex, Array x) throws SQLException {
		((PreparedStatement) this.m_statement).setArray(parameterIndex, x);
		String msg = "Array不能导出";
		this.m_parameters.add(parameterIndex - 1, msg);
	}

	public ResultSetMetaData getMetaData() throws SQLException {
		return ((PreparedStatement) this.m_statement).getMetaData();
	}

	public void setDate(int parameterIndex, java.sql.Date x, Calendar cal)
			throws SQLException {
		((PreparedStatement) this.m_statement).setDate(parameterIndex, x, cal);
		this.m_parameters.add(parameterIndex - 1, x);
	}

	public void setTime(int parameterIndex, java.sql.Time x, Calendar cal)
			throws SQLException {
		((PreparedStatement) this.m_statement).setTime(parameterIndex, x, cal);
		this.m_parameters.add(parameterIndex - 1, x);
	}

	public void setTimestamp(int parameterIndex, java.sql.Timestamp x,
			Calendar cal) throws SQLException {
		((PreparedStatement) this.m_statement).setTimestamp(parameterIndex, x,
				cal);
		this.m_parameters.add(parameterIndex - 1, x);
	}

	public void setNull(int parameterIndex, int sqlType, String typeName)
			throws SQLException {
		((PreparedStatement) this.m_statement).setNull(parameterIndex, sqlType,
				typeName);
		this.m_parameters.add(parameterIndex - 1, "NULL");
	}

	public void setURL(int parameterIndex, java.net.URL x) throws SQLException {
		((PreparedStatement) this.m_statement).setURL(parameterIndex, x);
		this.m_parameters.add(parameterIndex - 1, x);
	}

	public ParameterMetaData getParameterMetaData() throws SQLException {
		return ((PreparedStatement) this.m_statement).getParameterMetaData();
	}

	public void setAsciiStream(int parameterIndex, InputStream x)
			throws SQLException {
		((PreparedStatement) this.m_statement)
				.setAsciiStream(parameterIndex, x);
		String msg = "InputStream不能导出";
		this.m_parameters.add(parameterIndex - 1, msg);

	}

	public void setAsciiStream(int parameterIndex, InputStream x, long length)
			throws SQLException {
		((PreparedStatement) this.m_statement).setAsciiStream(parameterIndex,
				x, length);
		String msg = "InputStream不能导出";
		this.m_parameters.add(parameterIndex - 1, msg);
	}

	public void setBinaryStream(int parameterIndex, InputStream x)
			throws SQLException {
		((PreparedStatement) this.m_statement).setBinaryStream(parameterIndex,
				x);
		String msg = "InputStream不能导出";
		this.m_parameters.add(parameterIndex - 1, msg);
	}

	public void setBinaryStream(int parameterIndex, InputStream x, long length)
			throws SQLException {
		((PreparedStatement) this.m_statement).setBinaryStream(parameterIndex,
				x, length);
		String msg = "InputStream不能导出";
		this.m_parameters.add(parameterIndex - 1, msg);
	}

	public void setBlob(int parameterIndex, InputStream inputStream)
			throws SQLException {
		((PreparedStatement) this.m_statement).setBlob(parameterIndex,
				inputStream);
		String msg = "InputStream不能导出";
		this.m_parameters.add(parameterIndex - 1, msg);
	}

	public void setBlob(int parameterIndex, InputStream inputStream, long length)
			throws SQLException {
		((PreparedStatement) this.m_statement).setBlob(parameterIndex,
				inputStream, length);
		String msg = "InputStream不能导出";
		this.m_parameters.add(parameterIndex - 1, msg);

	}

	public void setCharacterStream(int parameterIndex, Reader reader)
			throws SQLException {
		((PreparedStatement) this.m_statement).setCharacterStream(
				parameterIndex, reader);
		String msg = "Reader不能导出";
		this.m_parameters.add(parameterIndex - 1, msg);

	}

	public void setCharacterStream(int parameterIndex, Reader reader,
			long length) throws SQLException {
		((PreparedStatement) this.m_statement).setCharacterStream(
				parameterIndex, reader, length);
		String msg = "Reader不能导出";
		this.m_parameters.add(parameterIndex - 1, msg);

	}

	public void setClob(int parameterIndex, Reader reader) throws SQLException {
		((PreparedStatement) this.m_statement).setClob(parameterIndex, reader);
		String msg = "Reader不能导出";
		this.m_parameters.add(parameterIndex - 1, msg);

	}

	public void setClob(int parameterIndex, Reader reader, long length)
			throws SQLException {
		((PreparedStatement) this.m_statement).setClob(parameterIndex, reader,
				length);
		String msg = "Reader不能导出";
		this.m_parameters.add(parameterIndex - 1, msg);

	}

	public void setNCharacterStream(int parameterIndex, Reader value)
			throws SQLException {
		((PreparedStatement) this.m_statement).setNCharacterStream(
				parameterIndex, value);
		String msg = "Reader不能导出";
		this.m_parameters.add(parameterIndex - 1, msg);
	}

	public void setNCharacterStream(int parameterIndex, Reader value,
			long length) throws SQLException {
		((PreparedStatement) this.m_statement).setNCharacterStream(
				parameterIndex, value, length);
		String msg = "Reader不能导出";
		this.m_parameters.add(parameterIndex - 1, msg);
	}

	public void setNClob(int parameterIndex, NClob value) throws SQLException {
		((PreparedStatement) this.m_statement).setNClob(parameterIndex, value);
		String msg = "NClob不能导出";
		this.m_parameters.add(parameterIndex - 1, msg);
	}

	public void setNClob(int parameterIndex, Reader reader) throws SQLException {
		((PreparedStatement) this.m_statement).setNClob(parameterIndex, reader);
		String msg = "Reader不能导出";
		this.m_parameters.add(parameterIndex - 1, msg);
	}

	public void setNClob(int parameterIndex, Reader reader, long length)
			throws SQLException {
		((PreparedStatement) this.m_statement).setNClob(parameterIndex, reader,
				length);
		String msg = "Reader不能导出";
		this.m_parameters.add(parameterIndex - 1, msg);
	}

	public void setNString(int parameterIndex, String value)
			throws SQLException {
		((PreparedStatement) this.m_statement)
				.setNString(parameterIndex, value);
		String msg = "NString不能导出";
		this.m_parameters.add(parameterIndex - 1, msg);
	}

	public void setRowId(int parameterIndex, RowId x) throws SQLException {
		((PreparedStatement) this.m_statement).setRowId(parameterIndex, x);
		this.m_parameters.add(parameterIndex - 1, x);
	}

	public void setSQLXML(int parameterIndex, SQLXML xmlObject)
			throws SQLException {
		((PreparedStatement) this.m_statement).setSQLXML(parameterIndex,
				xmlObject);
		String msg = "SQLXML不能导出";
		this.m_parameters.add(parameterIndex - 1, msg);
	}

}
