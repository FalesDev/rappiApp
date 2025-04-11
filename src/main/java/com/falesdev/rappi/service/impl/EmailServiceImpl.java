package com.falesdev.rappi.service.impl;

import com.falesdev.rappi.exception.EmailException;
import com.falesdev.rappi.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Async
    @Override
    public void sendOtpEmail(String to, String otp) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("rappiappdemo@gmail.com");
            helper.setTo(to);
            helper.setSubject("Tu código de acceso");

            Context context = new Context();
            context.setVariable("otp", otp);
            context.setVariable("imageResourceName", "logo.png");
            String html = templateEngine.process("otp-email", context);

            helper.setText(html, true);

            ClassPathResource logo = new ClassPathResource("static/logo.png");
            helper.addInline("logo", logo);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new EmailException("Error sending welcome email");
        }
    }
}
