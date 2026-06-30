package com.dating.datingsystem.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dating.datingsystem.config.DeepSeekConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DeepSeek AI 服务
 * 提供智能匹配推荐理由、情感分析、智能聊天等功能
 */
@Service
public class DeepSeekService {

    private static final Logger logger = LoggerFactory.getLogger(DeepSeekService.class);

    private final DeepSeekConfig deepSeekConfig;

    public DeepSeekService(DeepSeekConfig deepSeekConfig) {
        this.deepSeekConfig = deepSeekConfig;
    }

    /**
     * 调用 DeepSeek API
     */
    public String callDeepSeek(String systemPrompt, String userPrompt) {
        // 如果未配置 API Key 或是占位符，返回模拟响应
        if (deepSeekConfig.getApiKey() == null || deepSeekConfig.getApiKey().isEmpty() 
                || deepSeekConfig.getApiKey().equals("sk-your-api-key-here")) {
            logger.info("DeepSeek API Key 未配置，使用模拟响应");
            return getMockResponse(systemPrompt, userPrompt);
        }

        try {
            URL url = new URL(deepSeekConfig.getBaseUrl() + "/chat/completions");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + deepSeekConfig.getApiKey());
            conn.setDoOutput(true);
            // 增加超时时间
            conn.setConnectTimeout(30000);  // 30秒连接超时
            conn.setReadTimeout(60000);     // 60秒读取超时
            
            logger.debug("正在调用 DeepSeek API: {}", deepSeekConfig.getBaseUrl());

            // 构建请求体
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", deepSeekConfig.getModel());

            JSONArray messages = new JSONArray();
            
            // 系统提示词
            JSONObject systemMsg = new JSONObject();
            systemMsg.put("role", "system");
            systemMsg.put("content", systemPrompt);
            messages.add(systemMsg);

            // 用户提示词
            JSONObject userMsg = new JSONObject();
            userMsg.put("role", "user");
            userMsg.put("content", userPrompt);
            messages.add(userMsg);

            requestBody.put("messages", messages);
            requestBody.put("max_tokens", deepSeekConfig.getMaxTokens());
            requestBody.put("temperature", deepSeekConfig.getTemperature());

            // 发送请求
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = requestBody.toJSONString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // 读取响应
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }

