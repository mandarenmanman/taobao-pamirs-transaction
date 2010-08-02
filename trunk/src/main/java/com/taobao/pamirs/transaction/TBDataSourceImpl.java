package com.taobao.pamirs.transaction;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.beans.factory.BeanNameAware;

/**
 * ����Դ�İ����࣬û�а�����������Դ�������������������Ч
 * ��Ҫע�����:��Ҫ����������ײ��ԭʼ����Դ��
 * ���� :
 * 		CRM_1,CRM_2 ͨ�����ֺ󣬱��CRM����Դ��
 * 		��Ӧ�ð�������CRM_1,CRM_2��������CRM.
 * 
 * @author xuannan
 *
 */
public class TBDataSourceImpl implements DataSource, BeanNameAware {
	String dataSourceName;
	DataSource dataSource;
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

	public Connection getConnection() throws SQLException {
		TBTransactionManagerImpl manager =((TBTransactionManagerImpl) TransactionManager
				.getTransactionManager());
		return manager.getConnection(this.dataSourceName,this.dataSource);
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
