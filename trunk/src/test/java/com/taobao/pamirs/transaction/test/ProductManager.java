package com.taobao.pamirs.unittest.test;

public class ProductManager {
	public ProductDO queryProduct(long productId){
		System.out.println("call com.taobao.pamirs.monitor.test.ProductManager.queryProduct");
		return new ProductDO(productId,"�����콢��",50.0);
	}
}
