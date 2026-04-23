package ch.scs.scoutsummoners.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Map;

@Service
public class RecaptchaService {

    @Value("${recaptcha.site-key}")
    private String siteKey;

    @Value("${recaptcha.secret-key}")
    private String secretKey;

    @Value("${recaptcha.verify-url}")
    private String verifyUrl;

    /**
     * Verifies the reCAPTCHA response from the client
     * @param recaptchaResponse The g-recaptcha-response token from the form
     * @return true if verification succeeds, false otherwise
     */
    public boolean verifyRecaptcha(String recaptchaResponse) {
        // Bypass reCAPTCHA verification if using placeholder keys (development mode)
        if ("YOUR_SITE_KEY_HERE".equals(siteKey) || "YOUR_SECRET_KEY_HERE".equals(secretKey)) {
            System.out.println("WARNING: Using placeholder reCAPTCHA keys - verification bypassed");
            return true; // Skip verification during development
        }

        if (recaptchaResponse == null || recaptchaResponse.isEmpty()) {
            return false;
        }

        try {
            RestTemplate restTemplate = new RestTemplate();

            // Prepare request parameters
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("secret", secretKey);
            params.add("response", recaptchaResponse);

            // Make POST request to Google's verification endpoint
            Map<String, Object> response = restTemplate.postForObject(
                verifyUrl,
                params,
                Map.class
            );

            // Check if verification was successful
            if (response != null && response.containsKey("success")) {
                return (Boolean) response.get("success");
            }

            return false;
        } catch (Exception e) {
            // Log error and fail closed (reject if verification fails)
            System.err.println("reCAPTCHA verification error: " + e.getMessage());
            return false;
        }
    }
}
