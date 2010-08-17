/**
 * Project: taobao-pamirs-transaction
 * 
 * AbstractTransactionalDatasourceTests.java File Created at 2010-8-6 ÏÂÎç04:00:38
 * 
 * 
 * Copyright 2009 Taobao.com Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Taobao Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Taobao.com.
 */
package com.taobao.pamirs.transaction.test;

import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import com.taobao.pamirs.transaction.TransactionManager;

/**
 * @author <a href="mailto:canjian@taobao.com">canjian</a>
 * 
 */
public abstract class AbstractTransactionalDatasourceTests extends AbstractDependencyInjectionSpringContextTests {

	@Override
	protected void onSetUp() throws Exception {
		TransactionManager.getTransactionManager().begin();
	}

	@Override
	protected void onTearDown() throws Exception {
		TransactionManager.getTransactionManager().rollback();
	}

}
