package in.clarigence.contactapi.service;

import in.clarigence.contactapi.dto.AdminSessionResponse;
import in.clarigence.contactapi.entity.AdminUser;
import in.clarigence.contactapi.exception.InvalidCredentialsException;
import in.clarigence.contactapi.repository.AdminUserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.stereotype.Service;

@Service
public class AdminAuthService {

    private final AuthenticationManager authenticationManager;
    private final SessionAuthenticationStrategy sessionAuthenticationStrategy;
    private final SecurityContextRepository securityContextRepository;
    private final AdminUserRepository adminUserRepository;
    private final CsrfTokenRepository csrfTokenRepository;
    private final SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();

    public AdminAuthService(AuthenticationManager authenticationManager,
                            SessionAuthenticationStrategy sessionAuthenticationStrategy,
                            SecurityContextRepository securityContextRepository,
                            AdminUserRepository adminUserRepository,
                            CsrfTokenRepository csrfTokenRepository) {
        this.authenticationManager = authenticationManager;
        this.sessionAuthenticationStrategy = sessionAuthenticationStrategy;
        this.securityContextRepository = securityContextRepository;
        this.adminUserRepository = adminUserRepository;
        this.csrfTokenRepository = csrfTokenRepository;
    }

    public AdminSessionResponse login(String email, String password,
                                      HttpServletRequest request, HttpServletResponse response) {
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new InvalidCredentialsException();
        }
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(email, password));
        } catch (AuthenticationException exception) {
            throw new InvalidCredentialsException();
        }

        sessionAuthenticationStrategy.onAuthentication(authentication, request, response);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);

        String normalizedEmail = adminUserRepository.findByEmailIgnoreCase(authentication.getName())
                .map(AdminUser::getEmail)
                .orElseThrow(() -> new AuthenticationServiceException("Authenticated admin account disappeared."));
        return new AdminSessionResponse(normalizedEmail, "Signed in successfully.");
    }

    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        logoutHandler.logout(request, response, authentication);
        csrfTokenRepository.saveToken(null, request, response);
    }
}
