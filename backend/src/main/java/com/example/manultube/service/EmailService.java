package com.example.manultube.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendEmail(String to, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("Your verification code");

            String html = """
    <div style="font-family: Arial; padding:20px;">
        <h2 style="color:#333;">Verify your account</h2>
        <p>Use the code below:</p>
        <div style="
            font-size: 28px;
            font-weight: bold;
            letter-spacing: 5px;
            background: #f4f4f4;
            padding: 10px;
            display: inline-block;">
            %s
        </div>
        <p style="margin-top:20px;">
            Expires in 15 minutes.
        </p>
    </div>
""".formatted(code);

            helper.setText(html, true);

            mailSender.send(message);

        } catch (MessagingException e) {
            logger.error("Error occurred",e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
}
