package com.taobao.pamirs.transaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Stack;
import javax.sql.DataSource;
/**
 * �����������Ҫ֧������Ĺ���ͻָ�
 * 
 * @author xuannan
 * 
 */
public class TBTransactionManagerImpl implements TBTransactionManager {
	/**
	 * Transaction��ջ,���������(suspend)��ʱ�򣬻�ѵ�ǰ����push����ջ�С�
	 * ��resum�����ʱ�򵯳���ǰ����
	 */
	protected Stack<TBTransactionImpl> m_transactionStack = new Stack<TBTransactionImpl>();

	/**
	 * ��ǰ����
	 */
	protected TBTransactionImpl m_currentTransaction;
	protected int queryTimeOut = -1;
	protected int warnTime = 1000;//���񱨾�����
	

	protected TBTransactionManagerImpl(int aQueryTimeOut,int aWarnTime) {
		this.queryTimeOut = aQueryTimeOut;
		this.warnTime = aWarnTime;
		m_currentTransaction = new TBTransactionImpl(this.queryTimeOut,this.warnTime);
	}
    public TBTransactionImpl getCurrentTransaction(){
    	return this.m_currentTransaction;
    }
	public void begin() throws SQLException {
		m_currentTransaction.begin();
	}

	/**
	 * �ж��Ƿ��Ѿ���ʼ����
	 * 
	 * @return boolean
	 */
	public boolean isStartTransaction() {
		return this.m_currentTransaction.isStartTransaction();
	}

	/**
	 * �ύ����
	 * 
	 * @throws AIException
	 */
	public void commit() throws SQLException {
		this.m_currentTransaction.commit();
	}

	/**
	 * �ع�����
	 * 
	 * @throws AIException
	 */
	public void rollback() throws SQLException {
		this.m_currentTransaction.rollback();
	}
	

	public Connection getConnection(String sourceName,DataSource ds,String aDbType,boolean aIsCommitOnCloseConnection,boolean aIsCheckDBOnCommit)throws java.sql.SQLException{
		 return this.m_currentTransaction.getConnection(sourceName,ds,aDbType,aIsCommitOnCloseConnection,aIsCheckDBOnCommit);
	}
	

	public void suspend() throws SQLException {
		if (this.m_currentTransaction.isStartTransaction() == false) {
			throw new SQLException("��û���������񣬲��ܹ�������");
		}
		this.m_transactionStack.push(this.m_currentTransaction);
		this.m_currentTransaction = new TBTransactionImpl(this.queryTimeOut,this.warnTime);
	}

	public void resume() throws SQLException {
		// �׳����������״̬
		if (this.m_transactionStack.size() == 0) {
			throw new SQLException("û�й�������񣬲��ָܻ�");
		}
		this.m_currentTransaction = this.m_transactionStack.pop();
	}

	public boolean isRollbackOnly() {
		return this.m_currentTransaction.isRollbackOnly();
	}

	public void setRollbackOnly() {
		this.m_currentTransaction.setRollbackOnly();
		
	}
	
}
