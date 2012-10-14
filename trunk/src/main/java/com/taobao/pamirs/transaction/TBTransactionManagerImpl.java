package com.taobao.pamirs.transaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Stack;
import javax.sql.DataSource;
/**
 * 事务管理器，要支持事务的挂起和恢复
 * 
 * @author xuannan
 * 
 */
public class TBTransactionManagerImpl implements TBTransactionManager {
	/**
	 * Transaction堆栈,当事务挂起(suspend)的时候，会把当前事务push到堆栈中。
	 * 当resum事务的时候弹出当前事务。
	 */
	protected Stack<TBTransactionImpl> m_transactionStack = new Stack<TBTransactionImpl>();

	/**
	 * 当前事务
	 */
	protected TBTransactionImpl m_currentTransaction;
	protected int queryTimeOut = -1;
	protected int warnTime = 1000;//事务报警长度
	

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
	 * 判断是否已经开始事务
	 * 
	 * @return boolean
	 */
	public boolean isStartTransaction() {
		return this.m_currentTransaction.isStartTransaction();
	}

	/**
	 * 提交事务
	 * 
	 * @throws AIException
	 */
	public void commit() throws SQLException {
		this.m_currentTransaction.commit();
	}

	/**
	 * 回滚事务
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
			throw new SQLException("还没有启动事务，不能挂起事务");
		}
		this.m_transactionStack.push(this.m_currentTransaction);
		this.m_currentTransaction = new TBTransactionImpl(this.queryTimeOut,this.warnTime);
	}

	public void resume() throws SQLException {
		// 抛出顶层的事务状态
		if (this.m_transactionStack.size() == 0) {
			throw new SQLException("没有挂起的事务，不能恢复");
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
