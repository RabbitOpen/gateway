--- h2仅作单元测试使用


-- 事件表
CREATE TABLE EVENT (id IDENTITY PRIMARY KEY,  event VARCHAR(1024), created_time BIGINT);

-- 插件表
CREATE TABLE plugin (id varchar(40) PRIMARY KEY, name varchar(60), type varchar(20), 
    target varchar(40) , plugin_schema varchar(1024));

-- 权限表
CREATE TABLE privilege (id varchar(40)  PRIMARY KEY,
  credential varchar(60) ,
  public_key varchar(2048) ,
  private_key varchar(2048) ,
  privileges varchar(2048));

-- 路由表
CREATE TABLE route  (
  id varchar(40) PRIMARY KEY,
  code varchar(40) ,
  service_code varchar(40),
  method varchar(20),
  rules varchar(1024),
  request_rate_limit varchar(1024),
  path varchar(300) ,
  mapping_uri varchar(300)
);

-- 服务表
CREATE TABLE service  (
  id varchar(40) PRIMARY KEY,
  code varchar(40),
  protocol varchar(20),
  upstreams varchar(1024)
);
