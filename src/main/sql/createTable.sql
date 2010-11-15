
create table PAMIRS_SQL_CHECK
(
  ID              NUMBER not null,
  PRODUCT_NAME    VARCHAR2(100) not null,
  PROJECT_NAME    VARCHAR2(100) not null,
  SQL_TEXT        VARCHAR2(4000) not null,
  OWNER			  VARCHAR2(30),	
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
  is '产品线名称';
comment on column PAMIRS_SQL_CHECK.PROJECT_NAME
  is '第一次产生此SQL的项目或者日常名称';
comment on column PAMIRS_SQL_CHECK.SQL_TEXT
  is 'SQL语句';
comment on column PAMIRS_SQL_CHECK.CHECK_OK
  is '是否SQL检查通过，只有DBA能修改此字段 0--未审核 1--审核通过9--审核不通过 ';
comment on column PAMIRS_SQL_CHECK.CHECK_TIME
  is '审核通过时间';
comment on column PAMIRS_SQL_CHECK.GMT_CREATE
  is '创建时间';
comment on column PAMIRS_SQL_CHECK.GMT_MODIFIED
  is '修改时间';
comment on column PAMIRS_SQL_CHECK.DESC_DATA_NUM
  is '每次执行的数据量说明';
comment on column PAMIRS_SQL_CHECK.DESC_DATA_COUNT
  is '执行频率描述';
