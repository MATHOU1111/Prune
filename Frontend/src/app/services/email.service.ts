import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Email, EmailRequest } from '../models/email.model';

@Injectable({
  providedIn: 'root'
})
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