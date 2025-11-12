package com.project.Prune.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "emails")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Email {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String fromEmail;
    
    @Column(nullable = false)
    private String toEmail;
    
    private String ccEmail;
    
    private String bccEmail;
    
    @Column(nullable = false)
    private String subject;
    
    @Column(columnDefinition = "TEXT")
    private String body;
    
    @Column(name = "sent_date")
    private LocalDateTime sentDate;
    
    @Column(name = "received_date")
    private LocalDateTime receivedDate;
    
    @Enumerated(EnumType.STRING)
    private EmailStatus status;
    
    @Enumerated(EnumType.STRING)
    private EmailType type; // SENT, RECEIVED, DRAFT
    
    public enum EmailStatus {
        DRAFT, SENT, DELIVERED, FAILED, READ, UNREAD
    }
    
    public enum EmailType {
        SENT, RECEIVED, DRAFT
    }
}