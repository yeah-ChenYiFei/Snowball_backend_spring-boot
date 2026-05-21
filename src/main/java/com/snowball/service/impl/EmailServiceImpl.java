package com.snowball.service.impl;

import com.snowball.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendVerificationCode(String toEmail, String code) {
        sendMail(toEmail, "SnowBall 邮箱验证码", String.format("""
                您的验证码是：%s

                有效期为 10 分钟，请勿泄露给他人哟~！

                —— SnowBall平台""", code));
    }

    @Override
    public void sendPasswordResetCode(String toEmail, String code) {
        sendMail(toEmail, "SnowBall 重置密码验证码", String.format("""
                您的密码重置验证码是：%s

                有效期为 10 分钟，请勿泄露给他人。
                如非您本人操作，请忽略此邮件。

                —— SnowBall平台""", code));
    }

    @Override
    public void sendChangeEmailCode(String toEmail, String code) {
        sendMail(toEmail, "SnowBall 换绑邮箱验证码", String.format("""
                您的换绑邮箱验证码是：%s

                有效期为 10 分钟，请勿泄露给他人。
                如非您本人操作，请忽略此邮件。

                —— SnowBall平台""", code));
    }

    private void sendMail(String toEmail, String subject, String text) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(text, false);
            mailSender.send(message);
            log.info("{} 已发送至 {}", subject, toEmail);
        } catch (Exception e) {
            log.error("邮件发送失败: {}", e.getMessage());
            throw new RuntimeException("邮件发送失败，请稍后重试", e);
        }
    }
}
