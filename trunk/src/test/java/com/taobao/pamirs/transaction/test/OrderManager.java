package com.taobao.pamirs.unittest.test;

public class OrderManager {

	UserCheck userCheck;
	ProductManager productManager;
	RateManager rateManager;

	public RateResultDO createOrder(long userId, long productId, int billCycle) {
		userCheck.checkUser(userId, productId);
		ProductDO productDO = productManager.queryProduct(productId);
		return rateManager.computer(productDO, billCycle);
	}
	public void setUserCheck(UserCheck userCheck) {
		this.userCheck = userCheck;
	}

	public void setProductManager(ProductManager productManager) {
		this.productManager = productManager;
	}

	public void setRateManager(RateManager rateManager) {
		this.rateManager = rateManager;
	}

}
