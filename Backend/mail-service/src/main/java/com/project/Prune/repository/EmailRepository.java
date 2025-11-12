package com.project.Prune.repository;

import com.project.Prune.model.Email;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EmailRepository extends JpaRepository<Email, Long> {
    
    List<Email> findByTypeOrderBySentDateDesc(Email.EmailType type);
    
    List<Email> findByToEmailOrderByReceivedDateDesc(String toEmail);
    
    List<Email> findByFromEmailOrderBySentDateDesc(String fromEmail);
    
    @Query("SELECT e FROM Email e WHERE e.toEmail = :email OR e.fromEmail = :email ORDER BY COALESCE(e.sentDate, e.receivedDate) DESC")
    List<Email> findAllEmailsByAddress(@Param("email") String email);
    
    List<Email> findByStatusAndType(Email.EmailStatus status, Email.EmailType type);
    
    @Query("SELECT e FROM Email e WHERE e.sentDate BETWEEN :startDate AND :endDate ORDER BY e.sentDate DESC")
    List<Email> findEmailsBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}