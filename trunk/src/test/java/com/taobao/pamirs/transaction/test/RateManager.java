package com.taobao.pamirs.unittest.test;

/**
 * 
 * @author xuannan
 * 
 */
public class RateManager {
	public RateResultDO computer(ProductDO productDO,int billCycle) {
		return new RateResultDO(productDO.getId(), productDO.getPrice() * billCycle);
	}

}
