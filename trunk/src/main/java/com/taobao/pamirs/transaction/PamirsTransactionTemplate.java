package com.taobao.pamirs.transaction;

public class PamirsTransactionTemplate {

	/**
	 * ΪJoin����ģ��
	 * 
	 * @param action
	 * @return
	 */
	public Object execute(PamirsTransactionAction action) {
		PamirsTransactionStatus status = new PamirsTransactionStatus();
		try {
			return TransactionManager.executeMethod(new PamirsMethodAction(action,
					status), TBTransactionType.JOIN);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * ΪINDEPEND����ģ��
	 * 
	 * @param action
	 * @return
	 */
	public Object executeIndepend(PamirsTransactionAction action) {
		PamirsTransactionStatus status = new PamirsTransactionStatus();
		try {
			return TransactionManager.executeMethod(new PamirsMethodAction(action,
					status), TBTransactionType.INDEPEND);
			 
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	class PamirsMethodAction implements TBMethodAction {

		private PamirsTransactionAction action;
		private PamirsTransactionStatus status;

		public PamirsMethodAction(PamirsTransactionAction action,
				PamirsTransactionStatus status) {
			this.action = action;
			this.status = status;
		}

		public String getMethodName() throws Throwable {
			return this.getClass().getName();
		}

		public Object proceed() throws Throwable {
			Object result = null;
			result = action.doInTransaction(status);
			if (status.isRollbackOnly()) { // �˴���ֹҵ��ʹ����δ���쳣�׳�,ֻ�����˻ع�
				TransactionManager.getTransactionManager().setRollbackOnly();
			}

			return result;
		}

	}
}
