package com.project.Prune.controller;

import com.project.Prune.dto.EmailRequest;
import com.project.Prune.model.Email;
import com.project.Prune.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/emails")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class EmailController {
    
    private final EmailService emailService;
    
    @PostMapping("/send")
    public ResponseEntity<Email> sendEmail(@RequestBody EmailRequest request) {
        try {
            Email sentEmail = emailService.sendEmail(request);
            return ResponseEntity.ok(sentEmail);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping("/draft")
    public ResponseEntity<Email> saveAsDraft(@RequestBody EmailRequest request) {
        try {
            Email draftEmail = emailService.saveAsDraft(request);
            return ResponseEntity.ok(draftEmail);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping
    public ResponseEntity<List<Email>> getAllEmails() {
        List<Email> emails = emailService.getAllEmails();
        return ResponseEntity.ok(emails);
    }
    
    @GetMapping("/sent")
    public ResponseEntity<List<Email>> getSentEmails() {
        List<Email> emails = emailService.getSentEmails();
        return ResponseEntity.ok(emails);
    }
    
    @GetMapping("/received")
    public ResponseEntity<List<Email>> getReceivedEmails() {
        List<Email> emails = emailService.getReceivedEmails();
        return ResponseEntity.ok(emails);
    }
    
    @GetMapping("/drafts")
    public ResponseEntity<List<Email>> getDraftEmails() {
        List<Email> emails = emailService.getDraftEmails();
        return ResponseEntity.ok(emails);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Email> getEmailById(@PathVariable Long id) {
        Email email = emailService.getEmailById(id);
        if (email != null) {
            return ResponseEntity.ok(email);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmail(@PathVariable Long id) {
        try {
            emailService.deleteEmail(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}