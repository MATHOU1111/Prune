import { Component, OnInit } from '@angular/core';
import { EmailService } from '../../services/email.service';
import { Email, EmailType } from '../../models/email.model';
import { CommonModule } from '@angular/common';
import { MatTabsModule } from '@angular/material/tabs';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { Router } from '@angular/router';

@Component({
  selector: 'app-email-list',
  imports: [
    CommonModule,
    MatTabsModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatSnackBarModule
  ],
  templateUrl: './email-list.html',
  styleUrl: './email-list.css'
})
export class EmailListComponent implements OnInit {
  sentEmails: Email[] = [];
  receivedEmails: Email[] = [];
  draftEmails: Email[] = [];
  displayedColumns: string[] = ['from', 'to', 'subject', 'date', 'status', 'actions'];

  constructor(
    private emailService: EmailService,
    private snackBar: MatSnackBar,
    private router: Router
  ) {}

  ngOnInit() {
    this.loadEmails();
  }

  loadEmails() {
    this.emailService.getSentEmails().subscribe({
      next: (emails) => this.sentEmails = emails,
      error: (error) => console.error('Erreur lors du chargement des emails envoyés:', error)
    });

    this.emailService.getReceivedEmails().subscribe({
      next: (emails) => this.receivedEmails = emails,
      error: (error) => console.error('Erreur lors du chargement des emails reçus:', error)
    });

    this.emailService.getDraftEmails().subscribe({
      next: (emails) => this.draftEmails = emails,
      error: (error) => console.error('Erreur lors du chargement des brouillons:', error)
    });
  }

  deleteEmail(id: number) {
    if (confirm('Êtes-vous sûr de vouloir supprimer cet email ?')) {
      this.emailService.deleteEmail(id).subscribe({
        next: () => {
          this.snackBar.open('Email supprimé', 'Fermer', { duration: 3000 });
          this.loadEmails();
        },
        error: (error) => {
          this.snackBar.open('Erreur lors de la suppression', 'Fermer', { duration: 3000 });
        }
      });
    }
  }

  viewEmail(id: number) {
    this.router.navigate(['/email', id]);
  }

  formatDate(date: Date | undefined): string {
    if (!date) return 'N/A';
    return new Date(date).toLocaleString('fr-FR');
  }
}