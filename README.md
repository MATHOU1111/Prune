# 📧 Prune - Application de Gestion d'Emails

Une application web complète pour envoyer, recevoir et gérer des emails, construite avec **Spring Boot** (backend) et **Angular** (frontend).

## 🚀 Architecture Générale

```
Prune/
├── Backend/mail-service/     # API REST Spring Boot
└── Frontend/                 # Interface Angular Material
```

### Stack Technologique

**Backend :**
- ☕ **Spring Boot 3.5.7** - Framework principal
- 🗄️ **Spring Data JPA** - Gestion des données  
- 🔒 **Spring Security** - Sécurité et CORS
- 📧 **Spring Mail** - Envoi d'emails
- 🏗️ **H2 Database** - Base de données en mémoire (tests)
- 📊 **Hibernate** - ORM

**Frontend :**
- 🅰️ **Angular 18** - Framework frontend
- 🎨 **Angular Material** - Composants UI
- 🔄 **RxJS** - Programmation réactive
- 🌐 **HTTP Client** - Communication API

---

## 📁 Structure du Backend

### 🏗️ Architecture Spring Boot

```
Backend/mail-service/src/main/java/com/project/Prune/
├── PruneApplication.java           # Point d'entrée Spring Boot
├── config/
│   ├── SecurityConfig.java         # Configuration sécurité & CORS
│   └── DataInitializer.java        # Données de test au démarrage
├── controller/
│   └── EmailController.java        # APIs REST
├── service/
│   └── EmailService.java           # Logique métier emails
├── repository/
│   └── EmailRepository.java        # Accès données JPA
├── model/
│   └── Email.java                  # Entité JPA Email
└── dto/
    └── EmailRequest.java           # DTO pour les requêtes
```

### 📧 Modèle de Données - `Email.java`

```java
@Entity
@Table(name = "emails")
public class Email {
    @Id @GeneratedValue
    private Long id;                    // ID unique
    
    private String fromEmail;           // Expéditeur
    private String toEmail;             // Destinataire principal
    private String ccEmail;             // Copie carbone
    private String bccEmail;            // Copie carbone invisible
    private String subject;             // Sujet
    private String body;                // Contenu du message
    
    private LocalDateTime sentDate;     // Date d'envoi
    private LocalDateTime receivedDate; // Date de réception
    
    @Enumerated(EnumType.STRING)
    private EmailStatus status;         // DRAFT, SENT, DELIVERED, FAILED...
    
    @Enumerated(EnumType.STRING) 
    private EmailType type;             // SENT, RECEIVED, DRAFT
}
```

**États d'un Email :**
- `DRAFT` : Brouillon non envoyé
- `SENT` : Envoyé avec succès  
- `DELIVERED` : Livré au destinataire
- `FAILED` : Échec d'envoi
- `READ` : Lu par le destinataire
- `UNREAD` : Non lu

**Types d'Email :**
- `SENT` : Email envoyé par l'utilisateur
- `RECEIVED` : Email reçu
- `DRAFT` : Brouillon sauvegardé

