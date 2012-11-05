package com.taobao.pamirs.transaction;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;

public class TBMethodInvocation  implements TBMethodAction{

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
		 throw new RuntimeException("没有实现的方法");
	}

	public Object getThis() {
		 return this.runObject;
	}

	public Object proceed() throws Throwable {
		return this.method.invoke(this.runObject,this.arguments);
	}

	public String getMethodName() throws Throwable {
		return method.getDeclaringClass().getName() + "."
				+ method.getName();
	}

}
