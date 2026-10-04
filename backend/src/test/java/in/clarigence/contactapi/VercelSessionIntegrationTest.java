package in.clarigence.contactapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import in.clarigence.contactapi.entity.AdminUser;
import in.clarigence.contactapi.repository.AdminUserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(properties = {
        "spring.session.jdbc.initialize-schema=always",
        "spring.datasource.url=jdbc:h2:mem:vercel_session_test;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE"
})
@AutoConfigureMockMvc
@ActiveProfiles({"production", "vercel", "test"})
class VercelSessionIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private AdminUserRepository adminUserRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Test
    void restoresLoginFromDatabaseAndProtectsLogoutWithCsrf() throws Exception {
        adminUserRepository.saveAndFlush(new AdminUser(
                "session-admin@example.com", passwordEncoder.encode("isolated-test-password-123")));
        Cookie csrf = mockMvc.perform(get("/api/admin/auth/csrf").secure(true))
                .andExpect(status().isOk()).andReturn().getResponse().getCookie("XSRF-TOKEN");
        assertNotNull(csrf);

        MvcResult login = mockMvc.perform(post("/api/admin/auth/login").secure(true)
                        .cookie(csrf).header("X-XSRF-TOKEN", csrf.getValue())
                        .contentType("application/json")
                        .content("{\"email\":\"session-admin@example.com\",\"password\":\"isolated-test-password-123\"}"))
                .andExpect(status().isOk()).andReturn();
        Cookie session = login.getResponse().getCookie("JSESSIONID");
        assertNotNull(session);
        assertTrue(login.getResponse().getHeaders("Set-Cookie").stream().anyMatch(value ->
                value.startsWith("JSESSIONID=") && value.contains("Secure")
                        && value.contains("HttpOnly") && value.contains("SameSite=Strict")));
        assertEquals(1, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM SPRING_SESSION", Integer.class));
        assertTrue(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM SPRING_SESSION_ATTRIBUTES", Integer.class) > 0);

        // A fresh request has only the cookie: no in-memory MockHttpSession is reused.
        mockMvc.perform(get("/api/admin/dashboard").secure(true).cookie(session))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/admin/auth/logout").secure(true).cookie(session))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/admin/auth/logout").secure(true)
                        .cookie(session, csrf).header("X-XSRF-TOKEN", csrf.getValue()))
                .andExpect(status().isNoContent());
        assertEquals(0, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM SPRING_SESSION", Integer.class));
        mockMvc.perform(get("/api/admin/dashboard").secure(true).cookie(session))
                .andExpect(status().isUnauthorized());
    }
}
