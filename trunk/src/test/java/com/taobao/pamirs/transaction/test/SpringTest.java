package com.taobao.pamirs.unittest.test;


import org.unitils.UnitilsJUnit4;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByName;

import com.taobao.pamirs.unittest.CreateTestUnit;
import com.taobao.pamirs.unittest.MonitorCallItem;
import com.taobao.pamirs.unittest.MonitorManager;
import com.taobao.pamirs.unittest.TestConfig;

/**
 * 
 * @author xuannan
 *
 */
@SpringApplicationContext( { "SpringConfig.xml" })
public class SpringTest extends UnitilsJUnit4{
	@SpringBeanByName
	OrderManager orderManager;	
	@org.junit.Test
	public void testUnitCreate() throws Exception{
		MonitorManager.startMonitor(new MonitorCallItem());
		this.orderManager.createOrder(101,1000,5);
		MonitorCallItem root = MonitorManager.getRoot().getChildren().get(0);
		MonitorManager.clearMonitor();	
		StringBuilder builder = new StringBuilder();
		root.print(builder, 1);	
		System.out.println(builder);
		CreateTestUnit.writeTestData(root, new TestConfig[] {
				new TestConfig("orderManager", "createOrder",
						new String[] { "productManager.queryProduct" }, null),
				new TestConfig("userCheck", "checkUser", null, null),
				new TestConfig("rateManager", "computer", null, null) });
	}

	@org.junit.Test
	public void testUnitTestRun() throws Exception{
		CreateTestUnit.runTest("orderManager$createOrder");
		CreateTestUnit.runTest("rateManager$computer");
		CreateTestUnit.runTest("userCheck$checkUser");
	}

	public void setOrderManager(OrderManager orderManager) {
		this.orderManager = orderManager;
	}


}
