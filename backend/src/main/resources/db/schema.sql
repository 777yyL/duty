-- ========================================
-- 留置管理中心电子值班看板系统
-- 数据库表结构设计
-- 数据库: PostgreSQL 11
-- ========================================

-- 删除已存在的表（按依赖关系倒序）
DROP TABLE IF EXISTS duty_record_detail CASCADE;
DROP TABLE IF EXISTS duty_record CASCADE;
DROP TABLE IF EXISTS duty_schedule CASCADE;
DROP TABLE IF EXISTS record_template CASCADE;
DROP TABLE IF EXISTS shift_config CASCADE;

-- ========================================
-- 1. 班次配置表
-- ========================================
CREATE TABLE shift_config (
    id BIGSERIAL PRIMARY KEY,
    shift_name VARCHAR(50) NOT NULL COMMENT '班次名称',
    shift_code VARCHAR(20) NOT NULL UNIQUE COMMENT '班次代码',
    start_time VARCHAR(5) NOT NULL COMMENT '开始时间 HH:mm',
    end_time VARCHAR(5) NOT NULL COMMENT '结束时间 HH:mm',
    sort_order INT DEFAULT 0 COMMENT '排序号',
    status INT DEFAULT 1 COMMENT '状态：1-启用 0-禁用',
    remark VARCHAR(500) COMMENT '备注',
    create_by VARCHAR(50) COMMENT '创建人',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(50) COMMENT '更新人',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '删除标识：0-未删除 1-已删除'
);

COMMENT ON TABLE shift_config IS '班次配置表';
COMMENT ON COLUMN shift_config.id IS '主键ID';
COMMENT ON COLUMN shift_config.shift_name IS '班次名称，如：早班、晚班';
COMMENT ON COLUMN shift_config.shift_code IS '班次代码，如：MORNING、EVENING';
COMMENT ON COLUMN shift_config.start_time IS '开始时间，格式：HH:mm';
COMMENT ON COLUMN shift_config.end_time IS '结束时间，格式：HH:mm';
COMMENT ON COLUMN shift_config.sort_order IS '排序号，数字越小越靠前';
COMMENT ON COLUMN shift_config.status IS '状态：1-启用 0-禁用';

-- 插入默认班次数据
INSERT INTO shift_config (shift_name, shift_code, start_time, end_time, sort_order, remark) VALUES
('夜班', 'NIGHT', '00:00', '08:30', 1, '默认夜班时间段'),
('白班', 'DAY', '08:30', '18:00', 2, '默认白班时间段'),
('晚班', 'EVENING', '18:00', '23:59', 3, '默认晚班时间段');

-- ========================================
-- 2. 排班表
-- ========================================
CREATE TABLE duty_schedule (
    id BIGSERIAL PRIMARY KEY,
    duty_date DATE NOT NULL COMMENT '值班日期',
    shift_id BIGINT NOT NULL COMMENT '班次ID',
    shift_name VARCHAR(50) COMMENT '班次名称（冗余字段）',
    person_id VARCHAR(50) NOT NULL COMMENT '人员ID（来自人员库）',
    person_name VARCHAR(50) NOT NULL COMMENT '人员姓名',
    person_gender VARCHAR(10) COMMENT '人员性别',
    dept_id VARCHAR(50) COMMENT '部门ID',
    dept_name VARCHAR(100) COMMENT '部门名称',
    status INT DEFAULT 1 COMMENT '状态：1-正常 0-取消',
    remark VARCHAR(500) COMMENT '备注',
    create_by VARCHAR(50) COMMENT '创建人',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(50) COMMENT '更新人',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '删除标识：0-未删除 1-已删除',
    CONSTRAINT uk_schedule UNIQUE (duty_date, shift_id, person_id)
);

