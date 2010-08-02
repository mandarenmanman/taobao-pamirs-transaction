package com.taobao.pamirs.transaction.test;

import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.unitils.UnitilsJUnit4;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByName;

import com.taobao.pamirs.transaction.TransactionManager;
import com.taobao.pamirs.transaction.TBDataSourceImpl;
/**
 * 
 * @author xuannan
 *
 */
@SpringApplicationContext( { "TransactionSpring.xml" })
public class TransactionTest extends UnitilsJUnit4 implements Runnable{
    
	public static void main(String[] args) throws Exception {
		Map<String,DataSource> dsList = new HashMap<String,DataSource>();
		BasicDataSource dsHj = new BasicDataSource();
		dsHj.setDriverClassName("oracle.jdbc.driver.OracleDriver");
		dsHj.setUrl("jdbc:oracle:thin:@localhost:1521:oracle9i");
		dsHj.setUsername("hj");
		dsHj.setPassword("hj");
		dsList.put("hj", new TBDataSourceImpl("hj",dsHj));
		BasicDataSource dsAppframe = new BasicDataSource();
		dsAppframe.setDriverClassName("oracle.jdbc.driver.OracleDriver");
		dsAppframe.setUrl("jdbc:oracle:thin:@localhost:1521:oracle9i");
		dsAppframe.setUsername("jz");
		dsAppframe.setPassword("jz");
		
		dsList.put("jz", new TBDataSourceImpl("jz",dsAppframe));
		
		new TransactionManager().setDataSourceMap(dsList);
		
		for(int i=0;i<10;i++){
			new Thread(new TransactionTest(i + 1)).start();
		}
	}
	public TransactionTest(){
		
	}
	public TransactionTest(int num){
		this.threadNum = num;
	}
    public void run(){

		for(int i=0;i <100;i++){
    	try{
    		test();
    	            //this.executeHj("sss");
    	           //Thread.sleep(10);
      
    	}catch(Exception e){
    		e.printStackTrace();
    	}
		}  
    }
	int threadNum =0;
	
	@SpringBeanByName
    TransactionManager transactionManager;
	
	public void setTransactionManager(TransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}
	@org.junit.Test
	public void test() throws Exception{
		try{
      TransactionManager.getTransactionManager().begin();
      executeHj("qh4");
      TransactionManager.getTransactionManager().suspend();
      TransactionManager.getTransactionManager().begin();
      try{
        executeAppframe("lhl4");   
        TransactionManager.getTransactionManager().commit();
      }catch(Throwable e){
    	  e.printStackTrace();
    	  TransactionManager.getTransactionManager().rollback();
      }finally{
          TransactionManager.getTransactionManager().resume();
      }
      TransactionManager.getTransactionManager().commit();
		}catch(Throwable e){
			e.printStackTrace();
			TransactionManager.getTransactionManager().rollback();
		}
	}
	public void executeHj(String s)throws Exception{
		Connection conn = null;
		try{
			conn = TransactionManager.getConnection("hj");
			 Statement statement = conn.createStatement();
			 statement.execute("update tt set name = '" + s + "',num = num + 1,MODIFY_DATE = sysdate where product_template_id =" + this.threadNum);
			 System.out.println(Thread.currentThread() + " : update tt set name = '" + s + "',num = num + 1,MODIFY_DATE = sysdate where product_template_id =" + this.threadNum);
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
	public void executeAppframe(String s )throws Exception{
		Connection conn = null;
		try{
			 conn = TransactionManager.getConnection("jz");
			 Statement statement = conn.createStatement();
			 statement.execute("update test set name = '" + s + "' ,MODIFY_DATE = sysdate,num = num + 1 where id =" + this.threadNum);
			 statement.close();
		}finally{
			if(conn != null){
				conn.close();
			}
		}
	}
}
