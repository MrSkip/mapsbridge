package com.example.mapsbridge.service.impl;

import com.example.mapsbridge.service.MailtrapService;
import io.mailtrap.client.MailtrapClient;
import io.mailtrap.model.request.emails.Address;
import io.mailtrap.model.request.emails.MailtrapMail;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.List;
import java.util.Map;

/**
 * Implementation of the MailtrapService interface using the official Mailtrap Java client.
 */
@Slf4j
@Service
public class MailtrapServiceImpl implements MailtrapService {

    private final MailtrapClient mailtrapClient;
    private final TemplateEngine templateEngine;

    @Value("${mailtrap.enabled:false}")
    private boolean mailtrapEnabled;

    @Value("${mailtrap.sender.email:no-reply@example.com}")
    private String senderEmail;

    @Value("${mailtrap.sender.name:Maps Bridge}")
    private String senderName;

    @Autowired
    public MailtrapServiceImpl(MailtrapClient mailtrapClient, TemplateEngine templateEngine) {
        this.mailtrapClient = mailtrapClient;
        this.templateEngine = templateEngine;
    }

    @Override
    public boolean sendSimpleEmail(String to, String subject, String text) {
        if (!mailtrapEnabled || mailtrapClient == null) {
            log.info("Mailtrap client is disabled. Would have sent email to: {}", to);
            return false;
        }

        try {

            final MailtrapMail mail = MailtrapMail.builder()
                    .from(new Address(senderEmail))
                    .to(List.of(new Address(to)))
                    .subject(subject)
                    .text(text)
                    .build();

            log.info("Sending simple email to: {}", to);
            mailtrapClient.send(mail);
            log.info("Email sent successfully to: {}", to);
            return true;
        } catch (Exception e) {
            log.error("Failed to send simple email to: {}", to, e);
            return false;
        }
    }

    @Override
    public boolean sendHtmlEmail(String to, String subject, String htmlContent) {
        if (!mailtrapEnabled || mailtrapClient == null) {
            log.info("Mailtrap client is disabled. Would have sent HTML email to: {}", to);
            return false;
        }

        try {
            final MailtrapMail mail = MailtrapMail.builder()
                    .from(new Address(senderEmail, senderName))
                    .to(List.of(new Address(to)))
                    .subject(subject)
                    .html(htmlContent)
                    .build();

            log.info("Sending HTML email to: {}", to);
            mailtrapClient.send(mail);
            log.info("Email sent successfully to: {}", to);
            return true;
        } catch (Exception e) {
            log.error("Failed to send HTML email to: {}", to, e);
            return false;
        }
    }

    @Override
    public boolean sendTemplateEmail(String to, String subject, String template, Map<String, Object> model) {
        if (!mailtrapEnabled || mailtrapClient == null) {
            log.info("Mailtrap client is disabled. Would have sent template email to: {}", to);
            return false;
        }

        try {
            Context context = new Context();
            if (model != null) {
                model.forEach(context::setVariable);
            }

            String htmlContent = templateEngine.process(template, context);
            return sendHtmlEmail(to, subject, htmlContent);
        } catch (Exception e) {
            log.error("Failed to send template email to: {}", to, e);
            return false;
        }
    }
}
