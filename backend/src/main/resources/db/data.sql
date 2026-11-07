INSERT IGNORE INTO users (id, username, password, name, phone, email, avatar, role, status) VALUES
(1, 'admin', '$2a$10$u9hZvdznjaeZgzSL.qxP0uyY4OEJL47g2TKODq.VMfsNwK148YGxC', '平台管理员', '13800000001', 'admin@tunan.cn', NULL, 'ADMIN', 1),
(2, 'tunan', '$2a$10$u9hZvdznjaeZgzSL.qxP0uyY4OEJL47g2TKODq.VMfsNwK148YGxC', '兔南影迷', '13800000002', 'tunan@example.com', NULL, 'USER', 1),
(3, 'xiaoyu', '$2a$10$u9hZvdznjaeZgzSL.qxP0uyY4OEJL47g2TKODq.VMfsNwK148YGxC', '小雨同学', '13800000003', 'xiaoyu@example.com', NULL, 'USER', 1),
(10, 'cinema', '$2a$10$u9hZvdznjaeZgzSL.qxP0uyY4OEJL47g2TKODq.VMfsNwK148YGxC', '万象店经理', '13900000001', 'cinema@tunan.cn', NULL, 'CINEMA', 1),
(11, 'newcinema', '$2a$10$u9hZvdznjaeZgzSL.qxP0uyY4OEJL47g2TKODq.VMfsNwK148YGxC', '滨江店经理', '13900000002', 'newcinema@tunan.cn', NULL, 'CINEMA', 1);

INSERT IGNORE INTO genres (id, name, color, sort_no) VALUES
(1, '科幻', '#5b8cff', 1), (2, '冒险', '#27b8a5', 2), (3, '犯罪', '#f25f5c', 3),
(4, '悬疑', '#8b6cef', 4), (5, '爱情', '#ff7e9d', 5), (6, '剧情', '#f0a23b', 6),
(7, '动画', '#3bb4ff', 7), (8, '家庭', '#70c46b', 8), (9, '动作', '#ef5b36', 9),
(10, '喜剧', '#f4c542', 10), (11, '惊悚', '#5d6472', 11), (12, '古装', '#a87949', 12), (13, '历史', '#8c6f54', 13), (14, '战争', '#78645f', 14), (15, '灾难', '#657b91', 15), (16, '奇幻', '#9b7ce8', 16);

