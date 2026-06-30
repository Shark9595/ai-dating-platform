INSERT INTO vip_package (name, vip_level, duration_days, price, original_price, benefits, sort, status, create_time, update_time) VALUES
('月度会员', 1, 30, 29.90, 39.90, '不限次数AI匹配,查看访客记录,隐身浏览模式,赠送虚拟礼物', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('季度会员', 2, 90, 79.90, 119.90, '不限次数AI匹配,查看访客记录,隐身浏览模式,赠送虚拟礼物,红娘牵线1次', 2, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('年度会员', 3, 365, 299.00, 499.00, '不限次数AI匹配,查看访客记录,隐身浏览模式,赠送虚拟礼物,红娘牵线6次,情感咨询服务', 3, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO virtual_gift (name, image, price, sort, status, create_time) VALUES
('玫瑰花', 'rose.png', 9.90, 1, 1, CURRENT_TIMESTAMP),
('巧克力', 'chocolate.png', 19.90, 2, 1, CURRENT_TIMESTAMP),
('小熊玩偶', 'bear.png', 39.90, 3, 1, CURRENT_TIMESTAMP),
('钻石项链', 'diamond.png', 99.90, 4, 1, CURRENT_TIMESTAMP),
('豪华游艇', 'yacht.png', 520.00, 5, 1, CURRENT_TIMESTAMP);

INSERT INTO daily_task (name, description, points, task_key, sort, status, create_time) VALUES
('每日登录', '每日登录APP即可获得积分', 5, 'daily_login', 1, 1, CURRENT_TIMESTAMP),
('完善资料', '完善个人资料获得积分', 20, 'complete_profile', 2, 1, CURRENT_TIMESTAMP),
('发布动态', '发布一条动态获得积分', 10, 'post_dynamic', 3, 1, CURRENT_TIMESTAMP),
('点赞动态', '点赞5条动态获得积分', 5, 'like_posts', 4, 1, CURRENT_TIMESTAMP),
('浏览推荐', '浏览10位推荐用户', 5, 'browse_users', 5, 1, CURRENT_TIMESTAMP),
('邀请好友', '邀请好友注册', 50, 'invite_friend', 6, 1, CURRENT_TIMESTAMP);

INSERT INTO sys_config (config_key, config_value, remark, create_time, update_time) VALUES
('site_name', 'AI婚恋平台', '网站名称', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('daily_match_limit', '10', '普通用户每日匹配次数限制', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('chat_unlock_match', 'true', '是否需要互相喜欢才能聊天', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('real_name_required', 'false', '是否必须实名认证才能使用', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('min_age', '18', '最小注册年龄', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('max_age', '80', '最大注册年龄', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('points_expire_months', '12', '积分有效期（月）', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO sensitive_word (word, type, status, create_time) VALUES
('诈骗', 1, 1, CURRENT_TIMESTAMP),
('赌博', 1, 1, CURRENT_TIMESTAMP),
('毒品', 1, 1, CURRENT_TIMESTAMP),
('传销', 1, 1, CURRENT_TIMESTAMP),
('色情', 1, 1, CURRENT_TIMESTAMP),
('贷款', 2, 1, CURRENT_TIMESTAMP),
('投资', 2, 1, CURRENT_TIMESTAMP),
('理财', 2, 1, CURRENT_TIMESTAMP);

INSERT INTO sys_user (username, password, nickname, role, vip_level, points, real_name_status, status, create_time, update_time, deleted) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '管理员', 'ADMIN', 3, 9999, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('matchmaker01', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '红娘小美', 'MATCHMAKER', 3, 0, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO sys_user (username, password, phone, nickname, avatar, gender, age, role, vip_level, points, real_name_status, status, create_time, update_time, deleted) VALUES
('user01', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '13800138001', '温柔小姐姐', 'avatar1.jpg', 0, 25, 'USER', 1, 100, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('user02', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '13800138002', '阳光男孩', 'avatar2.jpg', 1, 28, 'USER', 0, 50, 0, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('user03', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '13800138003', '文艺青年', 'avatar3.jpg', 1, 30, 'USER', 2, 200, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('user04', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '13800138004', '可爱萌妹', 'avatar4.jpg', 0, 23, 'USER', 0, 30, 0, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('user05', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '13800138005', '成熟大叔', 'avatar5.jpg', 1, 35, 'USER', 3, 500, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO user_profile (user_id, real_name, height, weight, education, occupation, salary, city, district, marriage_status, has_child, hobbies, introduction, constellation, smoke, drink, create_time, update_time) VALUES
(3, '张三', 175, 65, '本科', '程序员', 15000.00, '北京', '朝阳区', 0, 0, '编程,篮球,音乐', '热爱生活，积极向上', '天秤座', 0, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, '李四', 165, 50, '硕士', '设计师', 12000.00, '上海', '浦东新区', 0, 0, '画画,旅行,美食', '温柔善良，热爱生活', '双鱼座', 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, '王五', 180, 75, '本科', '产品经理', 20000.00, '深圳', '南山区', 0, 0, '健身,读书,电影', '成熟稳重，事业心强', '摩羯座', 0, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, '赵六', 160, 45, '本科', '教师', 8000.00, '广州', '天河区', 0, 0, '看书,养花,瑜伽', '温柔可爱，热爱生活', '处女座', 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7, '钱七', 178, 80, '博士', '医生', 25000.00, '杭州', '西湖区', 1, 0, '医学研究,跑步,摄影', '成熟稳重，有责任心', '天蝎座', 0, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO match_preference (user_id, min_age, max_age, min_height, max_height, education, city, min_salary, marriage_status, create_time, update_time) VALUES
(3, 22, 30, 160, 175, '本科', '北京', 5000.00, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 25, 35, 175, 190, '本科', '上海', 10000.00, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 22, 32, 160, 175, '本科', '深圳', 8000.00, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, 25, 40, 170, 190, '本科', '广州', 10000.00, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7, 25, 38, 160, 175, '硕士', '杭州', 8000.00, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO offline_activity (title, description, cover_image, location, start_time, end_time, max_participants, current_participants, price, vip_price, status, create_time, update_time) VALUES
('8分钟相亲会', '高效快速的相亲模式，每人8分钟交流时间，遇见你的缘分', 'activity1.jpg', '北京市朝阳区某咖啡厅', CURRENT_TIMESTAMP + INTERVAL 7 DAY, CURRENT_TIMESTAMP + INTERVAL 7 DAY + INTERVAL 3 HOUR, 30, 10, 99.00, 79.00, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('户外徒步联谊', '周末户外徒步活动，在大自然中邂逅美好爱情', 'activity2.jpg', '北京市香山公园', CURRENT_TIMESTAMP + INTERVAL 14 DAY, CURRENT_TIMESTAMP + INTERVAL 14 DAY + INTERVAL 8 HOUR, 50, 25, 199.00, 159.00, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('高端单身派对', '高端人士私人派对，精致的场地和美食，遇见对的人', 'activity3.jpg', '上海市黄浦区五星酒店', CURRENT_TIMESTAMP + INTERVAL 21 DAY, CURRENT_TIMESTAMP + INTERVAL 21 DAY + INTERVAL 5 HOUR, 100, 60, 599.00, 499.00, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO post (user_id, content, images, like_count, comment_count, view_count, status, audit_status, create_time, update_time) VALUES
(3, '今天天气真好，出去走走~', 'post1.jpg', 15, 3, 120, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, '周末做了一顿大餐，有没有想尝尝的？', 'post2.jpg,post3.jpg', 28, 8, 200, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, '新的一周，努力工作！', NULL, 10, 2, 80, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, '读完了一本好书，推荐给大家', 'post4.jpg', 20, 5, 150, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
