package com.taobao.pamirs.transaction;

public abstract class PamirsTransactionAction {
	
	public abstract Object doInTransaction(PamirsTransactionStatus status) throws Exception;

}
