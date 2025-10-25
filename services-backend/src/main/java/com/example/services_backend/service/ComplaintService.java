package com.example.services_backend.service;

import com.example.services_backend.model.Complaint;
import com.example.services_backend.repository.ComplaintRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final RestTemplate restTemplate;

    @Autowired
    public ComplaintService(ComplaintRepository complaintRepository, RestTemplate restTemplate) {
        this.complaintRepository = complaintRepository;
        this.restTemplate = restTemplate;
    }

    public Complaint saveComplaint(Complaint complaint) {
        Complaint saved = complaintRepository.save(complaint);

        // ✅ Call AutoResponse microservice after saving complaint
        try {
            String url = "http://localhost:8082/api/autoresponse/generate"; // fixed URL

            Map<String, String> request = new HashMap<>();
            request.put("complaint", complaint.getDescription());

            @SuppressWarnings("unchecked")
            Map<String, String> response = restTemplate.postForObject(url, request, Map.class);

            if (response != null && response.containsKey("autoResponse")) {
                saved.setAutoComment(response.get("autoResponse")); // ✅ store auto-response
                saved = complaintRepository.save(saved); // update DB with autoComment
            }
        } catch (Exception e) {
            System.err.println("⚠️ Could not connect to AutoResponse service: " + e.getMessage());
            saved.setAutoComment("⚠️ Auto-response unavailable.");
            saved = complaintRepository.save(saved);
        }

        return saved;
    }

    public List<Complaint> getAllComplaints() {
        return complaintRepository.findAll();
    }

    public Optional<Complaint> getComplaintById(Long id) {
        return complaintRepository.findById(id);
    }

    public List<Complaint> getComplaintsByUsername(String username) {
        return complaintRepository.findByUsername(username);
    }

    public void deleteComplaint(Long id) {
        complaintRepository.deleteById(id);
    }
}