INSERT IGNORE INTO movies (id, title, english_title, director, duration, release_date, status, score, box_office, want_count, poster, backdrop, trailer_url, synopsis, is_featured) VALUES
(1, '爱情神话', 'B FOR BUSY', '邵艺辉', 112, '2026-10-24', 'NOW', 9.8, 59.6, 14800, '/posters/original/love-myth.jpg', '/posters/original/love-myth-wide.jpg', 'https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4', '一场关于上海弄堂、中年爱情与生活选择的故事。几个性格迥异的人，在饭桌与街巷之间重新理解爱和体面。', 1),
(2, '海绵宝宝：拯救比奇堡', 'SAVING BIKINI BOTTOM', '丽萨·约翰逊', 97, '2026-10-18', 'NOW', 8.6, 238.4, 12600, '/posters/original/spongebob.jpg', '/posters/original/spongebob-wide.jpg', 'https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4', '比奇堡突然从海底消失，海绵宝宝和朋友们踏上前往陆地的奇妙冒险，用友谊和勇气找回自己的家园。', 1),
(3, '变形金刚：起源', 'TRANSFORMERS ONE', '乔什·库雷', 104, '2026-09-27', 'NOW', 8.1, 195.8, 11800, '/posters/original/transformers-one.jpg', '/posters/original/transformers-one-wide.jpg', 'https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4', '在成为宿敌之前，擎天柱与威震天曾是并肩作战的伙伴。一次发现改变了塞伯坦的命运，也改变了他们。', 1),
(4, '749局', 'BUREAU 749', '陆川', 123, '2026-10-01', 'NOW', 7.2, 68.4, 7600, '/posters/original/749.jpg', '/posters/original/749-wide.jpg', 'https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4', '少年马山被带入神秘的749局，与一群同样拥有特殊能力的伙伴共同面对一场未知危机。', 0),
(5, '熊猫计划', 'PANDA PLAN', '张栾', 99, '2026-10-01', 'NOW', 9.0, 435.8, 17600, '/posters/original/panda-plan.jpg', '/posters/original/panda-plan-wide.jpg', 'https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4', '国际功夫巨星意外与一只拥有特殊身份的熊猫结伴，在欢笑与追逐中完成一场守护行动。', 1),
(6, '危机航线', 'HIGH FORCES', '彭顺', 119, '2026-09-30', 'NOW', 7.8, 86.2, 9200, '/posters/original/crisis-route.jpg', '/posters/original/crisis-route-wide.jpg', 'https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4', '万米高空，一架客机被劫持。安保专家必须在有限时间里保护乘客，并阻止一场更大的阴谋。', 1),
(7, '毒液：最后一舞', 'VENOM: THE LAST DANCE', '凯莉·马塞尔', 109, '2026-10-23', 'NOW', 8.5, 276.5, 16900, '/posters/original/venom-last-dance.jpg', '/posters/original/venom-last-dance-wide.jpg', 'https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4', '埃迪与毒液迎来最后的旅程。面对来自故乡的追兵，他们必须做出一个改变彼此命运的选择。', 1),
(8, '志愿军：存亡之战', 'THE VOLUNTEERS: TO THE WAR', '陈凯歌', 144, '2026-09-30', 'NOW', 9.2, 247.5, 15300, '/posters/original/volunteers.jpg', '/posters/original/volunteers-wide.jpg', 'https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4', '志愿军战士在铁原阻击战中坚守阵地，以血肉之躯筑起防线，守护身后的家与国。', 1),
(9, '最后的里程', 'LAST MILE', '冢原亚由子', 128, '2026-11-08', 'SOON', 0, 0, 6800, '/posters/original/last-mile.jpg', '/posters/original/last-mile-wide.jpg', 'https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4', '一场看似普通的物流事故，将快递员、调查员与隐藏在系统中的真相串联在一起。', 0),
(10, '伟大征程', 'THE GREAT JOURNEY', '中央广播电视总台', 121, '2026-11-15', 'SOON', 0, 0, 5200, '/posters/original/great-journey.jpg', '/posters/original/great-journey-wide.jpg', 'https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4', '以恢弘舞台与影像记录时代征程，在歌声、舞蹈与真实故事中回望共同走过的路。', 0),
(11, '红色一号：冬日行动', 'RED ONE', '杰克·卡斯丹', 123, '2026-11-20', 'SOON', 0, 0, 7800, '/posters/original/red-one.jpg', '/posters/original/red-one-wide.jpg', 'https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4', '圣诞老人被绑架，北极安保主管被迫与世界上最臭名昭著的赏金猎人合作，展开一场全球营救。', 1),
(12, '哈利·波特与凤凰社', 'HARRY POTTER AND THE ORDER OF THE PHOENIX', '大卫·叶茨', 138, '2026-11-22', 'SOON', 0, 0, 9600, '/posters/original/harry-phoenix.jpg', '/posters/original/harry-phoenix-wide.jpg', 'https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4', '伏地魔归来，魔法部却拒绝承认真相。哈利与邓布利多军秘密集结，在黑暗中守护希望。', 1),
(13, '这个杀手不太冷', 'LEON: THE PROFESSIONAL', '吕克·贝松', 110, '2026-11-11', 'SOON', 0, 0, 12000, '/posters/original/leon.jpg', '/posters/original/leon-wide.jpg', 'https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4', '孤独的职业杀手里昂收留了失去家人的少女玛蒂尔达，两个被世界遗落的人彼此照亮。', 1),
(14, '爱你很久很久', 'LOVE YOU FOR A LONG TIME', '赖孟杰', 112, '2026-11-09', 'SOON', 0, 0, 4300, '/posters/original/love-you-long.jpg', '/posters/original/love-you-long-wide.jpg', 'https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4', '从校园到成年，一段跨越多年的喜欢在重逢时重新被看见。', 0),
(15, '那个不为人知的故事', 'THE UNSUNG STORY', '张岩', 113, '2026-11-09', 'SOON', 0, 0, 5600, '/posters/original/unknown-story.jpg', '/posters/original/unknown-story-wide.jpg', 'https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4', '一次偶然相遇，让两个各自背负过往的人走进彼此生命，也走向一个无法回避的结局。', 0),
(16, '蓦然回首', 'LOOK BACK', '押山清高', 58, '2026-11-10', 'SOON', 0, 0, 6100, '/posters/original/look-back.jpg', '/posters/original/look-back-wide.jpg', 'https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4', '两个热爱漫画的少女在成长中相遇、分离又重新理解彼此，用画笔记录无法回头的青春。', 0);

