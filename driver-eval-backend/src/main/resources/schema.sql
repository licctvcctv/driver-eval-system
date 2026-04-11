-- 网约车平台司机评价信息管理系统 - 完整数据库脚本
-- 包含表结构 + 种子数据，可直接导入空数据库
-- 所有账号密码统一为: admin123

SET NAMES utf8mb4;
CREATE DATABASE IF NOT EXISTS driver_eval DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE driver_eval;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `announcement` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(200) NOT NULL,
  `content` text NOT NULL,
  `type` tinyint NOT NULL DEFAULT '1' COMMENT '1=公告 2=新闻 3=通知',
  `status` tinyint DEFAULT '1' COMMENT '1=发布 0=草稿',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='公告新闻通知';
/*!40101 SET character_set_client = @saved_cs_client */;
INSERT INTO `announcement` VALUES (1,'平台上线公告','网约车平台司机评价信息管理系统正式上线，欢迎使用！',1,1,'2026-04-10 10:32:39','2026-04-10 10:32:39',0),(2,'安全出行提示','请乘客在上车前确认司机和车辆信息，确保安全出行。',3,1,'2026-04-10 10:32:39','2026-04-10 10:32:39',0),(8,'春季安全驾驶提醒','春季多雨，路面湿滑，请各位司机注意安全驾驶，保持车距，减速慢行。遇到大雨天气请开启双闪灯。',3,1,'2026-04-01 09:00:00','2026-04-10 10:46:48',0),(9,'五一假期出行高峰预告','五一假期临近，预计4月30日至5月5日期间订单量将大幅增加，请司机师傅提前做好准备，确保车辆状态良好。',1,1,'2026-04-05 10:00:00','2026-04-10 10:46:48',0),(10,'司机评分系统升级公告','我们对司机评分系统进行了升级，新增了多维度评价指标，包括安全驾驶、服务态度、车辆整洁等方面，让评价更加公平合理。',2,1,'2026-04-07 14:00:00','2026-04-10 10:46:48',0),(11,'关于严格处理绕路行为的通知','近期收到多起关于司机绕路的投诉，平台将加强对绕路行为的监管。一经核实，将对司机进行严肃处理，包括扣分、暂停接单等。',3,1,'2026-04-08 11:00:00','2026-04-10 10:46:48',0),(12,'新能源车型补贴政策','为响应绿色出行号召，平台将对使用新能源车辆的司机给予额外补贴，每单补贴2元，具体政策详见司机端APP。',2,1,'2026-04-09 16:00:00','2026-04-10 10:46:48',0);
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `appeal` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `complaint_id` bigint NOT NULL,
  `driver_id` bigint NOT NULL,
  `content` varchar(1000) NOT NULL,
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '0=待审核 1=通过 2=驳回',
  `admin_remark` varchar(500) DEFAULT '',
  `review_time` datetime DEFAULT NULL,
  `reviewer_id` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `complaint_id` (`complaint_id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='申诉';
/*!40101 SET character_set_client = @saved_cs_client */;
INSERT INTO `appeal` VALUES (4,9,4,'当时接到家人紧急电话不得不接听，已经使用了蓝牙耳机，并未影响驾驶安全，请管理员重新审核',0,NULL,NULL,NULL,'2026-04-09 21:00:00','2026-04-10 10:47:56'),(5,10,5,'当时二环严重拥堵，导航推荐走三环绕行，实际上节省了20分钟时间，并非故意绕路',1,'申诉成立，经核实当时二环确实拥堵，撤销处罚','2026-04-09 14:00:00',1,'2026-04-09 11:00:00','2026-04-10 10:47:56'),(6,16,15,'我承认当天开车确实有点急，因为前面有几个订单延误了。以后会注意安全驾驶，请给个机会',2,'驾驶安全是底线，不予撤销处罚，请务必注意安全驾驶','2026-04-10 16:00:00',1,'2026-04-10 14:30:00','2026-04-10 10:47:56'),(7,20,40,'我错了',1,'下不为例','2026-04-10 20:57:13',1,'2026-04-10 20:56:48','2026-04-10 20:56:48');
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `complaint` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL,
  `passenger_id` bigint NOT NULL,
  `driver_id` bigint NOT NULL,
  `content` varchar(1000) NOT NULL,
  `images` varchar(1000) DEFAULT '' COMMENT '逗号分隔图片路径',
  `is_anonymous` tinyint DEFAULT '0',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '0=待审核 1=通过 2=驳回',
  `admin_remark` varchar(500) DEFAULT '',
  `review_time` datetime DEFAULT NULL,
  `reviewer_id` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `order_id` (`order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='投诉';
/*!40101 SET character_set_client = @saved_cs_client */;
INSERT INTO `complaint` VALUES (17,78,7,5,'司机故意绕路，多收了钱','',0,1,'经核实确实存在绕路情况','2026-04-02 10:00:00',NULL,'2026-04-01 11:20:00','2026-04-10 12:41:01'),(18,85,8,4,'空调开太低不听劝','',0,1,'已提醒司机注意','2026-04-05 09:00:00',NULL,'2026-04-04 15:55:00','2026-04-10 12:19:39'),(19,93,8,5,'态度冷淡全程不说话','',0,0,NULL,NULL,NULL,'2026-04-08 14:40:00','2026-04-10 12:19:39'),(20,109,2,40,'54363546','/uploads/137aab1768c446d0a5b16ee96c92963d.png',1,1,'','2026-04-10 20:55:33',1,'2026-04-10 20:54:28','2026-04-10 20:54:28');
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `driver_evaluation` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL,
  `driver_id` bigint NOT NULL,
  `passenger_id` bigint NOT NULL,
  `content` varchar(500) DEFAULT '',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `order_id` (`order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='司机评价乘客';
/*!40101 SET character_set_client = @saved_cs_client */;
INSERT INTO `driver_evaluation` VALUES (6,40,4,2,'乘客很有礼貌，上车就说你好，准时到达上车点','2026-04-08 09:40:00','2026-04-10 10:47:37'),(7,45,5,3,'乘客迟到了5分钟，希望以后能准时','2026-04-07 11:15:00','2026-04-10 10:47:37'),(8,50,6,7,'非常好的乘客，还帮忙关了车门','2026-04-06 09:30:00','2026-04-10 10:47:37'),(9,51,6,8,'乘客在车上吃东西，弄了一些碎屑在座位上','2026-04-07 14:05:00','2026-04-10 10:47:37'),(10,44,4,2,'老乘客了，每次都很准时，沟通顺畅','2026-04-10 08:45:00','2026-04-10 10:47:37'),(11,107,37,2,'111','2026-04-10 20:30:26','2026-04-10 20:30:26'),(12,108,40,2,'111','2026-04-10 20:47:21','2026-04-10 20:47:21');
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `driver_info` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `score` decimal(5,2) NOT NULL DEFAULT '80.00' COMMENT '动态评分0-100',
  `level` tinyint NOT NULL DEFAULT '1' COMMENT '1=普通 2=银牌 3=金牌',
  `total_orders` int DEFAULT '0',
  `total_complaints` int DEFAULT '0',
  `week_complaints` int DEFAULT '0',
  `online_status` tinyint DEFAULT '0' COMMENT '0=离线 1=在线 2=处罚中',
  `latitude` decimal(10,7) DEFAULT NULL,
  `longitude` decimal(10,7) DEFAULT NULL,
  `punish_end_time` datetime DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=37 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='司机扩展信息';
/*!40101 SET character_set_client = @saved_cs_client */;
INSERT INTO `driver_info` VALUES (1,4,85.00,2,121,2,0,1,39.9916970,116.4104650,NULL,'2026-04-10 10:32:39','2026-04-10 10:46:48'),(2,5,72.00,1,50,3,3,1,39.9142000,116.3974000,'2026-04-12 00:00:00','2026-04-10 10:32:39','2026-04-10 12:06:00'),(3,6,92.00,3,200,1,0,2,39.8942000,116.4174000,'2026-04-13 20:56:01','2026-04-10 10:32:39','2026-04-10 10:46:48'),(8,12,88.50,2,156,1,0,1,39.9489000,116.3553000,NULL,'2026-04-10 10:46:48','2026-04-10 10:46:48'),(9,13,65.00,1,38,0,0,1,39.9820000,116.4150000,NULL,'2026-04-10 10:46:48','2026-04-10 10:46:48'),(10,14,93.20,3,280,0,0,1,39.9070000,116.3910000,NULL,'2026-04-10 10:46:48','2026-04-10 10:46:48'),(11,15,76.00,2,95,1,1,0,39.9340000,116.4520000,'2026-04-13 14:00:00','2026-04-10 10:46:48','2026-04-10 12:06:00'),(22,36,88.50,2,95,2,0,1,39.9200000,116.4600000,NULL,'2026-04-10 12:17:54','2026-04-10 12:17:54'),(23,37,76.00,2,61,3,1,1,39.8800000,116.3400000,NULL,'2026-04-10 12:17:54','2026-04-10 12:17:54'),(24,38,95.00,3,250,1,0,1,39.9500000,116.3900000,NULL,'2026-04-10 12:17:54','2026-04-10 12:17:54'),(25,39,70.00,1,30,4,2,1,39.9100000,116.4300000,NULL,'2026-04-10 12:17:54','2026-04-10 12:17:54'),(26,40,82.00,2,112,2,0,1,39.9102360,116.3880770,'2026-04-13 20:56:16','2026-04-10 12:17:54','2026-04-10 12:17:54');
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `driver_punish` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `driver_id` bigint NOT NULL,
  `punish_reason` varchar(200) NOT NULL,
  `punish_days` int NOT NULL DEFAULT '3',
  `punish_start` datetime NOT NULL,
  `punish_end` datetime NOT NULL,
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '1=生效中 2=已过期',
  `week_complaints` int DEFAULT '0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='司机处罚记录';
/*!40101 SET character_set_client = @saved_cs_client */;
INSERT INTO `driver_punish` VALUES (3,5,'一周内收到3次投诉，系统自动处罚',3,'2026-04-09 00:00:00','2026-04-12 00:00:00',1,3,'2026-04-09 00:00:00','2026-04-10 10:46:48'),(4,15,'驾驶安全投诉经核实，处罚3天',3,'2026-04-10 14:00:00','2026-04-13 14:00:00',2,1,'2026-04-10 14:00:00','2026-04-10 10:46:48'),(5,6,'不好',3,'2026-04-10 20:56:01','2026-04-13 20:56:01',1,0,'2026-04-10 20:56:01','2026-04-10 20:56:01'),(6,40,'111',3,'2026-04-10 20:56:16','2026-04-13 20:56:16',2,1,'2026-04-10 20:56:16','2026-04-10 20:56:16');
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `driver_score_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `driver_id` bigint NOT NULL,
  `old_score` decimal(5,2) NOT NULL,
  `new_score` decimal(5,2) NOT NULL,
  `change_reason` varchar(200) NOT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='司机评分变更日志';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eval_tag` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tag_name` varchar(50) NOT NULL,
  `tag_type` tinyint NOT NULL COMMENT '1=好评 2=差评',
  `sort_order` int DEFAULT '0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='评价标签';
/*!40101 SET character_set_client = @saved_cs_client */;
INSERT INTO `eval_tag` VALUES (1,'服务态度好',1,1,'2026-04-10 10:32:39','2026-04-10 10:32:39',0),(2,'驾驶平稳',1,2,'2026-04-10 10:32:39','2026-04-10 10:32:39',0),(3,'车内整洁',1,3,'2026-04-10 10:32:39','2026-04-10 10:32:39',0),(4,'准时到达',1,4,'2026-04-10 10:32:39','2026-04-10 10:32:39',0),(5,'沟通友好',1,5,'2026-04-10 10:32:39','2026-04-10 10:32:39',0),(6,'路线熟悉',1,6,'2026-04-10 10:32:39','2026-04-10 10:32:39',0),(7,'服务态度差',2,7,'2026-04-10 10:32:39','2026-04-10 10:32:39',0),(8,'驾驶不稳',2,8,'2026-04-10 10:32:39','2026-04-10 10:32:39',0),(9,'车内不整洁',2,9,'2026-04-10 10:32:39','2026-04-10 10:32:39',0),(10,'迟到',2,10,'2026-04-10 10:32:39','2026-04-10 10:32:39',0),(11,'绕路',2,11,'2026-04-10 10:32:39','2026-04-10 10:32:39',0),(12,'态度恶劣',2,12,'2026-04-10 10:32:39','2026-04-10 10:32:39',0);
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `evaluation` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL,
  `passenger_id` bigint NOT NULL,
  `driver_id` bigint NOT NULL,
  `star_rating` tinyint NOT NULL COMMENT '1-5星',
  `content` varchar(500) DEFAULT '',
  `is_anonymous` tinyint DEFAULT '0',
  `driver_reply` varchar(500) DEFAULT NULL,
  `reply_time` datetime DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `order_id` (`order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=69 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='评价';
/*!40101 SET character_set_client = @saved_cs_client */;
INSERT INTO `evaluation` VALUES (40,76,2,4,5,'服务很好，驾驶平稳！',0,'感谢好评，下次继续为您服务！','2026-04-01 11:10:00','2026-04-01 09:10:00','2026-04-10 12:41:01'),(41,77,3,6,5,'服务很好，驾驶平稳！',0,'感谢好评，下次继续为您服务！','2026-04-01 11:45:00','2026-04-01 09:45:00','2026-04-10 12:41:01'),(42,84,7,6,5,'服务很好，驾驶平稳！',0,'感谢好评，下次继续为您服务！','2026-04-04 12:40:00','2026-04-04 10:40:00','2026-04-10 12:41:01'),(43,95,10,6,5,'服务很好，驾驶平稳！',0,'感谢好评，下次继续为您服务！','2026-04-09 14:23:00','2026-04-09 12:23:00','2026-04-10 12:41:01'),(44,100,2,38,5,'服务很好，驾驶平稳！',0,NULL,NULL,'2026-04-10 14:20:00','2026-04-10 12:41:01'),(47,79,8,12,4,'总体不错，推荐',0,NULL,NULL,'2026-04-02 08:35:00','2026-04-10 12:19:39'),(48,80,9,14,4,'总体不错，推荐',0,NULL,NULL,'2026-04-02 11:35:00','2026-04-10 12:19:39'),(49,82,11,39,4,'总体不错，推荐',0,NULL,NULL,'2026-04-03 10:25:00','2026-04-10 12:19:39'),(50,83,3,40,4,'总体不错，推荐',0,NULL,NULL,'2026-04-03 14:05:00','2026-04-10 12:19:39'),(51,86,2,37,4,'总体不错，推荐',0,NULL,NULL,'2026-04-05 08:50:00','2026-04-10 12:19:39'),(52,88,10,15,4,'总体不错，推荐',0,NULL,NULL,'2026-04-06 19:15:00','2026-04-10 12:19:39'),(53,89,11,14,4,'总体不错，推荐',0,NULL,NULL,'2026-04-06 20:13:00','2026-04-10 12:19:39'),(54,90,3,36,4,'总体不错，推荐',0,NULL,NULL,'2026-04-07 08:25:00','2026-04-10 12:19:39'),(55,93,8,5,4,'总体不错，推荐',0,NULL,NULL,'2026-04-08 14:15:00','2026-04-10 12:19:39'),(56,94,9,12,4,'总体不错，推荐',0,NULL,NULL,'2026-04-09 08:45:00','2026-04-10 12:19:39'),(57,96,11,4,4,'总体不错，推荐',0,NULL,NULL,'2026-04-09 17:25:00','2026-04-10 12:19:39'),(58,97,2,39,4,'总体不错，推荐',0,NULL,NULL,'2026-04-10 08:40:00','2026-04-10 12:19:39'),(59,98,3,37,4,'总体不错，推荐',0,NULL,NULL,'2026-04-10 09:40:00','2026-04-10 12:19:39'),(62,78,7,5,2,'绕路了，体验一般',0,'抱歉体验不好，我会改进的','2026-04-01 13:55:00','2026-04-01 10:55:00','2026-04-10 12:19:39'),(63,91,7,38,5,'金牌司机名不虚传！',0,NULL,NULL,'2026-04-07 16:35:00','2026-04-10 12:19:39'),(64,85,8,4,4,'空调有点冷，其他都好',0,NULL,NULL,'2026-04-04 15:30:00','2026-04-10 12:19:39'),(65,81,10,36,4,'周师傅路有点远但很稳',0,NULL,NULL,'2026-04-03 09:05:00','2026-04-10 12:41:01'),(66,107,2,37,5,'666',1,NULL,NULL,'2026-04-10 20:31:05','2026-04-10 20:31:05'),(67,108,2,40,5,'475457',1,NULL,NULL,'2026-04-10 20:47:46','2026-04-10 20:47:46'),(68,109,2,40,5,'111',0,'11111','2026-04-10 20:54:56','2026-04-10 20:54:17','2026-04-10 20:54:17');
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `evaluation_tag_relation` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `evaluation_id` bigint NOT NULL,
  `tag_id` bigint NOT NULL,
  `driver_id` bigint NOT NULL COMMENT '冗余字段加速统计',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=152 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='评价标签关联';
/*!40101 SET character_set_client = @saved_cs_client */;
INSERT INTO `evaluation_tag_relation` VALUES (47,1,1,4,'2026-04-08 09:35:00'),(48,1,3,4,'2026-04-08 09:35:00'),(49,1,4,4,'2026-04-08 09:35:00'),(50,2,1,4,'2026-04-08 14:30:00'),(51,2,2,4,'2026-04-08 14:30:00'),(52,3,6,4,'2026-04-09 10:20:00'),(53,3,4,4,'2026-04-09 10:20:00'),(54,4,7,4,'2026-04-09 17:00:00'),(55,4,2,4,'2026-04-09 17:00:00'),(56,5,1,4,'2026-04-10 08:40:00'),(57,5,4,4,'2026-04-10 08:40:00'),(58,5,5,4,'2026-04-10 08:40:00'),(59,6,2,5,'2026-04-07 11:10:00'),(60,6,1,5,'2026-04-07 11:10:00'),(61,7,11,5,'2026-04-07 18:40:00'),(62,7,7,5,'2026-04-07 18:40:00'),(63,8,12,5,'2026-04-08 20:20:00'),(64,8,7,5,'2026-04-08 20:20:00'),(65,8,9,5,'2026-04-08 20:20:00'),(66,9,1,5,'2026-04-09 07:50:00'),(67,9,5,5,'2026-04-09 07:50:00'),(68,10,1,5,'2026-04-09 21:25:00'),(69,10,3,5,'2026-04-09 21:25:00'),(70,10,5,5,'2026-04-09 21:25:00'),(71,11,1,6,'2026-04-06 09:25:00'),(72,11,2,6,'2026-04-06 09:25:00'),(73,11,5,6,'2026-04-06 09:25:00'),(74,12,3,6,'2026-04-07 14:00:00'),(75,12,1,6,'2026-04-07 14:00:00'),(76,13,4,6,'2026-04-08 15:40:00'),(77,13,6,6,'2026-04-08 15:40:00'),(78,14,1,6,'2026-04-09 11:50:00'),(79,14,3,6,'2026-04-09 11:50:00'),(80,14,2,6,'2026-04-09 11:50:00'),(81,14,5,6,'2026-04-09 11:50:00'),(82,15,1,6,'2026-04-10 08:00:00'),(83,15,3,6,'2026-04-10 08:00:00'),(84,16,6,6,'2026-04-10 18:40:00'),(85,16,4,6,'2026-04-10 18:40:00'),(86,17,1,12,'2026-04-09 14:40:00'),(87,17,5,12,'2026-04-09 14:40:00'),(88,18,1,14,'2026-04-09 17:10:00'),(89,18,3,14,'2026-04-09 17:10:00'),(90,18,2,14,'2026-04-09 17:10:00'),(91,19,8,15,'2026-04-10 10:40:00'),(92,19,1,15,'2026-04-10 10:40:00'),(95,40,1,4,'2026-04-10 12:19:39'),(96,41,1,6,'2026-04-10 12:19:39'),(97,42,1,6,'2026-04-10 12:19:39'),(98,43,1,6,'2026-04-10 12:19:39'),(99,44,1,38,'2026-04-10 12:19:39'),(100,47,1,12,'2026-04-10 12:19:39'),(101,48,1,14,'2026-04-10 12:19:39'),(102,49,1,39,'2026-04-10 12:19:39'),(103,50,1,40,'2026-04-10 12:19:39'),(104,51,1,37,'2026-04-10 12:19:39'),(105,52,1,15,'2026-04-10 12:19:39'),(106,53,1,14,'2026-04-10 12:19:39'),(107,54,1,36,'2026-04-10 12:19:39'),(108,55,1,5,'2026-04-10 12:19:39'),(109,56,1,12,'2026-04-10 12:19:39'),(110,57,1,4,'2026-04-10 12:19:39'),(111,58,1,39,'2026-04-10 12:19:39'),(112,59,1,37,'2026-04-10 12:19:39'),(113,63,1,38,'2026-04-10 12:19:39'),(114,64,1,4,'2026-04-10 12:19:39'),(115,65,1,36,'2026-04-10 12:19:39'),(126,40,2,4,'2026-04-10 12:19:39'),(127,42,2,6,'2026-04-10 12:19:39'),(128,44,2,38,'2026-04-10 12:19:39'),(129,48,2,14,'2026-04-10 12:19:39'),(130,50,2,40,'2026-04-10 12:19:39'),(131,52,2,15,'2026-04-10 12:19:39'),(132,54,2,36,'2026-04-10 12:19:39'),(133,56,2,12,'2026-04-10 12:19:39'),(134,58,2,39,'2026-04-10 12:19:39'),(135,64,2,4,'2026-04-10 12:19:39'),(141,40,4,4,'2026-04-10 12:19:39'),(142,41,4,6,'2026-04-10 12:19:39'),(143,42,4,6,'2026-04-10 12:19:39'),(144,43,4,6,'2026-04-10 12:19:39'),(145,44,4,38,'2026-04-10 12:19:39'),(146,63,4,38,'2026-04-10 12:19:39'),(148,62,11,5,'2026-04-10 12:19:39'),(149,66,1,37,'2026-04-10 20:31:05'),(150,67,4,40,'2026-04-10 20:47:46'),(151,68,8,40,'2026-04-10 20:54:17');
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_info` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_no` varchar(32) NOT NULL,
  `passenger_id` bigint NOT NULL,
  `driver_id` bigint DEFAULT NULL,
  `vehicle_id` bigint DEFAULT NULL,
  `departure` varchar(200) NOT NULL,
  `departure_lng` decimal(10,7) NOT NULL,
  `departure_lat` decimal(10,7) NOT NULL,
  `destination` varchar(200) NOT NULL,
  `dest_lng` decimal(10,7) NOT NULL,
  `dest_lat` decimal(10,7) NOT NULL,
  `distance` decimal(10,2) DEFAULT NULL COMMENT '公里',
  `price` decimal(10,2) DEFAULT NULL,
  `dispatch_score` decimal(10,4) DEFAULT NULL COMMENT '派单综合分',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '0=派单中 1=已派单 2=已接单 3=进行中 4=已完成 5=乘客取消 6=司机取消',
  `driver_score` decimal(5,2) DEFAULT NULL COMMENT '派单时司机评分快照',
  `driver_level` tinyint DEFAULT NULL COMMENT '派单时司机等级快照',
  `complete_time` datetime DEFAULT NULL,
  `cancel_time` datetime DEFAULT NULL,
  `cancel_reason` varchar(200) DEFAULT '',
  `is_evaluated` tinyint DEFAULT '0',
  `is_complained` tinyint DEFAULT '0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `order_no` (`order_no`)
) ENGINE=InnoDB AUTO_INCREMENT=112 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订单信息';
/*!40101 SET character_set_client = @saved_cs_client */;
INSERT INTO `order_info` VALUES (76,'ORD20260401001',2,4,NULL,'北京西站',116.3228000,39.8959000,'中关村',116.3118000,39.9816000,10.20,35.00,NULL,4,85.00,2,'2026-04-01 09:05:00',NULL,'',1,0,'2026-04-01 08:30:00','2026-04-10 12:41:01'),(77,'ORD20260401002',3,6,NULL,'天安门',116.3975000,39.9087000,'望京SOHO',116.4742000,39.9980000,12.50,42.00,NULL,4,92.00,3,'2026-04-01 09:40:00',NULL,'',1,0,'2026-04-01 09:00:00','2026-04-10 12:19:39'),(78,'ORD20260401003',7,5,NULL,'北京南站',116.3789000,39.8652000,'三里屯',116.4530000,39.9325000,10.80,36.00,NULL,4,72.00,1,'2026-04-01 10:50:00',NULL,'',1,1,'2026-04-01 10:15:00','2026-04-10 12:19:39'),(79,'ORD20260402001',8,12,NULL,'国贸',116.4586000,39.9099000,'五道口',116.3380000,39.9920000,15.10,52.00,NULL,4,88.50,2,'2026-04-02 08:30:00',NULL,'',1,0,'2026-04-02 07:45:00','2026-04-10 12:19:39'),(80,'ORD20260402002',9,14,NULL,'西单',116.3730000,39.9130000,'奥林匹克公园',116.3917000,39.9929000,9.50,32.00,NULL,4,93.20,3,'2026-04-02 11:30:00',NULL,'',1,0,'2026-04-02 11:00:00','2026-04-10 12:41:01'),(81,'ORD20260403001',10,36,NULL,'中关村',116.3118000,39.9816000,'亦庄',116.5030000,39.7900000,25.00,82.00,NULL,4,88.50,2,'2026-04-03 09:00:00',NULL,'',1,0,'2026-04-03 08:00:00','2026-04-10 12:19:39'),(82,'ORD20260403002',11,39,NULL,'朝阳门',116.4340000,39.9280000,'通州万达',116.6570000,39.9050000,22.00,72.00,NULL,4,70.00,1,'2026-04-03 10:20:00',NULL,'',1,0,'2026-04-03 09:30:00','2026-04-10 12:19:39'),(83,'ORD20260403003',3,40,NULL,'大望路',116.4740000,39.9089000,'首都机场T3',116.6031000,40.0799000,25.50,88.00,NULL,4,82.00,2,'2026-04-03 14:00:00',NULL,'',1,0,'2026-04-03 13:00:00','2026-04-10 12:19:39'),(84,'ORD20260404001',7,6,NULL,'西直门',116.3530000,39.9430000,'颐和园',116.2735000,39.9998000,10.00,34.00,NULL,4,92.00,3,'2026-04-04 10:35:00',NULL,'',1,0,'2026-04-04 10:00:00','2026-04-10 12:19:39'),(85,'ORD20260404002',8,4,NULL,'东直门',116.4380000,39.9426000,'798艺术区',116.4930000,39.9840000,7.50,26.00,NULL,4,85.00,2,'2026-04-04 15:25:00',NULL,'',1,1,'2026-04-04 15:00:00','2026-04-10 12:19:39'),(86,'ORD20260405001',2,37,NULL,'北京南站',116.3789000,39.8652000,'CBD',116.4586000,39.9099000,9.00,30.00,NULL,4,76.00,2,'2026-04-05 08:45:00',NULL,'',1,0,'2026-04-05 08:15:00','2026-04-10 12:19:39'),(87,'ORD20260405002',9,13,NULL,'鸟巢',116.3917000,39.9929000,'清华大学',116.3266000,40.0024000,6.50,22.00,NULL,4,65.00,1,'2026-04-05 11:20:00',NULL,'',0,0,'2026-04-05 11:00:00','2026-04-10 12:19:39'),(88,'ORD20260406001',10,15,NULL,'簋街',116.4210000,39.9380000,'雍和宫',116.4170000,39.9470000,1.20,8.00,NULL,4,76.00,2,'2026-04-06 19:10:00',NULL,'',1,0,'2026-04-06 19:00:00','2026-04-10 12:19:39'),(89,'ORD20260406002',11,14,NULL,'三元桥',116.4530000,39.9610000,'燕莎',116.4610000,39.9540000,1.50,9.00,NULL,4,93.20,3,'2026-04-06 20:08:00',NULL,'',1,0,'2026-04-06 20:00:00','2026-04-10 12:19:39'),(90,'ORD20260407001',3,36,NULL,'双井',116.4580000,39.8990000,'宋庄',116.6540000,39.9140000,20.00,66.00,NULL,4,88.50,2,'2026-04-07 08:20:00',NULL,'',1,0,'2026-04-07 07:30:00','2026-04-10 12:19:39'),(91,'ORD20260407002',7,38,NULL,'知春路',116.3310000,39.9740000,'北京西站',116.3228000,39.8959000,8.80,30.00,NULL,4,95.00,3,'2026-04-07 16:30:00',NULL,'',1,0,'2026-04-07 16:00:00','2026-04-10 12:19:39'),(92,'ORD20260408001',2,40,NULL,'安贞门',116.4050000,39.9650000,'丰台科技园',116.2870000,39.8350000,18.00,60.00,NULL,4,82.00,2,'2026-04-08 09:50:00',NULL,'',0,0,'2026-04-08 09:00:00','2026-04-10 12:19:39'),(93,'ORD20260408002',8,5,NULL,'后海',116.3830000,39.9420000,'南锣鼓巷',116.4020000,39.9370000,2.00,10.00,NULL,4,72.00,1,'2026-04-08 14:10:00',NULL,'',1,0,'2026-04-08 14:00:00','2026-04-10 12:19:39'),(94,'ORD20260409001',9,12,NULL,'国家图书馆',116.3190000,39.9430000,'西二旗',116.3100000,40.0530000,13.00,44.00,NULL,4,88.50,2,'2026-04-09 08:40:00',NULL,'',1,0,'2026-04-09 08:00:00','2026-04-10 12:19:39'),(95,'ORD20260409002',10,6,NULL,'王府井',116.4101000,39.9143000,'CBD',116.4586000,39.9099000,5.00,18.00,NULL,4,92.00,3,'2026-04-09 12:18:00',NULL,'',1,0,'2026-04-09 12:00:00','2026-04-10 12:19:39'),(96,'ORD20260409003',11,4,NULL,'北京站',116.4270000,39.9027000,'劲松',116.4620000,39.8830000,5.50,20.00,NULL,4,85.00,2,'2026-04-09 17:20:00',NULL,'',1,0,'2026-04-09 17:00:00','2026-04-10 12:19:39'),(97,'ORD20260410A01',2,39,NULL,'世贸天阶',116.4460000,39.9180000,'大红门',116.3900000,39.8450000,10.00,34.00,NULL,4,70.00,1,'2026-04-10 08:35:00',NULL,'',1,0,'2026-04-10 08:00:00','2026-04-10 12:19:39'),(98,'ORD20260410A02',3,37,NULL,'十里河',116.4650000,39.8730000,'草桥',116.3640000,39.8440000,11.00,38.00,NULL,4,76.00,2,'2026-04-10 09:35:00',NULL,'',1,0,'2026-04-10 09:00:00','2026-04-10 12:19:39'),(99,'ORD20260410A03',7,15,NULL,'牡丹园',116.3710000,39.9770000,'积水潭',116.3760000,39.9470000,3.50,14.00,NULL,4,76.00,2,'2026-04-10 10:12:00',NULL,'',0,0,'2026-04-10 10:00:00','2026-04-10 12:19:39'),(100,'ORD20260410A04',2,38,NULL,'天坛公园',116.4066000,39.8822000,'方庄',116.4320000,39.8680000,4.00,15.00,NULL,4,95.00,3,'2026-04-10 14:15:00',NULL,'',1,0,'2026-04-10 14:00:00','2026-04-10 12:19:39'),(101,'ORD20260403C01',8,NULL,NULL,'望京',116.4742000,39.9980000,'中关村',116.3118000,39.9816000,15.00,50.00,NULL,5,NULL,NULL,NULL,'2026-04-03 11:00:00','临时有事不去了',0,0,'2026-04-03 10:55:00','2026-04-10 12:19:39'),(102,'ORD20260405C01',9,4,NULL,'西单',116.3730000,39.9130000,'北京南站',116.3789000,39.8652000,6.00,22.00,NULL,6,NULL,NULL,NULL,'2026-04-05 16:00:00','路况拥堵无法到达',0,0,'2026-04-05 15:50:00','2026-04-10 12:19:39'),(103,'ORD20260407C01',10,NULL,NULL,'东单',116.4180000,39.9150000,'朝阳公园',116.4700000,39.9380000,6.00,20.00,NULL,5,NULL,NULL,NULL,'2026-04-07 12:00:00','等太久了',0,0,'2026-04-07 11:50:00','2026-04-10 12:19:39'),(104,'ORD20260409C01',2,14,NULL,'天坛',116.4066000,39.8822000,'前门',116.3950000,39.8990000,2.50,10.00,NULL,6,NULL,NULL,NULL,'2026-04-09 19:00:00','车辆故障',0,0,'2026-04-09 18:55:00','2026-04-10 12:19:39'),(105,'ORD20260410C01',11,NULL,NULL,'三里屯',116.4530000,39.9325000,'国贸',116.4586000,39.9099000,3.00,12.00,NULL,5,NULL,NULL,NULL,'2026-04-10 11:00:00','选错了目的地',0,0,'2026-04-10 10:58:00','2026-04-10 12:19:39'),(106,'ORD202604102025171293',2,25,16,'北京南站',116.3789000,39.8652000,'望京SOHO',116.4802000,39.9977000,NULL,NULL,92.8000,5,82.00,2,NULL,'2026-04-10 12:41:01','系统清理：司机数据已重置',0,0,'2026-04-10 20:25:18','2026-04-10 12:41:01'),(107,'ORD202604102029169854',2,37,28,'北京西站',116.3228000,39.8959000,'北京站',116.4271000,39.9028000,NULL,NULL,90.4000,4,76.00,2,'2026-04-10 20:30:15',NULL,'',1,0,'2026-04-10 20:29:16','2026-04-10 20:29:16'),(108,'ORD202604102046195084',2,40,31,'北京南站',116.3789000,39.8652000,'中关村',116.3117000,39.9818000,NULL,NULL,92.8000,4,82.00,2,'2026-04-10 20:47:14',NULL,'',1,0,'2026-04-10 20:46:20','2026-04-10 20:46:20'),(109,'ORD202604102053335262',2,40,31,'北京南站',116.3789000,39.8652000,'中关村',116.3117000,39.9818000,NULL,NULL,92.8000,4,82.00,2,'2026-04-10 20:53:55',NULL,'',1,1,'2026-04-10 20:53:33','2026-04-10 20:53:33'),(110,'ORD202604102102583848',2,39,30,'北京站',116.4271000,39.9028000,'中关村',116.3117000,39.9818000,NULL,NULL,88.0000,4,70.00,1,'2026-04-10 13:14:21',NULL,'',0,0,'2026-04-10 21:02:58','2026-04-10 13:14:21'),(111,'ORD202604102113416946',2,37,28,'北京西站',116.3228000,39.8959000,'朝阳大悦城',116.5190090,39.9245440,17.00,47.59,90.4000,4,76.00,2,'2026-04-10 13:14:07',NULL,'',0,0,'2026-04-10 21:13:42','2026-04-10 13:14:07');
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sensitive_word` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `word` varchar(100) NOT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='敏感词';
/*!40101 SET character_set_client = @saved_cs_client */;
INSERT INTO `sensitive_word` VALUES (1,'骂人','2026-04-10 10:32:39',0),(2,'脏话','2026-04-10 10:32:39',0),(3,'威胁','2026-04-10 10:32:39',0),(4,'侮辱','2026-04-10 10:32:39',0),(5,'暴力','2026-04-10 10:32:39',0),(6,'恐吓','2026-04-10 10:32:39',0);
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password` varchar(100) NOT NULL,
  `real_name` varchar(50) DEFAULT '',
  `phone` varchar(20) DEFAULT '',
  `avatar` varchar(255) DEFAULT '',
  `role` tinyint NOT NULL COMMENT '1=乘客 2=司机 3=管理员',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '1=正常 0=禁用 2=处罚中',
  `id_card_img` varchar(255) DEFAULT '' COMMENT '司机证件照',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=56 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='系统用户';
/*!40101 SET character_set_client = @saved_cs_client */;
INSERT INTO `sys_user` VALUES (1,'admin','$2a$10$DafaSGSg3DAh20sE3nPVRuDqsE1f5wsezih58E8eqoF9dlQ12VgJK','系统管理员','13800000000','',3,1,'','2026-04-10 10:32:39','2026-04-10 10:32:47',0),(2,'passenger1','$2a$10$DafaSGSg3DAh20sE3nPVRuDqsE1f5wsezih58E8eqoF9dlQ12VgJK','张三','13800000001','',1,1,'','2026-04-10 10:32:39','2026-04-10 10:32:47',0),(3,'passenger2','$2a$10$DafaSGSg3DAh20sE3nPVRuDqsE1f5wsezih58E8eqoF9dlQ12VgJK','李四','13800000002','',1,1,'','2026-04-10 10:32:39','2026-04-10 10:32:47',0),(4,'driver1','$2a$10$DafaSGSg3DAh20sE3nPVRuDqsE1f5wsezih58E8eqoF9dlQ12VgJK','王师傅','13800000003','',2,1,'','2026-04-10 10:32:39','2026-04-10 10:32:47',0),(5,'driver2','$2a$10$DafaSGSg3DAh20sE3nPVRuDqsE1f5wsezih58E8eqoF9dlQ12VgJK','赵师傅','13800000004','',2,1,'','2026-04-10 10:32:39','2026-04-10 12:06:00',0),(6,'driver3','$2a$10$DafaSGSg3DAh20sE3nPVRuDqsE1f5wsezih58E8eqoF9dlQ12VgJK','刘师傅','13800000005','',2,2,'','2026-04-10 10:32:39','2026-04-10 20:56:01',0),(7,'passenger3','$2a$10$DafaSGSg3DAh20sE3nPVRuDqsE1f5wsezih58E8eqoF9dlQ12VgJK','王小明','13800000006','',1,1,'','2026-04-10 10:46:48','2026-04-10 10:46:48',0),(8,'passenger4','$2a$10$DafaSGSg3DAh20sE3nPVRuDqsE1f5wsezih58E8eqoF9dlQ12VgJK','陈丽华','13800000007','',1,1,'','2026-04-10 10:46:48','2026-04-10 10:46:48',0),(9,'passenger5','$2a$10$DafaSGSg3DAh20sE3nPVRuDqsE1f5wsezih58E8eqoF9dlQ12VgJK','赵雪梅','13800000008','',1,1,'','2026-04-10 10:46:48','2026-04-10 10:46:48',0),(10,'passenger6','$2a$10$DafaSGSg3DAh20sE3nPVRuDqsE1f5wsezih58E8eqoF9dlQ12VgJK','孙大伟','13800000009','',1,1,'','2026-04-10 10:46:48','2026-04-10 10:46:48',0),(11,'passenger7','$2a$10$DafaSGSg3DAh20sE3nPVRuDqsE1f5wsezih58E8eqoF9dlQ12VgJK','周婷婷','13800000010','',1,1,'','2026-04-10 10:46:48','2026-04-10 10:46:48',0),(12,'driver4','$2a$10$DafaSGSg3DAh20sE3nPVRuDqsE1f5wsezih58E8eqoF9dlQ12VgJK','陈师傅','13800000011','',2,1,'','2026-04-10 10:46:48','2026-04-10 10:46:48',0),(13,'driver5','$2a$10$DafaSGSg3DAh20sE3nPVRuDqsE1f5wsezih58E8eqoF9dlQ12VgJK','杨师傅','13800000012','',2,1,'','2026-04-10 10:46:48','2026-04-10 10:46:48',0),(14,'driver6','$2a$10$DafaSGSg3DAh20sE3nPVRuDqsE1f5wsezih58E8eqoF9dlQ12VgJK','黄师傅','13800000013','',2,1,'','2026-04-10 10:46:48','2026-04-10 10:46:48',0),(15,'driver7','$2a$10$DafaSGSg3DAh20sE3nPVRuDqsE1f5wsezih58E8eqoF9dlQ12VgJK','马师傅','13800000014','',2,1,'','2026-04-10 10:46:48','2026-04-10 20:56:04',0),(36,'driver8','$2a$10$DafaSGSg3DAh20sE3nPVRuDqsE1f5wsezih58E8eqoF9dlQ12VgJK','周师傅','13900000006','',2,1,'','2026-04-10 12:17:54','2026-04-10 12:17:54',0),(37,'driver9','$2a$10$DafaSGSg3DAh20sE3nPVRuDqsE1f5wsezih58E8eqoF9dlQ12VgJK','吴师傅','13900000007','',2,1,'','2026-04-10 12:17:54','2026-04-10 12:17:54',0),(38,'driver10','$2a$10$DafaSGSg3DAh20sE3nPVRuDqsE1f5wsezih58E8eqoF9dlQ12VgJK','郑师傅','13900000008','',2,1,'','2026-04-10 12:17:54','2026-04-10 12:17:54',0),(39,'driver11','$2a$10$DafaSGSg3DAh20sE3nPVRuDqsE1f5wsezih58E8eqoF9dlQ12VgJK','钱师傅','13900000009','',2,1,'','2026-04-10 12:17:54','2026-04-10 12:17:54',0),(40,'driver12','$2a$10$DafaSGSg3DAh20sE3nPVRuDqsE1f5wsezih58E8eqoF9dlQ12VgJK','孙师傅','13900000010','',2,1,'','2026-04-10 12:17:54','2026-04-10 20:56:16',0);
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vehicle_info` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `driver_id` bigint NOT NULL COMMENT '司机用户ID',
  `plate_number` varchar(20) NOT NULL,
  `brand` varchar(50) DEFAULT '',
  `model` varchar(50) DEFAULT '',
  `color` varchar(20) DEFAULT '',
  `vehicle_type_id` bigint DEFAULT NULL,
  `seats` int DEFAULT '5',
  `status` tinyint DEFAULT '1' COMMENT '1=正常 0=停用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='车辆信息';
/*!40101 SET character_set_client = @saved_cs_client */;
INSERT INTO `vehicle_info` VALUES (1,4,'京A12345','丰田','卡罗拉','白色',1,5,1,'2026-04-10 10:32:39','2026-04-10 10:32:39',0),(2,5,'京B23456','大众','帕萨特','黑色',2,5,1,'2026-04-10 10:32:39','2026-04-10 10:32:39',0),(3,6,'京C34567','奔驰','E300L','黑色',3,5,1,'2026-04-10 10:32:39','2026-04-10 10:32:39',0),(8,12,'京D45678','本田','雅阁','银色',2,5,1,'2026-04-10 10:46:48','2026-04-10 10:46:48',0),(9,13,'京E56789','比亚迪','汉EV','蓝色',1,5,1,'2026-04-10 10:46:48','2026-04-10 10:46:48',0),(10,14,'京F67890','宝马','5系','黑色',3,5,1,'2026-04-10 10:46:48','2026-04-10 10:46:48',0),(11,15,'京G78901','别克','GL8','白色',4,7,1,'2026-04-10 10:46:48','2026-04-10 10:46:48',0),(27,36,'京D45678','比亚迪','汉EV','银色',2,5,1,'2026-04-10 12:19:39','2026-04-10 12:19:39',0),(28,37,'京E56789','本田','雅阁','灰色',2,5,1,'2026-04-10 12:19:39','2026-04-10 12:26:51',0),(29,38,'京F67890','宝马','5系','黑色',3,5,1,'2026-04-10 12:19:39','2026-04-10 12:19:39',0),(30,39,'京G78901','日产','轩逸','白色',1,5,1,'2026-04-10 12:19:39','2026-04-10 12:19:39',0),(31,40,'京H89012','特斯拉','Model 3','白色',2,5,1,'2026-04-10 12:19:39','2026-04-10 12:19:39',0);
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vehicle_type` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `type_name` varchar(50) NOT NULL,
  `description` varchar(200) DEFAULT '',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='车辆类型';
/*!40101 SET character_set_client = @saved_cs_client */;
INSERT INTO `vehicle_type` VALUES (1,'经济型','经济实惠的出行选择','2026-04-10 10:32:39','2026-04-10 10:32:39',0),(2,'舒适型','宽敞舒适的乘坐体验','2026-04-10 10:32:39','2026-04-10 10:32:39',0),(3,'商务型','高端商务出行','2026-04-10 10:32:39','2026-04-10 10:32:39',0),(4,'SUV','大空间多功能车型','2026-04-10 10:32:39','2026-04-10 10:32:39',0);
