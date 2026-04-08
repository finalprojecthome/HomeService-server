package com.homeservice.homeservice_server.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.homeservice.homeservice_server.enums.UserRole;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(name = "users_phone_key", columnNames = "phone"),
        @UniqueConstraint(name = "users_email_key", columnNames = "email")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @Column(name = "user_id", nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID userId;

    @NotBlank
    @Size(max = 100)
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @NotBlank
    @Size(min = 10, max = 10)
    @Pattern(regexp = "^0[1-9][0-9]{8}$", message = "รูปแบบเบอร์โทรศัพท์ไม่ถูกต้อง")
    @Column(name = "phone", nullable = false, length = 10)
    private String phone;

    @NotBlank
    @Email
    @Column(name = "email", nullable = false, columnDefinition = "text")
    private String email;

    @Column(name = "img_url", columnDefinition = "text")
    private String imgUrl;

    @NotNull
    @Column(name = "role", nullable = false)
    @Builder.Default
    private UserRole role = UserRole.USER;

    @NotNull
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    void prePersist() {
        if (updatedAt == null) {
            updatedAt = OffsetDateTime.now();
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
