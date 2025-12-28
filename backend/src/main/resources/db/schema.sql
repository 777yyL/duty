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
-- public.shift_config definition

-- Drop table

-- DROP TABLE public.shift_config;

CREATE TABLE public.shift_config (
                                     id bigserial NOT NULL,
                                     shift_name varchar(50) NOT NULL,
                                     shift_code varchar(20) NOT NULL,
                                     start_time varchar(5) NOT NULL,
                                     end_time varchar(5) NOT NULL,
                                     sort_order int4 DEFAULT 0 NULL,
                                     status int4 DEFAULT 1 NULL,
                                     remark varchar(500) NULL,
                                     create_by varchar(50) NULL,
                                     create_time timestamp DEFAULT CURRENT_TIMESTAMP NULL,
                                     update_by varchar(50) NULL,
                                     update_time timestamp DEFAULT CURRENT_TIMESTAMP NULL,
                                     deleted int4 DEFAULT 0 NULL,
                                     CONSTRAINT shift_config_pkey PRIMARY KEY (id),
                                     CONSTRAINT shift_config_shift_code_key UNIQUE (shift_code)
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
-- public.duty_schedule definition

-- Drop table

-- DROP TABLE public.duty_schedule;

CREATE TABLE public.duty_schedule (
                                      id bigserial NOT NULL,
                                      duty_date date NOT NULL,
                                      shift_id int8 NOT NULL,
                                      shift_name varchar(50) NULL,
                                      person_id varchar(50) NOT NULL,
                                      person_name varchar(50) NOT NULL,
                                      person_gender varchar(10) NULL,
                                      dept_id varchar(50) NULL,
                                      dept_name varchar(100) NULL,
                                      status int4 DEFAULT 1 NULL,
                                      remark varchar(500) NULL,
                                      create_by varchar(50) NULL,
                                      create_time timestamp DEFAULT CURRENT_TIMESTAMP NULL,
                                      update_by varchar(50) NULL,
                                      update_time timestamp DEFAULT CURRENT_TIMESTAMP NULL,
                                      deleted int4 DEFAULT 0 NULL,
                                      CONSTRAINT duty_schedule_pkey PRIMARY KEY (id),
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
CREATE TABLE public.record_template (
                                        id bigserial NOT NULL,
                                        category_name varchar(100) NOT NULL,
                                        category_code varchar(50) NOT NULL,
                                        field_description varchar(500) NULL,
                                        default_value text NULL,
                                        input_type varchar(20) DEFAULT 'TEXT'::character varying NULL,
                                        "options" text NULL,
                                        is_required int4 DEFAULT 0 NULL,
                                        sort_order int4 DEFAULT 0 NULL,
                                        status int4 DEFAULT 1 NULL,
                                        remark varchar(500) NULL,
                                        create_by varchar(50) NULL,
                                        create_time timestamp DEFAULT CURRENT_TIMESTAMP NULL,
                                        update_by varchar(50) NULL,
                                        update_time timestamp DEFAULT CURRENT_TIMESTAMP NULL,
                                        deleted int4 DEFAULT 0 NULL,
                                        CONSTRAINT record_template_category_code_key UNIQUE (category_code),
                                        CONSTRAINT record_template_pkey PRIMARY KEY (id)
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
-- public.duty_record definition

-- Drop table

-- DROP TABLE public.duty_record;

CREATE TABLE public.duty_record (
                                    id bigserial NOT NULL,
                                    schedule_id int8 NOT NULL,
                                    duty_date date NOT NULL,
                                    shift_id int8 NOT NULL,
                                    shift_name varchar(50) NULL,
                                    person_id varchar(50) NOT NULL,
                                    person_name varchar(50) NOT NULL,
                                    record_time timestamp DEFAULT CURRENT_TIMESTAMP NULL,
                                    status int4 DEFAULT 1 NULL,
                                    create_by varchar(50) NULL,
                                    create_time timestamp DEFAULT CURRENT_TIMESTAMP NULL,
                                    update_by varchar(50) NULL,
                                    update_time timestamp DEFAULT CURRENT_TIMESTAMP NULL,
                                    deleted int4 DEFAULT 0 NULL,
                                    CONSTRAINT duty_record_pkey PRIMARY KEY (id)
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
CREATE TABLE public.duty_record_detail (
                                           id bigserial NOT NULL,
                                           record_id int8 NOT NULL,
                                           template_id int8 NOT NULL,
                                           category_name varchar(100) NOT NULL,
                                           category_code varchar(50) NOT NULL,
                                           record_content text NULL,
                                           create_time timestamp DEFAULT CURRENT_TIMESTAMP NULL,
                                           update_time timestamp DEFAULT CURRENT_TIMESTAMP NULL,
                                           deleted int4 DEFAULT 0 NULL,
                                           CONSTRAINT duty_record_detail_pkey PRIMARY KEY (id)
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

