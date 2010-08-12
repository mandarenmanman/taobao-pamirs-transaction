package com.taobao.pamirs.transaction;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * ϵͳʱ�����Ϊ�˱���������ʱ�䲻һ�µ��µ����⣬ͳһ��ȡ���ݿ�ʱ��
 * 
 * @author xuannan
 * 
 */
@TBTransactionAnnotation
public class TBSysdateManager implements BeanPostProcessor {
	private static transient Log log = LogFactory
			.getLog(TBSysdateManager.class);
	/**
	 * ϵͳ����ʱ������ݿ�ʱ��
	 */
	private static long initialDataBaseTime = -2;
	/**
	 * ϵͳ����ʱ��ĵ�ǰ����ʱ��
	 */
	private static long initialLocalServerBaseTime = -1;

	private DataSource dataSource;

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public static long getCurrentTimeMillis() {
		if (initialDataBaseTime > 0) {
			return System.currentTimeMillis() - initialLocalServerBaseTime
					+ initialDataBaseTime;
		} else {
			// û����Spring�����������Ϣ��ֻ��ȡ��ǰ����ʱ��
			return System.currentTimeMillis();
		}
	}

	public Object postProcessBeforeInitialization(Object bean, String beanName)
			throws BeansException {
		return bean;
	}

	public Object postProcessAfterInitialization(Object bean, String beanName)
			throws BeansException {
		if (dataSource == null) {
			initialDataBaseTime = -1;
		}
		Connection conn = null;
		try {
			conn = dataSource.getConnection();
			String sql = "";
			String dbType = conn.getMetaData().getDatabaseProductName();
			if ("oracle".equalsIgnoreCase(dbType)) {
				sql = "select sysdate from dual";
			} else if ("mysql".equalsIgnoreCase(dbType)) {
				sql = "select now()";
			}
			Statement statement = conn.createStatement();
			// ͬ����ʼ�����ط�������ʱ�����
			initialLocalServerBaseTime = System.currentTimeMillis();
			ResultSet resultSet = statement.executeQuery(sql);
			if (resultSet.next()) {
				initialDataBaseTime = resultSet.getTimestamp(1).getTime();
			} else {
				throw new SQLException("ȡϵͳʱ�����");
			}
			resultSet.close();
			statement.close();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					log.error(e.getMessage(), e);
				}
			}
		}
		log.info("��ʼ��ϵͳʱ��ɹ�,���ݿ��ʼʱ�䣺" + initialDataBaseTime + " ��ǰ����ʱ�䣺" + initialLocalServerBaseTime);
		return bean;
	}
}