INSERT IGNORE INTO movie_genres (movie_id, genre_id) VALUES
(1,6),(1,10),(1,5),(2,7),(2,2),(2,10),(3,9),(3,1),(3,2),(4,1),(4,2),(4,9),(5,10),(5,9),(5,8),(6,9),(6,3),(6,11),(7,9),(7,1),(7,11),(8,6),(8,13),(8,14),(9,6),(9,4),(10,6),(10,13),(11,9),(11,10),(11,2),(12,16),(12,2),(12,8),(13,6),(13,9),(13,3),(14,5),(14,6),(15,5),(15,6),(15,4),(16,7),(16,6);

INSERT IGNORE INTO movie_cast (id, movie_id, name, role_name, avatar, sort_no) VALUES
(1,1,'徐峥','老白',NULL,1),
(2,1,'马伊琍','李小姐',NULL,2),
(3,1,'吴越','蓓蓓',NULL,3),
(4,2,'汤姆·肯尼','海绵宝宝',NULL,4),
(5,2,'比尔·法格巴克','派大星',NULL,5),
(6,2,'卡罗琳·劳伦斯','珊迪',NULL,6),
(7,3,'克里斯·海姆斯沃斯','擎天柱',NULL,7),
(8,3,'布莱恩·泰里·亨利','威震天',NULL,8),
(9,3,'斯嘉丽·约翰逊','艾丽塔',NULL,9),
(10,4,'王俊凯','马山',NULL,10),
(11,4,'苗苗','夏婳',NULL,11),
(12,4,'郑恺','乔东北',NULL,12),
(13,5,'成龙','Jackie',NULL,13),
(14,5,'魏翔','大卫',NULL,14),
(15,5,'史策','苏小竹',NULL,15),
(16,6,'刘德华','高皓军',NULL,16),
(17,6,'张子枫','高小军',NULL,17),
(18,6,'屈楚萧','麦克斯',NULL,18),
(19,7,'汤姆·哈迪','埃迪·布洛克',NULL,19),
(20,7,'切瓦特·埃加福','德雷克',NULL,20),
(21,7,'朱诺·坦普尔','佩恩',NULL,21),
(22,8,'朱一龙','李想',NULL,22),
(23,8,'辛柏青','李默尹',NULL,23),
(24,8,'张子枫','李晓',NULL,24),
(25,9,'满岛光','舟渡',NULL,25),
(26,9,'冈田将生','梨本',NULL,26),
(27,10,'中央广播电视总台','演出',NULL,27),
(28,11,'道恩·强森','卡勒姆',NULL,28),
(29,11,'克里斯·埃文斯','杰克',NULL,29),
(30,11,'刘玉玲','佐伊',NULL,30),
(31,12,'丹尼尔·雷德克里夫','哈利·波特',NULL,31),
(32,12,'艾玛·沃森','赫敏',NULL,32),
(33,12,'鲁伯特·格林特','罗恩',NULL,33),
(34,13,'让·雷诺','莱昂',NULL,34),
(35,13,'娜塔莉·波特曼','玛蒂尔达',NULL,35),
(36,13,'加里·奥德曼','斯坦菲尔德',NULL,36),
(37,14,'李沐','周予',NULL,37),
(38,14,'吴子霏','林夏',NULL,38),
(39,15,'邱泽','陈铭生',NULL,39),
(40,15,'郎月婷','杨昭',NULL,40),
(41,16,'河合优实','藤野',NULL,41),
(42,16,'吉田美月喜','京本',NULL,42);

