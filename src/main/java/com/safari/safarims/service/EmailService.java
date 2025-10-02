package com.safari.safarims.service;

import com.safari.safarims.entity.OutboundEmail;
import com.safari.safarims.repository.OutboundEmailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final OutboundEmailRepository outboundEmailRepository;

    @Value("${app.email.from}")
    private String fromEmail;

    @Value("${app.email.enabled:false}")
    private boolean emailEnabled;

    public void sendOtp(String toEmail, String otp, String purpose) {
        String subject = "Safari Management System - " + purpose + " OTP";
        String body = buildOtpEmailBody(otp, purpose);
        sendEmail(toEmail, subject, body, "OTP_TEMPLATE");
    }

    public void sendBookingConfirmation(String toEmail, String bookingDetails) {
        String subject = "Safari Management System - Booking Confirmation";
        String body = buildBookingConfirmationBody(bookingDetails);
        sendEmail(toEmail, subject, body, "BOOKING_CONFIRMATION");
    }

    public void sendPaymentReminder(String toEmail, String bookingId, String amount) {
        String subject = "Safari Management System - Payment Reminder";
        String body = buildPaymentReminderBody(bookingId, amount);
        sendEmail(toEmail, subject, body, "PAYMENT_REMINDER");
    }

    public void sendPaymentExpiry(String toEmail, String bookingId) {
        String subject = "Safari Management System - Payment Expired";
        String body = buildPaymentExpiryBody(bookingId);
        sendEmail(toEmail, subject, body, "PAYMENT_EXPIRY");
    }

    private void sendEmail(String toEmail, String subject, String body, String templateName) {
        // Save email to database for tracking
        OutboundEmail outboundEmail = OutboundEmail.builder()
            .toEmail(toEmail)
            .subject(subject)
            .body(body)
            .templateName(templateName)
            .build();

        try {
            if (emailEnabled) {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromEmail);
                message.setTo(toEmail);
                message.setSubject(subject);
                message.setText(body);

                mailSender.send(message);

                outboundEmail.setStatus("SENT");
                outboundEmail.setSentAt(LocalDateTime.now());
                log.info("Email sent successfully to: {}", toEmail);
            } else {
                // In development mode, just log the email
                log.info("EMAIL (DEV MODE) - To: {}, Subject: {}, Body: {}", toEmail, subject, body);
                outboundEmail.setStatus("SENT");
                outboundEmail.setSentAt(LocalDateTime.now());
            }
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage());
            outboundEmail.setStatus("FAILED");
            outboundEmail.setErrorMessage(e.getMessage());
        }

        outboundEmailRepository.save(outboundEmail);
    }

    private String buildOtpEmailBody(String otp, String purpose) {
        return String.format("""
            Dear User,
            
            Your %s OTP for Safari Management System is: %s
            
            This OTP will expire in 10 minutes. Please do not share this code with anyone.
            
            If you did not request this, please ignore this email.
            
            Best regards,
            Safari Management System Team
            """, purpose, otp);
    }

    private String buildBookingConfirmationBody(String bookingDetails) {
        return String.format("""
            Dear Customer,
            
            Your safari booking has been confirmed! Here are the details:
            
            %s
            
            Please proceed with payment to secure your booking.
            
            Best regards,
            Safari Management System Team
            """, bookingDetails);
    }

    private String buildPaymentReminderBody(String bookingId, String amount) {
        return String.format("""
            Dear Customer,
            
            This is a reminder that your payment for booking #%s is still pending.
            
            Amount: $%s
            
            Please complete your payment to confirm your safari booking.
            
            Best regards,
            Safari Management System Team
            """, bookingId, amount);
    }

    private String buildPaymentExpiryBody(String bookingId) {
        return String.format("""
            Dear Customer,
            
            We regret to inform you that your booking #%s has been cancelled due to payment timeout.
            
            You can create a new booking request if you're still interested in our safari packages.
            
            Best regards,
            Safari Management System Team
            """, bookingId);
    }
}
