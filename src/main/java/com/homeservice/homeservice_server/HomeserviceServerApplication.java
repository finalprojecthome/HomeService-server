package com.homeservice.homeservice_server;

import com.homeservice.homeservice_server.config.AdminAuthProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class HomeserviceServerApplication {
	private static final Logger log = LoggerFactory.getLogger(HomeserviceServerApplication.class);

	private final AdminAuthProperties adminAuthProperties;

	public HomeserviceServerApplication(AdminAuthProperties adminAuthProperties) {
		this.adminAuthProperties = adminAuthProperties;
	}

	public static void main(String[] args) {
		SpringApplication.run(HomeserviceServerApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void logAdminRegistrationStatus() {
		boolean enabled = adminAuthProperties.inviteCode() != null && !adminAuthProperties.inviteCode().isBlank();
		log.info("Admin registration is {}", enabled ? "ENABLED" : "DISABLED");
	}

}
