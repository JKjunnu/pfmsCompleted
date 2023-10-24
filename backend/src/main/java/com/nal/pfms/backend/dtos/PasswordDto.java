package com.nal.pfms.backend.dtos;

import lombok.Data;

@Data
public class PasswordDto {

    private String email;
    private String oldPassword;
    private String newPassword;
}
