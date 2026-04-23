package ch.scs.scoutsummoners.controller;

import ch.scs.scoutsummoners.entity.User;
import ch.scs.scoutsummoners.service.EmailService;
import ch.scs.scoutsummoners.service.RecaptchaService;
import ch.scs.scoutsummoners.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private RecaptchaService recaptchaService;

    @Autowired
    private EmailService emailService;

    @Value("${recaptcha.site-key}")
    private String recaptchaSiteKey;

    @GetMapping("/")
    public String root() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("recaptchaSiteKey", recaptchaSiteKey);
        return "login";
    }

    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("recaptchaSiteKey", recaptchaSiteKey);
        return "register";
    }

    @PostMapping("/register")
    public String register(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam(name = "g-recaptcha-response") String recaptchaResponse,
            Model model) {

        // Verify reCAPTCHA first
        if (!recaptchaService.verifyRecaptcha(recaptchaResponse)) {
            model.addAttribute("error", "Please complete the reCAPTCHA verification");
            model.addAttribute("recaptchaSiteKey", recaptchaSiteKey);
            return "register";
        }

        try {
            User newUser = userService.registerUser(username, password);

            // Send approval email to admin with token
            emailService.sendAccountApprovalRequest(username, newUser.getId(), newUser.getApprovalToken());

            model.addAttribute("success", "Registration successful! Your account is pending approval. You will receive an email once approved.");
            model.addAttribute("recaptchaSiteKey", recaptchaSiteKey);
            return "register";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("recaptchaSiteKey", recaptchaSiteKey);
            return "register";
        }
    }
}
