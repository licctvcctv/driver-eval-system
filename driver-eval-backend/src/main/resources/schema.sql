-- 网约车平台司机评价信息管理系统 数据库脚本
-- 数据库名: driver_eval

CREATE DATABASE IF NOT EXISTS driver_eval DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_general_ci;
USE driver_eval;

-- 1. 系统用户表
CREATE TABLE sys_user (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  username    VARCHAR(50)  NOT NULL UNIQUE,
  password    VARCHAR(100) NOT NULL,
  real_name   VARCHAR(50)  DEFAULT '',
  phone       VARCHAR(20)  DEFAULT '',
  avatar      VARCHAR(255) DEFAULT '',
  role        TINYINT      NOT NULL COMMENT '1=乘客 2=司机 3=管理员',
  status      TINYINT      NOT NULL DEFAULT 1 COMMENT '1=正常 0=禁用 2=处罚中',
  id_card_img VARCHAR(255) DEFAULT '' COMMENT '司机证件照',
  create_time DATETIME     DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted  TINYINT      DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户';

-- 2. 司机扩展信息表
CREATE TABLE driver_info (
  id               BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id          BIGINT       NOT NULL UNIQUE,
  score            DECIMAL(5,2) NOT NULL DEFAULT 80.00 COMMENT '动态评分0-100',
  level            TINYINT      NOT NULL DEFAULT 1 COMMENT '1=普通 2=银牌 3=金牌',
  total_orders     INT          DEFAULT 0,
  total_complaints INT          DEFAULT 0,
  week_complaints  INT          DEFAULT 0,
  online_status    TINYINT      DEFAULT 0 COMMENT '0=离线 1=在线 2=处罚中',
  latitude         DECIMAL(10,7) DEFAULT NULL,
  longitude        DECIMAL(10,7) DEFAULT NULL,
  punish_end_time  DATETIME     DEFAULT NULL,
  create_time      DATETIME     DEFAULT CURRENT_TIMESTAMP,
  update_time      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='司机扩展信息';

-- 3. 车辆类型表
CREATE TABLE vehicle_type (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  type_name   VARCHAR(50)  NOT NULL,
  description VARCHAR(200) DEFAULT '',
  create_time DATETIME     DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted  TINYINT      DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车辆类型';

-- 4. 车辆信息表
CREATE TABLE vehicle_info (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  driver_id       BIGINT       NOT NULL COMMENT '司机用户ID',
  plate_number    VARCHAR(20)  NOT NULL,
  brand           VARCHAR(50)  DEFAULT '',
  model           VARCHAR(50)  DEFAULT '',
  color           VARCHAR(20)  DEFAULT '',
  vehicle_type_id BIGINT       DEFAULT NULL,
  seats           INT          DEFAULT 5,
  status          TINYINT      DEFAULT 1 COMMENT '1=正常 0=停用',
  create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP,
  update_time     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted      TINYINT      DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车辆信息';

-- 5. 订单信息表
CREATE TABLE order_info (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_no        VARCHAR(32)   NOT NULL UNIQUE,
  passenger_id    BIGINT        NOT NULL,
  driver_id       BIGINT        DEFAULT NULL,
  vehicle_id      BIGINT        DEFAULT NULL,
  departure       VARCHAR(200)  NOT NULL,
  departure_lng   DECIMAL(10,7) NOT NULL,
  departure_lat   DECIMAL(10,7) NOT NULL,
  destination     VARCHAR(200)  NOT NULL,
  dest_lng        DECIMAL(10,7) NOT NULL,
  dest_lat        DECIMAL(10,7) NOT NULL,
  distance        DECIMAL(10,2) DEFAULT NULL COMMENT '公里',
  price           DECIMAL(10,2) DEFAULT NULL,
  dispatch_score  DECIMAL(10,4) DEFAULT NULL COMMENT '派单综合分',
  status          TINYINT       NOT NULL DEFAULT 0 COMMENT '0=派单中 1=已派单 2=已接单 3=进行中 4=已完成 5=乘客取消 6=司机取消',
  driver_score    DECIMAL(5,2)  DEFAULT NULL COMMENT '派单时司机评分快照',
  driver_level    TINYINT       DEFAULT NULL COMMENT '派单时司机等级快照',
  complete_time   DATETIME      DEFAULT NULL,
  cancel_time     DATETIME      DEFAULT NULL,
  cancel_reason   VARCHAR(200)  DEFAULT '',
  is_evaluated    TINYINT       DEFAULT 0,
  is_complained   TINYINT       DEFAULT 0,
  create_time     DATETIME      DEFAULT CURRENT_TIMESTAMP,
  update_time     DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单信息';

-- 6. 评价表
CREATE TABLE evaluation (
  id           BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id     BIGINT       NOT NULL UNIQUE,
  passenger_id BIGINT       NOT NULL,
  driver_id    BIGINT       NOT NULL,
  star_rating  TINYINT      NOT NULL COMMENT '1-5星',
  content      VARCHAR(500) DEFAULT '',
  is_anonymous TINYINT      DEFAULT 0,
  driver_reply VARCHAR(500) DEFAULT NULL,
  reply_time   DATETIME     DEFAULT NULL,
  create_time  DATETIME     DEFAULT CURRENT_TIMESTAMP,
  update_time  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价';

-- 7. 评价标签字典
CREATE TABLE eval_tag (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  tag_name    VARCHAR(50) NOT NULL,
  tag_type    TINYINT     NOT NULL COMMENT '1=好评 2=差评',
  sort_order  INT         DEFAULT 0,
  create_time DATETIME    DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted  TINYINT     DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价标签';

-- 8. 评价-标签关联表
CREATE TABLE evaluation_tag_relation (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  evaluation_id BIGINT NOT NULL,
  tag_id        BIGINT NOT NULL,
  driver_id     BIGINT NOT NULL COMMENT '冗余字段加速统计',
  create_time   DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价标签关联';

-- 9. 投诉表
CREATE TABLE complaint (
  id           BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id     BIGINT        NOT NULL UNIQUE,
  passenger_id BIGINT        NOT NULL,
  driver_id    BIGINT        NOT NULL,
  content      VARCHAR(1000) NOT NULL,
  images       VARCHAR(1000) DEFAULT '' COMMENT '逗号分隔图片路径',
  is_anonymous TINYINT       DEFAULT 0,
  status       TINYINT       NOT NULL DEFAULT 0 COMMENT '0=待审核 1=通过 2=驳回',
  admin_remark VARCHAR(500)  DEFAULT '',
  review_time  DATETIME      DEFAULT NULL,
  reviewer_id  BIGINT        DEFAULT NULL,
  create_time  DATETIME      DEFAULT CURRENT_TIMESTAMP,
  update_time  DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='投诉';

-- 10. 申诉表
CREATE TABLE appeal (
  id           BIGINT PRIMARY KEY AUTO_INCREMENT,
  complaint_id BIGINT        NOT NULL UNIQUE,
  driver_id    BIGINT        NOT NULL,
  content      VARCHAR(1000) NOT NULL,
  status       TINYINT       NOT NULL DEFAULT 0 COMMENT '0=待审核 1=通过 2=驳回',
  admin_remark VARCHAR(500)  DEFAULT '',
  review_time  DATETIME      DEFAULT NULL,
  reviewer_id  BIGINT        DEFAULT NULL,
  create_time  DATETIME      DEFAULT CURRENT_TIMESTAMP,
  update_time  DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='申诉';

-- 11. 司机处罚记录表
CREATE TABLE driver_punish (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  driver_id       BIGINT       NOT NULL,
  punish_reason   VARCHAR(200) NOT NULL,
  punish_days     INT          NOT NULL DEFAULT 3,
  punish_start    DATETIME     NOT NULL,
  punish_end      DATETIME     NOT NULL,
  status          TINYINT      NOT NULL DEFAULT 1 COMMENT '1=生效中 2=已过期',
  week_complaints INT          DEFAULT 0,
  create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP,
  update_time     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='司机处罚记录';

-- 12. 敏感词表
CREATE TABLE sensitive_word (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  word        VARCHAR(100) NOT NULL,
  create_time DATETIME     DEFAULT CURRENT_TIMESTAMP,
  is_deleted  TINYINT      DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='敏感词';

-- 13. 公告/新闻/通知表
CREATE TABLE announcement (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  title       VARCHAR(200) NOT NULL,
  content     TEXT         NOT NULL,
  type        TINYINT      NOT NULL DEFAULT 1 COMMENT '1=公告 2=新闻 3=通知',
  status      TINYINT      DEFAULT 1 COMMENT '1=发布 0=草稿',
  create_time DATETIME     DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted  TINYINT      DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告新闻通知';

-- 14. 司机评分变更日志
CREATE TABLE driver_score_log (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  driver_id     BIGINT       NOT NULL,
  old_score     DECIMAL(5,2) NOT NULL,
  new_score     DECIMAL(5,2) NOT NULL,
  change_reason VARCHAR(200) NOT NULL,
  create_time   DATETIME     DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='司机评分变更日志';

-- 15. 司机评价乘客表
CREATE TABLE driver_evaluation (
  id           BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id     BIGINT       NOT NULL UNIQUE,
  driver_id    BIGINT       NOT NULL,
  passenger_id BIGINT       NOT NULL,
  content      VARCHAR(500) DEFAULT '',
  create_time  DATETIME     DEFAULT CURRENT_TIMESTAMP,
  update_time  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='司机评价乘客';

-- ==================== 种子数据 ====================

-- 管理员账号 (密码: admin123)
INSERT INTO sys_user (username, password, real_name, phone, role, status) VALUES
('admin', '$2a$10$DafaSGSg3DAh20sE3nPVRuDqsE1f5wsezih58E8eqoF9dlQ12VgJK', '系统管理员', '13800000000', 3, 1);

-- 测试乘客 (密码: admin123)
INSERT INTO sys_user (username, password, real_name, phone, role, status) VALUES
('passenger1', '$2a$10$DafaSGSg3DAh20sE3nPVRuDqsE1f5wsezih58E8eqoF9dlQ12VgJK', '张三', '13800000001', 1, 1),
('passenger2', '$2a$10$DafaSGSg3DAh20sE3nPVRuDqsE1f5wsezih58E8eqoF9dlQ12VgJK', '李四', '13800000002', 1, 1);

-- 测试司机 (密码: admin123)
INSERT INTO sys_user (username, password, real_name, phone, role, status) VALUES
('driver1', '$2a$10$DafaSGSg3DAh20sE3nPVRuDqsE1f5wsezih58E8eqoF9dlQ12VgJK', '王师傅', '13800000003', 2, 1),
('driver2', '$2a$10$DafaSGSg3DAh20sE3nPVRuDqsE1f5wsezih58E8eqoF9dlQ12VgJK', '赵师傅', '13800000004', 2, 1),
('driver3', '$2a$10$DafaSGSg3DAh20sE3nPVRuDqsE1f5wsezih58E8eqoF9dlQ12VgJK', '刘师傅', '13800000005', 2, 1);

-- 司机扩展信息（模拟位置在北京附近）
INSERT INTO driver_info (user_id, score, level, total_orders, online_status, latitude, longitude) VALUES
(4, 85.00, 2, 120, 1, 39.9042000, 116.4074000),
(5, 72.00, 1, 50,  1, 39.9142000, 116.3974000),
(6, 92.00, 3, 200, 1, 39.8942000, 116.4174000);

-- 车辆类型
INSERT INTO vehicle_type (type_name, description) VALUES
('经济型', '经济实惠的出行选择'),
('舒适型', '宽敞舒适的乘坐体验'),
('商务型', '高端商务出行'),
('SUV', '大空间多功能车型');

-- 车辆信息
INSERT INTO vehicle_info (driver_id, plate_number, brand, model, color, vehicle_type_id, seats) VALUES
(4, '京A12345', '丰田', '卡罗拉', '白色', 1, 5),
(5, '京B23456', '大众', '帕萨特', '黑色', 2, 5),
(6, '京C34567', '奔驰', 'E300L', '黑色', 3, 5);

-- 评价标签
INSERT INTO eval_tag (tag_name, tag_type, sort_order) VALUES
('服务态度好', 1, 1),
('驾驶平稳', 1, 2),
('车内整洁', 1, 3),
('准时到达', 1, 4),
('沟通友好', 1, 5),
('路线熟悉', 1, 6),
('服务态度差', 2, 7),
('驾驶不稳', 2, 8),
('车内不整洁', 2, 9),
('迟到', 2, 10),
('绕路', 2, 11),
('态度恶劣', 2, 12);

-- 敏感词
INSERT INTO sensitive_word (word) VALUES
('骂人'),('脏话'),('威胁'),('侮辱'),('暴力'),('恐吓');

-- 系统公告
INSERT INTO announcement (title, content, type, status) VALUES
('平台上线公告', '网约车平台司机评价信息管理系统正式上线，欢迎使用！', 1, 1),
('安全出行提示', '请乘客在上车前确认司机和车辆信息，确保安全出行。', 3, 1);
