package com.taobao.pamirs.transaction;

import java.sql.SQLException;

/**
 * 
 * @author xuannan
 * 
 */

public interface TBTransactionManager {
	public boolean isStartTransaction();
	public void begin() throws SQLException;

	public void commit() throws SQLException;

	public void rollback() throws SQLException;

	public void suspend()throws SQLException;
	public void resume()throws SQLException;
	
	public void setRollbackOnly() throws SQLException;

}
