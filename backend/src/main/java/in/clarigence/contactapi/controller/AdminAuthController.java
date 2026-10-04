package in.clarigence.contactapi.controller;

import in.clarigence.contactapi.dto.AdminLoginRequest;
import in.clarigence.contactapi.dto.AdminSessionResponse;
import in.clarigence.contactapi.dto.CsrfResponse;
import in.clarigence.contactapi.service.AdminAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    public AdminAuthController(AdminAuthService adminAuthService) {
        this.adminAuthService = adminAuthService;
    }

    @GetMapping("/csrf")
    public CsrfResponse csrf(CsrfToken csrfToken) {
        return new CsrfResponse(csrfToken.getToken());
    }

    @PostMapping("/login")
    public ResponseEntity<AdminSessionResponse> login(
            @Valid @RequestBody AdminLoginRequest request,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse) {
        return ResponseEntity.ok(adminAuthService.login(
                request.email(), request.password(), servletRequest, servletResponse));
    }

    @GetMapping("/session")
    public AdminSessionResponse session(Authentication authentication) {
        return new AdminSessionResponse(authentication.getName(), "Signed in.");
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response,
                                       Authentication authentication) {
        adminAuthService.logout(request, response, authentication);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
