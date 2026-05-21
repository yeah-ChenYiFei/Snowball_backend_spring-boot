package com.snowball.service;

public interface EmailService {
    void sendVerificationCode(String toEmail, String code);
    void sendPasswordResetCode(String toEmail, String code);
    void sendChangeEmailCode(String toEmail, String code);
}
