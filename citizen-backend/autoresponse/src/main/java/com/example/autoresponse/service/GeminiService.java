package com.example.autoresponse.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public String generateResponse(String complaintText) {
        // ✅ Use latest stable Gemini endpoint (v1)
        String url = "https://generativelanguage.googleapis.com/v1/models/gemini-1.5-flash-latest:generateContent?key=" + geminiApiKey;

        // Request body follows the Gemini v1 API structure
        Map<String, Object> content = Map.of(
                "role", "user",
                "parts", List.of(Map.of("text", "Citizen complaint: " + complaintText))
        );

        Map<String, Object> request = Map.of(
                "contents", List.of(content)
        );

        // Set JSON headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        // Send POST request
        @SuppressWarnings("unchecked")
        Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);

        if (response == null || !response.containsKey("candidates")) {
            return "No response generated from Gemini.";
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
        if (candidates.isEmpty()) {
            return "No response candidates received.";
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> firstCandidate = candidates.get(0);
        @SuppressWarnings("unchecked")
        Map<String, Object> contentObj = (Map<String, Object>) firstCandidate.get("content");

        if (contentObj == null || !contentObj.containsKey("parts")) {
            return "No content generated.";
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> parts = (List<Map<String, Object>>) contentObj.get("parts");
        if (parts == null || parts.isEmpty()) {
            return "No parts generated.";
        }

        return parts.get(0).get("text").toString();
    }
}