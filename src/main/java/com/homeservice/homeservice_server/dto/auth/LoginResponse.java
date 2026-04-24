package com.homeservice.homeservice_server.dto.auth;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class LoginResponse {
	String message;
	String accessToken;
}
