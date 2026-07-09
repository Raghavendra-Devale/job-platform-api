package com.jobrecommendation.resume.application;

import com.jobrecommendation.resume.domain.repository.ResumeRepository;
import com.jobrecommendation.user.domain.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class ResumeValidator {

    private final ResumeRepository resumeRepository;

    public void validateUpload(UserEntity user, MultipartFile file) {
        validateNotEmpty(file);
        validateResumeLimit(user);
        validateFileSize(file);
        validateFileType(file);
    }

    public void validateNotEmpty(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
    }

    public void validateResumeLimit(UserEntity user) {
        long resumeCount = resumeRepository.countByUser(user);
        if (resumeCount >= 4) {
            throw new IllegalArgumentException("You can have a maximum of 4 resumes. Please delete an existing resume to upload a new one.");
        }
    }

    public void validateFileSize(MultipartFile file) {
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("File size exceeds limit of 5MB");
        }
    }

    public void validateFileType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("application/pdf") &&
                !contentType.equals("application/msword") &&
                !contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))) {
            throw new IllegalArgumentException("Only PDF and Word documents (.doc, .docx) are allowed");
        }
    }
}
