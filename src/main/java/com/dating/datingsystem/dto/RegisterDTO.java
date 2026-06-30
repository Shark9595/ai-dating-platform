package com.dating.datingsystem.dto;

import lombok.Data;

@Data
public class RegisterDTO {
    private String username;
    private String password;
    private String phone;
    private String code;
    private Integer gender;
    private Integer age;
    private String nickname;
}
