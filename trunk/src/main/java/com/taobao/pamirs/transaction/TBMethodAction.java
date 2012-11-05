package com.taobao.pamirs.transaction;

public interface TBMethodAction {
	
	Object proceed() throws Throwable;
	
	String getMethodName() throws Throwable;

}
