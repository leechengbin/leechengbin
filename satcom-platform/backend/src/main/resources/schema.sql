CREATE TABLE IF NOT EXISTS sys_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  username VARCHAR(64) NOT NULL COMMENT '用户名',
  password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希',
  display_name VARCHAR(64) COMMENT '显示名称',
  role_code VARCHAR(32) COMMENT '角色编码',
  enabled TINYINT(1) DEFAULT 1 COMMENT '是否启用',
  created_at DATETIME COMMENT '创建时间',
  updated_at DATETIME COMMENT '更新时间'
) COMMENT='系统用户';

CREATE TABLE IF NOT EXISTS sys_user_equipment (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  equipment_id BIGINT NOT NULL COMMENT '装备ID',
  view_permission TINYINT(1) DEFAULT 0 COMMENT '查看权限',
  edit_permission TINYINT(1) DEFAULT 0 COMMENT '编辑权限',
  created_at DATETIME COMMENT '创建时间',
  updated_at DATETIME COMMENT '更新时间',
  FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
  FOREIGN KEY (equipment_id) REFERENCES equipment(id) ON DELETE CASCADE
) COMMENT='用户装备权限关联';

CREATE TABLE IF NOT EXISTS equipment (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  asset_code VARCHAR(64) NOT NULL COMMENT '资产编号',
  model VARCHAR(64) COMMENT '设备型号',
  status VARCHAR(32) COMMENT '设备状态',
  owner_unit VARCHAR(128) COMMENT '所属单位',
  firmware_version VARCHAR(64) COMMENT '固件版本',
  locked TINYINT(1) DEFAULT 0 COMMENT '是否锁机',
  latitude DOUBLE COMMENT '设备纬度(WGS84)',
  longitude DOUBLE COMMENT '设备经度(WGS84)',
  last_maintenance_at DATETIME COMMENT '最近维护时间',
  created_at DATETIME COMMENT '创建时间',
  updated_at DATETIME COMMENT '更新时间'
) COMMENT='装备档案';

CREATE TABLE IF NOT EXISTS equipment_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  equipment_id BIGINT NOT NULL COMMENT '装备ID',
  record_type VARCHAR(64) COMMENT '记录类型',
  description VARCHAR(512) COMMENT '描述',
  record_at DATETIME COMMENT '记录时间',
  created_at DATETIME COMMENT '创建时间'
) COMMENT='装备维护记录';

CREATE TABLE IF NOT EXISTS monitoring_status (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  equipment_id BIGINT NOT NULL COMMENT '装备ID',
  signal_quality DOUBLE COMMENT '信号质量',
  snr DOUBLE COMMENT '信噪比',
  power DOUBLE COMMENT '功率',
  packet_loss_rate DOUBLE COMMENT '丢包率',
  latency_ms DOUBLE COMMENT '延迟(毫秒)',
  latitude DOUBLE COMMENT '上报纬度(WGS84)',
  longitude DOUBLE COMMENT '上报经度(WGS84)',
  diagnosis VARCHAR(255) COMMENT '诊断结果',
  collected_at DATETIME COMMENT '采集时间'
) COMMENT='状态监控上报';

CREATE TABLE IF NOT EXISTS billing_plan (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  plan_name VARCHAR(64) NOT NULL COMMENT '套餐名称',
  billing_mode VARCHAR(32) COMMENT '计费模式',
  price DOUBLE COMMENT '单价',
  unit VARCHAR(16) COMMENT '计费单位',
  cycle_days INT COMMENT '计费周期(天)',
  active TINYINT(1) DEFAULT 1 COMMENT '是否启用',
  created_at DATETIME COMMENT '创建时间'
) COMMENT='资费套餐';

CREATE TABLE IF NOT EXISTS billing_account (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  equipment_id BIGINT NOT NULL COMMENT '装备ID',
  plan_id BIGINT COMMENT '套餐ID',
  balance DOUBLE DEFAULT 0 COMMENT '余额',
  expire_at DATETIME COMMENT '到期时间',
  updated_at DATETIME COMMENT '更新时间'
) COMMENT='资费账户';