INSERT IGNORE INTO cinemas (id, name, brand, city, address, phone, cover, intro, facilities, status, owner_id, audit_remark, audit_time) VALUES
(1, '兔南国际影城·万象店', '兔南影城', '南京', '建邺区江东中路88号万象天地L5', '025-88886661', '/cinemas/cinema-1.svg', '城市中心旗舰影城，拥有激光巨幕与全景观影休息区，主打沉浸式观影体验。', '免费停车,IMAX,激光巨幕,杜比全景声,儿童座椅', 'APPROVED', 10, '资质齐全，审核通过', NOW()),
(2, '星河影城·中心店', '星河影城', '南京', '秦淮区中山南路218号星光广场6层', '025-88886662', '/cinemas/cinema-2.svg', '位于城市中轴线的老牌影城，交通便利，拥有舒适的宽排距座椅与主题休息区。', '地铁直达,情侣座,无障碍通道,会员休息区', 'APPROVED', NULL, '资质齐全，审核通过', NOW()),
(3, '光年电影空间·滨江店', '光年电影', '南京', '鼓楼区江东北路169号滨江生活广场4层', '025-88886663', '/cinemas/cinema-3.svg', '面向年轻观众的社区型电影空间，以艺术影展和主题放映为特色。', '艺术影展,咖啡吧,免费WiFi,无障碍通道', 'APPROVED', NULL, '资质齐全，审核通过', NOW()),
(4, '拾光影院·大学城店', '拾光影院', '南京', '江宁区弘景大道99号大学城商业街3层', '025-88886664', '/cinemas/cinema-4.svg', '大学城旁的青春影院，学生友好，定期举办映后交流和导演沙龙。', '学生优惠,映后沙龙,免费WiFi,自助取票', 'APPROVED', NULL, '资质齐全，审核通过', NOW()),
(5, '云端影城·江北店', '云端影城', '南京', '浦口区文德路12号云端中心5层', '025-88886665', '/cinemas/cinema-5.svg', '正在认证中的江北新区影城，规划有六个主题影厅。', '停车,IMAX,会员休息区', 'PENDING', 11, NULL, NULL);

INSERT IGNORE INTO rooms (id, cinema_id, name, seat_rows, seat_cols, room_type, status) VALUES
(1,1,'1号激光巨幕厅',8,8,'激光IMAX',1),(2,1,'2号杜比厅',8,8,'杜比全景声',1),(3,1,'3号经典厅',8,8,'激光厅',1),(4,1,'4号亲子厅',8,8,'亲子厅',1),
(5,2,'1号星空厅',8,8,'激光厅',1),(6,2,'2号情侣厅',8,8,'情侣厅',1),
(7,3,'1号艺术厅',8,8,'艺术厅',1),(8,3,'2号滨江厅',8,8,'激光厅',1),
(9,4,'1号青春厅',8,8,'激光厅',1),(10,4,'2号沙龙厅',8,8,'沙龙厅',1),(11,5,'1号云端厅',8,8,'激光IMAX',1);

