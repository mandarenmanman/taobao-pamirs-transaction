package com.taobao.pamirs.transaction.test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.springframework.util.Assert;

import com.taobao.pamirs.transaction.TBTransactionAnnotation;
import com.taobao.pamirs.transaction.TBTransactionHint;
import com.taobao.pamirs.transaction.TBTransactionImpl;
import com.taobao.pamirs.transaction.TBTransactionManagerImpl;
import com.taobao.pamirs.transaction.TBTransactionType;
import com.taobao.pamirs.transaction.TBTransactionTypeAnnotation;
import com.taobao.pamirs.transaction.TransactionManager;

/**
 * 
 * @author xuannan
 *
 */
@TBTransactionAnnotation
public class TestBean implements ITestBean,TBTransactionHint {
	int threadNum =5;
	public void testOneTransaction()throws Exception{

		executeHj("abc");
		executeJz("abc");
	}

	public void executeHj(String s)throws Exception{

		Connection conn = null;
		try{
 			 conn = TransactionManager.getConnection("hj");
			 Statement statement = conn.createStatement();
			 statement.execute("update tt set name = '" + s + "',num = num + 1 where product_template_id =" + this.threadNum);
			 statement.close();
			 conn.commit();
		}catch(Throwable e){
			e.printStackTrace();
			if(conn !=null){
			conn.rollback();
			}
			throw new Exception(e);
		}finally{
			if(conn != null){
				conn.close();
			}
		}
	}
	
	public void insertHJ(int id)throws Exception{

		Connection conn = null;
		try{
 			 conn = TransactionManager.getConnection("hj");
			 Statement statement = conn.createStatement();
			 statement.execute("insert into tt(product_template_id,name) values(" + id +",'qh-" + id +"')");
			 statement.close();
			 conn.commit();
		}catch(Throwable e){
			e.printStackTrace();
			if(conn !=null){
			conn.rollback();
			}
			throw new Exception(e);
		}finally{
			if(conn != null){
				conn.close();
			}
		}
	}	
	
	@TBTransactionTypeAnnotation(TBTransactionType.INDEPEND)
	public void insertHJIndepend(int id)throws Exception{
		this.insertHJ(id);
	}
	public void selectHJ(int id)throws Exception{
		Connection conn = null;
		try{
 			 conn = TransactionManager.getConnection("hj");
			 Statement statement = conn.createStatement();
			 ResultSet rs = statement.executeQuery("select * from  tt where product_template_id=" + id);
			 if(rs.next()){
				 System.out.println(rs.getString("name"));
			 }
			 rs.close();
			 statement.close();
			 conn.commit();
		}catch(Throwable e){
			e.printStackTrace();
			if(conn !=null){
			conn.rollback();
			}
			throw new Exception(e);
		}finally{
			if(conn != null){
				conn.close();
			}
		}
	}	
	
	public void executeJz(String s )throws Exception{
		Connection conn = null;
		try{
			 conn = TransactionManager.getConnection("jz");
			 Statement statement = conn.createStatement();
			 statement.execute("update test set name = '" + s + "',num = num + 1 where id =" + this.threadNum);
			 statement.close();
		}finally{
			if(conn != null){
				conn.close();
			}
		}
	}
    public TBTransactionImpl getCurrentTransaction(){
    	return ((TBTransactionManagerImpl)TransactionManager.getTransactionManager()).getCurrentTransaction();
    	
    }
	@TBTransactionTypeAnnotation(TBTransactionType.JOIN)
	public void executeSelect(String s )throws Exception{
		executeSelectInner(s);
		executeSelectInner(s);
			
	}
    public void executeSelectInner(String s )throws Exception{
    	Connection conn = null;
		try{
			 conn = TransactionManager.getConnection("hj");
			 Statement statement = conn.createStatement();
			 statement.executeQuery("select 1 from dual");
			 statement.close();
		//	 Assert.state(getCurrentTransaction().getConnectionCount() == 0, "在没有执行数据修改操作的时候，事务池中的连接数应该是 0 ");
//			 conn = TransactionManager.getConnection("hj");
//			 statement = conn.createStatement();
//			 statement.execute("update tt set name = '" + s + "',num = num + 1,MODIFY_DATE = sysdate where product_template_id =" + this.threadNum);
//			 statement.close();
			 Assert.state(getCurrentTransaction().getConnectionCount() == 1, "执行数据修改操作的时候，事务池中的连接数应该是1 ");
		}finally{
			if(conn != null){
				conn.close();
			}
		}
		
	}

	public String upper(String s) {
		return s.toUpperCase();
	}

}
