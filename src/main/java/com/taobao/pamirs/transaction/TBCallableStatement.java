package com.taobao.pamirs.transaction;

import java.sql.*;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Map;

public class TBCallableStatement extends TBPreparedStatement implements
		CallableStatement {

	public TBCallableStatement(TBConnection conn, CallableStatement statement,
			String sql, int aQueryTimeOut) throws SQLException {
		super(conn, statement, sql, aQueryTimeOut);
	}

	public void registerOutParameter(int parameterIndex, int sqlType)
			throws SQLException {
		((CallableStatement) this.m_statement).registerOutParameter(
				parameterIndex, sqlType);
	}

	public void registerOutParameter(int parameterIndex, int sqlType, int scale)
			throws SQLException {
		((CallableStatement) this.m_statement).registerOutParameter(
				parameterIndex, sqlType, scale);

	}

	public boolean wasNull() throws SQLException {
		return ((CallableStatement) this.m_statement).wasNull();
	}

	public String getString(int parameterIndex) throws SQLException {
		return ((CallableStatement) this.m_statement).getString(parameterIndex);
	}

	public boolean getBoolean(int parameterIndex) throws SQLException {
		return ((CallableStatement) this.m_statement)
				.getBoolean(parameterIndex);
	}

	public byte getByte(int parameterIndex) throws SQLException {
		return ((CallableStatement) this.m_statement).getByte(parameterIndex);
	}

	public short getShort(int parameterIndex) throws SQLException {
		return ((CallableStatement) this.m_statement).getShort(parameterIndex);
	}

	public int getInt(int parameterIndex) throws SQLException {
		return ((CallableStatement) this.m_statement).getInt(parameterIndex);
	}

	public long getLong(int parameterIndex) throws SQLException {
		return ((CallableStatement) this.m_statement).getLong(parameterIndex);
	}

	public float getFloat(int parameterIndex) throws SQLException {
		return ((CallableStatement) this.m_statement).getFloat(parameterIndex);
	}

	public double getDouble(int parameterIndex) throws SQLException {
		return ((CallableStatement) this.m_statement).getDouble(parameterIndex);
	}

	@SuppressWarnings("deprecation")
	public BigDecimal getBigDecimal(int parameterIndex, int scale)
			throws SQLException {
		return ((CallableStatement) this.m_statement).getBigDecimal(
				parameterIndex, scale);
	}

	public byte[] getBytes(int parameterIndex) throws SQLException {
		return ((CallableStatement) this.m_statement).getBytes(parameterIndex);
	}

	public java.sql.Date getDate(int parameterIndex) throws SQLException {
		return ((CallableStatement) this.m_statement).getDate(parameterIndex);
	}

	public java.sql.Time getTime(int parameterIndex) throws SQLException {
		return ((CallableStatement) this.m_statement).getTime(parameterIndex);
	}

	public java.sql.Timestamp getTimestamp(int parameterIndex)
			throws SQLException {
		return ((CallableStatement) this.m_statement)
				.getTimestamp(parameterIndex);
	}

	public Object getObject(int parameterIndex) throws SQLException {
		return ((CallableStatement) this.m_statement).getObject(parameterIndex);
	}

	public BigDecimal getBigDecimal(int parameterIndex) throws SQLException {
		return ((CallableStatement) this.m_statement)
				.getBigDecimal(parameterIndex);
	}

	public Ref getRef(int i) throws SQLException {
		return ((CallableStatement) this.m_statement).getRef(i);
	}

	public Blob getBlob(int i) throws SQLException {
		return ((CallableStatement) this.m_statement).getBlob(i);
	}

	public Clob getClob(int i) throws SQLException {
		return ((CallableStatement) this.m_statement).getClob(i);
	}

	public Array getArray(int i) throws SQLException {
		return ((CallableStatement) this.m_statement).getArray(i);
	}

	public java.sql.Date getDate(int parameterIndex, Calendar cal)
			throws SQLException {
		return ((CallableStatement) this.m_statement).getDate(parameterIndex,
				cal);
	}

	public java.sql.Time getTime(int parameterIndex, Calendar cal)
			throws SQLException {
		return ((CallableStatement) this.m_statement).getTime(parameterIndex,
				cal);
	}

	public java.sql.Timestamp getTimestamp(int parameterIndex, Calendar cal)
			throws SQLException {
		return ((CallableStatement) this.m_statement).getTimestamp(
				parameterIndex, cal);
	}

	public void registerOutParameter(int paramIndex, int sqlType,
			String typeName) throws SQLException {
		((CallableStatement) this.m_statement).registerOutParameter(paramIndex,
				sqlType, typeName);
	}

	public void registerOutParameter(String parameterName, int sqlType)
			throws SQLException {
		((CallableStatement) this.m_statement).registerOutParameter(
				parameterName, sqlType);
	}

	public void registerOutParameter(String parameterName, int sqlType,
			int scale) throws SQLException {
		((CallableStatement) this.m_statement).registerOutParameter(
				parameterName, sqlType, scale);
	}

	public void registerOutParameter(String parameterName, int sqlType,
			String typeName) throws SQLException {
		((CallableStatement) this.m_statement).registerOutParameter(
				parameterName, sqlType, typeName);
	}

	public java.net.URL getURL(int parameterIndex) throws SQLException {
		return ((CallableStatement) this.m_statement).getURL(parameterIndex);
	}

	public void setURL(String parameterName, java.net.URL val)
			throws SQLException {
		((CallableStatement) this.m_statement).setURL(parameterName, val);
	}

	public void setNull(String parameterName, int sqlType) throws SQLException {
		((CallableStatement) this.m_statement).setNull(parameterName, sqlType);
	}

	public void setBoolean(String parameterName, boolean x) throws SQLException {
		((CallableStatement) this.m_statement).setBoolean(parameterName, x);
	}

	public void setByte(String parameterName, byte x) throws SQLException {
		((CallableStatement) this.m_statement).setByte(parameterName, x);
	}

	public void setShort(String parameterName, short x) throws SQLException {
		((CallableStatement) this.m_statement).setShort(parameterName, x);
	}

	public void setInt(String parameterName, int x) throws SQLException {
		((CallableStatement) this.m_statement).setInt(parameterName, x);
	}

	public void setLong(String parameterName, long x) throws SQLException {
		((CallableStatement) this.m_statement).setLong(parameterName, x);
	}

	public void setFloat(String parameterName, float x) throws SQLException {
		((CallableStatement) this.m_statement).setFloat(parameterName, x);
	}

	public void setDouble(String parameterName, double x) throws SQLException {
		((CallableStatement) this.m_statement).setDouble(parameterName, x);
	}

	public void setBigDecimal(String parameterName, BigDecimal x)
			throws SQLException {
		((CallableStatement) this.m_statement).setBigDecimal(parameterName, x);
	}

	public void setString(String parameterName, String x) throws SQLException {
		((CallableStatement) this.m_statement).setString(parameterName, x);
	}

	public void setBytes(String parameterName, byte x[]) throws SQLException {
		((CallableStatement) this.m_statement).setBytes(parameterName, x);
	}

	public void setDate(String parameterName, java.sql.Date x)
			throws SQLException {
		((CallableStatement) this.m_statement).setDate(parameterName, x);
	}

	public void setTime(String parameterName, java.sql.Time x)
			throws SQLException {
		((CallableStatement) this.m_statement).setTime(parameterName, x);
	}

	public void setTimestamp(String parameterName, java.sql.Timestamp x)
			throws SQLException {
		((CallableStatement) this.m_statement).setTimestamp(parameterName, x);
	}

	public void setAsciiStream(String parameterName, java.io.InputStream x,
			int length) throws SQLException {
		((CallableStatement) this.m_statement).setAsciiStream(parameterName, x,
				length);
	}

	public void setBinaryStream(String parameterName, java.io.InputStream x,
			int length) throws SQLException {
		((CallableStatement) this.m_statement).setBinaryStream(parameterName,
				x, length);
	}

	public void setObject(String parameterName, Object x, int targetSqlType,
			int scale) throws SQLException {
		((CallableStatement) this.m_statement).setObject(parameterName, x,
				targetSqlType, scale);
	}

	public void setObject(String parameterName, Object x, int targetSqlType)
			throws SQLException {
		((CallableStatement) this.m_statement).setObject(parameterName, x,
				targetSqlType);
	}

	public void setObject(String parameterName, Object x) throws SQLException {
		((CallableStatement) this.m_statement).setObject(parameterName, x);
	}

	public void setCharacterStream(String parameterName, java.io.Reader reader,
			int length) throws SQLException {
		((CallableStatement) this.m_statement).setCharacterStream(
				parameterName, reader, length);
	}

	public void setDate(String parameterName, java.sql.Date x, Calendar cal)
			throws SQLException {
		((CallableStatement) this.m_statement).setDate(parameterName, x, cal);
	}

	public void setTime(String parameterName, java.sql.Time x, Calendar cal)
			throws SQLException {
		((CallableStatement) this.m_statement).setTime(parameterName, x, cal);
	}

	public void setTimestamp(String parameterName, java.sql.Timestamp x,
			Calendar cal) throws SQLException {
		((CallableStatement) this.m_statement).setTimestamp(parameterName, x,
				cal);
	}

	public void setNull(String parameterName, int sqlType, String typeName)
			throws SQLException {
		((CallableStatement) this.m_statement).setNull(parameterName, sqlType,
				typeName);
	}

	public String getString(String parameterName) throws SQLException {
		return ((CallableStatement) this.m_statement).getString(parameterName);
	}

	public boolean getBoolean(String parameterName) throws SQLException {
		return ((CallableStatement) this.m_statement).getBoolean(parameterName);
	}

	public byte getByte(String parameterName) throws SQLException {
		return ((CallableStatement) this.m_statement).getByte(parameterName);
	}

	public short getShort(String parameterName) throws SQLException {
		return ((CallableStatement) this.m_statement).getShort(parameterName);
	}

	public int getInt(String parameterName) throws SQLException {
		return ((CallableStatement) this.m_statement).getInt(parameterName);
	}

	public long getLong(String parameterName) throws SQLException {
		return ((CallableStatement) this.m_statement).getLong(parameterName);
	}

	public float getFloat(String parameterName) throws SQLException {
		return ((CallableStatement) this.m_statement).getFloat(parameterName);
	}

	public double getDouble(String parameterName) throws SQLException {
		return ((CallableStatement) this.m_statement).getDouble(parameterName);
	}

	public byte[] getBytes(String parameterName) throws SQLException {
		return ((CallableStatement) this.m_statement).getBytes(parameterName);
	}

	public java.sql.Date getDate(String parameterName) throws SQLException {
		return ((CallableStatement) this.m_statement).getDate(parameterName);
	}

	public java.sql.Time getTime(String parameterName) throws SQLException {
		return ((CallableStatement) this.m_statement).getTime(parameterName);
	}

	public java.sql.Timestamp getTimestamp(String parameterName)
			throws SQLException {
		return ((CallableStatement) this.m_statement)
				.getTimestamp(parameterName);
	}

	public Object getObject(String parameterName) throws SQLException {
		return ((CallableStatement) this.m_statement).getObject(parameterName);
	}

	public BigDecimal getBigDecimal(String parameterName) throws SQLException {
		return ((CallableStatement) this.m_statement)
				.getBigDecimal(parameterName);
	}

	public Ref getRef(String parameterName) throws SQLException {
		return ((CallableStatement) this.m_statement).getRef(parameterName);
	}

	public Blob getBlob(String parameterName) throws SQLException {
		return ((CallableStatement) this.m_statement).getBlob(parameterName);
	}

	public Clob getClob(String parameterName) throws SQLException {
		return ((CallableStatement) this.m_statement).getClob(parameterName);
	}

	public Array getArray(String parameterName) throws SQLException {
		return ((CallableStatement) this.m_statement).getArray(parameterName);
	}

	public java.sql.Date getDate(String parameterName, Calendar cal)
			throws SQLException {
		return ((CallableStatement) this.m_statement).getDate(parameterName,
				cal);
	}

	public java.sql.Time getTime(String parameterName, Calendar cal)
			throws SQLException {
		return ((CallableStatement) this.m_statement).getTime(parameterName,
				cal);
	}

	public java.sql.Timestamp getTimestamp(String parameterName, Calendar cal)
			throws SQLException {
		return ((CallableStatement) this.m_statement).getTimestamp(
				parameterName, cal);
	}

	public java.net.URL getURL(String parameterName) throws SQLException {
		return ((CallableStatement) this.m_statement).getURL(parameterName);
	}

	public Reader getCharacterStream(int parameterIndex) throws SQLException {
		return ((CallableStatement) this.m_statement)
				.getCharacterStream(parameterIndex);
	}

	public Reader getCharacterStream(String parameterName) throws SQLException {
		return ((CallableStatement) this.m_statement)
				.getCharacterStream(parameterName);
	}

	public Reader getNCharacterStream(int parameterIndex) throws SQLException {
		return ((CallableStatement) this.m_statement)
				.getNCharacterStream(parameterIndex);
	}

	public Reader getNCharacterStream(String parameterName) throws SQLException {
		return ((CallableStatement) this.m_statement)
				.getCharacterStream(parameterName);
	}

	public NClob getNClob(int parameterIndex) throws SQLException {
		return ((CallableStatement) this.m_statement).getNClob(parameterIndex);
	}

	public NClob getNClob(String parameterName) throws SQLException {
		return ((CallableStatement) this.m_statement).getNClob(parameterName);
	}

	public String getNString(int parameterIndex) throws SQLException {
		return ((CallableStatement) this.m_statement)
				.getNString(parameterIndex);
	}

	public String getNString(String parameterName) throws SQLException {
		return ((CallableStatement) this.m_statement).getNString(parameterName);
	}

	public Object getObject(int parameterIndex, Map<String, Class<?>> map)
			throws SQLException {
		return ((CallableStatement) this.m_statement).getObject(parameterIndex,
				map);
	}

	public RowId getRowId(int parameterIndex) throws SQLException {
		return ((CallableStatement) this.m_statement).getRowId(parameterIndex);
	}

	public RowId getRowId(String parameterName) throws SQLException {
		return ((CallableStatement) this.m_statement).getRowId(parameterName);
	}

	public SQLXML getSQLXML(int parameterIndex) throws SQLException {
		return ((CallableStatement) this.m_statement).getSQLXML(parameterIndex);
	}

	public SQLXML getSQLXML(String parameterName) throws SQLException {
		return ((CallableStatement) this.m_statement).getSQLXML(parameterName);
	}

	public void setAsciiStream(String parameterName, InputStream x)
			throws SQLException {
		((CallableStatement) this.m_statement).setAsciiStream(parameterName, x);
	}

	public void setAsciiStream(String parameterName, InputStream x, long length)
			throws SQLException {
		((CallableStatement) this.m_statement).setAsciiStream(parameterName, x,
				length);
	}

	public void setBinaryStream(String parameterName, InputStream x)
			throws SQLException {
		((CallableStatement) this.m_statement)
				.setBinaryStream(parameterName, x);

	}

	public void setBinaryStream(String parameterName, InputStream x, long length)
			throws SQLException {
		((CallableStatement) this.m_statement).setBinaryStream(parameterName,
				x, length);

	}

	public void setBlob(String parameterName, Blob x) throws SQLException {
		((CallableStatement) this.m_statement).setBlob(parameterName, x);

	}

	public void setBlob(String parameterName, InputStream inputStream)
			throws SQLException {
		((CallableStatement) this.m_statement).setBlob(parameterName,
				inputStream);

	}

	public void setBlob(String parameterName, InputStream inputStream,
			long length) throws SQLException {
		((CallableStatement) this.m_statement).setBlob(parameterName,
				inputStream, length);

	}

	public void setCharacterStream(String parameterName, Reader reader)
			throws SQLException {
		((CallableStatement) this.m_statement).setCharacterStream(
				parameterName, reader);
	}

	public void setCharacterStream(String parameterName, Reader reader,
			long length) throws SQLException {
		((CallableStatement) this.m_statement).setCharacterStream(
				parameterName, reader, length);

	}

	public void setClob(String parameterName, Clob x) throws SQLException {
		((CallableStatement) this.m_statement).setClob(parameterName, x);

	}

	public void setClob(String parameterName, Reader reader)
			throws SQLException {
		((CallableStatement) this.m_statement).setClob(parameterName, reader);

	}

	public void setClob(String parameterName, Reader reader, long length)
			throws SQLException {
		((CallableStatement) this.m_statement).setClob(parameterName, reader,
				length);

	}

	public void setNCharacterStream(String parameterName, Reader value)
			throws SQLException {
		((CallableStatement) this.m_statement).setNCharacterStream(
				parameterName, value);

	}

	public void setNCharacterStream(String parameterName, Reader value,
			long length) throws SQLException {
		((CallableStatement) this.m_statement).setNCharacterStream(
				parameterName, value, length);

	}

	public void setNClob(String parameterName, NClob value) throws SQLException {
		((CallableStatement) this.m_statement).setNClob(parameterName, value);

	}

	public void setNClob(String parameterName, Reader reader)
			throws SQLException {
		((CallableStatement) this.m_statement).setNClob(parameterName, reader);

	}

	public void setNClob(String parameterName, Reader reader, long length)
			throws SQLException {
		((CallableStatement) this.m_statement).setNClob(parameterName, reader,
				length);

	}

	public void setNString(String parameterName, String value)
			throws SQLException {
		((CallableStatement) this.m_statement).setNString(parameterName, value);

	}

	public void setRowId(String parameterName, RowId x) throws SQLException {
		((CallableStatement) this.m_statement).setRowId(parameterName, x);

	}

	public void setSQLXML(String parameterName, SQLXML xmlObject)
			throws SQLException {
		((CallableStatement) this.m_statement).setSQLXML(parameterName,
				xmlObject);
	}

	public Object getObject(String parameterName, Map<String, Class<?>> map)
			throws SQLException {
		return ((CallableStatement) this.m_statement).getObject(parameterName,
				map);
	}

}
