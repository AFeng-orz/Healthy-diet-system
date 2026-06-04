CREATE DATABASE IF NOT EXISTS healthy_diet_system
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE healthy_diet_system;

CREATE TABLE IF NOT EXISTS t_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  username VARCHAR(50) NOT NULL COMMENT '用户名',
  password VARCHAR(100) NOT NULL COMMENT '加密密码',
  nickname VARCHAR(50) DEFAULT NULL COMMENT '昵称',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
  UNIQUE KEY uk_user_username (username),
  KEY idx_user_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

CREATE TABLE IF NOT EXISTS t_user_profile (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  gender VARCHAR(20) DEFAULT NULL COMMENT '性别',
  age INT DEFAULT NULL COMMENT '年龄',
  height_cm DECIMAL(5,2) DEFAULT NULL COMMENT '身高cm',
  weight_kg DECIMAL(5,2) DEFAULT NULL COMMENT '当前体重kg',
  target_weight_kg DECIMAL(5,2) DEFAULT NULL COMMENT '目标体重kg',
  activity_level VARCHAR(30) DEFAULT NULL COMMENT '运动频率',
  diet_goal VARCHAR(30) DEFAULT NULL COMMENT '饮食目标',
  avoid_foods VARCHAR(500) DEFAULT NULL COMMENT '忌口',
  allergies VARCHAR(500) DEFAULT NULL COMMENT '过敏',
  preferences VARCHAR(500) DEFAULT NULL COMMENT '饮食偏好',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
  UNIQUE KEY uk_profile_user_id (user_id),
  KEY idx_profile_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户健康档案表';

CREATE TABLE IF NOT EXISTS t_food (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  name VARCHAR(100) NOT NULL COMMENT '食物名称',
  category VARCHAR(50) DEFAULT NULL COMMENT '分类',
  calories DECIMAL(8,2) NOT NULL DEFAULT 0 COMMENT '每100g热量kcal',
  protein DECIMAL(8,2) NOT NULL DEFAULT 0 COMMENT '每100g蛋白质g',
  fat DECIMAL(8,2) NOT NULL DEFAULT 0 COMMENT '每100g脂肪g',
  carbs DECIMAL(8,2) NOT NULL DEFAULT 0 COMMENT '每100g碳水g',
  meal_tags VARCHAR(100) DEFAULT NULL COMMENT '适合餐次',
  high_sugar TINYINT NOT NULL DEFAULT 0 COMMENT '是否高糖',
  high_fat TINYINT NOT NULL DEFAULT 0 COMMENT '是否高脂',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
  UNIQUE KEY uk_food_name (name),
  KEY idx_food_category (category),
  KEY idx_food_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='食物营养库表';

CREATE TABLE IF NOT EXISTS t_health_question (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  question_key VARCHAR(80) NOT NULL COMMENT '题目标识',
  dimension VARCHAR(50) NOT NULL COMMENT '画像维度',
  title VARCHAR(200) NOT NULL COMMENT '题目标题',
  options_json TEXT NOT NULL COMMENT '选项JSON',
  sort_order INT NOT NULL DEFAULT 0 COMMENT '排序',
  enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
  UNIQUE KEY uk_question_key (question_key),
  KEY idx_question_dimension (dimension),
  KEY idx_question_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='健康问卷题目表';

CREATE TABLE IF NOT EXISTS t_health_assessment (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  tag_codes VARCHAR(300) NOT NULL COMMENT '画像标签编码，逗号分隔',
  tag_names VARCHAR(500) NOT NULL COMMENT '画像标签名称，逗号分隔',
  suggestions TEXT NOT NULL COMMENT '改善建议，换行分隔',
  total_score INT NOT NULL DEFAULT 0 COMMENT '总风险分',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
  KEY idx_assessment_user_time (user_id, create_time),
  KEY idx_assessment_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='健康画像评估表';

CREATE TABLE IF NOT EXISTS t_health_answer (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  assessment_id BIGINT NOT NULL COMMENT '评估ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  question_key VARCHAR(80) NOT NULL COMMENT '题目标识',
  dimension VARCHAR(50) NOT NULL COMMENT '画像维度',
  score INT NOT NULL DEFAULT 0 COMMENT '选项分值',
  answer_label VARCHAR(200) DEFAULT NULL COMMENT '选项文案',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
  KEY idx_answer_assessment (assessment_id),
  KEY idx_answer_user (user_id),
  KEY idx_answer_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='健康问卷答案表';

CREATE TABLE IF NOT EXISTS t_diet_plan (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  plan_date DATE NOT NULL COMMENT '计划日期',
  target_calories DECIMAL(8,2) NOT NULL DEFAULT 0 COMMENT '目标热量kcal',
  total_calories DECIMAL(8,2) NOT NULL DEFAULT 0 COMMENT '计划总热量kcal',
  total_protein DECIMAL(8,2) NOT NULL DEFAULT 0 COMMENT '计划总蛋白质g',
  total_fat DECIMAL(8,2) NOT NULL DEFAULT 0 COMMENT '计划总脂肪g',
  total_carbs DECIMAL(8,2) NOT NULL DEFAULT 0 COMMENT '计划总碳水g',
  summary VARCHAR(500) DEFAULT NULL COMMENT '计划说明',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
  KEY idx_diet_plan_user_time (user_id, create_time),
  KEY idx_diet_plan_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='饮食计划表';

CREATE TABLE IF NOT EXISTS t_diet_plan_item (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  plan_id BIGINT NOT NULL COMMENT '饮食计划ID',
  food_id BIGINT NOT NULL COMMENT '食物ID',
  meal_type VARCHAR(30) NOT NULL COMMENT '餐次：breakfast/lunch/dinner',
  food_name VARCHAR(100) NOT NULL COMMENT '食物名称快照',
  food_category VARCHAR(50) DEFAULT NULL COMMENT '食物分类快照',
  grams DECIMAL(8,2) NOT NULL DEFAULT 0 COMMENT '推荐克数',
  calories DECIMAL(8,2) NOT NULL DEFAULT 0 COMMENT '本项热量kcal',
  protein DECIMAL(8,2) NOT NULL DEFAULT 0 COMMENT '本项蛋白质g',
  fat DECIMAL(8,2) NOT NULL DEFAULT 0 COMMENT '本项脂肪g',
  carbs DECIMAL(8,2) NOT NULL DEFAULT 0 COMMENT '本项碳水g',
  reason VARCHAR(500) DEFAULT NULL COMMENT '推荐原因',
  sort_order INT NOT NULL DEFAULT 0 COMMENT '排序',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
  KEY idx_plan_item_plan (plan_id),
  KEY idx_plan_item_food (food_id),
  KEY idx_plan_item_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='饮食计划明细表';

CREATE TABLE IF NOT EXISTS t_diet_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  food_id BIGINT NOT NULL COMMENT '食物ID',
  record_date DATE NOT NULL COMMENT '记录日期',
  meal_type VARCHAR(30) NOT NULL COMMENT '餐次：breakfast/lunch/dinner/snack',
  food_name VARCHAR(100) NOT NULL COMMENT '食物名称快照',
  food_category VARCHAR(50) DEFAULT NULL COMMENT '食物分类快照',
  grams DECIMAL(8,2) NOT NULL DEFAULT 0 COMMENT '摄入克数',
  calories DECIMAL(8,2) NOT NULL DEFAULT 0 COMMENT '本项热量kcal',
  protein DECIMAL(8,2) NOT NULL DEFAULT 0 COMMENT '本项蛋白质g',
  fat DECIMAL(8,2) NOT NULL DEFAULT 0 COMMENT '本项脂肪g',
  carbs DECIMAL(8,2) NOT NULL DEFAULT 0 COMMENT '本项碳水g',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
  KEY idx_diet_record_user_date (user_id, record_date),
  KEY idx_diet_record_food (food_id),
  KEY idx_diet_record_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='每日饮食记录表';

CREATE TEMPORARY TABLE IF NOT EXISTS tmp_food_seed (
  name VARCHAR(100) NOT NULL,
  category VARCHAR(50) NOT NULL,
  calories DECIMAL(8,2) NOT NULL,
  protein DECIMAL(8,2) NOT NULL,
  fat DECIMAL(8,2) NOT NULL,
  carbs DECIMAL(8,2) NOT NULL,
  meal_tags VARCHAR(100),
  high_sugar TINYINT DEFAULT 0,
  high_fat TINYINT DEFAULT 0,
  remark VARCHAR(255)
);

TRUNCATE TABLE tmp_food_seed;

INSERT INTO tmp_food_seed
(name, category, calories, protein, fat, carbs, meal_tags, high_sugar, high_fat, remark)
VALUES
('燕麦片', '主食', 379, 13.20, 6.50, 67.70, '早餐,加餐', 0, 0, '适合早餐，注意控制份量'),
('糙米饭', '主食', 111, 2.60, 0.90, 23.00, '午餐,晚餐', 0, 0, '粗粮主食'),
('白米饭', '主食', 116, 2.60, 0.30, 25.90, '午餐,晚餐', 0, 0, '常见主食'),
('全麦面包', '主食', 246, 9.00, 3.50, 46.00, '早餐,加餐', 0, 0, '优先选择低糖款'),
('红薯', '主食', 86, 1.60, 0.10, 20.10, '早餐,午餐,加餐', 0, 0, '饱腹感较好'),
('玉米', '主食', 112, 4.00, 1.20, 22.80, '早餐,午餐,加餐', 0, 0, '可替代部分精制主食'),
('荞麦面', '主食', 340, 12.60, 3.10, 70.60, '午餐,晚餐', 0, 0, '干重数据'),
('鸡胸肉', '蛋白质', 133, 24.60, 2.80, 0.00, '午餐,晚餐', 0, 0, '低脂高蛋白'),
('鸡蛋', '蛋白质', 144, 13.30, 8.80, 2.80, '早餐,加餐', 0, 0, '常见优质蛋白'),
('牛肉瘦肉', '蛋白质', 125, 20.20, 4.20, 1.20, '午餐,晚餐', 0, 0, '注意烹调油'),
('三文鱼', '蛋白质', 208, 20.40, 13.40, 0.00, '午餐,晚餐', 0, 0, '脂肪高于白肉鱼，控制总量'),
('虾仁', '蛋白质', 99, 20.90, 1.10, 1.50, '午餐,晚餐', 0, 0, '低脂蛋白'),
('豆腐', '蛋白质', 84, 8.10, 4.20, 3.40, '午餐,晚餐', 0, 0, '植物蛋白'),
('无糖希腊酸奶', '乳制品', 73, 9.00, 1.90, 4.00, '早餐,加餐', 0, 0, '注意选择无糖款'),
('低脂牛奶', '乳制品', 43, 3.40, 1.30, 4.90, '早餐,加餐', 0, 0, '适合作为蛋白补充'),
('西兰花', '蔬菜', 36, 4.10, 0.60, 4.30, '午餐,晚餐', 0, 0, '高纤维蔬菜'),
('生菜', '蔬菜', 16, 1.40, 0.20, 2.10, '午餐,晚餐', 0, 0, '适合沙拉'),
('番茄', '蔬菜', 15, 0.90, 0.20, 3.30, '午餐,晚餐,加餐', 0, 0, '低热量'),
('黄瓜', '蔬菜', 16, 0.80, 0.20, 2.90, '午餐,晚餐,加餐', 0, 0, '低热量'),
('菠菜', '蔬菜', 28, 2.60, 0.30, 4.50, '午餐,晚餐', 0, 0, '绿叶菜'),
('胡萝卜', '蔬菜', 41, 0.90, 0.20, 9.60, '午餐,晚餐', 0, 0, '可搭配蛋白质'),
('苹果', '水果', 53, 0.30, 0.20, 13.80, '加餐', 0, 0, '整果优先，控制份量'),
('香蕉', '水果', 93, 1.40, 0.20, 22.00, '早餐,加餐', 0, 0, '运动前后可选'),
('蓝莓', '水果', 57, 0.70, 0.30, 14.50, '早餐,加餐', 0, 0, '可搭配酸奶'),
('橙子', '水果', 48, 0.80, 0.20, 11.10, '加餐', 0, 0, '整果优先'),
('牛油果', '水果', 160, 2.00, 14.70, 8.50, '早餐,加餐', 0, 1, '脂肪较高'),
('杏仁', '坚果', 578, 21.20, 50.60, 19.70, '加餐', 0, 1, '小份食用'),
('核桃', '坚果', 646, 14.90, 58.80, 19.10, '加餐', 0, 1, '脂肪较高'),
('花生', '坚果', 567, 25.80, 49.20, 16.10, '加餐', 0, 1, '注意过敏和份量'),
('橄榄油', '油脂', 884, 0.00, 100.00, 0.00, '午餐,晚餐', 0, 1, '烹调油需计量'),
('黑咖啡', '饮品', 2, 0.30, 0.00, 0.00, '早餐,加餐', 0, 0, '不加糖奶时热量低'),
('无糖豆浆', '饮品', 31, 3.00, 1.60, 1.20, '早餐,加餐', 0, 0, '优先无糖'),
('可乐', '饮品', 43, 0.00, 0.00, 10.60, '加餐', 1, 0, '高糖饮料'),
('奶茶', '饮品', 90, 1.00, 2.50, 15.00, '加餐', 1, 0, '通常含糖较高'),
('薯片', '零食', 536, 6.60, 34.60, 52.90, '加餐', 0, 1, '高脂高盐零食'),
('蛋白粉', '补充品', 390, 78.00, 6.00, 8.00, '加餐', 0, 0, '按产品标签调整'),
('鸡腿肉去皮', '蛋白质', 181, 19.00, 11.00, 0.00, '午餐,晚餐', 0, 0, '脂肪高于鸡胸，注意烹调油'),
('鳕鱼', '蛋白质', 88, 20.40, 0.70, 0.00, '午餐,晚餐', 0, 0, '低脂鱼类'),
('金枪鱼水浸', '蛋白质', 116, 25.50, 1.00, 0.00, '午餐,晚餐', 0, 0, '注意钠含量'),
('鹰嘴豆', '蛋白质', 164, 8.90, 2.60, 27.40, '午餐,晚餐', 0, 0, '植物蛋白和碳水'),
('藜麦', '主食', 120, 4.40, 1.90, 21.30, '午餐,晚餐', 0, 0, '熟重数据'),
('土豆', '主食', 77, 2.00, 0.10, 17.20, '午餐,晚餐', 0, 0, '烹调方式影响热量'),
('紫薯', '主食', 82, 1.90, 0.20, 17.60, '早餐,午餐,加餐', 0, 0, '可替代部分主食'),
('青椒', '蔬菜', 22, 1.00, 0.20, 5.40, '午餐,晚餐', 0, 0, '低热量蔬菜'),
('蘑菇', '蔬菜', 24, 2.70, 0.10, 4.10, '午餐,晚餐', 0, 0, '可增加饱腹感'),
('海带', '蔬菜', 13, 1.20, 0.10, 2.10, '午餐,晚餐', 0, 0, '注意钠含量'),
('草莓', '水果', 32, 0.70, 0.30, 7.70, '加餐', 0, 0, '水果加餐'),
('梨', '水果', 51, 0.30, 0.10, 13.30, '加餐', 0, 0, '控制份量'),
('低脂奶酪', '乳制品', 160, 22.00, 6.00, 4.00, '早餐,加餐', 0, 0, '根据品牌差异调整'),
('鸡肉沙拉', '其他', 120, 12.00, 5.00, 8.00, '午餐,晚餐', 0, 0, '复合菜品估算值');

DELETE food
FROM t_food food
JOIN t_food duplicate_food
  ON food.name = duplicate_food.name
  AND food.deleted = 0
  AND duplicate_food.deleted = 0
  AND food.id > duplicate_food.id;

INSERT INTO t_food
(name, category, calories, protein, fat, carbs, meal_tags, high_sugar, high_fat, remark)
SELECT
  seed.name,
  seed.category,
  seed.calories,
  seed.protein,
  seed.fat,
  seed.carbs,
  seed.meal_tags,
  seed.high_sugar,
  seed.high_fat,
  seed.remark
FROM tmp_food_seed seed
WHERE NOT EXISTS (
  SELECT 1
  FROM t_food food
  WHERE food.name = seed.name
    AND food.deleted = 0
);

DROP TEMPORARY TABLE tmp_food_seed;

UPDATE t_food
SET high_sugar = 0
WHERE deleted = 0
  AND name IN ('苹果', '香蕉', '蓝莓', '橙子', '草莓', '梨');

UPDATE t_food
SET high_fat = 0, remark = '脂肪高于白肉鱼，控制总量'
WHERE deleted = 0 AND name = '三文鱼';

UPDATE t_food
SET high_fat = 0, remark = '通常含糖较高'
WHERE deleted = 0 AND name = '奶茶';

UPDATE t_food
SET high_fat = 0, remark = '脂肪高于鸡胸，注意烹调油'
WHERE deleted = 0 AND name = '鸡腿肉去皮';

UPDATE t_food
SET high_fat = 0
WHERE deleted = 0 AND name = '低脂奶酪';
