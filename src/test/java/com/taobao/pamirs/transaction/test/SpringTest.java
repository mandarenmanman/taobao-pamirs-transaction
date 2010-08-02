package com.taobao.pamirs.transaction.test;


import org.unitils.UnitilsJUnit4;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByName;

import com.taobao.pamirs.transaction.TransactionManager;
import com.taobao.pamirs.transaction.TBTransactionImpl;

/**
 * 
 * @author xuannan
 *
 */
@SpringApplicationContext( { "TransactionSpring.xml" })
public class SpringTest extends UnitilsJUnit4{
	@SpringBeanByName
	ITestBean testBean;
	public void setTestBean(ITestBean testBean) {
		this.testBean = testBean;
	}
	
	@org.junit.Test
	public void testCloseConnectionOnlySelect() throws Exception{
		this.testBean.executeSelect("sss");
	}
	@org.junit.Test
	public void test() throws Exception{
		TransactionManager.getTransactionManager().begin();
		TBTransactionImpl.debug();
		this.testBean.executeHj("ssssss");
		TBTransactionImpl.debug();
		this.testBean.executeAppframe("ssssss");
		TBTransactionImpl.debug();
		TransactionManager.getTransactionManager().commit();
		TBTransactionImpl.debug();
	}

}
