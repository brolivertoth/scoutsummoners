package ch.scs.scoutsummoners.controller;

import ch.scs.scoutsummoners.entity.User;
import ch.scs.scoutsummoners.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/approve/{userId}")
    public String approveUser(@PathVariable Long userId) {
        User user = userRepository.findById(userId).orElse(null);

        if (user != null) {
            user.setEnabled(true);
            userRepository.save(user);
            return "admin-approval-success";
        }

        return "admin-approval-error";
    }

    @GetMapping("/reject/{userId}")
    public String rejectUser(@PathVariable Long userId) {
        User user = userRepository.findById(userId).orElse(null);

        if (user != null) {
            userRepository.delete(user);
            return "admin-rejection-success";
        }

        return "admin-approval-error";
    }
}
