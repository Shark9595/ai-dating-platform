package com.dating.datingsystem.service;

import com.dating.datingsystem.dto.LoginDTO;
import com.dating.datingsystem.dto.RegisterDTO;
import com.dating.datingsystem.dto.UserDTO;
import com.dating.datingsystem.entity.MatchPreference;
import com.dating.datingsystem.entity.User;
import com.dating.datingsystem.entity.UserProfile;
import com.dating.datingsystem.repository.MatchPreferenceRepository;
import com.dating.datingsystem.repository.UserProfileRepository;
import com.dating.datingsystem.repository.UserRepository;
import com.dating.datingsystem.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private MatchPreferenceRepository matchPreferenceRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuditLogService auditLogService;

    @Transactional
    public UserDTO register(RegisterDTO registerDTO) {
        if (userRepository.existsByUsername(registerDTO.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        if (registerDTO.getPhone() != null && userRepository.existsByPhone(registerDTO.getPhone())) {
            throw new RuntimeException("手机号已注册");
        }

        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setPhone(registerDTO.getPhone());
        user.setNickname(registerDTO.getNickname() != null ? registerDTO.getNickname() : registerDTO.getUsername());
        user.setGender(registerDTO.getGender());
        user.setAge(registerDTO.getAge());
        user = userRepository.save(user);

        UserProfile profile = new UserProfile();
        profile.setUserId(user.getId());
        userProfileRepository.save(profile);

        MatchPreference preference = new MatchPreference();
        preference.setUserId(user.getId());
        matchPreferenceRepository.save(preference);

        return convertToDTO(user);
    }

    public UserDTO login(LoginDTO loginDTO) {
        User user = userRepository.findByUsername(loginDTO.getUsername())
                .orElseThrow(() -> new RuntimeException("用户名或密码错误"));

        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        if (user.getStatus() != 1) {
            throw new RuntimeException("账号已被禁用");
        }

        user.setLastLoginTime(LocalDateTime.now());
        userRepository.save(user);

        UserDTO userDTO = convertToDTO(user);
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        userDTO.setToken(token);
        
        // 记录登录日志
        auditLogService.logLogin(user.getId(), loginDTO.getIpAddress(), loginDTO.getUserAgent(), AuditLogService.STATUS_SUCCESS);
        
        return userDTO;
    }

    public UserDTO getUserInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return convertToDTO(user);
    }

    public UserProfile getUserProfile(Long userId) {
        return userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("用户资料不存在"));
    }

    @Transactional
    public UserProfile updateUserProfile(Long userId, UserProfile profile) {
        UserProfile existProfile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("用户资料不存在"));

        // 记录修改的字段
        Map<String, Object> modifiedFields = new HashMap<>();
        if (profile.getHeight() != null && !profile.getHeight().equals(existProfile.getHeight())) {
            modifiedFields.put("height", profile.getHeight());
        }
        if (profile.getWeight() != null && !profile.getWeight().equals(existProfile.getWeight())) {
            modifiedFields.put("weight", profile.getWeight());
        }
        if (profile.getEducation() != null && !profile.getEducation().equals(existProfile.getEducation())) {
            modifiedFields.put("education", profile.getEducation());
        }
        if (profile.getOccupation() != null && !profile.getOccupation().equals(existProfile.getOccupation())) {
            modifiedFields.put("occupation", profile.getOccupation());
        }
        if (profile.getSalary() != null && !profile.getSalary().equals(existProfile.getSalary())) {
            modifiedFields.put("salary", profile.getSalary());
        }
        if (profile.getHometown() != null && !profile.getHometown().equals(existProfile.getHometown())) {
            modifiedFields.put("hometown", profile.getHometown());
        }
        if (profile.getCity() != null && !profile.getCity().equals(existProfile.getCity())) {
            modifiedFields.put("city", profile.getCity());
        }
        if (profile.getIntroduction() != null && !profile.getIntroduction().equals(existProfile.getIntroduction())) {
            modifiedFields.put("introduction", profile.getIntroduction());
        }
        
        profile.setId(existProfile.getId());
        profile.setUserId(userId);
        UserProfile savedProfile = userProfileRepository.save(profile);
        
        // 记录资料修改日志（如果有修改）
        if (!modifiedFields.isEmpty()) {
            auditLogService.logProfileModified(userId, modifiedFields);
        }
        
        return savedProfile;
    }

    public MatchPreference getMatchPreference(Long userId) {
        return matchPreferenceRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("择偶偏好不存在"));
    }

    @Transactional
    public MatchPreference updateMatchPreference(Long userId, MatchPreference preference) {
        MatchPreference existPreference = matchPreferenceRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("择偶偏好不存在"));

        preference.setId(existPreference.getId());
        preference.setUserId(userId);
        return matchPreferenceRepository.save(preference);
    }

    @Transactional
    public void submitRealName(Long userId, String realName, String idCard) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElse(new UserProfile());
        profile.setUserId(userId);
        profile.setRealName(realName);
        profile.setIdCard(idCard);
        userProfileRepository.save(profile);

        user.setRealNameStatus(1);
        userRepository.save(user);
        
        // 记录实名认证日志
        auditLogService.logRealNameAuth(userId, AuditLogService.STATUS_SUCCESS);
    }

    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        // 记录密码修改日志（这里无法获取IP，需要在Controller层传递）
        auditLogService.logPasswordChanged(userId, null);
    }

    @Transactional
    public void resetPassword(String phone, String newPassword) {
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("手机号未注册"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public void updateAvatar(Long userId, String avatar) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setAvatar(avatar);
        userRepository.save(user);
        
        // 记录头像更新日志
        auditLogService.logAvatarUpdated(userId, avatar);
    }

    @Transactional
    public void updateNickname(Long userId, String nickname) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setNickname(nickname);
        userRepository.save(user);
        
        // 记录昵称更新日志
        auditLogService.logNicknameUpdated(userId, nickname);
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        dto.setAvatar(user.getAvatar());
        dto.setGender(user.getGender());
        dto.setAge(user.getAge());
        dto.setRole(user.getRole());
        dto.setVipLevel(user.getVipLevel());
        dto.setPoints(user.getPoints());
        dto.setRealNameStatus(user.getRealNameStatus());
        dto.setStatus(user.getStatus());
        return dto;
    }
}
