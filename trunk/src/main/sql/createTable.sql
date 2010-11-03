
create table PAMIRS_SQL_CHECK
(
  ID              NUMBER not null,
  PRODUCT_NAME    VARCHAR2(100) not null,
  PROJECT_NAME    VARCHAR2(100) not null,
  SQL_TEXT        VARCHAR2(4000) not null,
  CHECK_OK        NUMBER not null,
  CHECK_TIME      DATE,
  GMT_CREATE      DATE not null,
  GMT_MODIFIED    DATE not null,
  DESC_DATA_NUM   VARCHAR2(1000),
  DESC_DATA_COUNT VARCHAR2(1000)
) ;

alter table PAMIRS_SQL_CHECK  add constraint PK_SQL_CHECK primary key (ID);
create unique index IND_SQL_CHECK_SQLTEXT on PAMIRS_SQL_CHECK (SQL_TEXT);


comment on column PAMIRS_SQL_CHECK.PRODUCT_NAME
  is '��Ʒ������';
comment on column PAMIRS_SQL_CHECK.PROJECT_NAME
  is '��һ�β�����SQL����Ŀ�����ճ�����';
comment on column PAMIRS_SQL_CHECK.SQL_TEXT
  is 'SQL���';
comment on column PAMIRS_SQL_CHECK.CHECK_OK
  is '�Ƿ�SQL���ͨ����ֻ��DBA���޸Ĵ��ֶ� 0--δ��� 1--���ͨ��9--��˲�ͨ�� ';
comment on column PAMIRS_SQL_CHECK.CHECK_TIME
  is '���ͨ��ʱ��';
comment on column PAMIRS_SQL_CHECK.GMT_CREATE
  is '����ʱ��';
comment on column PAMIRS_SQL_CHECK.GMT_MODIFIED
  is '�޸�ʱ��';
comment on column PAMIRS_SQL_CHECK.DESC_DATA_NUM
  is 'ÿ��ִ�е�������˵��';
comment on column PAMIRS_SQL_CHECK.DESC_DATA_COUNT
  is 'ִ��Ƶ������';
