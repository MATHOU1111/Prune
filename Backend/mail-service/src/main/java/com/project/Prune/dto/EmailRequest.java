package com.project.Prune.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequest {
    private String to;
    private String cc;
    private String bcc;
    private String subject;
    private String body;
    private boolean isHtml;
}