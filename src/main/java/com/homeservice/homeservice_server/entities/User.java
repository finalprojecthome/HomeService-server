package com.homeservice.homeservice_server.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
    @Pattern(regexp = "^0[1-9][0-9]{8}$", message = "phone must be a Thai mobile number (10 digits starting with 0)")
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

    @Column(name = "password", columnDefinition = "text")
    private String password;

    @Column(name = "last_login_at")
    private java.time.OffsetDateTime lastLoginAt;

    @Column(name = "created_at")
    private java.time.OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private java.time.OffsetDateTime updatedAt;

    @Column(name = "address_detail", columnDefinition = "text")
    private String addressDetail;

    @Column(name = "sub_district_id")
    private Integer subDistrictId;
}
