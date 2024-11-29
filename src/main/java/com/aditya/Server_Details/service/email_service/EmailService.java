package com.aditya.Server_Details.service.email_service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Value("$(spring.mail.username)")
    private String fromEmail;

/*    @Value("$(toEmail)")
    private String toEmail;*/

    @Autowired
    JavaMailSender mailSender;
    public void sendEmail(String recipient, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(recipient);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

}
