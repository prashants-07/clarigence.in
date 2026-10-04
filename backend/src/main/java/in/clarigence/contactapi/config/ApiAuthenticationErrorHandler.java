package in.clarigence.contactapi.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.clarigence.contactapi.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class ApiAuthenticationErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public ApiAuthenticationErrorHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException exception) throws IOException {
        writeError(request, response, HttpServletResponse.SC_UNAUTHORIZED,
                "Unauthorized", "Please sign in to access the admin dashboard.");
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException exception) throws IOException {
        writeError(request, response, HttpServletResponse.SC_FORBIDDEN,
                "Forbidden", "You do not have permission to perform this action.");
    }

    private void writeError(HttpServletRequest request, HttpServletResponse response,
                            int status, String error, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getOutputStream(),
                new ApiErrorResponse(Instant.now(), status, error, message, request.getRequestURI(), Map.of()));
    }
}
