
package com.taobao.pamirs.transaction.test;
import org.junit.Test;
import org.unitils.UnitilsJUnit4;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByName;

import com.taobao.pamirs.transaction.TransactionManager;

/**
 * 
 * @author xuannan
 *
 */

@SpringApplicationContext( { "TransactionSpring4Mysql.xml" })
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
      this.testBean.testOneTransaction();
	}
	@Test
	public void testIndepend() throws Exception{
		TransactionManager.getTransactionManager().begin();
		this.testBean.insertHJ(-100);
		this.testBean.selectHJ(-100);
		this.testBean.insertHJIndepend(-200);
		this.testBean.selectHJ(-200);
		TransactionManager.getTransactionManager().rollback();
	}
}	