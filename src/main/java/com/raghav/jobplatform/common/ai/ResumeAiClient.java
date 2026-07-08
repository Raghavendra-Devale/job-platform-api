package com.raghav.jobplatform.common.ai;

import com.raghav.jobplatform.common.ai.dto.ResumeIntelligenceResponse;
import com.raghav.jobplatform.common.ai.dto.ResumeProcessRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@Component
public class ResumeAiClient {

    private final WebClient webClient;
    private final AIProperties properties;

    public ResumeAiClient(@Qualifier("aiWebClient") WebClient webClient, AIProperties properties) {
        this.webClient = webClient;
        this.properties = properties;
    }

    public ResumeIntelligenceResponse processResume(ResumeProcessRequest request) {
        if (!properties.isEnabled()) {
            log.warn("AI processing requested but AI Client is disabled.");
            throw new AIException("AI Client is disabled by configuration");
        }

        if (request == null || request.getFileBytes() == null || request.getFileBytes().length == 0) {
            throw new AIException("Resume file bytes cannot be empty");
        }

        String filename = request.getFilename() != null ? request.getFilename() : "resume.pdf";

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", new ByteArrayResource(request.getFileBytes()) {
            @Override
            public String getFilename() {
                return filename;
            }
        }, MediaType.APPLICATION_PDF);

        if (request.getResumeId() != null) {
            builder.part("resumeId", request.getResumeId().toString());
        }
        if (request.getUserId() != null) {
            builder.part("userId", request.getUserId().toString());
        }

        MultiValueMap<String, HttpEntity<?>> body = builder.build();

        long startTime = System.currentTimeMillis();
        String endpoint = "/api/v1/resume/process";
        log.info("Sending resume process request to endpoint: {}", endpoint);

        try {
            ResumeIntelligenceResponse response = webClient.post()
                    .uri(endpoint)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(ResumeIntelligenceResponse.class)
                    .block();

            long duration = System.currentTimeMillis() - startTime;
            log.info("AI resume processing successful. Endpoint: {}, Duration: {}ms, Success: {}", 
                     endpoint, duration, response != null && response.isSuccess());

            if (response == null) {
                throw new AIException("Received null response from AI Engine");
            }

            return response;

        } catch (WebClientResponseException e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("AI engine returned error status: {}. Endpoint: {}, Duration: {}ms, Success: false", 
                      e.getStatusCode(), endpoint, duration);
            throw new AIException("AI Engine returned error: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("AI engine communication failed. Endpoint: {}, Duration: {}ms, Success: false", 
                      endpoint, duration, e);
            throw new AIException("Failed to communicate with AI Engine", e);
        }
    }
}
