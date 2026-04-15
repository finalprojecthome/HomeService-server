package com.homeservice.homeservice_server.services.admin;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AdminServiceUploadProperties {
	private final String bucketName;
	private final long maxFileSizeBytes;

	public AdminServiceUploadProperties(
			@Value("${supabase.bucket.adminservice:admin-service}") String bucketName,
			@Value("${admin.service-upload.max-file-size-bytes:10485760}") long maxFileSizeBytes
	) {
		this.bucketName = bucketName == null ? "" : bucketName.trim();
		this.maxFileSizeBytes = maxFileSizeBytes;
	}

	public String bucketName() {
		return bucketName;
	}

	public long maxFileSizeBytes() {
		return maxFileSizeBytes;
	}
}
