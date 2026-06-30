package com.dating.datingsystem.service;

import com.dating.datingsystem.entity.SensitiveWord;
import com.dating.datingsystem.repository.SensitiveWordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 内容过滤服务 - 敏感词过滤
 */
@Service
public class ContentFilterService {

    private static final Logger logger = LoggerFactory.getLogger(ContentFilterService.class);

    @Autowired
    private SensitiveWordRepository sensitiveWordRepository;

    /**
     * 敏感词集合（DFA算法使用的敏感词Map）
     */
    private Map<Character, Object> sensitiveWordMap = new HashMap<>();

    /**
     * 基础敏感词列表（用于初始化）
     */
    private static final Set<String> DEFAULT_SENSITIVE_WORDS = new HashSet<>(Arrays.asList(
            // 色情相关
            "色情", "黄色", "性爱", "做爱", "强奸", "乱伦", "嫖娼", "卖淫", "援交", "约炮",
            "一夜情", "裸聊", "裸体", "裸照", "黄片", "A片", "av", "AV", "成人电影",
            "淫秽", "淫乱", "黄色电影", "黄色网站", "黄色视频", "黄色图片",
            // 暴力相关
            "暴力", "杀人", "凶杀", "谋杀", "暗杀", "恐怖袭击", "炸弹", "爆炸", "袭击",
            "血腥", "残忍", "虐待", "酷刑", "肢解", "碎尸", "凶器", "武器",
            // 诈骗相关
            "诈骗", "骗钱", "骗贷", "传销", "非法集资", "集资诈骗", "金融诈骗",
            "电信诈骗", "网络诈骗", "短信诈骗", "电话诈骗", "钓鱼网站",
            "套现", "洗钱", "地下钱庄", "非法经营", "非法传销",
            // 赌博相关
            "赌博", "赌钱", "赌场", "赌球", "赌马", "百家乐", "老虎机", "押注", "下注",
            "博彩", "彩票", "六合彩", "私彩", "网络赌场",
            // 毒品相关
            "毒品", "大麻", "海洛因", "冰毒", "摇头丸", "可卡因", "吗啡", "鸦片",
            "吸毒", "贩毒", "运毒", "制毒",
            // 政治敏感
            "反动", "颠覆", "煽动", "分裂国家", "推翻政府", "反政府",
            // 其他敏感词
            "自杀", "自残", "跳楼", "上吊", "割腕", "安乐死",
            "代孕", "买卖儿童", "拐卖", "人贩子"
    ));

    /**
     * 初始化敏感词库
     */
    @PostConstruct
    public void init() {
        loadSensitiveWords();
    }

    /**
     * 加载敏感词（从数据库和默认列表）
     */
    public void loadSensitiveWords() {
        Map<Character, Object> newMap = new HashMap<>();

        // 添加默认敏感词
        for (String word : DEFAULT_SENSITIVE_WORDS) {
            addWordToMap(newMap, word);
        }

        // 从数据库加载敏感词
        try {
            List<SensitiveWord> dbWords = sensitiveWordRepository.findByStatus(1);
            for (SensitiveWord sw : dbWords) {
                if (sw.getWord() != null && !sw.getWord().isEmpty()) {
                    addWordToMap(newMap, sw.getWord());
                }
            }
        } catch (Exception e) {
            logger.warn("从数据库加载敏感词失败，仅使用默认敏感词: {}", e.getMessage());
        }

        this.sensitiveWordMap = newMap;
        logger.info("敏感词库加载完成，共 {} 个敏感词", countSensitiveWords(newMap));
    }

    /**
     * 计算敏感词数量
     */
    private int countSensitiveWords(Map<Character, Object> map) {
        int count = 0;
        for (Object value : map.values()) {
            if (value instanceof Map) {
                count += countSensitiveWords((Map<Character, Object>) value);
            } else if (value instanceof Boolean && (Boolean) value) {
                count++;
            }
        }
        return count;
    }

    /**
     * 将敏感词添加到Map中（DFA算法）
     */
    @SuppressWarnings("unchecked")
    private void addWordToMap(Map<Character, Object> map, String word) {
        Map<Character, Object> currentMap = map;
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            Object obj = currentMap.get(c);
            if (obj == null) {
                Map<Character, Object> newMap = new HashMap<>();
                currentMap.put(c, newMap);
                currentMap = newMap;
            } else {
                currentMap = (Map<Character, Object>) obj;
            }
            // 最后一个字符标记为结束
            if (i == word.length() - 1) {
                currentMap.put((char) 0, true);
            }
        }
    }

    /**
     * 过滤敏感词
     * 
     * @param content 待过滤内容
     * @return 过滤后的内容，敏感词替换为***
     */
    public String filter(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }

        StringBuilder result = new StringBuilder(content);
        Set<String> sensitiveWords = findSensitiveWords(content);

        for (String word : sensitiveWords) {
            int index;
            while ((index = result.indexOf(word)) != -1) {
                result.replace(index, index + word.length(), getReplacement(word.length()));
            }
        }

        return result.toString();
    }

    /**
     * 检查是否包含敏感词
     */
    public boolean containsSensitiveWord(String content) {
        return !findSensitiveWords(content).isEmpty();
    }

    /**
     * 查找所有敏感词
     */
    public Set<String> findSensitiveWords(String content) {
        Set<String> sensitiveWords = new HashSet<>();
        if (content == null || content.isEmpty()) {
            return sensitiveWords;
        }

        for (int i = 0; i < content.length(); i++) {
            int length = checkSensitiveWord(content, i);
            if (length > 0) {
                sensitiveWords.add(content.substring(i, i + length));
                i += length - 1;
            }
        }

        return sensitiveWords;
    }

    /**
     * 检查从指定位置开始的敏感词
     * 
     * @return 敏感词长度，如果不是敏感词则返回0
     */
    @SuppressWarnings("unchecked")
    private int checkSensitiveWord(String content, int beginIndex) {
        Map<Character, Object> currentMap = sensitiveWordMap;
        int matchLength = 0;
        int lastMatchLength = 0;

        for (int i = beginIndex; i < content.length(); i++) {
            char c = content.charAt(i);
            currentMap = (Map<Character, Object>) currentMap.get(c);
            if (currentMap == null) {
                break;
            }
            matchLength++;
            if (currentMap.containsKey((char) 0)) {
                lastMatchLength = matchLength;
            }
        }

        return lastMatchLength;
    }

    /**
     * 生成替换字符串
     */
    private String getReplacement(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append("*");
        }
        return sb.toString();
    }

    /**
     * 添加新敏感词
     */
    public void addSensitiveWord(String word) {
        if (word != null && !word.isEmpty()) {
            addWordToMap(sensitiveWordMap, word);
        }
    }

    /**
     * 重新加载敏感词库
     */
    public void reloadSensitiveWords() {
        loadSensitiveWords();
    }
}