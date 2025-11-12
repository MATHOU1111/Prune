import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { EmailService } from '../../services/email.service';
import { EmailRequest } from '../../models/email.model';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { CommonModule } from '@angular/common';

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
          this.snackBar.open('Email envoyé avec succès!', 'Fermer', {
            duration: 3000
          });
          this.emailForm.reset();
        },
        error: (error) => {
          this.isLoading = false;
          this.snackBar.open('Erreur lors de l\'envoi de l\'email', 'Fermer', {
            duration: 3000
          });
        }
      });
    }
  }

  onSaveAsDraft() {
    if (this.emailForm.get('to')?.value || this.emailForm.get('subject')?.value) {
      this.isLoading = true;
      const emailRequest: EmailRequest = this.emailForm.value;
      
      this.emailService.saveAsDraft(emailRequest).subscribe({
        next: (response) => {
          this.isLoading = false;
          this.snackBar.open('Brouillon sauvegardé!', 'Fermer', {
            duration: 3000
          });
        },
        error: (error) => {
          this.isLoading = false;
          this.snackBar.open('Erreur lors de la sauvegarde', 'Fermer', {
            duration: 3000
          });
        }
      });
    }
  }

  onReset() {
    this.emailForm.reset();
  }
}
export class EmailCompose {

}
