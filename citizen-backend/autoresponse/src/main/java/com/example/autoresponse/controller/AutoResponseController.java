package com.example.autoresponse.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@RestController
@RequestMapping("/api/autoresponse")
public class AutoResponseController {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/generate")
    public Map<String, String> generateResponse(@RequestBody Map<String, String> request) throws Exception {
        String complaintText = request.get("complaint");

        // Gemini API expects this JSON structure
        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text",
                                        "Citizen complaint: " + complaintText +
                                        "\n\nWrite a short, polite, and professional acknowledgment (2–3 sentences max) for this complaint. " +
                                        "It should sound like an official government response. Example:\n" +
                                        "\"Thank you for contacting us regarding [issue]. Your report has been received and will be reviewed. We appreciate your patience.\""
                                )
                        ))
                )
        );

        // ✅ Gemini API key must be passed as query parameter
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + geminiApiKey;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        // Call Gemini API
        String response = restTemplate.postForObject(url, entity, String.class);

        // Parse JSON
        JsonNode root = objectMapper.readTree(response);
        String autoResponse = root.path("candidates").get(0)
                                  .path("content").path("parts").get(0)
                                  .path("text").asText();

        // ✅ Keep response short and safe
        if (autoResponse.length() > 250) {
            autoResponse = autoResponse.substring(0, 250) + "...";
        }

        Map<String, String> result = new HashMap<>();
        result.put("complaint", complaintText);
        result.put("autoResponse", autoResponse);

        return result;
    }
}