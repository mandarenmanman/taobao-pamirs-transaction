TBTransaction��ʹ��˵����

1���ṩһ��ʹ�ü򵥵���������������Ϳ�����Աͨ�������������ĸ��Ӷȡ� 
2��ͬʱ֧�ֶ�����Դ��ͬʱ�ύ��ͬʱ�ع�����һ�㣬�����ϴ��ڲ���ȫ���أ���ʵ��ʹ�ÿ�������ȫ��������ʵ�ʵĹ�����Ҫ��
3��ϵͳ��ʶ��ֻ�в�ѯ�������������ӣ��ἰʱ�ͷţ����ⲻ��Ҫ�����ݿ�������Դռ�á�


ʵ��ԭ��
1��TBTransactionHandler ��Spring��ʼ�����ǣ���������ע��@TBTransactionAnnotation����ʵ�ֽӿ�TBTransactionHint
	��������TransactionRoundAdvice���ڷ���ִ�е�ʱ����������Ŀ��ƴ���
2��Ҫ�����е�����Դ����TBDataSourceImpl���а����������������������ϣ�ʵ��ȫ���������
3���ڵ���һ��������ʱ��ǰ�����жϵ�ǰ�߳��Ƿ��Ѿ�����������ơ�
       ���û�У�����������ơ�ͬʱ���жϷ����ϵ�ע���Ƿ�independ,����ǣ��������������ڿ���������
4���ڵ��÷���������ʱ��������Լ��������������ύ����������������������ͬʱ�ָ��������

5��ͬʱ���°����� Connection,statement,PreparedStatement,CallableStatement�����ӿ�
       ��ִ�������޸Ĳ�����ʱ����־���ӵ������޸�״̬��
6�������ӹرյ�ʱ�����û�м���������ֱ�ӹرա�����������񣬵�û�������޸Ĳ�����Ҳֱ�ӹرա�
      �����ѯ������ȡ������ռ��ʱ�������

��Ҫ��������⣺
    ��ΪSpring����ʵ�ֿ��ܴ������⵼����һ��BEAN�ڲ��������õ�ʱ��
     �������ߵ��������õ�TBTransactionType.INDEPEND������Ч
	@TBTransactionTypeAnnotation(TBTransactionType.INDEPEND)
	public void executeHj(String s)throws Exception{
		executeCrm("ffffff");
		.....................
    }
	@TBTransactionTypeAnnotation(TBTransactionType.INDEPEND)
	public void executeCrm(String s )throws Exception{
	..................
	}
  ���ڳ�����Ƶ�ʱ��ע�⡣��������취�Ż�
  

�������裺

1������Դ�������:��TBDataSourceImpl��������������Դ��
��
       ��Ҫ����ȫ��������Ƶ�����Դ����Ҫͨ��TBDataSourceImpl������ʹ�á�
       ��Ҫע�����:��Ҫ����������ײ��ԭʼ����Դ��
      ���� :
  		CRM_1,CRM_2 ͨ�����ֺ󣬱��CRM����Դ��
  		��Ӧ�ð�������CRM_1,CRM_2��������CRM.
  		
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
 
2������Ҫ����ȫ��������Ƶ����ϼ�ע��@TBTransactionAnnotation �������ӱ�־�ӿ�TBTransactionHint
    @TBTransactionAnnotation
    public class TestBean implements ITestBean;
    ���ߣ�
    public class TestBean implements ITestBean,TBTransactionHint;

3�����з�����ִ��ȱʡ�� JOIN�������ĳ����������Ҫ�������������ڷ���������ע��@TBTransactionTypeAnnotation(TBTransactionType.INDEPEND)    
      ���磺
   	@TBTransactionTypeAnnotation(TBTransactionType.INDEPEND)
	public void executeHj(String s)throws Exception;


