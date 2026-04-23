package ch.scs.scoutsummoners.config;

import ch.scs.scoutsummoners.service.RecaptchaService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

/**
 * Custom authentication filter that verifies reCAPTCHA before processing login
 */
public class RecaptchaAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final RecaptchaService recaptchaService;

    public RecaptchaAuthenticationFilter(RecaptchaService recaptchaService) {
        this.recaptchaService = recaptchaService;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {

        // Only verify reCAPTCHA for POST requests (actual login submissions)
        if ("POST".equalsIgnoreCase(request.getMethod())) {
            // Get reCAPTCHA response from request
            String recaptchaResponse = request.getParameter("g-recaptcha-response");

            // Verify reCAPTCHA before proceeding with authentication
            if (recaptchaResponse == null || !recaptchaService.verifyRecaptcha(recaptchaResponse)) {
                throw new BadCredentialsException("Invalid reCAPTCHA verification");
            }
        }

        // If reCAPTCHA is valid, proceed with normal authentication
        return super.attemptAuthentication(request, response);
    }
}
