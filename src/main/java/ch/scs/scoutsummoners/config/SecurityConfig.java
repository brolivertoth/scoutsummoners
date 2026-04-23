package ch.scs.scoutsummoners.config;

import ch.scs.scoutsummoners.service.CustomUserDetailsService;
import ch.scs.scoutsummoners.service.RecaptchaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    @Autowired
    private RecaptchaService recaptchaService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {
        // Create reCAPTCHA verification filter
        RecaptchaAuthenticationFilter recaptchaFilter = new RecaptchaAuthenticationFilter(recaptchaService);

        http
            .authenticationManager(authenticationManager)
            // Enable CSRF protection
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/admin/approve/**", "/admin/reject/**") // Token-based endpoints
            )
            // Security headers
            .headers(headers -> headers
                .frameOptions(frame -> frame.deny()) // Prevent clickjacking
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives("default-src 'self'; " +
                        "script-src 'self' 'unsafe-inline' https://www.google.com/recaptcha/ https://www.gstatic.com/recaptcha/ https://cdn.jsdelivr.net/ https://cdnjs.cloudflare.com/ https://cdn.quilljs.com/; " +
                        "style-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net/ https://cdn.quilljs.com/; " +
                        "font-src 'self' https://cdn.jsdelivr.net/; " +
                        "frame-src https://www.google.com/recaptcha/; " +
                        "connect-src 'self' https://www.google.com/ https://www.gstatic.com/; " +
                        "img-src 'self' data: blob:;")
                )
            )
            // Session management
            .sessionManagement(session -> session
                .sessionFixation(fixation -> fixation.migrateSession()) // Prevent session fixation
                .sessionConcurrency(concurrency -> concurrency
                    .maximumSessions(1) // One session per user
                    .maxSessionsPreventsLogin(false) // Allow new login, invalidate old session
                )
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/register", "/css/**", "/js/**", "/login", "/admin/approve/**", "/admin/reject/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(recaptchaFilter, UsernamePasswordAuthenticationFilter.class)
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/perform_login")
                .defaultSuccessUrl("/dashboard", true)
                .failureHandler((request, response, exception) -> {
                    // Check if the exception is due to disabled account
                    if (exception instanceof org.springframework.security.authentication.DisabledException) {
                        response.sendRedirect("/login?disabled");
                    } else {
                        response.sendRedirect("/login?error");
                    }
                })
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new Sha256PasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http, PasswordEncoder passwordEncoder) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
            http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder
            .userDetailsService(userDetailsService)
            .passwordEncoder(passwordEncoder);
        return authenticationManagerBuilder.build();
    }
}
