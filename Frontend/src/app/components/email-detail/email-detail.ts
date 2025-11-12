import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { EmailService } from '../../services/email.service';
import { Email } from '../../models/email.model';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';

@Component({
  selector: 'app-email-detail',
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatDividerModule
  ],
  templateUrl: './email-detail.html',
  styleUrl: './email-detail.css'
})
export class EmailDetailComponent implements OnInit {
  email: Email | null = null;
  loading = true;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private emailService: EmailService
  ) {}

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadEmail(+id);
    }
  }

  loadEmail(id: number) {
    this.emailService.getEmailById(id).subscribe({
      next: (email) => {
        this.email = email;
        this.loading = false;
      },
      error: (error) => {
        console.error('Erreur lors du chargement de l\'email:', error);
        this.loading = false;
        this.router.navigate(['/emails']);
      }
    });
  }

  goBack() {
    this.router.navigate(['/emails']);
  }

  formatDate(date: Date | undefined): string {
    if (!date) return 'N/A';
    return new Date(date).toLocaleString('fr-FR');
  }

  deleteEmail() {
    if (this.email && confirm('Êtes-vous sûr de vouloir supprimer cet email ?')) {
      this.emailService.deleteEmail(this.email.id!).subscribe({
        next: () => {
          this.router.navigate(['/emails']);
        },
        error: (error) => {
          console.error('Erreur lors de la suppression:', error);
        }
      });
    }
  }
}