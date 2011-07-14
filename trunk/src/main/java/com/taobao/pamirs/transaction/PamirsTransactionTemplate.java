package com.taobao.pamirs.transaction;

import com.taobao.pamirs.transaction.TBTransactionAnnotation;
import com.taobao.pamirs.transaction.TBTransactionType;
import com.taobao.pamirs.transaction.TBTransactionTypeAnnotation;
import com.taobao.pamirs.transaction.TransactionManager;

@TBTransactionAnnotation
public class PamirsTransactionTemplate {
	/**
	 * ΪJoin����ģ��
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
				//throw new RuntimeException("�����Ѿ����ع������ؽ��:" + result);
			}

		} catch (Throwable e) {
			status.setRollbackOnly();
			throw new RuntimeException(e);
		}
		return result;
	}
	/**
	 * ΪINDEPEND����ģ��
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
				//throw new RuntimeException("�����Ѿ����ع������ؽ��:" + result);
			}

		} catch (Throwable e) {
			status.setRollbackOnly();
			throw new RuntimeException(e);
		}
		return result;
	}
}