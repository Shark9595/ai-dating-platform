package com.dating.datingsystem.config;

import com.dating.datingsystem.entity.*;
import com.dating.datingsystem.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private MatchPreferenceRepository matchPreferenceRepository;

    @Autowired
    private VipPackageRepository vipPackageRepository;

    @Autowired
    private VirtualGiftRepository virtualGiftRepository;

    @Autowired
    private DailyTaskRepository dailyTaskRepository;

    @Autowired
    private SysConfigRepository sysConfigRepository;

    @Autowired
    private SensitiveWordRepository sensitiveWordRepository;

    @Autowired
    private OfflineActivityRepository activityRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        initVipPackages();
        initVirtualGifts();
        initDailyTasks();
        initSysConfigs();
        initSensitiveWords();
        initAdminUser();
        initTestUsers();
        initActivities();
        initPosts();
    }

    private void initVipPackages() {
        if (vipPackageRepository.count() == 0) {
            VipPackage p1 = new VipPackage();
            p1.setName("月度会员");
            p1.setVipLevel(1);
            p1.setDurationDays(30);
            p1.setPrice(new BigDecimal("29.90"));
            p1.setOriginalPrice(new BigDecimal("39.90"));
            p1.setBenefits("不限次数AI匹配,查看访客记录,隐身浏览模式,赠送虚拟礼物");
            p1.setSort(1);
            vipPackageRepository.save(p1);

            VipPackage p2 = new VipPackage();
            p2.setName("季度会员");
            p2.setVipLevel(2);
            p2.setDurationDays(90);
            p2.setPrice(new BigDecimal("79.90"));
            p2.setOriginalPrice(new BigDecimal("119.90"));
            p2.setBenefits("不限次数AI匹配,查看访客记录,隐身浏览模式,赠送虚拟礼物,红娘牵线1次");
            p2.setSort(2);
            vipPackageRepository.save(p2);

            VipPackage p3 = new VipPackage();
            p3.setName("年度会员");
            p3.setVipLevel(3);
            p3.setDurationDays(365);
            p3.setPrice(new BigDecimal("299.00"));
            p3.setOriginalPrice(new BigDecimal("499.00"));
            p3.setBenefits("不限次数AI匹配,查看访客记录,隐身浏览模式,赠送虚拟礼物,红娘牵线6次,情感咨询服务");
            p3.setSort(3);
            vipPackageRepository.save(p3);

            System.out.println("VIP套餐数据初始化完成");
        }
    }

    private void initVirtualGifts() {
        if (virtualGiftRepository.count() == 0) {
            String[] names = {"玫瑰花", "巧克力", "小熊玩偶", "钻石项链", "豪华游艇"};
            BigDecimal[] prices = {new BigDecimal("9.90"), new BigDecimal("19.90"),
                    new BigDecimal("39.90"), new BigDecimal("99.90"), new BigDecimal("520.00")};
            String[] images = {"rose.png", "chocolate.png", "bear.png", "diamond.png", "yacht.png"};

            for (int i = 0; i < names.length; i++) {
                VirtualGift gift = new VirtualGift();
                gift.setName(names[i]);
                gift.setPrice(prices[i]);
                gift.setImage(images[i]);
                gift.setSort(i + 1);
                virtualGiftRepository.save(gift);
            }
            System.out.println("虚拟礼物数据初始化完成");
        }
    }

    private void initDailyTasks() {
        if (dailyTaskRepository.count() == 0) {
            String[][] tasks = {
                    {"每日登录", "每日登录APP即可获得积分", "5", "daily_login", "1"},
                    {"完善资料", "完善个人资料获得积分", "20", "complete_profile", "2"},
                    {"发布动态", "发布一条动态获得积分", "10", "post_dynamic", "3"},
                    {"点赞动态", "点赞5条动态获得积分", "5", "like_posts", "4"},
                    {"浏览推荐", "浏览10位推荐用户", "5", "browse_users", "5"},
                    {"邀请好友", "邀请好友注册", "50", "invite_friend", "6"}
            };

            for (String[] task : tasks) {
                DailyTask t = new DailyTask();
                t.setName(task[0]);
                t.setDescription(task[1]);
                t.setPoints(Integer.parseInt(task[2]));
                t.setTaskKey(task[3]);
                t.setSort(Integer.parseInt(task[4]));
                dailyTaskRepository.save(t);
            }
            System.out.println("日常任务数据初始化完成");
        }
    }

    private void initSysConfigs() {
        if (sysConfigRepository.count() == 0) {
            String[][] configs = {
                    {"site_name", "心遇", "网站名称"},
                    {"daily_match_limit", "10", "普通用户每日匹配次数限制"},
                    {"chat_unlock_match", "true", "是否需要互相喜欢才能聊天"},
                    {"real_name_required", "false", "是否必须实名认证才能使用"},
                    {"min_age", "18", "最小注册年龄"},
                    {"max_age", "80", "最大注册年龄"},
                    {"points_expire_months", "12", "积分有效期（月）"}
            };

            for (String[] config : configs) {
                SysConfig c = new SysConfig();
                c.setConfigKey(config[0]);
                c.setConfigValue(config[1]);
                c.setRemark(config[2]);
                sysConfigRepository.save(c);
            }
            System.out.println("系统配置数据初始化完成");
        }
    }

    private void initSensitiveWords() {
        if (sensitiveWordRepository.count() == 0) {
            String[] words = {"诈骗", "赌博", "毒品", "传销", "色情", "贷款", "投资", "理财"};
            int[] types = {1, 1, 1, 1, 1, 2, 2, 2};

            for (int i = 0; i < words.length; i++) {
                SensitiveWord w = new SensitiveWord();
                w.setWord(words[i]);
                w.setType(types[i]);
                sensitiveWordRepository.save(w);
            }
            System.out.println("敏感词数据初始化完成");
        }
    }

    private void initAdminUser() {
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setNickname("系统管理员");
            admin.setRole("ADMIN");
            admin.setVipLevel(3);
            admin.setPoints(9999);
            admin.setRealNameStatus(1);
            admin.setStatus(1);
            userRepository.save(admin);

            UserProfile profile = new UserProfile();
            profile.setUserId(admin.getId());
            profile.setRealName("管理员");
            userProfileRepository.save(profile);

            MatchPreference preference = new MatchPreference();
            preference.setUserId(admin.getId());
            matchPreferenceRepository.save(preference);

            System.out.println("管理员账号初始化完成: admin / admin123");
        }

        if (!userRepository.existsByUsername("matchmaker01")) {
            User matchmaker = new User();
            matchmaker.setUsername("matchmaker01");
            matchmaker.setPassword(passwordEncoder.encode("123456"));
            matchmaker.setNickname("红娘小美");
            matchmaker.setGender(0);
            matchmaker.setAge(28);
            matchmaker.setRole("MATCHMAKER");
            matchmaker.setVipLevel(3);
            matchmaker.setRealNameStatus(1);
            matchmaker.setStatus(1);
            userRepository.save(matchmaker);

            UserProfile profile = new UserProfile();
            profile.setUserId(matchmaker.getId());
            profile.setRealName("王美丽");
            profile.setCity("北京");
            profile.setOccupation("红娘");
            userProfileRepository.save(profile);

            MatchPreference preference = new MatchPreference();
            preference.setUserId(matchmaker.getId());
            matchPreferenceRepository.save(preference);

            System.out.println("红娘账号初始化完成: matchmaker01 / 123456");
        }
    }

    private void initTestUsers() {
        String[][] users = {
                {"user01", "温柔小姐姐", "0", "25", "北京", "本科", "UI设计师", "USER", "1", "https://api.dicebear.com/7.x/avataaars/svg?seed=user01"},
                {"user02", "阳光男孩", "1", "28", "上海", "硕士", "后端工程师", "USER", "0", "https://api.dicebear.com/7.x/avataaars/svg?seed=user02"},
                {"user03", "文艺青年", "1", "30", "深圳", "本科", "产品经理", "USER", "2", "https://api.dicebear.com/7.x/avataaars/svg?seed=user03"},
                {"user04", "可爱萌妹", "0", "23", "广州", "本科", "小学教师", "USER", "0", "https://api.dicebear.com/7.x/avataaars/svg?seed=user04"},
                {"user05", "成熟大叔", "1", "35", "杭州", "博士", "主任医师", "USER", "3", "https://api.dicebear.com/7.x/avataaars/svg?seed=user05"},
                {"user06", "气质女神", "0", "26", "成都", "硕士", "金融分析师", "USER", "2", "https://api.dicebear.com/7.x/avataaars/svg?seed=user06"},
                {"user07", "运动健将", "1", "27", "武汉", "本科", "健身教练", "USER", "0", "https://api.dicebear.com/7.x/avataaars/svg?seed=user07"},
                {"user08", "甜美萝莉", "0", "22", "西安", "大专", "护士", "USER", "0", "https://api.dicebear.com/7.x/avataaars/svg?seed=user08"},
                {"user09", "儒雅绅士", "1", "32", "南京", "硕士", "律师", "USER", "1", "https://api.dicebear.com/7.x/avataaars/svg?seed=user09"},
                {"user10", "知性美女", "0", "29", "重庆", "博士", "大学讲师", "USER", "3", "https://api.dicebear.com/7.x/avataaars/svg?seed=user10"}
        };

        String[] zodiacs = {"白羊座", "金牛座", "双子座", "巨蟹座", "狮子座", "处女座", "天秤座", "天蝎座", "射手座", "摩羯座", "水瓶座", "双鱼座"};
        String[] bloodTypes = {"A型", "B型", "AB型", "O型"};
        String[] constellations = {"白羊座", "金牛座", "双子座", "巨蟹座", "狮子座", "处女座", "天秤座", "天蝎座", "射手座", "摩羯座", "水瓶座", "双鱼座"};

        for (String[] u : users) {
            if (!userRepository.existsByUsername(u[0])) {
                User user = new User();
                user.setUsername(u[0]);
                user.setPassword(passwordEncoder.encode("123456"));
                user.setNickname(u[1]);
                user.setGender(Integer.parseInt(u[2]));
                user.setAge(Integer.parseInt(u[3]));
                user.setAvatar(u[9]);
                user.setRole(u[7]);
                user.setVipLevel(Integer.parseInt(u[8]));
                user.setPoints(100 + (int)(Math.random() * 500));
                user.setRealNameStatus(1);
                user.setStatus(1);
                user = userRepository.save(user);

                UserProfile profile = new UserProfile();
                profile.setUserId(user.getId());
                profile.setCity(u[4]);
                profile.setEducation(u[5]);
                profile.setOccupation(u[6]);
                profile.setHeight(Integer.parseInt(u[2]) == 0 ? 158 + (int)(Math.random() * 17) : 170 + (int)(Math.random() * 18));
                profile.setWeight(Integer.parseInt(u[2]) == 0 ? 45 + (int)(Math.random() * 18) : 60 + (int)(Math.random() * 25));
                profile.setSalary(new BigDecimal(8000 + (int)(Math.random() * 30000)));
                profile.setMarriageStatus((int)(Math.random() * 3));
                profile.setConstellation(constellations[(int)(Math.random() * constellations.length)]);
                profile.setBloodType(bloodTypes[(int)(Math.random() * bloodTypes.length)]);
                profile.setSmoke((int)(Math.random() * 2));
                profile.setDrink((int)(Math.random() * 2));
                profile.setHasChild((int)(Math.random() * 2));
                profile.setHometown(u[4]);
                profile.setCompany(getRandomCompany());
                profile.setIntroduction(getRandomIntroduction(u[1], u[6]));
                profile.setHobbies(getRandomHobbies());
                profile.setPhotos("[\"https://picsum.photos/400/400?random=" + u[0] + "\"]");
                userProfileRepository.save(profile);

                MatchPreference preference = new MatchPreference();
                preference.setUserId(user.getId());
                preference.setMinAge(20);
                preference.setMaxAge(40);
                preference.setCity(u[4]);
                preference.setEducation("本科");
                preference.setMarriageStatus(Integer.parseInt(u[2]) == 0 ? 0 : null);
                preference.setMinHeight(Integer.parseInt(u[2]) == 0 ? 165 : 155);
                preference.setMaxHeight(Integer.parseInt(u[2]) == 0 ? 185 : 175);
                preference.setMinSalary(new BigDecimal(5000));
                matchPreferenceRepository.save(preference);

                System.out.println("测试用户初始化完成: " + u[0] + " / 123456");
            }
        }
    }

    private String getRandomCompany() {
        String[] companies = {"互联网科技有限公司", "金融投资集团", "教育咨询公司", "医疗健康集团", "文化传媒公司", "房地产开发公司", "电商平台", "软件科技公司", "生物医药公司", "新能源科技公司"};
        String[] prefixes = {"北京", "上海", "深圳", "广州", "杭州", "成都", "武汉", "南京"};
        return prefixes[(int)(Math.random() * prefixes.length)] + companies[(int)(Math.random() * companies.length)];
    }

    private String getRandomIntroduction(String nickname, String occupation) {
        String[] intros = {
                "大家好，我是" + nickname + "，一名" + occupation + "。喜欢旅行和美食，希望能找到一个志同道合的人一起探索世界。",
                "你好呀！我是" + nickname + "，从事" + occupation + "工作。平时喜欢看书、看电影，周末会去健身房。期待遇见那个对的人~",
                "Hi~ 我是" + nickname + "，" + occupation + "。热爱生活，喜欢户外运动，性格开朗随和。希望在这里能找到一份真挚的感情。",
                "大家好！我是" + nickname + "，做" + occupation + "的。平时喜欢烹饪和烘焙，会做各种美食。期待和你一起分享生活的美好！",
                "你好！我是" + nickname + "，一名" + occupation + "。喜欢音乐和艺术，周末经常去看展览。希望能遇到懂我的人。"
        };
        return intros[(int)(Math.random() * intros.length)];
    }

    private String getRandomHobbies() {
        String[] hobbies = {"旅游", "美食", "电影", "音乐", "运动", "阅读", "摄影", "烹饪", "游戏", "宠物", "手工", "绘画"};
        StringBuilder sb = new StringBuilder();
        int count = 3 + (int)(Math.random() * 3);
        for (int i = 0; i < count; i++) {
            if (i > 0) sb.append(",");
            sb.append(hobbies[(int)(Math.random() * hobbies.length)]);
        }
        return sb.toString();
    }

    private void initActivities() {
        if (activityRepository.count() == 0) {
            OfflineActivity a1 = new OfflineActivity();
            a1.setTitle("8分钟相亲会");
            a1.setDescription("高效快速的相亲模式，每人8分钟交流时间，遇见你的缘分");
            a1.setCoverImage("activity1.jpg");
            a1.setLocation("北京市朝阳区某咖啡厅");
            a1.setStartTime(LocalDateTime.now().plusDays(7));
            a1.setEndTime(LocalDateTime.now().plusDays(7).plusHours(3));
            a1.setMaxParticipants(30);
            a1.setCurrentParticipants(10);
            a1.setPrice(new BigDecimal("99.00"));
            a1.setVipPrice(new BigDecimal("79.00"));
            activityRepository.save(a1);

            OfflineActivity a2 = new OfflineActivity();
            a2.setTitle("户外徒步联谊");
            a2.setDescription("周末户外徒步活动，在大自然中邂逅美好爱情");
            a2.setCoverImage("activity2.jpg");
            a2.setLocation("北京市香山公园");
            a2.setStartTime(LocalDateTime.now().plusDays(14));
            a2.setEndTime(LocalDateTime.now().plusDays(14).plusHours(8));
            a2.setMaxParticipants(50);
            a2.setCurrentParticipants(25);
            a2.setPrice(new BigDecimal("199.00"));
            a2.setVipPrice(new BigDecimal("159.00"));
            activityRepository.save(a2);

            OfflineActivity a3 = new OfflineActivity();
            a3.setTitle("高端单身派对");
            a3.setDescription("高端人士私人派对，精致的场地和美食，遇见对的人");
            a3.setCoverImage("activity3.jpg");
            a3.setLocation("上海市黄浦区五星酒店");
            a3.setStartTime(LocalDateTime.now().plusDays(21));
            a3.setEndTime(LocalDateTime.now().plusDays(21).plusHours(5));
            a3.setMaxParticipants(100);
            a3.setCurrentParticipants(60);
            a3.setPrice(new BigDecimal("599.00"));
            a3.setVipPrice(new BigDecimal("499.00"));
            activityRepository.save(a3);

            System.out.println("线下活动数据初始化完成");
        }
    }

    private void initPosts() {
        if (postRepository.count() == 0) {
            String[] contents = {
                    "今天天气真好，出去走走~",
                    "周末做了一顿大餐，有没有想尝尝的？",
                    "新的一周，努力工作！",
                    "读完了一本好书，推荐给大家"
            };

            for (int i = 0; i < contents.length; i++) {
                Post post = new Post();
                post.setUserId((long) (3 + i));
                post.setContent(contents[i]);
                post.setLikeCount(10 + i * 5);
                post.setCommentCount(i + 2);
                post.setViewCount(80 + i * 30);
                post.setStatus(1);  // 已发布
                post.setAuditStatus(1);  // 审核通过
                postRepository.save(post);
            }
            System.out.println("动态数据初始化完成");
        }
    }
}