CREATE TABLE IF NOT EXISTS dispatch_task (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  title VARCHAR(128) NOT NULL COMMENT '任务标题',
  level VARCHAR(32) COMMENT '任务等级',
  status VARCHAR(32) COMMENT '任务状态',
  target_unit VARCHAR(128) COMMENT '目标单位',
  message VARCHAR(512) COMMENT '指令内容',
  contact VARCHAR(128) COMMENT '联系人',
  created_at DATETIME COMMENT '创建时间',
  updated_at DATETIME COMMENT '更新时间'
) COMMENT='应急调度任务';

-- 菜单表
CREATE TABLE IF NOT EXISTS sys_menu (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  name VARCHAR(50) NOT NULL COMMENT '菜单名称',
  path VARCHAR(100) NOT NULL COMMENT '路由路径',
  icon VARCHAR(50) COMMENT '图标',
  parent_id BIGINT DEFAULT 0 COMMENT '父菜单ID',
  `order` INT DEFAULT 0 COMMENT '排序',
  visible TINYINT(1) DEFAULT 1 COMMENT '是否可见',
  created_at DATETIME COMMENT '创建时间',
  updated_at DATETIME COMMENT '更新时间'
) COMMENT='系统菜单';

-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  code VARCHAR(50) NOT NULL COMMENT '角色编码',
  name VARCHAR(50) NOT NULL COMMENT '角色名称',
  description VARCHAR(200) COMMENT '角色描述',
  created_at DATETIME COMMENT '创建时间',
  updated_at DATETIME COMMENT '更新时间'
) COMMENT='系统角色';

-- 角色菜单关联表
CREATE TABLE IF NOT EXISTS sys_role_menu (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  role_id BIGINT NOT NULL COMMENT '角色ID',
  menu_id BIGINT NOT NULL COMMENT '菜单ID',
  created_at DATETIME COMMENT '创建时间',
  updated_at DATETIME COMMENT '更新时间'
) COMMENT='角色菜单关联';

-- 初始化菜单数据
INSERT INTO sys_menu (name, path, icon, parent_id, `order`, visible, created_at, updated_at) VALUES
('仪表盘', '/dashboard', 'HomeFilled', 0, 1, 1, NOW(), NOW()),
('装备管理', '/equipment', 'Monitor', 0, 2, 1, NOW(), NOW()),
('监控中心', '/monitoring', 'VideoCamera', 0, 3, 1, NOW(), NOW()),
('设备监控', '/monitoring', 'DataAnalysis', 3, 1, 1, NOW(), NOW()),
('主站监控', '/station-monitoring', 'Station', 3, 2, 1, NOW(), NOW()),
('计费管理', '/billing', 'Money', 0, 4, 1, NOW(), NOW()),
('调度管理', '/dispatch', 'Guide', 0, 5, 1, NOW(), NOW()),
('系统管理', '/system', 'Setting', 0, 6, 1, NOW(), NOW()),
('用户管理', '/users', 'User', 6, 1, 1, NOW(), NOW()),
('角色管理', '/roles', 'Avatar', 6, 2, 1, NOW(), NOW()),
('菜单管理', '/menus', 'Menu', 6, 3, 1, NOW(), NOW());

-- 初始化角色数据
INSERT INTO sys_role (code, name, description, created_at, updated_at) VALUES
('ADMIN', '管理员', '系统管理员，拥有所有权限', NOW(), NOW()),
('OP', '运维', '运维人员，负责设备管理', NOW(), NOW()),
('FIN', '财务', '财务人员，负责计费管理', NOW(), NOW());

-- 初始化角色菜单关联数据
INSERT INTO sys_role_menu (role_id, menu_id, created_at, updated_at) VALUES
-- 管理员权限（所有菜单）
(1, 1, NOW(), NOW()),
(1, 2, NOW(), NOW()),
(1, 3, NOW(), NOW()),
(1, 4, NOW(), NOW()),
(1, 5, NOW(), NOW()),
(1, 6, NOW(), NOW()),
(1, 7, NOW(), NOW()),
(1, 8, NOW(), NOW()),
(1, 9, NOW(), NOW()),
(1, 10, NOW(), NOW()),
(1, 11, NOW(), NOW()),
(1, 12, NOW(), NOW()),
-- 运维权限
(2, 1, NOW(), NOW()),
(2, 2, NOW(), NOW()),
(2, 3, NOW(), NOW()),
(2, 4, NOW(), NOW()),
(2, 5, NOW(), NOW()),
(2, 6, NOW(), NOW()),
-- 财务权限
(3, 1, NOW(), NOW()),
(3, 7, NOW(), NOW());
