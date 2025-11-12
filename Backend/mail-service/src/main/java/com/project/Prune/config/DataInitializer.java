package com.project.Prune.config;

import com.project.Prune.model.Email;
import com.project.Prune.repository.EmailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private EmailRepository emailRepository;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== INITIALISATION DES DONNÉES DE TEST ===");
        
        if (emailRepository.count() == 0) {
            System.out.println("Création des données de test...");
            
            // Email envoyé 1
            Email email1 = new Email();
            email1.setFromEmail("admin@prune.com");
            email1.setToEmail("john.doe@example.com");
            email1.setSubject("Bienvenue dans Prune");
            email1.setBody("Bonjour John,\n\nNous sommes ravis de vous accueillir sur notre plateforme Prune !\n\nCordialement,\nL'équipe Prune");
            email1.setSentDate(LocalDateTime.now().minusDays(2));
            email1.setStatus(Email.EmailStatus.SENT);
            email1.setType(Email.EmailType.SENT);
            emailRepository.save(email1);
            
            // Email envoyé 2
            Email email2 = new Email();
            email2.setFromEmail("admin@prune.com");
            email2.setToEmail("sarah.martin@company.fr");
            email2.setCcEmail("manager@company.fr");
            email2.setSubject("Rapport mensuel");
            email2.setBody("Chère Sarah,\n\nVeuillez trouver le rapport mensuel des activités.\n\nBien cordialement,\nAdmin");
            email2.setSentDate(LocalDateTime.now().minusDays(1));
            email2.setStatus(Email.EmailStatus.SENT);
            email2.setType(Email.EmailType.SENT);
            emailRepository.save(email2);
            
            // Email reçu 1
            Email email3 = new Email();
            email3.setFromEmail("client@business.com");
            email3.setToEmail("admin@prune.com");
            email3.setSubject("Demande de support");
            email3.setBody("Bonjour,\n\nJ'ai une question concernant votre application.\n\nMerci,\nClient Business");
            email3.setReceivedDate(LocalDateTime.now().minusHours(3));
            email3.setStatus(Email.EmailStatus.UNREAD);
            email3.setType(Email.EmailType.RECEIVED);
            emailRepository.save(email3);
            
            // Email reçu 2
            Email email4 = new Email();
            email4.setFromEmail("newsletter@techworld.com");
            email4.setToEmail("admin@prune.com");
            email4.setSubject("Newsletter Tech World");
            email4.setBody("Découvrez les dernières tendances :\n\n- Intelligence Artificielle\n- Cloud Computing\n- Développement Web");
            email4.setReceivedDate(LocalDateTime.now().minusHours(12));
            email4.setStatus(Email.EmailStatus.READ);
            email4.setType(Email.EmailType.RECEIVED);
            emailRepository.save(email4);
            
            // Brouillon 1
            Email email5 = new Email();
            email5.setFromEmail("admin@prune.com");
            email5.setToEmail("partner@startup.io");
            email5.setSubject("Proposition de partenariat");
            email5.setBody("Cher partenaire,\n\nNous souhaiterions explorer les possibilités de collaboration.\n\n[BROUILLON - À TERMINER]");
            email5.setStatus(Email.EmailStatus.DRAFT);
            email5.setType(Email.EmailType.DRAFT);
            emailRepository.save(email5);
            
            // Brouillon 2
            Email email6 = new Email();
            email6.setFromEmail("admin@prune.com");
            email6.setToEmail("marketing@agency.com");
            email6.setCcEmail("ceo@agency.com");
            email6.setSubject("Campagne publicitaire Q1");
            email6.setBody("Bonjour,\n\nNous aimerions discuter d'une campagne publicitaire.\n\n[À COMPLÉTER]");
            email6.setStatus(Email.EmailStatus.DRAFT);
            email6.setType(Email.EmailType.DRAFT);
            emailRepository.save(email6);
            
            System.out.println("✅ 6 emails de test créés avec succès !");
        } else {
            System.out.println("⚠️ Des données existent déjà dans la base de données");
        }
        
        System.out.println("Total emails dans la base: " + emailRepository.count());
        System.out.println("=== FIN INITIALISATION ===");
    }
}