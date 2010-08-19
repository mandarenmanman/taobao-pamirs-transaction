package com.taobao.pamirs.transaction.test;

import com.taobao.pamirs.transaction.TBTransactionAnnotation;
import com.taobao.pamirs.transaction.TBTransactionType;
import com.taobao.pamirs.transaction.TBTransactionTypeAnnotation;

@TBTransactionAnnotation
public interface ITestBean {
	public String upper(String s);
	public void executeHj(String s)throws Exception;
	public void executeJz(String s)throws Exception;
	public void executeSelect(String s )throws Exception;
	public void testOneTransaction()throws Exception;
	public void insertHJ(int id)throws Exception;
	public void selectHJ(int id)throws Exception;
	
	@TBTransactionTypeAnnotation(TBTransactionType.INDEPEND)
	public void insertHJIndepend(int id)throws Exception;
}
