package com.taobao.pamirs.unittest.test;

public class ProductDO {
	private long id;
	private String name;
	private double price;

	public ProductDO(long aId, String aName,double aPrice) {
		this.id = aId;
		this.name = aName;
		this.price = aPrice;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

}
