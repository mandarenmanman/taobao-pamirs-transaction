package com.taobao.pamirs.transaction;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

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
import org.springframework.core.PriorityOrdered;
/**
 * 事务处理类
 * @author xuannan
 *
 */
@SuppressWarnings("serial")
public class TBTransactionHandler extends AbstractAutoProxyCreator implements BeanFactoryAware ,PriorityOrdered {
	private static transient Log log = LogFactory.getLog(TBTransactionHandler.class);
	
	/**
	 * 需要进行事务控制的bean名称
	 */
	private List<String> beanList;
	
	BeanFactory beanFactory;

	/**
	 * 设置是否在TBConnection中获取SessionId
	 * @param isSetConnectionInfo
	 */
	public void setSetConnectionInfo(boolean isSetConnectionInfo) {
		 TBConnection.isSetConnectionInfo = isSetConnectionInfo;
	}
	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}
	
	public void setBeanList(List<String> beanList) {
		this.beanList = beanList;
	}
	
	public TBTransactionHandler(){
		//以最高优先级加载该代理
		this.setOrder(HIGHEST_PRECEDENCE);
	}
	
	@SuppressWarnings({"rawtypes" })
	protected Object[] getAdvicesAndAdvisorsForBean(Class beanClass,
			String beanName, TargetSource targetSource) throws BeansException {
        if (isAnnotationPresent(beanClass,TBTransactionAnnotation.class)
        		|| TBTransactionHint.class.isAssignableFrom(beanClass)
        		|| (this.beanList != null && this.beanList.contains(beanName))) {
			if (log.isDebugEnabled()) {
				log.debug("事务包裹" + beanClass + ":" + beanName);
			}
			/**
			 * 对需要被代理的类进行判断：如果被代理类的访问修饰符是final，则是使用PROXY来代理
			 * 如果被代理的类已经是PROXY代理，其访问修饰符也是final
			 * 如果存在被代理类有内部类，该类为final的，该被代理类仍可以使用CGLIB代理
			 */
			if(targetBeanIsFinal(beanClass)){
				this.setProxyTargetClass(false);//PROXY
			}else{
				this.setProxyTargetClass(true);//CGLIB
			}
			return new TransactionAdvisor[]{new TransactionAdvisor(beanClass)};
		}
		return DO_NOT_PROXY;
	}
	public static boolean isAnnotationPresent(Class<?> aClass,Class<? extends Annotation> annotationClass){
		while (aClass != null) {
			if (aClass.isAnnotationPresent(annotationClass)) {
				return true;
			}
			for (Class<?> interfaceClass : aClass.getInterfaces()) {
				if (interfaceClass.isAnnotationPresent(annotationClass)) {
					return true;
				}
			}
			aClass = aClass.getSuperclass();
		}
		return false;
	}	
	private boolean targetBeanIsFinal(Class<?> clazz){
		String inMods = Modifier.toString(clazz.getModifiers());
    	if(inMods.contains("final")){
    		return true;
    	}else{
    		return false;
    	}
	}
	
}

class TransactionAdvisor implements Advisor {
 
	TransactionRoundAdvice advice;
	TransactionAdvisor(Class<?> aBeanClass){
		advice = new TransactionRoundAdvice(aBeanClass);
	}
	public Advice getAdvice() {
		return advice;
	}

	public boolean isPerInstance() {
		return false;
	}
}



 
class ReflectMethodAction implements TBMethodAction {

	private MethodInvocation invocation;

	public ReflectMethodAction(MethodInvocation invocation) {
		this.invocation = invocation;
	}

	public Object proceed() throws Throwable {
		return invocation.proceed();
	}

	public String getMethodName() throws Throwable {
		return invocation.getMethod().getDeclaringClass().getName() + "."
				+ invocation.getMethod().getName();
	}

}


class TransactionRoundAdvice implements MethodInterceptor, Advice {
	private static transient Log log = LogFactory
			.getLog(TransactionRoundAdvice.class);
    private Class<?> beanClass; 
    TransactionRoundAdvice(Class<?> aBeanClass){
    	this.beanClass = aBeanClass;
    }
	public Object invoke(MethodInvocation invocation) throws Throwable {
		TBTransactionType transactionType = getTBTransactionType(invocation.getMethod());
		return invokeInner(new ReflectMethodAction(invocation),transactionType);
	}
	
		
	public TBTransactionType getTBTransactionType(Method interfaceMethod) throws Exception {
		//先看接口上是否定义了Annotation
		TBTransactionTypeAnnotation transactionTypeAnn = interfaceMethod.getAnnotation(TBTransactionTypeAnnotation.class);
		if(transactionTypeAnn == null){		
			//从实际的实现类上获取事务Annotation
			Method realMethod = this.beanClass.getMethod(interfaceMethod.getName(),interfaceMethod.getParameterTypes());
			transactionTypeAnn = realMethod.getAnnotation(TBTransactionTypeAnnotation.class);
		}	
		if (transactionTypeAnn != null) {
			return transactionTypeAnn.value();
		} else {
			return TBTransactionType.JOIN;
		}
	}

	
	public static Object invokeInner(MethodInvocation invocation,TBTransactionType transactionType) throws Throwable {
		return invokeInner(new ReflectMethodAction(invocation), transactionType);
	}
	
	
	public static Object invokeInner(TBMethodAction methodAction,TBTransactionType transactionType) throws Throwable {
		String methodName =methodAction.getMethodName();
		Object result = null;
		long startTime = System.currentTimeMillis();
		// 执行要处理对象的原本方法
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
				log.debug(methodName + ": suspendTransaction = "
						+ isSuspend + ", startTransaction = "
						+ isSelfStartTransaction);
			}
			result = methodAction.proceed();
			if (isSelfStartTransaction == true) {// 自己开始的,则提交事务
				TransactionManager.getTransactionManager().commit();
				if (log.isDebugEnabled()) {
					log.debug(methodName + ": commitTransaction ");
				}
			}
		} catch (Throwable e) {
			try {
				if (isSelfStartTransaction == true) {// 自己开始的,则回滚事务
					TransactionManager.getTransactionManager().rollback();
					if (log.isDebugEnabled()) {
						log.debug(methodName + ": rollbackTransaction ");
					}
				} else {// 设置事务为只能回滚
					TransactionManager.getTransactionManager().setRollbackOnly();
					if (log.isDebugEnabled()) {
						log.debug(methodName + ": setRollbackOnly ");
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
						log.debug(methodName + ": resumeTransaction ");
					}
				}
			} catch (Throwable ex) {
				log.fatal("恢复挂起的事务失败", ex);
			}
			if (log.isDebugEnabled()) {
				log.debug("execute " + methodName + " 耗时(ms):"
						+ (System.currentTimeMillis() - startTime));
			}
		}
		return result;
	}
}
