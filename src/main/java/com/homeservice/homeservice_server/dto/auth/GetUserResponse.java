package com.homeservice.homeservice_server.dto.auth;

import com.homeservice.homeservice_server.enums.UserRole;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class GetUserResponse {
    String id;
    String name;
    String phone;
    String email;
    String imgUrl;
    UserRole role;
}
