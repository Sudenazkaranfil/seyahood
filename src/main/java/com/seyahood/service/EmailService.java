package com.seyahood.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    public void sendVerificationCode(String to, String code) {
        sendEmail(to, "Seyahood - E-posta Doğrulama", buildVerificationHtml(code));
    }

    public void sendResetCode(String to, String code) {
        sendEmail(to, "Seyahood - Şifre Sıfırlama", buildResetHtml(code));
    }

    private void sendEmail(String to, String subject, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(fromAddress, "Seyahood");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            throw new RuntimeException("EMAIL_SEND_FAILED");
        }
    }

    private String buildVerificationHtml(String code) {
        String[] digits = code.split("");
        StringBuilder digitBoxes = new StringBuilder();
        for (String digit : digits) {
            digitBoxes.append(
                    "<td style='padding: 8px;'>" +
                            "<div style='width: 48px; height: 56px; background: #F0E8DC; border-radius: 12px; " +
                            "font-size: 28px; font-weight: 600; color: #2C2420; text-align: center; " +
                            "line-height: 56px;'>" + digit + "</div>" +
                            "</td>"
            );
        }
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'></head>" +
                "<body style='margin:0; padding:0; background:#FAFAF8; font-family: Arial, sans-serif;'>" +
                "<table width='100%' cellpadding='0' cellspacing='0'>" +
                "<tr><td align='center' style='padding: 40px 20px;'>" +
                "<table width='480' cellpadding='0' cellspacing='0' style='background: white; border-radius: 24px; overflow: hidden;'>" +
                "<tr><td style='background: #C4956A; padding: 32px; text-align: center;'>" +
                "<div style='font-size: 28px; font-weight: 600; color: white;'>Seyahood</div>" +
                "<div style='font-size: 14px; color: rgba(255,255,255,0.8); margin-top: 4px;'>senin yolculuğun, senin hikayen</div>" +
                "</td></tr>" +
                "<tr><td style='padding: 40px 32px; text-align: center;'>" +
                "<div style='font-size: 22px; font-weight: 600; color: #2C2420; margin-bottom: 12px;'>E-postanı Doğrula</div>" +
                "<div style='font-size: 15px; color: #9A8478; margin-bottom: 32px;'>Seyahood'a hoş geldin! Hesabını doğrulamak için aşağıdaki kodu kullan.</div>" +
                "<table cellpadding='0' cellspacing='0' style='margin: 0 auto;'><tr>" +
                digitBoxes.toString() +
                "</tr></table>" +
                "<div style='font-size: 13px; color: #9A8478; margin-top: 24px;'>Bu kod <strong>10 dakika</strong> geçerlidir.</div>" +
                "</td></tr>" +
                "<tr><td style='background: #FAFAF8; padding: 24px 32px; text-align: center; border-top: 1px solid #EDE8E3;'>" +
                "<div style='font-size: 13px; color: #9A8478;'>Bu maili sen göndermemişsen görmezden gelebilirsin.</div>" +
                "<div style='font-size: 13px; color: #C4956A; margin-top: 8px; font-weight: 500;'>Seyahood Ekibi</div>" +
                "</td></tr>" +
                "</table></td></tr></table></body></html>";
    }

    private String buildResetHtml(String code) {
        String[] digits = code.split("");
        StringBuilder digitBoxes = new StringBuilder();
        for (String digit : digits) {
            digitBoxes.append(
                    "<td style='padding: 8px;'>" +
                            "<div style='width: 48px; height: 56px; background: #F0E8DC; border-radius: 12px; " +
                            "font-size: 28px; font-weight: 600; color: #2C2420; text-align: center; " +
                            "line-height: 56px;'>" + digit + "</div>" +
                            "</td>"
            );
        }
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'></head>" +
                "<body style='margin:0; padding:0; background:#FAFAF8; font-family: Arial, sans-serif;'>" +
                "<table width='100%' cellpadding='0' cellspacing='0'>" +
                "<tr><td align='center' style='padding: 40px 20px;'>" +
                "<table width='480' cellpadding='0' cellspacing='0' style='background: white; border-radius: 24px; overflow: hidden;'>" +
                "<tr><td style='background: #C4956A; padding: 32px; text-align: center;'>" +
                "<div style='font-size: 28px; font-weight: 600; color: white;'>Seyahood</div>" +
                "<div style='font-size: 14px; color: rgba(255,255,255,0.8); margin-top: 4px;'>senin yolculuğun, senin hikayen</div>" +
                "</td></tr>" +
                "<tr><td style='padding: 40px 32px; text-align: center;'>" +
                "<div style='font-size: 22px; font-weight: 600; color: #2C2420; margin-bottom: 12px;'>Şifre Sıfırlama</div>" +
                "<div style='font-size: 15px; color: #9A8478; margin-bottom: 32px;'>Şifreni sıfırlamak için aşağıdaki kodu kullan.</div>" +
                "<table cellpadding='0' cellspacing='0' style='margin: 0 auto;'><tr>" +
                digitBoxes.toString() +
                "</tr></table>" +
                "<div style='font-size: 13px; color: #9A8478; margin-top: 24px;'>Bu kod <strong>10 dakika</strong> geçerlidir.</div>" +
                "</td></tr>" +
                "<tr><td style='background: #FAFAF8; padding: 24px 32px; text-align: center; border-top: 1px solid #EDE8E3;'>" +
                "<div style='font-size: 13px; color: #9A8478;'>Bu maili sen göndermemişsen görmezden gelebilirsin.</div>" +
                "<div style='font-size: 13px; color: #C4956A; margin-top: 8px; font-weight: 500;'>Seyahood Ekibi</div>" +
                "</td></tr>" +
                "</table></td></tr></table></body></html>";
    }
}
