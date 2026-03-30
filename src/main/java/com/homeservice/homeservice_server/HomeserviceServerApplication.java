package com.homeservice.homeservice_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.homeservice.homeservice_server.config.SupabaseProperties;

@SpringBootApplication
@EnableConfigurationProperties(SupabaseProperties.class)
public class HomeserviceServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(HomeserviceServerApplication.class, args);
	}

}
