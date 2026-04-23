package ch.scs.scoutsummoners.config;

import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Custom password encoder that expects passwords to already be SHA-256 hashed from the client side.
 * This encoder does NOT hash passwords - it assumes they arrive pre-hashed from the browser.
 */
public class Sha256PasswordEncoder implements PasswordEncoder {

    @Override
    public String encode(CharSequence rawPassword) {
        // Password is already SHA-256 hashed from client side, return as-is
        return rawPassword.toString();
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        // Direct comparison since both should be SHA-256 hashes
        return rawPassword.toString().equals(encodedPassword);
    }
}
