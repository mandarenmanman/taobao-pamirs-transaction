package com.taobao.pamirs.transaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author xuannan
 *
 */
public class TBTransactionImpl {
	private static transient Log log = LogFactory
			.getLog(TBTransactionImpl.class);
	protected static List<TBTransactionImpl> m_LeaveTransaction = Collections.synchronizedList(new ArrayList<TBTransactionImpl>());
	protected int timeOut = -1;
	protected int warnTime = 1000;//事务报警长度
	boolean m_isCommitError = false;
	/**
	 * 事务使用的连接池
	 */
	protected Map<String,TBConnection> m_conn = new HashMap<String,TBConnection>();
	private Stack<String> m_sourceStack = null;
	protected boolean isStartTransaction = false;
	/**
	 * 标记事务是否只能回滚
	 */
	protected boolean m_onlyRollback = false;
	protected Exception setRollbackOnlyAddr;

	/**
	 * 开始事务的地址
	 */
	protected Exception m_addr;

	/**
	 * 开始事务时间
	 */
	protected long m_startTime;
	protected long m_startHoldConnectionTime = -1;

	/**
	 * 默认构造方法，造福spring配置啊，反正有默认值
	 * guichen add
	 */
	public TBTransactionImpl() {
	}

	public TBTransactionImpl(int queryTimeOut,int aWarnTime) {
		this.timeOut = queryTimeOut;
		this.warnTime = aWarnTime;
	}

	/**
	 * 在Wrap后的数据源中，调用此方法。
	 * 如果数据源名称的conn已经存在，则直接返回
	 * 否则从数据源中获取一个新的连接，并wrap为DBConnection后返回
	 * @param sourceName
	 * @param ds
	 * @return
	 * @throws java.sql.SQLException
	 */
	public Connection getConnection(String sourceName,DataSource ds,String aDbType,boolean aIsCommitOnCloseConnection,boolean aIsCheckDBOnCommit)throws java.sql.SQLException{
		if(sourceName == null){
			throw new SQLException("数据源名称不能为 null ");
		}
		TBConnection result = this.m_conn.get(sourceName);
		if(result == null){
			if (this.isStartTransaction() == false) {
				result = TBConnection.wrap(sourceName,ds.getConnection(), this.timeOut,aDbType,aIsCommitOnCloseConnection,aIsCheckDBOnCommit);
			} else {
				if(m_startHoldConnectionTime <0){
				   m_startHoldConnectionTime = System.currentTimeMillis();
				}
				result = TBConnection.wrap(sourceName,ds.getConnection(), this, this.timeOut,aDbType,aIsCommitOnCloseConnection,aIsCheckDBOnCommit);
				this.m_conn.put(sourceName, result);

				//在连接进行数据修改操作的时候才执行setAutoCommit(false)
			}
		}
		return result;
	}
	public static void debug(){

		log.error("transaction print begin ------------------");
		for(TBTransactionImpl item: m_LeaveTransaction){
			Object[] connections = item.m_conn.values().toArray();
			log.error(item.toString() + ": isStart = " +  item.isStartTransaction +":");

			for(Object conn : connections){
				log.error("\t\t" + conn);
			}
		}
		log.error("transaction print end------------------");

	}
	public int getConnectionCount(){
		return this.m_conn.size();
	}
	/**
	 * 为了节约连接的开销， 在DBConnection中Close的时候判断，如果是加入事务，但没有执行数据修改的操作，
	 * 则直接关闭连接，并从事务管理其中移除次连接。当一个连接只执行查询操作的时候，避免了长时间占用连接。
	 * @param sourceName
	 */
	public void removeConnection(String sourceName){
		this.m_conn.remove(sourceName);
	}

	/**
	 * 挂起事务
	 * @throws SQLException
	 */
	public void suspend() throws SQLException {

	}

	public void resume() throws SQLException {

	}
	 public boolean isStartTransaction(){
		  return  this.isStartTransaction;
	  }
	public void begin() throws SQLException {
		if (this.isStartTransaction == true) {
			throw new SQLException("事务已经存在，不能重复启动事务");
		}
		this.isStartTransaction = true;
		this.m_addr = new Exception();
		this.m_startTime = System.currentTimeMillis();
		this.m_startHoldConnectionTime = -1;
		m_LeaveTransaction.add(this);
	}

	public void commit() throws SQLException {
		if (this.isStartTransaction == false) {
			throw new SQLException("不能提交未开始的事务");
		}
		if (this.m_onlyRollback == true) {
			if(log.isDebugEnabled()){
		        log.debug("因为在事务提交时，发现事务已经是只能回滚，所以直接执行rollback操作",setRollbackOnlyAddr);
			}
			this.rollback();//执行回滚操作
			return;
		}

		try {
			// 如果当前需要提交的数据库连接数量大于1，则需要分别校验下每个连接的可用性
			if (this.m_conn.size() > 1) {
				for (TBConnection conn:this.m_conn.values()) {
					// 校验每个连接的可用性，如果连接中断，则会抛异常
					conn.judgeConnAvailable();
				}
			}
			for (TBConnection conn:this.m_conn.values()) {
				conn.realCommit();
				conn.realClose();
			}
		} catch (Throwable e) {
			log.fatal(e.getMessage(), e);
			this.m_isCommitError = true;
			for (TBConnection conn:this.m_conn.values()) {
				try {
					conn.realRollback();
					conn.realClose();
				} catch (Throwable et) {
					log.fatal("Rollback ERROR:" + et.getMessage(), et);
				}
			}
			throw new SQLException(e.getMessage());
		} finally {
			if(log.isWarnEnabled() && this.m_startHoldConnectionTime > 0){
				long spendtime = (System.currentTimeMillis() - this.m_startHoldConnectionTime);
				if(spendtime >= warnTime){
				   log.warn("事务持续时间过长:" + spendtime,this.m_addr);
				}
			}
			this.clear();
		}
	}

	public void rollback() throws SQLException {
		if (this.m_isCommitError == true) {
			// 提交失败后抛出异常导致的重复回滚
			m_isCommitError = false;
			return;
		}
		Throwable ex = null;
		try {
			for (TBConnection conn:this.m_conn.values()) {
				try {
					conn.realRollback();
					conn.realClose();
				} catch (Throwable e) {
					ex = e;
				}
			}
		} finally {
			if(log.isWarnEnabled() && this.m_startHoldConnectionTime > 0){
				long spendtime = (System.currentTimeMillis() - this.m_startHoldConnectionTime);
				if(spendtime >= warnTime){
					if (log.isDebugEnabled()) {
						log.debug("事务持续时间过长:" + spendtime,this.m_addr);
					} else {
						log.warn("事务持续时间过长:" + spendtime);
					}
				}
			}
			this.clear();
		}
		if (ex != null) {
			throw new SQLException(ex.getMessage());
		}
	}

	public boolean isRollbackOnly() {
		return this.m_onlyRollback;
	}
	public void setRollbackOnly() {
		if(this.isStartTransaction() == false){
			log.warn("还没有开始事务，不能设置只能回滚的属性");
		} else {
			this.m_onlyRollback = true;
			this.setRollbackOnlyAddr = new Exception("调用setRollbackOnly的地址");
		}
	}

	public void clear() {
		this.isStartTransaction = false;
		this.m_isCommitError = false;
		m_conn.clear();
		if (m_sourceStack != null) {
			m_sourceStack.clear();
			m_sourceStack = null;
		}
		m_onlyRollback = false;
		m_addr = null;
		m_startTime = 0;
		timeOut = -1;
		m_LeaveTransaction.remove(this);
	}
}
