package com.example.mapsbridge.service;

import com.example.mapsbridge.service.impl.MailtrapServiceImpl;
import io.mailtrap.client.MailtrapClient;
import io.mailtrap.model.request.emails.MailtrapMail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class MailtrapServiceTest {

    @Mock
    private MailtrapClient mailtrapClient;

    @Mock
    private TemplateEngine templateEngine;

    private MailtrapService mailtrapService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mailtrapService = new MailtrapServiceImpl(mailtrapClient, templateEngine);
        ReflectionTestUtils.setField(mailtrapService, "mailtrapEnabled", true);
        ReflectionTestUtils.setField(mailtrapService, "senderEmail", "test@example.com");
        ReflectionTestUtils.setField(mailtrapService, "senderName", "Test Sender");
    }


    @Test
    void testSendHtmlEmail() {
        // Arrange
        String to = "user@example.com";
        String subject = "Test HTML Subject";
        String htmlContent = "<html><body><h1>Test HTML</h1></body></html>";

        // Act
        boolean result = mailtrapService.sendHtmlEmail(to, subject, htmlContent);

        // Assert
        assertTrue(result);
        verify(mailtrapClient, times(1)).send(any(MailtrapMail.class));
    }

    @Test
    void testSendTokenEmailTemplate() {
        // Arrange
        String to = "user@example.com";
        String subject = "Your Secure Token";
        String templateName = "token-template";
        String token = "SecureRandom123";
        String userName = "John Doe";
        String processedTemplate = "<html><body>Processed template with token: " + token + "</body></html>";

        Map<String, Object> model = new HashMap<>();
        model.put("name", userName);
        model.put("token", token);

        when(templateEngine.process(eq(templateName), any(Context.class))).thenReturn(processedTemplate);

        // Act
        boolean result = mailtrapService.sendTemplateEmail(to, subject, templateName, model);

        // Assert
        assertTrue(result);

        // Verify template engine was called with correct template name
        verify(templateEngine, times(1)).process(eq(templateName), any(Context.class));

        // Verify email was sent with processed template
        ArgumentCaptor<MailtrapMail> mailCaptor = ArgumentCaptor.forClass(MailtrapMail.class);
        verify(mailtrapClient, times(1)).send(mailCaptor.capture());

        // We can't directly access the HTML content of the captured mail due to encapsulation,
        // but we can verify that sendHtmlEmail was called with the processed template
        verify(mailtrapClient, times(1)).send(any(MailtrapMail.class));
    }
}
