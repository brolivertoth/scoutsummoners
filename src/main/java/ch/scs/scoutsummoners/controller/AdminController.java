package ch.scs.scoutsummoners.controller;

import ch.scs.scoutsummoners.entity.User;
import ch.scs.scoutsummoners.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/approve/{userId}")
    public String approveUser(@PathVariable Long userId, @RequestParam String token) {
        User user = userRepository.findById(userId).orElse(null);

        // Validate user exists
        if (user == null) {
            return "admin-approval-error";
        }

        // Validate token matches
        if (user.getApprovalToken() == null || !user.getApprovalToken().equals(token)) {
            return "admin-approval-error";
        }

        // Validate token not expired
        if (user.getApprovalTokenExpiry() == null || LocalDateTime.now().isAfter(user.getApprovalTokenExpiry())) {
            return "admin-approval-error";
        }

        // Approve user and invalidate token
        user.setEnabled(true);
        user.setApprovalToken(null); // Single-use token
        user.setApprovalTokenExpiry(null);
        userRepository.save(user);

        return "admin-approval-success";
    }

    @GetMapping("/reject/{userId}")
    public String rejectUser(@PathVariable Long userId, @RequestParam String token) {
        User user = userRepository.findById(userId).orElse(null);

        // Validate user exists
        if (user == null) {
            return "admin-approval-error";
        }

        // Validate token matches
        if (user.getApprovalToken() == null || !user.getApprovalToken().equals(token)) {
            return "admin-approval-error";
        }

        // Validate token not expired
        if (user.getApprovalTokenExpiry() == null || LocalDateTime.now().isAfter(user.getApprovalTokenExpiry())) {
            return "admin-approval-error";
        }

        // Delete user
        userRepository.delete(user);

        return "admin-rejection-success";
    }
}
