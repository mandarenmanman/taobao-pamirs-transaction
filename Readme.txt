TBTransaction的使用说明：

1、提供一个使用简单的事务管理器，降低开发人员通过代码控制事务的复杂度。 
2、同时支持多数据源的同时提交和同时回滚。这一点，理论上存在不安全因素，但实际使用可能中完全可以满足实际的工程需要。
3、系统会识别只有查询操作的数据连接，会及时释放，避免不必要的数据库连接资源占用。


实现原理：
1、TBTransactionHandler 在Spring初始化的是，对所有有注解@TBTransactionAnnotation或者实现接口TBTransactionHint
	的类增加TransactionRoundAdvice，在方法执行的时候切入事务的控制处理。
2、要求所有的数据源利用TBDataSourceImpl进行包裹。与事务管理器进行配合，实现全局事务控制
3、在调用一个方法的时候前，会判断当前线程是否已经开启事务控制。
       如果没有，则开启事务控制。同时会判断方法上的注解是否independ,如果是，则挂起外层事务，在开启新事务。
4、在调用方法结束的时候，如果是自己开启的事务，则提交事务。如果挂起了外层事务，则同时恢复外层事务。

5、同时重新包裹了 Connection,statement,PreparedStatement,CallableStatement三个接口
       在执行数据修改操作的时候会标志连接的数据修改状态。
6、在连接关闭的时候，如果没有加入事务，则直接关闭。如果加入事务，但没有数据修改操作，也直接关闭。
      避免查询操作获取的连接占用时间过长。

需要避免的问题：
    因为Spring代理实现可能存在问题导致在一个BEAN内部方法调用的时候，
     被调用者的事务设置的TBTransactionType.INDEPEND不能生效
	@TBTransactionTypeAnnotation(TBTransactionType.INDEPEND)
	public void executeHj(String s)throws Exception{
		executeCrm("ffffff");
		.....................
    }
	@TBTransactionTypeAnnotation(TBTransactionType.INDEPEND)
	public void executeCrm(String s )throws Exception{
	..................
	}
  请在程序设计的时候注意。后续会想办法优化
  

操作步骤：

1、数据源定义包裹:用TBDataSourceImpl包裹基础的数据源：
：
       需要进行全局事务控制的数据源，需要通过TBDataSourceImpl包裹后使用。
       需要注意的是:需要包裹的是最底层的原始数据源。
      例如 :
  		CRM_1,CRM_2 通过容灾后，变成CRM数据源。
  		则应该包裹的是CRM_1,CRM_2，而不是CRM.
  		
	<bean id="crm1" class="com.ql.transaction.TBDataSourceImpl">
	  <property name="dataSource">
			<ref bean="crm1base"/>
		</property>
	</bean>
	<bean id="crm2" class="com.ql.transaction.TBDataSourceImpl">
	  <property name="dataSource">
			<ref bean="crm2base"/>
		</property>
	</bean>
	<bean id="crm1base" class="org.apache.commons.dbcp.BasicDataSource"
		destroy-method="close">
		<property name="driverClassName">
			<value>oracle.jdbc.driver.OracleDriver</value>
		</property>
		<property name="url">
			<value>jdbc:oracle:thin:@localhost:1521:oracle9i</value>
		</property>
		<property name="username">
			<value>crm</value>
		</property>
		<property name="password">
			<value>crm</value>
		</property>
	</bean>
	<bean id="crm2base" class="org.apache.commons.dbcp.BasicDataSource"
		destroy-method="close">
		<property name="driverClassName">
			<value>oracle.jdbc.driver.OracleDriver</value>
		</property>
		<property name="url">
			<value>jdbc:oracle:thin:@localhost:1521:oracle9i</value>
		</property>
		<property name="username">
			<value>crm</value>
		</property>
		<property name="password">
			<value>crm</value>
		</property>
	</bean>	 		
 
2、在需要进行全局事务控制的类上加注解@TBTransactionAnnotation 或者增加标志接口TBTransactionHint
    @TBTransactionAnnotation
    public class TestBean implements ITestBean;
    或者：
    public class TestBean implements ITestBean,TBTransactionHint;

3、所有方法的执行缺省是 JOIN事务。如果某个方法是需要独立的事务。则在方法上增加注解@TBTransactionTypeAnnotation(TBTransactionType.INDEPEND)    
      例如：
   	@TBTransactionTypeAnnotation(TBTransactionType.INDEPEND)
	public void executeHj(String s)throws Exception;


