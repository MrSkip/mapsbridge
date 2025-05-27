package com.example.mapsbridge.service;

import java.util.Map;

/**
 * Service interface for sending emails using the official Mailtrap Java client.
 */
public interface MailtrapService {

    /**
     * Sends a simple text email using Mailtrap API.
     *
     * @param to      recipient email address
     * @param subject email subject
     * @param text    email body text
     * @return true if the email was sent successfully, false otherwise
     */
    boolean sendSimpleEmail(String to, String subject, String text);

    /**
     * Sends an HTML email using Mailtrap API.
     *
     * @param to          recipient email address
     * @param subject     email subject
     * @param htmlContent email body in HTML format
     * @return true if the email was sent successfully, false otherwise
     */
    boolean sendHtmlEmail(String to, String subject, String htmlContent);

    /**
     * Sends a template-based email using Mailtrap API.
     *
     * @param to       recipient email address
     * @param subject  email subject
     * @param template template name
     * @param model    model with variables for the template
     * @return true if the email was sent successfully, false otherwise
     */
    boolean sendTemplateEmail(String to, String subject, String template, Map<String, Object> model);
}