INSERT IGNORE INTO showings (id, movie_id, cinema_id, room_id, start_time, end_time, price, language, version, approval_status, audit_remark) VALUES
(1,1,1,1,DATE_ADD(NOW(), INTERVAL 2 HOUR),DATE_ADD(NOW(), INTERVAL 4 HOUR),49.90,'国语','IMAX 3D','APPROVED','已排片'),
(2,2,1,2,DATE_ADD(NOW(), INTERVAL 3 HOUR),DATE_ADD(NOW(), INTERVAL 5 HOUR),42.00,'国语','2D','APPROVED','已排片'),
(3,6,1,4,DATE_ADD(NOW(), INTERVAL 5 HOUR),DATE_ADD(NOW(), INTERVAL 7 HOUR),35.00,'国语','2D','APPROVED','已排片'),
(4,1,2,5,DATE_ADD(NOW(), INTERVAL 4 HOUR),DATE_ADD(NOW(), INTERVAL 6 HOUR),45.00,'国语','2D','APPROVED','已排片'),
(5,3,2,6,DATE_ADD(NOW(), INTERVAL 6 HOUR),DATE_ADD(NOW(), INTERVAL 8 HOUR),39.90,'国语','2D','APPROVED','已排片'),
(6,4,3,7,DATE_ADD(NOW(), INTERVAL 7 HOUR),DATE_ADD(NOW(), INTERVAL 9 HOUR),38.00,'国语','2D','APPROVED','已排片'),
(7,8,3,8,DATE_ADD(NOW(), INTERVAL 8 HOUR),DATE_ADD(NOW(), INTERVAL 10 HOUR),42.00,'国语','2D','APPROVED','已排片'),
(8,1,4,9,DATE_ADD(NOW(), INTERVAL 9 HOUR),DATE_ADD(NOW(), INTERVAL 11 HOUR),35.00,'国语','2D','APPROVED','已排片'),
(9,2,1,1,DATE_ADD(NOW(), INTERVAL 1 DAY),DATE_ADD(DATE_ADD(NOW(), INTERVAL 1 DAY), INTERVAL 2 HOUR),49.90,'国语','IMAX 3D','APPROVED','已排片'),
(10,5,1,2,DATE_ADD(DATE_ADD(NOW(), INTERVAL 1 DAY), INTERVAL 3 HOUR),DATE_ADD(DATE_ADD(NOW(), INTERVAL 1 DAY), INTERVAL 5 HOUR),45.00,'国语','2D','APPROVED','已排片'),
(11,7,2,5,DATE_ADD(DATE_ADD(NOW(), INTERVAL 1 DAY), INTERVAL 4 HOUR),DATE_ADD(DATE_ADD(NOW(), INTERVAL 1 DAY), INTERVAL 6 HOUR),39.90,'国语','2D','APPROVED','已排片'),
(12,9,2,6,DATE_ADD(DATE_ADD(NOW(), INTERVAL 1 DAY), INTERVAL 6 HOUR),DATE_ADD(DATE_ADD(NOW(), INTERVAL 1 DAY), INTERVAL 8 HOUR),42.00,'国语','2D','APPROVED','已排片'),
(13,3,3,7,DATE_ADD(DATE_ADD(NOW(), INTERVAL 2 DAY), INTERVAL 2 HOUR),DATE_ADD(DATE_ADD(NOW(), INTERVAL 2 DAY), INTERVAL 4 HOUR),38.00,'国语','2D','APPROVED','已排片'),
(14,4,3,8,DATE_ADD(DATE_ADD(NOW(), INTERVAL 2 DAY), INTERVAL 5 HOUR),DATE_ADD(DATE_ADD(NOW(), INTERVAL 2 DAY), INTERVAL 7 HOUR),38.00,'国语','2D','APPROVED','已排片'),
(15,10,4,9,DATE_ADD(DATE_ADD(NOW(), INTERVAL 2 DAY), INTERVAL 7 HOUR),DATE_ADD(DATE_ADD(NOW(), INTERVAL 2 DAY), INTERVAL 9 HOUR),30.00,'国语','2D','APPROVED','已排片'),
(16,1,1,1,DATE_ADD(DATE_ADD(NOW(), INTERVAL 3 DAY), INTERVAL 3 HOUR),DATE_ADD(DATE_ADD(NOW(), INTERVAL 3 DAY), INTERVAL 5 HOUR),55.00,'国语','IMAX 3D','APPROVED','已排片'),
(17,6,2,5,DATE_ADD(DATE_ADD(NOW(), INTERVAL 3 DAY), INTERVAL 5 HOUR),DATE_ADD(DATE_ADD(NOW(), INTERVAL 3 DAY), INTERVAL 7 HOUR),35.00,'国语','2D','APPROVED','已排片'),
(18,8,1,2,DATE_ADD(DATE_ADD(NOW(), INTERVAL 4 DAY), INTERVAL 2 HOUR),DATE_ADD(DATE_ADD(NOW(), INTERVAL 4 DAY), INTERVAL 4 HOUR),49.90,'国语','2D','APPROVED','已排片'),
(19,3,4,9,DATE_ADD(DATE_ADD(NOW(), INTERVAL 4 DAY), INTERVAL 7 HOUR),DATE_ADD(DATE_ADD(NOW(), INTERVAL 4 DAY), INTERVAL 9 HOUR),32.00,'国语','2D','APPROVED','已排片'),
(20,5,3,7,DATE_ADD(DATE_ADD(NOW(), INTERVAL 5 DAY), INTERVAL 4 HOUR),DATE_ADD(DATE_ADD(NOW(), INTERVAL 5 DAY), INTERVAL 6 HOUR),42.00,'国语','2D','APPROVED','已排片'),
(21,2,1,2,DATE_ADD(DATE_ADD(NOW(), INTERVAL 2 DAY), INTERVAL 4 HOUR),DATE_ADD(DATE_ADD(NOW(), INTERVAL 2 DAY), INTERVAL 6 HOUR),46.00,'国语','2D','PENDING',NULL),
(22,7,1,3,DATE_ADD(DATE_ADD(NOW(), INTERVAL 3 DAY), INTERVAL 6 HOUR),DATE_ADD(DATE_ADD(NOW(), INTERVAL 3 DAY), INTERVAL 8 HOUR),39.00,'国语','2D','PENDING',NULL),
(23,9,4,10,DATE_ADD(DATE_ADD(NOW(), INTERVAL 6 DAY), INTERVAL 3 HOUR),DATE_ADD(DATE_ADD(NOW(), INTERVAL 6 DAY), INTERVAL 5 HOUR),36.00,'国语','2D','PENDING',NULL);

