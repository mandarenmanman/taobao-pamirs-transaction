package com.taobao.pamirs.transaction.test;

public interface ITestBean {
	public String upper(String s);
	public void executeHj(String s)throws Exception;
	public void executeJz(String s)throws Exception;
	public void executeSelect(String s )throws Exception;
	public void testOneTransaction()throws Exception;
}
