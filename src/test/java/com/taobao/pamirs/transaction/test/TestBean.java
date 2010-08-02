package com.taobao.pamirs.transaction.test;

import java.sql.Connection;

import java.sql.Statement;

import org.springframework.util.Assert;

import com.taobao.pamirs.transaction.TransactionManager;
import com.taobao.pamirs.transaction.TBTransactionAnnotation;
import com.taobao.pamirs.transaction.TBTransactionHint;
import com.taobao.pamirs.transaction.TBTransactionImpl;
import com.taobao.pamirs.transaction.TBTransactionManagerImpl;
import com.taobao.pamirs.transaction.TBTransactionType;
import com.taobao.pamirs.transaction.TBTransactionTypeAnnotation;

/**
 * 
 * @author xuannan
 *
 */
@TBTransactionAnnotation
public class TestBean implements ITestBean,TBTransactionHint {

	int threadNum =5;
	
	public String getMyName(){

		return "TestBean";
	}
	public String upper(String s){
		String result = s.toUpperCase();
		System.out.println(s + " ==> " + result);
		System.out.println(this.getMyName());
		
		return result;
	}

	@TBTransactionTypeAnnotation(TBTransactionType.INDEPEND)
	public void executeHj(String s)throws Exception{
		executeAppframe("ffffff");
		Connection conn = null;
		try{
 			 conn = TransactionManager.getConnection("hj");
			 Statement statement = conn.createStatement();
			 statement.execute("update tt set name = '" + s + "',num = num + 1,MODIFY_DATE = sysdate where product_template_id =" + this.threadNum);
			 statement.close();
			 TBTransactionImpl.debug();
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

	
	@TBTransactionTypeAnnotation(TBTransactionType.JOIN)
	public void executeAppframe(String s )throws Exception{
		Connection conn = null;
		try{
			 conn = TransactionManager.getConnection("jz");
			 Statement statement = conn.createStatement();
			 statement.execute("update test set name = '" + s + "' ,MODIFY_DATE = sysdate,num = num + 1 where id =" + this.threadNum);
			 statement.close();
				TBTransactionImpl.debug();
		}finally{
			if(conn != null){
				conn.close();
			}
		}
	}
    public TBTransactionImpl getCurrentTransaction(){
    	return ((TBTransactionManagerImpl)TransactionManager.getTransactionManager()).getCurrentTransaction();
    	
    }
	@TBTransactionTypeAnnotation(TBTransactionType.INDEPEND)
	public void executeSelect(String s )throws Exception{
		Connection conn = null;
		try{
			 conn = TransactionManager.getConnection("hj");
			 Statement statement = conn.createStatement();
			 statement.executeQuery("select * from dual");
			 conn.close();
			 Assert.state(getCurrentTransaction().getConnectionCount() == 0, "在没有执行数据修改操作的时候，事务池中的连接数应该是 0 ");
			 conn = TransactionManager.getConnection("hj");
			 statement = conn.createStatement();
			 statement.execute("update tt set name = '" + s + "',num = num + 1,MODIFY_DATE = sysdate where product_template_id =" + this.threadNum);
			 Assert.state(getCurrentTransaction().getConnectionCount() == 1, "执行数据修改操作的时候，事务池中的连接数应该是1 ");
		}finally{
			if(conn != null && conn.isClosed() == false){
				conn.close();
			}
		}
		
	}

}
