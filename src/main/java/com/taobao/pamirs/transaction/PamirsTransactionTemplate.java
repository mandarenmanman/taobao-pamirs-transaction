package com.taobao.pamirs.transaction;

import com.taobao.pamirs.transaction.TBTransactionAnnotation;
import com.taobao.pamirs.transaction.TBTransactionType;
import com.taobao.pamirs.transaction.TBTransactionTypeAnnotation;
import com.taobao.pamirs.transaction.TransactionManager;

@TBTransactionAnnotation
public class PamirsTransactionTemplate {
	/**
	 * 为Join事务模板
	 * @param action
	 * @return
	 */
	@TBTransactionTypeAnnotation(TBTransactionType.JOIN)
	public Object execute(PamirsTransactionAction action) {
		Object result = null;
		PamirsTransactionStatus status = new PamirsTransactionStatus();
		try {

			result = action.doInTransaction(status);
			if (status.isRollbackOnly()) {
				TransactionManager.getTransactionManager().setRollbackOnly();
				//throw new RuntimeException("事务已经被回滚，返回结果:" + result);
			}

		} catch (Throwable e) {
			status.setRollbackOnly();
			throw new RuntimeException(e);
		}
		return result;
	}
	/**
	 * 为INDEPEND事务模板
	 * @param action
	 * @return
	 */
	@TBTransactionTypeAnnotation(TBTransactionType.INDEPEND)
	public Object executeIndepend(PamirsTransactionAction action) {
		Object result = null;
		PamirsTransactionStatus status = new PamirsTransactionStatus();
		try {

			result = action.doInTransaction(status);
			if (status.isRollbackOnly()) {
				TransactionManager.getTransactionManager().setRollbackOnly();
				//throw new RuntimeException("事务已经被回滚，返回结果:" + result);
			}

		} catch (Throwable e) {
			status.setRollbackOnly();
			throw new RuntimeException(e);
		}
		return result;
	}
}