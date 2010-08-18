package com.taobao.pamirs.unittest.test;

public class RateResultDO {
	private long productId;
	private double money;

	public RateResultDO(long aProductId, double aMoney) {
		this.productId = aProductId;
		this.money = aMoney;
	}

	public long getProductId() {
		return productId;
	}

	public void setProductId(long productId) {
		this.productId = productId;
	}

	public double getMoney() {
		return money;
	}

	public void setMoney(double money) {
		this.money = money;
	}

}
