package com.dating.datingsystem.dto;

import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private Integer gender;
    private Integer age;
    private String role;
    private Integer vipLevel;
    private Integer points;
    private Integer realNameStatus;
    private Integer status;
    private String token;
}
