package ch.scs.scoutsummoners.config;

import ch.scs.scoutsummoners.service.RecaptchaService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter that verifies reCAPTCHA before allowing login attempts
 */
public class RecaptchaAuthenticationFilter extends OncePerRequestFilter {

    private final RecaptchaService recaptchaService;

    public RecaptchaAuthenticationFilter(RecaptchaService recaptchaService) {
        this.recaptchaService = recaptchaService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Only verify reCAPTCHA for login POST requests
        if ("/perform_login".equals(request.getServletPath()) && "POST".equalsIgnoreCase(request.getMethod())) {
            String recaptchaResponse = request.getParameter("g-recaptcha-response");

            // Verify reCAPTCHA
            if (recaptchaResponse == null || !recaptchaService.verifyRecaptcha(recaptchaResponse)) {
                response.sendRedirect("/login?error");
                return;
            }
        }

        // If reCAPTCHA is valid or not a login request, continue
        filterChain.doFilter(request, response);
    }
}
