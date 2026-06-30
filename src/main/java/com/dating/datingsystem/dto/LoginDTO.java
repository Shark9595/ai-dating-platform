package com.dating.datingsystem.dto;

import lombok.Data;

@Data
public class LoginDTO {
    private String username;
    private String password;
    private String phone;
    private String code;
    private String ipAddress;
    private String userAgent;
}
