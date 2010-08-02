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
 * 事务处理类
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
	@SuppressWarnings("unchecked")
	protected Object[] getAdvicesAndAdvisorsForBean(Class beanClass,
			String beanName, TargetSource targetSource) throws BeansException {

		if (beanClass.isAnnotationPresent(TBTransactionAnnotation.class) || TBTransactionHint.class.isAssignableFrom(beanClass)) {
			if (log.isDebugEnabled()) {
				log.debug(beanClass + ":" + beanName);
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

	public static TBTransactionType getTBTransactionType(Method method) {
		TBTransactionTypeAnnotation transactionTypeAnn = method
				.getAnnotation(TBTransactionTypeAnnotation.class);
		if (transactionTypeAnn != null) {
			return transactionTypeAnn.value();
		} else {
			return TBTransactionType.JOIN;
		}
	}

	public Object invoke(MethodInvocation invocation) throws Throwable {
		Method method = invocation.getMethod();
		Object result = null;
		long startTime = System.currentTimeMillis();
		// 执行要处理对象的原本方法
		TBTransactionType transactionType = getTBTransactionType(method);
		boolean isSelfStartTransaction = false;
		boolean isSuspend = false;
		boolean isStartTransactionInParent = TransactionManager
				.getTransactionManager().isStartTransaction();
		try {
			if (transactionType == TBTransactionType.JOIN) {// 加入事务
				isSuspend = false;
				if (isStartTransactionInParent == false) {// 在父亲里面没有开始事务
					isSelfStartTransaction = true;
					TransactionManager.getTransactionManager().begin();
				} else {
					isSelfStartTransaction = false;
				}
			} else {// 独立事务
				if (isStartTransactionInParent == false) {// 在父亲里面没有开始事务,则不需要挂起外层事务
					TransactionManager.getTransactionManager().begin();
					isSuspend = false;
					isSelfStartTransaction = true;
				} else {// 挂起外部事务后,开始新的事务
					TransactionManager.getTransactionManager().suspend();
					isSuspend = true;
					TransactionManager.getTransactionManager().begin();
					isSelfStartTransaction = true;
				}
			}

			if (log.isDebugEnabled()) {
				log.debug(method.getDeclaringClass().getName() + "."
						+ method.getName() + ": suspendTransaction = "
						+ isSuspend + ", startTransaction = "
						+ isSelfStartTransaction);
			}
			result = invocation.proceed();
			if (isSelfStartTransaction == true) {// 自己开始的,则提交事务
				TransactionManager.getTransactionManager().commit();
				if (log.isDebugEnabled()) {
					log.debug(method.getDeclaringClass().getName() + "."
							+ method.getName() + ": commitTransaction ");
				}
			}
		} catch (Throwable e) {
			try {
				if (isSelfStartTransaction == true) {// 自己开始的,则回滚事务
					TransactionManager.getTransactionManager().rollback();
					if (log.isDebugEnabled()) {
						log.debug(method.getDeclaringClass().getName() + "."
								+ method.getName() + ": rollbackTransaction ");
					}
				} else {// 设置事务为只能回滚
					TransactionManager.getTransactionManager().setRollbackOnly();
					if (log.isDebugEnabled()) {
						log.debug(method.getDeclaringClass().getName() + "."
								+ method.getName() + ": setRollbackOnly ");
					}
				}
			} catch (Throwable ex) {
				log.fatal("回滚事务失败", ex);
			}
			throw e;
		} finally {
			try {
				if (isSuspend == true) {// 恢复挂起的事务
					TransactionManager.getTransactionManager().resume();
					if (log.isDebugEnabled()) {
						log.debug(method.getDeclaringClass().getName() + "."
								+ method.getName() + ": resumeTransaction ");
					}
				}
			} catch (Throwable ex) {
				log.fatal("恢复挂起的事务失败", ex);
			}
			if (log.isDebugEnabled()) {
				log.debug("execute " + method.getDeclaringClass().getName()
						+ "." + method.getName() + " 耗时:"
						+ (System.currentTimeMillis() - startTime));
			}
		}
		return result;
	}
}
