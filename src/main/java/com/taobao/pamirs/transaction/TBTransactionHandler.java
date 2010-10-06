package com.taobao.pamirs.transaction;

import java.lang.reflect.Method;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.Advisor;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
/**
 * ��������
 * @author xuannan
 *
 */
@SuppressWarnings("serial")
public class TBTransactionHandler extends AbstractAutoProxyCreator implements BeanFactoryAware {
	private static transient Log log = LogFactory
			.getLog(TBTransactionHandler.class);
	
	TransactionAdvisor[] asdvisors = new TransactionAdvisor[] { new TransactionAdvisor() };
	
	BeanFactory beanFactory;

	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected Object[] getAdvicesAndAdvisorsForBean(Class beanClass,
			String beanName, TargetSource targetSource) throws BeansException {
        if (beanClass.isAnnotationPresent(TBTransactionAnnotation.class) || TBTransactionHint.class.isAssignableFrom(beanClass)) {
			if (log.isDebugEnabled()) {
				log.debug("�������" + beanClass + ":" + beanName);
			}
			return asdvisors;
		}
		return DO_NOT_PROXY;
	}
}

class TransactionAdvisor implements Advisor {
	Advice advice = new TransactionRoundAdvice();

	public Advice getAdvice() {
		return advice;
	}

	public boolean isPerInstance() {
		return false;
	}
}

class TransactionRoundAdvice implements MethodInterceptor, Advice {
	private static transient Log log = LogFactory
			.getLog(TransactionRoundAdvice.class);

	public Object invoke(MethodInvocation invocation) throws Throwable {
		TBTransactionType transactionType = getTBTransactionType(invocation.getMethod());
		return invokeInner(invocation,transactionType);
	}
	public static TBTransactionType getTBTransactionType(Method method) {
		TBTransactionTypeAnnotation transactionTypeAnn = method
				.getAnnotation(TBTransactionTypeAnnotation.class);
		if (transactionTypeAnn != null) {
			return transactionTypeAnn.value();
		} else {
			return TBTransactionType.JOIN;
		}
	}

	public static Object invokeInner(MethodInvocation invocation,TBTransactionType transactionType) throws Throwable {
		String methodName =invocation.getMethod().getDeclaringClass().getName() + "."+ invocation.getMethod().getName(); 
		Object result = null;
		long startTime = System.currentTimeMillis();
		// ִ��Ҫ���������ԭ������
		boolean isSelfStartTransaction = false;
		boolean isSuspend = false;
		boolean isStartTransactionInParent = TransactionManager
				.getTransactionManager().isStartTransaction();
		try {
			if (transactionType == TBTransactionType.JOIN) {// ��������
				isSuspend = false;
				if (isStartTransactionInParent == false) {// �ڸ�������û�п�ʼ����
					isSelfStartTransaction = true;
					TransactionManager.getTransactionManager().begin();
				} else {
					isSelfStartTransaction = false;
				}
			} else {// ��������
				if (isStartTransactionInParent == false) {// �ڸ�������û�п�ʼ����,����Ҫ�����������
					TransactionManager.getTransactionManager().begin();
					isSuspend = false;
					isSelfStartTransaction = true;
				} else {// �����ⲿ�����,��ʼ�µ�����
					TransactionManager.getTransactionManager().suspend();
					isSuspend = true;
					TransactionManager.getTransactionManager().begin();
					isSelfStartTransaction = true;
				}
			}

			if (log.isDebugEnabled()) {
				log.debug(methodName + ": suspendTransaction = "
						+ isSuspend + ", startTransaction = "
						+ isSelfStartTransaction);
			}
			result = invocation.proceed();
			if (isSelfStartTransaction == true) {// �Լ���ʼ��,���ύ����
				TransactionManager.getTransactionManager().commit();
				if (log.isDebugEnabled()) {
					log.debug(methodName + ": commitTransaction ");
				}
			}
		} catch (Throwable e) {
			try {
				if (isSelfStartTransaction == true) {// �Լ���ʼ��,��ع�����
					TransactionManager.getTransactionManager().rollback();
					if (log.isDebugEnabled()) {
						log.debug(methodName + ": rollbackTransaction ");
					}
				} else {// ��������Ϊֻ�ܻع�
					TransactionManager.getTransactionManager().setRollbackOnly();
					if (log.isDebugEnabled()) {
						log.debug(methodName + ": setRollbackOnly ");
					}
				}
			} catch (Throwable ex) {
				log.fatal("�ع�����ʧ��", ex);
			}
			throw e;
		} finally {
			try {
				if (isSuspend == true) {// �ָ����������
					TransactionManager.getTransactionManager().resume();
					if (log.isDebugEnabled()) {
						log.debug(methodName + ": resumeTransaction ");
					}
				}
			} catch (Throwable ex) {
				log.fatal("�ָ����������ʧ��", ex);
			}
			if (log.isDebugEnabled()) {
				log.debug("execute " + methodName + " ��ʱ(ms):"
						+ (System.currentTimeMillis() - startTime));
			}
		}
		return result;
	}
}