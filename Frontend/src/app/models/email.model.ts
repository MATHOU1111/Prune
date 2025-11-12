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