package com.example.schoolhalper_api.domain;

import java.util.Base64;

public class TeacherProfileViewDTO {
    private final TeacherProfile profile;
    private final boolean hasCertificate;
    private final String certificateImageBase64;

    public TeacherProfileViewDTO(TeacherProfile profile) {
        this.profile = profile;
        this.hasCertificate = profile.getCertificateImage() != null;
        this.certificateImageBase64 = hasCertificate
                ? Base64.getEncoder().encodeToString(profile.getCertificateImage())
                : null;
    }

    public TeacherProfile getProfile() {
        return profile;
    }

    public boolean isHasCertificate() {
        return hasCertificate;
    }

    public String getCertificateImageBase64() {
        return certificateImageBase64;
    }
}