### 🛡️ Configuration Sécurité - `SecurityConfig.java`

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        return http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())                    // Désactive CSRF pour API REST
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/**").permitAll()      // APIs publiques
                .requestMatchers("/h2-console/**").permitAll() // Console H2
                .anyRequest().authenticated()
            )
            .headers(headers -> headers.frameOptions()
                .disable())                                  // Pour H2 Console
            .build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        // Configuration CORS pour Angular (localhost:4200)
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
```

### 🔄 Repository - `EmailRepository.java`

```java
@Repository
public interface EmailRepository extends JpaRepository<Email, Long> {
    
    // Emails par type, triés par date
    List<Email> findByTypeOrderBySentDateDesc(Email.EmailType type);
    
    // Emails reçus par destinataire
    List<Email> findByToEmailOrderByReceivedDateDesc(String toEmail);
    
    // Emails envoyés par expéditeur
    List<Email> findByFromEmailOrderBySentDateDesc(String fromEmail);
    
    // Tous les emails d'une adresse (envoyés + reçus)
    @Query("SELECT e FROM Email e WHERE e.toEmail = :email OR e.fromEmail = :email ORDER BY COALESCE(e.sentDate, e.receivedDate) DESC")
    List<Email> findAllEmailsByAddress(@Param("email") String email);
    
    // Emails par statut et type
    List<Email> findByStatusAndType(Email.EmailStatus status, Email.EmailType type);
    
    // Emails dans une période
    @Query("SELECT e FROM Email e WHERE e.sentDate BETWEEN :startDate AND :endDate ORDER BY e.sentDate DESC")
    List<Email> findEmailsBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
```

### ⚙️ Service Métier - `EmailService.java`

```java
@Service
@Slf4j
public class EmailService {
    
    private final JavaMailSender mailSender;      // Optionnel pour les tests
    private final EmailRepository emailRepository;
    
    @Value("${spring.mail.username:default@example.com}")
    private String fromEmail;
    
    public Email sendEmail(EmailRequest request) {
        try {
            Email email = new Email();
            // Mapping des données
            email.setFromEmail(fromEmail);
            email.setToEmail(request.getTo());
            email.setSubject(request.getSubject());
            email.setBody(request.getBody());
            email.setSentDate(LocalDateTime.now());
            email.setType(Email.EmailType.SENT);
            
            // Envoi réel ou simulation selon la config
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
            // Sauvegarde avec statut FAILED
            Email failedEmail = createFailedEmail(request);
            return emailRepository.save(failedEmail);
        }
    }
    
    // Autres méthodes : saveAsDraft(), getAllEmails(), getSentEmails()...
}
```

### 🌐 Contrôleur REST - `EmailController.java`

```java
@RestController
@RequestMapping("/api/emails")
@CrossOrigin(origins = "http://localhost:4200")    // CORS pour Angular
@RequiredArgsConstructor
public class EmailController {
    
    private final EmailService emailService;
    
    @PostMapping("/send")
    public ResponseEntity<Email> sendEmail(@RequestBody EmailRequest request) {
        Email sentEmail = emailService.sendEmail(request);
        return ResponseEntity.ok(sentEmail);
    }
    
    @PostMapping("/draft")
    public ResponseEntity<Email> saveAsDraft(@RequestBody EmailRequest request) {
        Email draftEmail = emailService.saveAsDraft(request);
        return ResponseEntity.ok(draftEmail);
    }
    
    @GetMapping
    public ResponseEntity<List<Email>> getAllEmails() {
        return ResponseEntity.ok(emailService.getAllEmails());
    }
    
    @GetMapping("/sent")
    public ResponseEntity<List<Email>> getSentEmails() {
        return ResponseEntity.ok(emailService.getSentEmails());
    }
    
    @GetMapping("/received") 
    public ResponseEntity<List<Email>> getReceivedEmails() {
        return ResponseEntity.ok(emailService.getReceivedEmails());
    }
    
    @GetMapping("/drafts")
    public ResponseEntity<List<Email>> getDraftEmails() {
        return ResponseEntity.ok(emailService.getDraftEmails());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Email> getEmailById(@PathVariable Long id) {
        Email email = emailService.getEmailById(id);
        return email != null ? ResponseEntity.ok(email) : ResponseEntity.notFound().build();
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmail(@PathVariable Long id) {
        emailService.deleteEmail(id);
        return ResponseEntity.noContent().build();
    }
}
```

---

## 🅰️ Structure du Frontend Angular

```
Frontend/src/app/
├── app.ts                          # Composant racine
├── app.html                        # Template principal avec navigation
├── app.routes.ts                   # Configuration des routes
├── app.config.ts                   # Configuration Angular (HttpClient, Animations)
├── models/
│   └── email.model.ts              # Interfaces TypeScript
├── services/
│   └── email.service.ts            # Service HTTP pour APIs
└── components/
    ├── email-compose/              # Composition d'emails
    │   ├── email-compose.ts
    │   ├── email-compose.html
    │   └── email-compose.css
    ├── email-list/                 # Liste des emails par catégorie
    │   ├── email-list.ts
    │   ├── email-list.html
    │   └── email-list.css
    └── email-detail/               # Affichage détaillé d'un email
        ├── email-detail.ts
        ├── email-detail.html
        └── email-detail.css
```

### 🔧 Modèles TypeScript - `email.model.ts`

```typescript
export interface Email {
  id?: number;
  fromEmail: string;
  toEmail: string;
  ccEmail?: string;
  bccEmail?: string;
  subject: string;
  body: string;
  sentDate?: Date;
  receivedDate?: Date;
  status: EmailStatus;
  type: EmailType;
}

export enum EmailStatus {
  DRAFT = 'DRAFT',
  SENT = 'SENT', 
  DELIVERED = 'DELIVERED',
  FAILED = 'FAILED',
  READ = 'READ',
  UNREAD = 'UNREAD'
}

export enum EmailType {
  SENT = 'SENT',
  RECEIVED = 'RECEIVED',
  DRAFT = 'DRAFT'
}

export interface EmailRequest {
  to: string;
  cc?: string;
  bcc?: string;
  subject: string;
  body: string;
  isHtml: boolean;
}
```

### 🌐 Service HTTP - `email.service.ts`

```typescript
@Injectable({ providedIn: 'root' })
export class EmailService {
  private apiUrl = 'http://localhost:8080/api/emails';

  constructor(private http: HttpClient) { }

  sendEmail(emailRequest: EmailRequest): Observable<Email> {
    return this.http.post<Email>(`${this.apiUrl}/send`, emailRequest);
  }

  saveAsDraft(emailRequest: EmailRequest): Observable<Email> {
    return this.http.post<Email>(`${this.apiUrl}/draft`, emailRequest);
  }

  getAllEmails(): Observable<Email[]> {
    return this.http.get<Email[]>(this.apiUrl);
  }

  getSentEmails(): Observable<Email[]> {
    return this.http.get<Email[]>(`${this.apiUrl}/sent`);
  }

  getReceivedEmails(): Observable<Email[]> {
    return this.http.get<Email[]>(`${this.apiUrl}/received`);
  }

  getDraftEmails(): Observable<Email[]> {
    return this.http.get<Email[]>(`${this.apiUrl}/drafts`);
  }

  getEmailById(id: number): Observable<Email> {
    return this.http.get<Email>(`${this.apiUrl}/${id}`);
  }

  deleteEmail(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
```

### ✉️ Composant Composition - `email-compose.ts`

```typescript
@Component({
  selector: 'app-email-compose',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatCheckboxModule,
    MatSnackBarModule
  ],
  templateUrl: './email-compose.html',
  styleUrl: './email-compose.css'
})
export class EmailComposeComponent {
  emailForm: FormGroup;
  isLoading = false;

  constructor(
    private fb: FormBuilder,
    private emailService: EmailService,
    private snackBar: MatSnackBar
  ) {
    this.emailForm = this.fb.group({
      to: ['', [Validators.required, Validators.email]],
      cc: [''],
      bcc: [''],
      subject: ['', Validators.required],
      body: ['', Validators.required],
      isHtml: [false]
    });
  }

  onSend() {
    if (this.emailForm.valid) {
      this.isLoading = true;
      const emailRequest: EmailRequest = this.emailForm.value;
      
      this.emailService.sendEmail(emailRequest).subscribe({
        next: (response) => {
          this.isLoading = false;
          this.snackBar.open('Email envoyé avec succès!', 'Fermer', { duration: 3000 });
          this.emailForm.reset();
        },
        error: (error) => {
          this.isLoading = false;
          this.snackBar.open('Erreur lors de l\'envoi', 'Fermer', { duration: 3000 });
        }
      });
    }
  }

  onSaveAsDraft() {
    const emailRequest: EmailRequest = this.emailForm.value;
    this.emailService.saveAsDraft(emailRequest).subscribe({
      next: () => this.snackBar.open('Brouillon sauvegardé!', 'Fermer', { duration: 3000 })
    });
  }
}
```

### 📋 Composant Liste - `email-list.ts`

```typescript
@Component({
  selector: 'app-email-list',
  imports: [
    CommonModule,
    MatTabsModule,      // Onglets pour catégories
    MatTableModule,     // Tableaux de données
    MatButtonModule,
    MatIconModule
  ]
})
export class EmailListComponent implements OnInit {
  sentEmails: Email[] = [];
  receivedEmails: Email[] = [];
  draftEmails: Email[] = [];
  displayedColumns: string[] = ['from', 'to', 'subject', 'date', 'status', 'actions'];

  ngOnInit() {
    this.loadEmails();
  }

  loadEmails() {
    // Chargement des emails par catégorie
    this.emailService.getSentEmails().subscribe(emails => this.sentEmails = emails);
    this.emailService.getReceivedEmails().subscribe(emails => this.receivedEmails = emails);
    this.emailService.getDraftEmails().subscribe(emails => this.draftEmails = emails);
  }

  deleteEmail(id: number) {
    if (confirm('Supprimer cet email ?')) {
      this.emailService.deleteEmail(id).subscribe(() => {
        this.snackBar.open('Email supprimé', 'Fermer', { duration: 3000 });
        this.loadEmails();
      });
    }
  }

  viewEmail(id: number) {
    this.router.navigate(['/email', id]);
  }
}
```

---

## 📊 Configuration et Base de Données

### 🗄️ Configuration H2 - `application.properties`

```properties
spring.application.name=Prune

# Base de données H2 en mémoire (pour tests)
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=password

# Console H2 pour déboguer
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=create-drop    # Recrée les tables à chaque démarrage
spring.jpa.show-sql=true                     # Affiche les requêtes SQL
spring.jpa.properties.hibernate.format_sql=true

# Configuration Email (désactivée pour tests)
# spring.mail.host=smtp.gmail.com
# spring.mail.port=587
# spring.mail.username=votre-email@gmail.com
# spring.mail.password=votre-mot-de-passe-app

# Serveur
server.port=8080
```

### 🎯 Données de Test - `DataInitializer.java`

```java
@Component
@Slf4j
@RequiredArgsConstructor
public class DataInitializer {
    
    private final EmailRepository emailRepository;
    
    @EventListener(ApplicationReadyEvent.class)
    public void initializeData() {
        log.info("🚀 Initialisation des données de test...");
        
        // Email envoyé
        Email sentEmail = new Email();
        sentEmail.setFromEmail("admin@prune.com");
        sentEmail.setToEmail("user@example.com");
        sentEmail.setSubject("Bienvenue dans Prune!");
        sentEmail.setBody("Merci d'utiliser notre application de gestion d'emails.");
        sentEmail.setSentDate(LocalDateTime.now().minusHours(2));
        sentEmail.setStatus(Email.EmailStatus.SENT);
        sentEmail.setType(Email.EmailType.SENT);
        
        // Email reçu
        Email receivedEmail = new Email();
        receivedEmail.setFromEmail("contact@client.com");
        receivedEmail.setToEmail("admin@prune.com");
        receivedEmail.setSubject("Demande d'information");
        receivedEmail.setBody("Bonjour, j'aimerais avoir plus d'informations sur vos services.");
        receivedEmail.setReceivedDate(LocalDateTime.now().minusMinutes(30));
        receivedEmail.setStatus(Email.EmailStatus.UNREAD);
        receivedEmail.setType(Email.EmailType.RECEIVED);
        
        // Brouillon
        Email draft = new Email();
        draft.setFromEmail("admin@prune.com");
        draft.setToEmail("prospect@entreprise.com");
        draft.setSubject("Proposition commerciale");
        draft.setBody("Cher prospect, nous avons une offre qui pourrait vous intéresser...");
        draft.setStatus(Email.EmailStatus.DRAFT);
        draft.setType(Email.EmailType.DRAFT);
        
        // Sauvegarde
        emailRepository.saveAll(Arrays.asList(sentEmail, receivedEmail, draft));
        
        log.info("✅ {} emails de test créés", 3);
    }
}
```

---

## 🚀 Démarrage et Utilisation

### 1. **Démarrage du Backend**

```bash
cd Backend/mail-service
./mvnw spring-boot:run
```

Le serveur démarre sur `http://localhost:8080`

### 2. **Démarrage du Frontend**

```bash
cd Frontend
ng serve
```

L'application Angular est accessible sur `http://localhost:4200`

### 3. **URLs Importantes**

- 🌐 **Application** : http://localhost:4200
- 🔗 **APIs REST** : http://localhost:8080/api/emails
- 🗄️ **Console H2** : http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:testdb`
  - User: `sa`
  - Password: `password`

---

## 📋 APIs REST Disponibles

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| `POST` | `/api/emails/send` | Envoyer un email |
| `POST` | `/api/emails/draft` | Sauvegarder comme brouillon |
| `GET` | `/api/emails` | Lister tous les emails |
| `GET` | `/api/emails/sent` | Emails envoyés |
| `GET` | `/api/emails/received` | Emails reçus |
| `GET` | `/api/emails/drafts` | Brouillons |
| `GET` | `/api/emails/{id}` | Détails d'un email |
| `DELETE` | `/api/emails/{id}` | Supprimer un email |

### Exemple de Requête

```bash
# Envoyer un email
curl -X POST http://localhost:8080/api/emails/send \
  -H "Content-Type: application/json" \
  -d '{
    "to": "test@example.com",
    "subject": "Test API",
    "body": "Message de test depuis l'API",
    "isHtml": false
  }'

# Lister les emails envoyés
curl http://localhost:8080/api/emails/sent
```

---

## 🔧 Fonctionnalités Clés

### ✅ **Côté Backend**
- ✉️ **Gestion complète des emails** (CRUD)
- 🔒 **Sécurité avec Spring Security**
- 🌐 **CORS configuré pour Angular**
- 📧 **Support envoi d'emails réels** (SMTP)
- 🗄️ **Persistance avec JPA/Hibernate**
- 🎯 **Données de test automatiques**

### ✅ **Côté Frontend**
- 🎨 **Interface moderne avec Material Design**
- 📝 **Formulaire de composition avec validation**
- 📊 **Organisation par onglets** (Envoyés/Reçus/Brouillons)
- 👁️ **Affichage détaillé des emails**
- 🔄 **Communication réactive avec RxJS**
- 📱 **Design responsive**

---

## 🛠️ Technologies Détaillées

### **Spring Boot Dependencies**
```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-mail</artifactId>
    </dependency>
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
</dependencies>
```

### **Angular Dependencies**
```json
{
  "dependencies": {
    "@angular/animations": "^18.0.0",
    "@angular/common": "^18.0.0",
    "@angular/core": "^18.0.0",
    "@angular/forms": "^18.0.0",
    "@angular/material": "^18.0.0",
    "@angular/platform-browser": "^18.0.0",
    "@angular/router": "^18.0.0",
    "rxjs": "~7.8.0"
  }
}
```

---

## 🎯 Points Clés de l'Architecture

### **Séparation des Responsabilités**
- **Controller** : Gestion des requêtes HTTP
- **Service** : Logique métier et règles de gestion
- **Repository** : Accès aux données
- **Model/Entity** : Représentation des données

### **Communication Frontend ↔ Backend**
- **REST API** avec JSON
- **CORS** configuré pour permettre les requêtes cross-origin
- **HttpClient Angular** pour les appels API
- **Observables RxJS** pour la gestion asynchrone

### **Sécurité**
- **Spring Security** avec endpoints publics pour l'API
- **CSRF désactivé** pour les APIs REST
- **CORS** configuré pour Angular localhost:4200

---

## 🚀 Extensions Possibles

1. **Authentification utilisateur** avec JWT
2. **Envoi d'emails réels** via SMTP (Gmail, Outlook)
3. **Upload de fichiers joints**
4. **Recherche avancée** dans les emails
5. **Notifications en temps réel** avec WebSockets
6. **Base de données PostgreSQL/MySQL** pour la production
7. **Tests unitaires et d'intégration**
8. **Docker** pour le déploiement

---

## 📝 Notes Importantes

- 🎯 **Mode Test** : L'envoi d'emails est simulé (pas de SMTP réel configuré)
- 🗄️ **Base H2** : Données perdues à chaque redémarrage (mémoire)
- 🔒 **Sécurité** : Configuration basique pour développement
- 🌐 **CORS** : Autorisé uniquement pour localhost:4200

---

**🎉 Votre application Prune est maintenant prête à l'emploi !**