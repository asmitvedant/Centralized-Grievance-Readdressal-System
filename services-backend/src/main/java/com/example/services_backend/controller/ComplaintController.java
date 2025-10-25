package com.example.services_backend.controller;

import com.example.services_backend.model.Complaint;
import com.example.services_backend.service.ComplaintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/complaints")
public class ComplaintController {

    @Autowired
    private ComplaintService complaintService;

    @PostMapping
    public Complaint fileComplaint(@RequestBody Complaint complaint) {
        // 1️⃣ Save complaint first
        Complaint saved = complaintService.saveComplaint(complaint);

        // 2️⃣ Call AutoResponse microservice (Gemini API wrapper)
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = "http://localhost:8082/api/autoresponse/generate"; // 👈 your AI service

            Map<String, String> request = Map.of("complaint", saved.getDescription());
            Map response = restTemplate.postForObject(url, request, Map.class);

            if (response != null && response.containsKey("autoResponse")) {
                saved.setAutoComment((String) response.get("autoResponse"));
                saved = complaintService.saveComplaint(saved); // update with autoComment
            }
        } catch (Exception e) {
            saved.setAutoComment("⚠️ Auto-response unavailable.");
            saved = complaintService.saveComplaint(saved);
        }

        return saved;
    }

    @GetMapping
    public List<Complaint> getAllComplaints() {
        return complaintService.getAllComplaints();
    }

    @GetMapping("/{id}")
    public Optional<Complaint> getComplaintById(@PathVariable Long id) {
        return complaintService.getComplaintById(id);
    }

    @GetMapping("/user/{username}")
    public List<Complaint> getComplaintsByUser(@PathVariable String username) {
        return complaintService.getComplaintsByUsername(username);
    }

    @DeleteMapping("/{id}")
    public String deleteComplaint(@PathVariable Long id) {
        complaintService.deleteComplaint(id);
        return "Complaint deleted successfully!";
    }
}