                    JSONObject jsonResponse = JSON.parseObject(response.toString());
                    JSONArray choices = jsonResponse.getJSONArray("choices");
                    if (choices != null && choices.size() > 0) {
                        JSONObject choice = choices.getJSONObject(0);
                        JSONObject message = choice.getJSONObject("message");
                        return message.getString("content");
                    }
                }
            } else {
                logger.error("DeepSeek API 调用失败，响应码: {}", responseCode);
                // 读取错误信息
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                    StringBuilder errorResponse = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        errorResponse.append(line);
                    }
                    logger.error("错误信息: {}", errorResponse.toString());
                }
            }
        } catch (Exception e) {
            logger.error("调用 DeepSeek API 异常: {}", e.getMessage(), e);
        }

        return getMockResponse(systemPrompt, userPrompt);
    }

    /**
     * 生成匹配推荐理由
     */
    public String generateMatchReason(Map<String, Object> user1, Map<String, Object> user2, double matchScore) {
        String systemPrompt = "你是一个专业的婚恋顾问，擅长分析两个人之间的匹配度，并用温暖、专业的语言给出推荐理由。请用简洁、有吸引力的方式描述。";

        String userPrompt = String.format("请为以下两位用户生成匹配推荐理由（要求50字以内）：\n" +
                "用户1：%s，%d岁，职业%s，学历%s，爱好%s\n" +
                "用户2：%s，%d岁，职业%s，学历%s，爱好%s\n" +
                "匹配度：%.0f%%\n" +
                "请直接输出推荐理由，不要有其他内容。",
                user1.get("nickname"), user1.get("age"),
                user1.getOrDefault("occupation", "未知"),
                user1.getOrDefault("education", "未知"),
                user1.getOrDefault("hobbies", "未知"),
                user2.get("nickname"), user2.get("age"),
                user2.getOrDefault("occupation", "未知"),
                user2.getOrDefault("education", "未知"),
                user2.getOrDefault("hobbies", "未知"),
                matchScore);

        return callDeepSeek(systemPrompt, userPrompt);
    }

    /**
     * 生成个性化介绍
     */
    public String generatePersonalIntro(Map<String, Object> user) {
        String systemPrompt = "你是一个专业的婚恋顾问，擅长用简洁、有吸引力的语言描述用户特点。";

        String userPrompt = String.format("请用50字以内为以下用户生成一条有吸引力的个人介绍：\n" +
                "昵称：%s\n" +
                "年龄：%d岁\n" +
                "职业：%s\n" +
                "学历：%s\n" +
                "爱好：%s\n" +
                "个人描述：%s\n" +
                "请直接输出介绍，不要有其他内容。",
                user.get("nickname"), user.get("age"),
                user.getOrDefault("occupation", "未知"),
                user.getOrDefault("education", "未知"),
                user.getOrDefault("hobbies", "未知"),
                user.getOrDefault("introduction", "暂无"));

        return callDeepSeek(systemPrompt, userPrompt);
    }

    /**
     * 情感分析
     */
    public Map<String, Object> analyzeEmotion(String text) {
        Map<String, Object> result = new HashMap<>();
        
        String systemPrompt = "你是一个情感分析专家，分析用户消息的情感倾向。";
        
        String userPrompt = String.format("请分析以下消息的情感倾向，返回JSON格式：\n" +
                "消息内容：%s\n" +
                "返回格式：{\"emotion\":\"positive/neutral/negative\",\"score\":0-1,\"suggestion\":\"建议\"}",
                text);

        String response = callDeepSeek(systemPrompt, userPrompt);
        
        try {
            JSONObject jsonResponse = JSON.parseObject(response);
            result.put("emotion", jsonResponse.getString("emotion"));
            result.put("score", jsonResponse.getDoubleValue("score"));
            result.put("suggestion", jsonResponse.getString("suggestion"));
        } catch (Exception e) {
            result.put("emotion", "neutral");
            result.put("score", 0.5);
            result.put("suggestion", "继续真诚交流");
        }
        
        return result;
    }

    /**
     * 智能聊天回复
     */
    public String getChatReply(String userMessage, String userInfo) {
        String systemPrompt = String.format("你是一个真诚、温暖的红娘顾问。用户信息：%s。请用友好、专业的方式回复，帮助用户解决情感困惑或聊天话题建议。回复要简洁，不超过100字。", userInfo);

        return callDeepSeek(systemPrompt, userMessage);
    }

    /**
     * 生成聊天话题建议
     */
    public List<String> generateChatTopics(Map<String, Object> user1, Map<String, Object> user2) {
        List<String> topics = new ArrayList<>();
        
        String systemPrompt = "你是一个聊天话题专家，擅长为两个可能感兴趣的用户推荐聊天话题。";
        
        String userPrompt = String.format("请为以下两位用户推荐3个适合的聊天话题：\n" +
                "用户1：%s，%d岁，职业%s，爱好%s\n" +
                "用户2：%s，%d岁，职业%s，爱好%s\n" +
                "请用JSON数组格式返回，格式：[{\"topic\":\"话题\",\"reason\":\"为什么推荐\"}]",
                user1.get("nickname"), user1.get("age"), user1.getOrDefault("occupation", "未知"), user1.getOrDefault("hobbies", "未知"),
                user2.get("nickname"), user2.get("age"), user2.getOrDefault("occupation", "未知"), user2.getOrDefault("hobbies", "未知"));

        String response = callDeepSeek(systemPrompt, userPrompt);
        
        try {
            JSONArray jsonArray = JSON.parseArray(response);
            for (int i = 0; i < Math.min(3, jsonArray.size()); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                topics.add(obj.getString("topic"));
            }
        } catch (Exception e) {
            // 返回默认话题
            topics.add("聊聊最近的旅行经历");
            topics.add("分享最喜欢的美食");
            topics.add("讨论理想的生活方式");
        }
        
        return topics;
    }

    /**
     * 获取约会建议
     */
    public String getDateSuggestion(Map<String, Object> user1, Map<String, Object> user2) {
        String systemPrompt = "你是一个约会策划专家，擅长为情侣推荐适合的约会方式和地点。";
        
        String userPrompt = String.format("请为以下两位用户推荐一个约会方案：\n" +
                "用户1：%s，%s，爱好%s\n" +
                "用户2：%s，%s，爱好%s\n" +
                "请用50字以内描述推荐的约会方式和地点。",
                user1.get("nickname"), user1.getOrDefault("occupation", "未知"),
                user1.getOrDefault("hobbies", "未知"),
                user2.get("nickname"), user2.getOrDefault("occupation", "未知"),
                user2.getOrDefault("hobbies", "未知"));

        return callDeepSeek(systemPrompt, userPrompt);
    }

    /**
     * 模拟响应（当未配置 API Key 时使用）
     */
    private String getMockResponse(String systemPrompt, String userPrompt) {
        if (systemPrompt.contains("匹配推荐理由")) {
            String[] reasons = {
                    "你们都是热爱生活的人，共同话题很多哦！",
                    "性格互补，他/她的稳重能给你安全感",
                    "相似的价值观和生活态度，相处会更融洽",
                    "都向往温馨的家庭生活，未来规划一致",
                    "你们的兴趣爱好有交集，聊天不会冷场"
            };
            return reasons[(int) (Math.random() * reasons.length)];
        } else if (systemPrompt.contains("个人介绍")) {
            return "一个热爱生活、积极向上的年轻人，期待遇见缘分~";
        } else if (systemPrompt.contains("聊天话题")) {
            return "[\"聊聊工作生活\",\"分享兴趣爱好\",\"讨论旅行美食\"]";
        } else if (systemPrompt.contains("约会策划")) {
            return "推荐去环境优雅的咖啡厅，既轻松又浪漫";
        } else if (systemPrompt.contains("红娘顾问")) {
            String[] replies = {
                    "继续保持真诚的交流，慢慢了解对方",
                    "可以试着聊聊彼此的兴趣爱好，找到共同话题",
                    "约会时选择一个安静舒适的场所会更放松哦"
            };
            return replies[(int) (Math.random() * replies.length)];
        }
        return "感谢你的分享，继续保持真诚的交流吧！";
    }
}
