package com.jobrecommendation.infrastructure.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeProcessRequest {
    private byte[] fileBytes;
    private String filename;
    private Long resumeId;
    private Long userId;

    public ResumeProcessRequest(byte[] fileBytes, String filename) {
        this.fileBytes = fileBytes;
        this.filename = filename;
    }
}
