package com.taobao.pamirs.transaction;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.beans.factory.BeanNameAware;

/**
 * 数据源的包裹类，没有包裹过的数据源不能在事务管理器中生效
 * 需要注意的是:需要包裹的是最底层的原始数据源。
 * 例如 :
 * 		CRM_1,CRM_2 通过容灾后，变成CRM数据源。
 * 		则应该包裹的是CRM_1,CRM_2，而不是CRM.
 * 
 * @author xuannan
 *
 */
public class TBDataSourceImpl implements DataSource, BeanNameAware {
	String dataSourceName;
	DataSource dataSource;
	
	/**
	 * 数据库类型:oracle,mysql,一般情况通过连接就可以知道数据库类型，不用设置。
	 * 但遇到TDDL的时候，没有实现底层接口，必须人工配置
	 */
	String dbType;
	/**
	 * 在关闭连接的时候，是否需要commit数据库连接，
	 * 主要解决mysql，当事务隔离级别是REPEATABLE_READ的时候不能读取到修改后的数据的问题。
	 */
	private boolean isCommitOnCloseConnection = true;
	

	/**
	 * 在跨库操作的时候，连接提交前是否需要进行数据库有效性检查，建议设置为true
	 */
	private boolean isCheckDBOnCommit = false;
	
	public TBDataSourceImpl(){
		
	}
	public TBDataSourceImpl(String name,DataSource ds){
		this.dataSourceName = name;
		this.dataSource = ds;
	}
	
	public void setBeanName(String aDataSourceName) {
		this.dataSourceName = aDataSourceName;
	}
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public void setDataSourceName(String dataSourceName) {
		this.dataSourceName = dataSourceName;
	}

	public String getDbType() {
		return dbType;
	}
	public void setDbType(String dbType) {
		this.dbType = dbType;
	}
	public boolean isCommitOnCloseConnection() {
		return isCommitOnCloseConnection;
	}
	public void setIsCommitOnCloseConnection(boolean isCommitOnCloseConnection) {
		this.isCommitOnCloseConnection = isCommitOnCloseConnection;
	}
	public boolean isCheckDBOnCommit() {
		return isCheckDBOnCommit;
	}
	public void setIsCheckDBOnCommit(boolean isCheckDBOnCommit) {
		this.isCheckDBOnCommit = isCheckDBOnCommit;
	}
	public Connection getConnection() throws SQLException {
		TBTransactionManagerImpl manager =((TBTransactionManagerImpl) TransactionManager
				.getTransactionManager());
		return manager.getConnection(this.dataSourceName,this.dataSource,this.dbType,isCommitOnCloseConnection,isCheckDBOnCommit);
	}

	public Connection getConnection(String username, String password)
			throws SQLException {
		throw new UnsupportedOperationException
		  ("Not supported by TBDataSource");
	}
	public PrintWriter getLogWriter() throws SQLException {
		return this.dataSource.getLogWriter();
	}
	public int getLoginTimeout() throws SQLException {
		return this.dataSource.getLoginTimeout();
	}
	public void setLogWriter(PrintWriter out) throws SQLException {
		this.dataSource.setLogWriter(out);
	}
	public void setLoginTimeout(int seconds) throws SQLException {
		this.dataSource.setLoginTimeout(seconds);
	}
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return this.dataSource.isWrapperFor(iface);
	}
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return this.dataSource.unwrap(iface);
	}
}
