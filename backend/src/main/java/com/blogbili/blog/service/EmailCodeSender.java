package com.blogbili.blog.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailCodeSender {

    private static final Logger log = LoggerFactory.getLogger(EmailCodeSender.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${blog.mail.from:}")
    private String fromAddress;

    @Value("${blog.auth.code-minutes:10}")
    private long codeMinutes;

    public EmailCodeSender(ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.mailSenderProvider = mailSenderProvider;
    }

    public boolean isConfigured() {
        return mailSenderProvider.getIfAvailable() != null && fromAddress != null && !fromAddress.isBlank();
    }

    public boolean send(String email, String code, String mode) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (!isConfigured() || mailSender == null) {
            log.info("Mail sender not configured. Verification code for {} is {}", email, code);
            return false;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(email);
            message.setSubject("BiliBlog 邮箱验证码");
            message.setText("你的 BiliBlog " + mode + " 验证码是：" + code + "，" + codeMinutes + " 分钟内有效。");
            mailSender.send(message);
            log.info("Verification code email sent to {}", email);
            return true;
        } catch (Exception ex) {
            log.warn("Failed to send mail to {}, fallback to debug mode. code={}", email, code, ex);
            return false;
        }
    }
}
