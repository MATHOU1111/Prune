import { Routes } from '@angular/router';
import { EmailListComponent } from './components/email-list/email-list';
import { EmailComposeComponent } from './components/email-compose/email-compose';
import { EmailDetailComponent } from './components/email-detail/email-detail';

export const routes: Routes = [
  { path: '', redirectTo: '/emails', pathMatch: 'full' },
  { path: 'emails', component: EmailListComponent },
  { path: 'compose', component: EmailComposeComponent },
  { path: 'email/:id', component: EmailDetailComponent },
  { path: '**', redirectTo: '/emails' }
];
