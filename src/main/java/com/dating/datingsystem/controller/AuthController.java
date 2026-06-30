package com.dating.datingsystem.controller;

import com.dating.datingsystem.common.Result;
import com.dating.datingsystem.dto.LoginDTO;
import com.dating.datingsystem.dto.RegisterDTO;
import com.dating.datingsystem.dto.UserDTO;
import com.dating.datingsystem.entity.MatchPreference;
import com.dating.datingsystem.entity.UserProfile;
import com.dating.datingsystem.service.UserService;
import com.dating.datingsystem.utils.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Result<UserDTO> register(@RequestBody RegisterDTO registerDTO) {
        UserDTO user = userService.register(registerDTO);
        return Result.success("注册成功", user);
    }

    @PostMapping("/login")
    public Result<UserDTO> login(@RequestBody LoginDTO loginDTO, HttpServletRequest request) {
        // 获取IP地址和User-Agent
        loginDTO.setIpAddress(getIpAddress(request));
        loginDTO.setUserAgent(request.getHeader("User-Agent"));
        
        UserDTO user = userService.login(loginDTO);
        return Result.success("登录成功", user);
    }

    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@RequestBody Map<String, String> params) {
        userService.resetPassword(params.get("phone"), params.get("newPassword"));
        return Result.success("密码重置成功");
    }
    
    /**
     * 获取客户端真实IP地址
     */
    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理时取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
