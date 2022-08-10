package com.project.sns.controller.response;

import com.project.sns.model.User;
import com.project.sns.model.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserLoginResponse {
    private String token;
}
