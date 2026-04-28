package com.homeservice.homeservice_server.services;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.homeservice.homeservice_server.dto.supabase.SupabaseUploadFileRequest;
import com.homeservice.homeservice_server.dto.user.UpdateProfileRequest;
import com.homeservice.homeservice_server.entities.User;
import com.homeservice.homeservice_server.exception.BadRequestException;
import com.homeservice.homeservice_server.exception.ConflictException;
import com.homeservice.homeservice_server.repositories.UserRepository;
import com.homeservice.homeservice_server.validation.FileValidationUtils;

@Service
public class UserService {

    private static final String USER_BUCKET = "user-assets";
    private static final DateTimeFormatter FILE_TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final UserRepository userRepository;
    private final SupabaseStorageClient storageClient;

    public UserService(UserRepository userRepository, SupabaseStorageClient storageClient) {
        this.userRepository = userRepository;
        this.storageClient = storageClient;
    }

    @Transactional
    public void updateProfile(UUID userId, UpdateProfileRequest request, MultipartFile profileImage) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("ไม่พบผู้ใช้งาน"));

        String newPhone = request.getPhone();
        String currentPhone = user.getPhone();
        if (newPhone != null && !newPhone.equals(currentPhone) && userRepository.existsByPhone(newPhone)) {
            throw new ConflictException("มีผู้ใช้งานที่ใช้เบอร์โทรศัพท์นี้อยู่แล้ว");
        }

        user.setName(request.getFullname());
        user.setPhone(request.getPhone());

        if (profileImage != null && !profileImage.isEmpty()) {
            FileValidationUtils.validateImageFile(profileImage, "รูปโปรไฟล์");
            String oldImgUrl = user.getImgUrl();

            String extension = FileValidationUtils.extractExtension(profileImage.getOriginalFilename());
            String timestamp = OffsetDateTime.now(ZoneOffset.UTC).format(FILE_TIMESTAMP_FORMATTER);
            String filename = user.getUserId().toString() + "-" + timestamp + extension;

            byte[] content = readBytes(profileImage);

            SupabaseUploadFileRequest uploadRequest = new SupabaseUploadFileRequest(
                    USER_BUCKET,
                    filename,
                    content,
                    profileImage.getContentType(),
                    true);

            String publicUrl = storageClient.uploadPublicFile(uploadRequest);
            user.setImgUrl(publicUrl);

            if (oldImgUrl != null && !oldImgUrl.isBlank()) {
                deleteOldImageIfPossible(oldImgUrl);
            }
        }

        userRepository.save(user);
    }

    private void deleteOldImageIfPossible(String oldImgUrl) {
        String withoutQuery = oldImgUrl.split("\\?", 2)[0];
        int publicIndex = withoutQuery.indexOf("/storage/v1/object/public/");
        if (publicIndex == -1) {
            return;
        }

        String suffix = withoutQuery.substring(publicIndex + "/storage/v1/object/public/".length());
        int firstSlash = suffix.indexOf('/');
        if (firstSlash == -1) {
            return;
        }

        String bucket = suffix.substring(0, firstSlash);
        String path = suffix.substring(firstSlash + 1);
        storageClient.deleteFile(bucket, path);
    }

    private static byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new BadRequestException("ไม่สามารถอ่านไฟล์รูปภาพได้");
        }
    }

}
