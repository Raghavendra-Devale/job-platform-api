package com.jobrecommendation.infrastructure.ai.client;

import com.jobrecommendation.infrastructure.ai.dto.RecommendationRequest;
import com.jobrecommendation.infrastructure.ai.dto.RecommendationResponse;
import com.jobrecommendation.infrastructure.ai.exception.AIException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@Component
public class RecommendationAiClient {

    private final WebClient webClient;

    public RecommendationAiClient(@Qualifier("aiEngineWebClient") WebClient webClient) {

        this.webClient = webClient;
    }

    public RecommendationResponse generateRecommendations(RecommendationRequest request) {
        if (request == null) {
            throw new AIException("Request cannot be null");
        }

        String endpoint = "/api/v1/recommendations/generate";
        log.info("Sending recommendations request to endpoint: {}", endpoint);

        try {
            RecommendationResponse response = webClient.post()
                    .uri(endpoint)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(RecommendationResponse.class)
                    .block();

            if (response == null) {
                throw new AIException("Received null response from AI Engine");
            }

            return response;
        } catch (WebClientResponseException e) {
            log.error("AI engine returned error status: {}. Endpoint: {}", e.getStatusCode(), endpoint);
            throw new AIException("AI Engine returned error: " + e.getResponseBodyAsString(), e);
        } catch (WebClientRequestException e) {
            log.error("Failed to connect to AI engine. Endpoint: {}", endpoint, e);
            throw new AIException("AI Engine is unreachable", e);
        } catch (Exception e) {
            log.error("AI engine communication failed. Endpoint: {}", endpoint, e);
            throw new AIException("Failed to communicate with AI Engine", e);
        }
    }
}