INSERT IGNORE INTO orders (id, order_no, user_id, showing_id, amount, status, pickup_code, pay_method, created_at, paid_at) VALUES
(1,'TN202611050001',2,1,99.80,'PAID','835291','微信支付',DATE_SUB(NOW(), INTERVAL 2 HOUR),DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(2,'TN202611040002',3,2,84.00,'USED','672104','支付宝',DATE_SUB(NOW(), INTERVAL 2 DAY),DATE_SUB(NOW(), INTERVAL 2 DAY)),
(3,'TN202611030003',2,4,135.00,'USED','194882','微信支付',DATE_SUB(NOW(), INTERVAL 3 DAY),DATE_SUB(NOW(), INTERVAL 3 DAY)),
(4,'TN202611020004',3,6,114.00,'PAID','520731','支付宝',DATE_SUB(NOW(), INTERVAL 5 HOUR),DATE_SUB(NOW(), INTERVAL 5 HOUR)),
(5,'TN202611010005',2,9,149.70,'PAID','740329','微信支付',DATE_SUB(NOW(), INTERVAL 5 DAY),DATE_SUB(NOW(), INTERVAL 5 DAY)),
(6,'TN202610310006',3,11,119.70,'CANCELLED','310026','微信支付',DATE_SUB(NOW(), INTERVAL 6 DAY),DATE_SUB(NOW(), INTERVAL 6 DAY)),
(7,'TN202610300007',2,13,114.00,'USED','662810','支付宝',DATE_SUB(NOW(), INTERVAL 7 DAY),DATE_SUB(NOW(), INTERVAL 7 DAY));

INSERT IGNORE INTO order_seats (id, order_id, showing_id, row_no, col_no, price) VALUES
(1,1,1,3,4,49.90),(2,1,1,3,5,49.90),
(3,2,2,4,3,42.00),(4,2,2,4,4,42.00),
(5,3,4,5,4,45.00),(6,3,4,5,5,45.00),(7,3,4,5,6,45.00),
(8,4,6,6,4,38.00),(9,4,6,6,5,38.00),(10,4,6,6,6,38.00),
(11,5,9,5,3,49.90),(12,5,9,5,4,49.90),(13,5,9,5,5,49.90),
(14,6,11,6,6,39.90),(15,6,11,6,7,39.90),(16,6,11,6,8,39.90),
(17,7,13,7,3,38.00),(18,7,13,7,4,38.00),(19,7,13,7,5,38.00);

INSERT IGNORE INTO favorites (user_id, movie_id) VALUES (2,1),(2,5),(2,9),(3,7),(3,12);
INSERT IGNORE INTO reviews (id, user_id, movie_id, score, content, created_at) VALUES
(1,2,1,10,'饭桌上的每一句话都很有劲，成年人爱情拍得轻盈又准确。',DATE_SUB(NOW(), INTERVAL 2 DAY)),
(2,3,1,9,'上海的生活气太迷人了，看完想去弄堂里走一走。',DATE_SUB(NOW(), INTERVAL 1 DAY)),
(3,2,5,10,'熊猫和成龙同框，笑点密集，适合全家一起看。',DATE_SUB(NOW(), INTERVAL 3 DAY)),
(4,3,7,9,'毒液和埃迪的告别很有情绪，最后一场大战值回票价。',DATE_SUB(NOW(), INTERVAL 2 DAY));

INSERT IGNORE INTO notices (id, title, content, type, status, created_at) VALUES
(1,'兔南电影购票系统正式上线','全新购票体验现已开启，支持在线选座、影院比价与积分收藏。','系统公告',1,DATE_SUB(NOW(), INTERVAL 5 DAY)),
(2,'周末观影温馨提示','周末黄金场次座位紧张，建议提前30分钟到店取票。','观影指南',1,DATE_SUB(NOW(), INTERVAL 2 DAY)),
(3,'影院认证审核说明','新影院提交认证后，平台将在1个工作日内完成审核。','影院公告',1,DATE_SUB(NOW(), INTERVAL 1 DAY));

