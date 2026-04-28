package com.homeservice.homeservice_server.validation;

import java.util.Set;

import org.springframework.web.multipart.MultipartFile;

import com.homeservice.homeservice_server.exception.BadRequestException;

public final class FileValidationUtils {

    /** Default max image size: 10MB. */
    private static final long DEFAULT_MAX_IMAGE_BYTES = 10L * 1024L * 1024L;

    private static final Set<String> IMAGE_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/jpg",
            "image/png");

    private static final Set<String> IMAGE_EXTENSIONS = Set.of(
            ".jpg",
            ".jpeg",
            ".png");

    private FileValidationUtils() {
    }

    public static void validateImageFile(MultipartFile file, String fieldLabel) {
        validateImageFile(file, fieldLabel, DEFAULT_MAX_IMAGE_BYTES);
    }

    public static void validateImageFile(MultipartFile file, String fieldLabel, long maxBytes) {
        if (file == null || file.isEmpty()) {
            return;
        }

        if (maxBytes > 0 && file.getSize() > maxBytes) {
            String label = (fieldLabel == null || fieldLabel.isBlank()) ? "ไฟล์" : fieldLabel;
            throw new BadRequestException(
                    label + " ต้องมีขนาดไม่เกิน " + (maxBytes / (1024 * 1024)) + "MB");
        }

        String contentType = file.getContentType();
        String extension = extractExtension(file.getOriginalFilename());

        boolean contentTypeOk = contentType != null
                && IMAGE_CONTENT_TYPES.contains(contentType.toLowerCase());
        boolean extensionOk = extension != null
                && IMAGE_EXTENSIONS.contains(extension.toLowerCase());

        if (!contentTypeOk || !extensionOk) {
            String label = (fieldLabel == null || fieldLabel.isBlank()) ? "ไฟล์" : fieldLabel;
            throw new BadRequestException(
                    label + " ต้องเป็นไฟล์รูปภาพชนิด JPG หรือ PNG เท่านั้น");
        }
    }

    public static String extractExtension(String originalFilename) {
        if (originalFilename == null) {
            return null;
        }
        String trimmed = originalFilename.trim();
        int lastDot = trimmed.lastIndexOf('.');
        if (lastDot == -1 || lastDot == trimmed.length() - 1) {
            return "";
        }
        return trimmed.substring(lastDot).toLowerCase();
    }
}
