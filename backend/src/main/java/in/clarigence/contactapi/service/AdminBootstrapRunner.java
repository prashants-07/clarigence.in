package in.clarigence.contactapi.service;

import in.clarigence.contactapi.entity.AdminUser;
import in.clarigence.contactapi.repository.AdminUserRepository;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class AdminBootstrapRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrapRunner.class);
    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final boolean enabled;
    private final String email;
    private final String password;

    public AdminBootstrapRunner(AdminUserRepository adminUserRepository,
                                PasswordEncoder passwordEncoder,
                                @Value("${ADMIN_BOOTSTRAP_ENABLED:false}") boolean enabled,
                                @Value("${ADMIN_EMAIL:}") String email,
                                @Value("${ADMIN_PASSWORD:}") String password) {
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.enabled = enabled;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!enabled) return;
        if (adminUserRepository.count() > 0) {
            log.info("Admin bootstrap skipped because an admin account already exists.");
            return;
        }
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        if (!StringUtils.hasText(normalizedEmail) || !normalizedEmail.contains("@")) {
            throw new IllegalStateException("Set ADMIN_EMAIL to a valid admin email before enabling admin bootstrap.");
        }
        int passwordBytes = password == null ? 0 : password.getBytes(StandardCharsets.UTF_8).length;
        if (passwordBytes < 12 || passwordBytes > 72) {
            throw new IllegalStateException("ADMIN_PASSWORD must contain 12 to 72 UTF-8 bytes for BCrypt.");
        }

        adminUserRepository.save(new AdminUser(normalizedEmail, passwordEncoder.encode(password)));
        log.info("Initial admin account created. Remove ADMIN_BOOTSTRAP_ENABLED, ADMIN_EMAIL, and ADMIN_PASSWORD from the environment.");
    }
}
