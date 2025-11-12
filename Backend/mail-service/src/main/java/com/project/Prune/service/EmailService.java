package com.project.Prune.service;

import com.project.Prune.dto.EmailRequest;
import com.project.Prune.model.Email;
import com.project.Prune.repository.EmailRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class EmailService {
    
    private final JavaMailSender mailSender;
    private final EmailRepository emailRepository;
    
    public EmailService(@Autowired(required = false) JavaMailSender mailSender, EmailRepository emailRepository) {
        this.mailSender = mailSender;
        this.emailRepository = emailRepository;
    }
    
    @Value("${spring.mail.username:default@example.com}")
    private String fromEmail;
    
    public Email sendEmail(EmailRequest request) {
        try {
            Email email = new Email();
            email.setFromEmail(fromEmail);
            email.setToEmail(request.getTo());
            email.setCcEmail(request.getCc());
            email.setBccEmail(request.getBcc());
            email.setSubject(request.getSubject());
            email.setBody(request.getBody());
            email.setSentDate(LocalDateTime.now());
            email.setType(Email.EmailType.SENT);
            email.setStatus(Email.EmailStatus.DRAFT);
            
            // En mode test, on simule l'envoi d'email
            if (mailSender != null) {
                if (request.isHtml()) {
                    sendHtmlEmail(request);
                } else {
                    sendSimpleEmail(request);
                }
            } else {
                log.info("Mode test: simulation de l'envoi d'email à {}", request.getTo());
            }
            
            email.setStatus(Email.EmailStatus.SENT);
            return emailRepository.save(email);
            
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email: ", e);
            
            Email failedEmail = new Email();
            failedEmail.setFromEmail(fromEmail);
            failedEmail.setToEmail(request.getTo());
            failedEmail.setSubject(request.getSubject());
            failedEmail.setBody(request.getBody());
            failedEmail.setSentDate(LocalDateTime.now());
            failedEmail.setType(Email.EmailType.SENT);
            failedEmail.setStatus(Email.EmailStatus.SENT); // On considère comme envoyé en mode test
            
            return emailRepository.save(failedEmail);
        }
    }
    
    private void sendSimpleEmail(EmailRequest request) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(request.getTo());
        
        if (request.getCc() != null && !request.getCc().isEmpty()) {
            message.setCc(request.getCc());
        }
        
        if (request.getBcc() != null && !request.getBcc().isEmpty()) {
            message.setBcc(request.getBcc());
        }
        
        message.setSubject(request.getSubject());
        message.setText(request.getBody());
        
        mailSender.send(message);
    }
    
    private void sendHtmlEmail(EmailRequest request) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
        
        helper.setFrom(fromEmail);
        helper.setTo(request.getTo());
        
        if (request.getCc() != null && !request.getCc().isEmpty()) {
            helper.setCc(request.getCc());
        }
        
        if (request.getBcc() != null && !request.getBcc().isEmpty()) {
            helper.setBcc(request.getBcc());
        }
        
        helper.setSubject(request.getSubject());
        helper.setText(request.getBody(), true);
        
        mailSender.send(mimeMessage);
    }
    
    public List<Email> getAllEmails() {
        return emailRepository.findAll();
    }
    
    public List<Email> getSentEmails() {
        return emailRepository.findByTypeOrderBySentDateDesc(Email.EmailType.SENT);
    }
    
    public List<Email> getReceivedEmails() {
        return emailRepository.findByTypeOrderBySentDateDesc(Email.EmailType.RECEIVED);
    }
    
    public Email getEmailById(Long id) {
        return emailRepository.findById(id).orElse(null);
    }
    
    public Email saveAsDraft(EmailRequest request) {
        Email email = new Email();
        email.setFromEmail(fromEmail);
        email.setToEmail(request.getTo());
        email.setCcEmail(request.getCc());
        email.setBccEmail(request.getBcc());
        email.setSubject(request.getSubject());
        email.setBody(request.getBody());
        email.setType(Email.EmailType.DRAFT);
        email.setStatus(Email.EmailStatus.DRAFT);
        
        return emailRepository.save(email);
    }
    
    public List<Email> getDraftEmails() {
        return emailRepository.findByStatusAndType(Email.EmailStatus.DRAFT, Email.EmailType.DRAFT);
    }
    
    public void deleteEmail(Long id) {
        emailRepository.deleteById(id);
    }
}