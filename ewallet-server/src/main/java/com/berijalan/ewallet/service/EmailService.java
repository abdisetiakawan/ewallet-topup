package com.berijalan.ewallet.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String from;

    public void sendEmailChangeToken(String toEmail, String token, int ttlMinutes) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(toEmail);
        message.setSubject("Email Change Request - WalletPay");
        message.setText(
                "You requested to change your WalletPay account email.\n\n" +
                "Use the following token to confirm the change:\n\n" +
                "  " + token + "\n\n" +
                "This token will expire in " + ttlMinutes + " minutes.\n\n" +
                "If you did not request this change, please ignore this email."
        );

        mailSender.send(message);
        log.info("Email change token sent to {}", toEmail);
    }
}