COMMENT ON TABLE duty_schedule IS '排班表';
COMMENT ON COLUMN duty_schedule.id IS '主键ID';
COMMENT ON COLUMN duty_schedule.duty_date IS '值班日期';
COMMENT ON COLUMN duty_schedule.shift_id IS '班次ID，关联shift_config表';
COMMENT ON COLUMN duty_schedule.shift_name IS '班次名称（冗余字段，便于查询）';
COMMENT ON COLUMN duty_schedule.person_id IS '人员ID，来自人员库系统';
COMMENT ON COLUMN duty_schedule.person_name IS '人员姓名（冗余字段）';
COMMENT ON COLUMN duty_schedule.person_gender IS '人员性别（冗余字段）';
COMMENT ON COLUMN duty_schedule.dept_id IS '部门ID';
COMMENT ON COLUMN duty_schedule.dept_name IS '部门名称（冗余字段）';
COMMENT ON COLUMN duty_schedule.status IS '状态：1-正常 0-取消';

-- 创建索引
CREATE INDEX idx_schedule_date ON duty_schedule(duty_date);
CREATE INDEX idx_schedule_person ON duty_schedule(person_id);
CREATE INDEX idx_schedule_dept ON duty_schedule(dept_id);

-- ========================================
-- 3. 记录模板配置表
-- ========================================
CREATE TABLE record_template (
    id BIGSERIAL PRIMARY KEY,
    category_name VARCHAR(100) NOT NULL COMMENT '类别名称',
    category_code VARCHAR(50) NOT NULL UNIQUE COMMENT '类别代码',
    field_description VARCHAR(500) COMMENT '字段描述/说明',
    default_value TEXT COMMENT '默认值',
    input_type VARCHAR(20) DEFAULT 'TEXT' COMMENT '输入类型：TEXT-文本框 TEXTAREA-多行文本 SELECT-下拉框',
    options TEXT COMMENT '选项配置（JSON格式），用于SELECT类型',
    is_required INT DEFAULT 0 COMMENT '是否必填：1-必填 0-非必填',
    sort_order INT DEFAULT 0 COMMENT '排序号',
    status INT DEFAULT 1 COMMENT '状态：1-启用 0-禁用',
    remark VARCHAR(500) COMMENT '备注',
    create_by VARCHAR(50) COMMENT '创建人',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(50) COMMENT '更新人',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '删除标识：0-未删除 1-已删除'
);

COMMENT ON TABLE record_template IS '记录模板配置表';
COMMENT ON COLUMN record_template.id IS '主键ID';
COMMENT ON COLUMN record_template.category_name IS '类别名称，如：留置对象、巡视检查情况';
COMMENT ON COLUMN record_template.category_code IS '类别代码，如：DETENTION、INSPECTION';
COMMENT ON COLUMN record_template.field_description IS '字段描述/说明';
COMMENT ON COLUMN record_template.default_value IS '默认值';
COMMENT ON COLUMN record_template.input_type IS '输入类型：TEXT-文本框 TEXTAREA-多行文本 SELECT-下拉框';
COMMENT ON COLUMN record_template.options IS '选项配置（JSON格式），用于SELECT类型';
COMMENT ON COLUMN record_template.is_required IS '是否必填：1-必填 0-非必填';
COMMENT ON COLUMN record_template.sort_order IS '排序号';

-- 插入默认模板数据（示例）
INSERT INTO record_template (category_name, category_code, field_description, default_value, input_type, is_required, sort_order) VALUES
('留置对象', 'DETENTION', '当前留置对象人数及基本情况', '人数：0人，情况正常', 'TEXTAREA', 1, 1),
('巡视检查情况', 'INSPECTION', '各区域巡视检查情况记录', '各区域正常，无异常情况', 'TEXTAREA', 1, 2),
('设施设备运行', 'FACILITY', '监控、安防等设施设备运行情况', '设施设备运行正常', 'TEXTAREA', 1, 3),
('交接班记录', 'HANDOVER', '交接班事项及重要提醒', '无特殊事项', 'TEXTAREA', 0, 4),
('其他事项', 'OTHER', '其他需要记录的事项', '', 'TEXTAREA', 0, 5);

-- ========================================
-- 4. 值班记录表
-- ========================================
CREATE TABLE duty_record (
    id BIGSERIAL PRIMARY KEY,
    schedule_id BIGINT NOT NULL COMMENT '排班ID，关联duty_schedule表',
    duty_date DATE NOT NULL COMMENT '值班日期',
    shift_id BIGINT NOT NULL COMMENT '班次ID',
    shift_name VARCHAR(50) COMMENT '班次名称',
    person_id VARCHAR(50) NOT NULL COMMENT '人员ID',
    person_name VARCHAR(50) NOT NULL COMMENT '人员姓名',
    record_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '记录填写时间',
    status INT DEFAULT 1 COMMENT '状态：1-正常 0-作废',
    create_by VARCHAR(50) COMMENT '创建人',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(50) COMMENT '更新人',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '删除标识：0-未删除 1-已删除'
);

