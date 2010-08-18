package com.taobao.pamirs.transaction;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInvocation;

public class TBMethodInvocation implements MethodInvocation {

	Method method;
	Object runObject;
	Object[] arguments;
	public TBMethodInvocation(Method aMethod,Object aRunObject,Object[] aArguments){
		this.method = aMethod;
		this.runObject = aRunObject;
		this.arguments = aArguments;
		
	}
	public Method getMethod() {
		 return method;
	}

	public Object[] getArguments() {
		 return arguments;
	}

	public AccessibleObject getStaticPart() {
		 throw new RuntimeException("û��ʵ�ֵķ���");
	}

	public Object getThis() {
		 return this.runObject;
	}

	public Object proceed() throws Throwable {
		return this.method.invoke(this.runObject,this.arguments);
	}

}
