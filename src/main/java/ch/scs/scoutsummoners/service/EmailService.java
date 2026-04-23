package ch.scs.scoutsummoners.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${spring.mail.from}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendAccountApprovalRequest(String username, Long userId) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(adminEmail);
        message.setSubject("New Account Registration - Approval Required");

        String approvalUrl = "http://localhost:8080/admin/approve/" + userId;
        String rejectionUrl = "http://localhost:8080/admin/reject/" + userId;

        message.setText(
            "A new user has registered:\n\n" +
            "Username: " + username + "\n" +
            "User ID: " + userId + "\n\n" +
            "To APPROVE this account, click here:\n" + approvalUrl + "\n\n" +
            "To REJECT and delete this account, click here:\n" + rejectionUrl + "\n\n" +
            "ScoutSummoners Admin System"
        );

        mailSender.send(message);
    }

    public void sendAccountApprovedNotification(String email, String username) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(email);
        message.setSubject("Account Approved - ScoutSummoners");
        message.setText(
            "Hello " + username + ",\n\n" +
            "Your ScoutSummoners account has been approved!\n" +
            "You can now log in at: http://localhost:8080/login\n\n" +
            "Best regards,\n" +
            "ScoutSummoners Team"
        );

        mailSender.send(message);
    }
}