COMMENT ON TABLE duty_record IS '值班记录表';
COMMENT ON COLUMN duty_record.id IS '主键ID';
COMMENT ON COLUMN duty_record.schedule_id IS '排班ID，关联duty_schedule表';
COMMENT ON COLUMN duty_record.duty_date IS '值班日期';
COMMENT ON COLUMN duty_record.shift_id IS '班次ID';
COMMENT ON COLUMN duty_record.shift_name IS '班次名称（冗余字段）';
COMMENT ON COLUMN duty_record.person_id IS '人员ID';
COMMENT ON COLUMN duty_record.person_name IS '人员姓名（冗余字段）';
COMMENT ON COLUMN duty_record.record_time IS '记录填写时间';
COMMENT ON COLUMN duty_record.status IS '状态：1-正常 0-作废';

-- 创建索引
CREATE INDEX idx_record_date ON duty_record(duty_date);
CREATE INDEX idx_record_person ON duty_record(person_id);
CREATE INDEX idx_record_schedule ON duty_record(schedule_id);

-- ========================================
-- 5. 值班记录明细表
-- ========================================
CREATE TABLE duty_record_detail (
    id BIGSERIAL PRIMARY KEY,
    record_id BIGINT NOT NULL COMMENT '值班记录ID，关联duty_record表',
    template_id BIGINT NOT NULL COMMENT '模板ID，关联record_template表',
    category_name VARCHAR(100) NOT NULL COMMENT '类别名称',
    category_code VARCHAR(50) NOT NULL COMMENT '类别代码',
    record_content TEXT COMMENT '记录内容',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '删除标识：0-未删除 1-已删除'
);

COMMENT ON TABLE duty_record_detail IS '值班记录明细表';
COMMENT ON COLUMN duty_record_detail.id IS '主键ID';
COMMENT ON COLUMN duty_record_detail.record_id IS '值班记录ID，关联duty_record表';
COMMENT ON COLUMN duty_record_detail.template_id IS '模板ID，关联record_template表';
COMMENT ON COLUMN duty_record_detail.category_name IS '类别名称（冗余字段）';
COMMENT ON COLUMN duty_record_detail.category_code IS '类别代码（冗余字段）';
COMMENT ON COLUMN duty_record_detail.record_content IS '记录内容';

-- 创建索引
CREATE INDEX idx_detail_record ON duty_record_detail(record_id);
CREATE INDEX idx_detail_template ON duty_record_detail(template_id);
CREATE UNIQUE INDEX uk_detail_record_template ON duty_record_detail(record_id, template_id) WHERE deleted = 0;

-- ========================================
-- 查询视图：值班记录列表视图
-- ========================================
CREATE OR REPLACE VIEW v_duty_record_list AS
SELECT
    dr.id,
    dr.duty_date,
    dr.person_name,
    sc.start_time AS shift_start_time,
    sc.end_time AS shift_end_time,
    dr.record_time,
    string_agg(
        '[' || drd.category_name || '] ' || COALESCE(drd.record_content, ''),
        '；' ORDER BY rt.sort_order
    ) AS work_log_summary
FROM duty_record dr
LEFT JOIN shift_config sc ON dr.shift_id = sc.id
LEFT JOIN duty_record_detail drd ON dr.id = drd.record_id AND drd.deleted = 0
LEFT JOIN record_template rt ON drd.template_id = rt.id AND rt.deleted = 0
WHERE dr.deleted = 0
GROUP BY dr.id, dr.duty_date, dr.person_name, sc.start_time, sc.end_time, dr.record_time
ORDER BY dr.duty_date DESC, sc.start_time;

COMMENT ON VIEW v_duty_record_list IS '值班记录列表视图';

-- ========================================
-- 初始化完成
-- ========================================
