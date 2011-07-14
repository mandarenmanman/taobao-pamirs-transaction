package com.taobao.pamirs.transaction;

public class PamirsTransactionStatus {
	private boolean rollbackOnly ;

	public boolean isRollbackOnly() {
		return rollbackOnly;
	}

	public void setRollbackOnly() {
		this.rollbackOnly = true;
	}
}