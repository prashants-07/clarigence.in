package in.clarigence.contactapi;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.clarigence.contactapi.entity.AdminUser;
import in.clarigence.contactapi.entity.Contact;
import in.clarigence.contactapi.entity.ContactStatus;
import in.clarigence.contactapi.repository.AdminUserRepository;
import in.clarigence.contactapi.repository.ContactRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(properties = "server.forward-headers-strategy=framework")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminDashboardIntegrationTest {

    private static final String ADMIN_EMAIL = "admin@example.com";
    private static final String ADMIN_PASSWORD = "local-admin-password-123";

    @Autowired private MockMvc mockMvc;
    @Autowired private ContactRepository contactRepository;
    @Autowired private AdminUserRepository adminUserRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private ObjectMapper objectMapper;

    @BeforeEach
    void prepareData() {
        contactRepository.deleteAll();
        adminUserRepository.deleteAll();
        adminUserRepository.save(new AdminUser(ADMIN_EMAIL, passwordEncoder.encode(ADMIN_PASSWORD)));
    }

    @Test
    void loginViewUpdateStatusAndDeleteEnquiry() throws Exception {
        mockMvc.perform(get("/admin/"))
                .andExpect(status().isOk());

        Contact saved = contactRepository.saveAndFlush(new Contact(
                "Riley Contact", "riley@example.com", "+1 555 010 2211", "Riley Studio",
                "Web development", "We are planning a new company website."));

        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isUnauthorized());

        Cookie csrfCookie = fetchCsrfCookie();

        MvcResult loginResult = mockMvc.perform(post("/api/admin/auth/login")
                        .cookie(csrfCookie)
                        .header("X-XSRF-TOKEN", csrfCookie.getValue())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsBytes(new LoginBody(ADMIN_EMAIL, ADMIN_PASSWORD))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(ADMIN_EMAIL))
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
        org.junit.jupiter.api.Assertions.assertNotNull(session);

        mockMvc.perform(get("/api/admin/dashboard").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEnquiries").value(1))
                .andExpect(jsonPath("$.enquiriesByStatus.NEW").value(1));

        mockMvc.perform(get("/api/admin/enquiries")
                        .param("query", "riley@example.com")
                        .param("status", "NEW")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(1))
                .andExpect(jsonPath("$.items[0].id").value(saved.getId()));

        mockMvc.perform(get("/api/admin/enquiries/{id}", saved.getId()).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Riley Contact"))
                .andExpect(jsonPath("$.message").value("We are planning a new company website."));

        mockMvc.perform(put("/api/admin/enquiries/{id}/status", saved.getId())
                        .header("Origin", "https://clarigencein-production.up.railway.app")
                        .header("X-Forwarded-Proto", "https")
                        .header("X-Forwarded-Host", "clarigencein-production.up.railway.app")
                        .header("X-Forwarded-Port", "443")
                        .session(session)
                        .cookie(csrfCookie)
                        .header("X-XSRF-TOKEN", csrfCookie.getValue())
                        .contentType("application/json")
                        .content("{\"status\":\"CONTACTED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONTACTED"));

        mockMvc.perform(delete("/api/admin/enquiries/{id}", saved.getId())
                        .header("Origin", "https://clarigencein-production.up.railway.app")
                        .header("X-Forwarded-Proto", "https")
                        .header("X-Forwarded-Host", "clarigencein-production.up.railway.app")
                        .header("X-Forwarded-Port", "443")
                        .session(session)
                        .cookie(csrfCookie)
                        .header("X-XSRF-TOKEN", csrfCookie.getValue()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/admin/dashboard").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEnquiries").value(0));

        mockMvc.perform(post("/api/admin/auth/logout")
                        .session(session)
                        .cookie(csrfCookie)
                        .header("X-XSRF-TOKEN", csrfCookie.getValue()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/admin/dashboard").session(session))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsInvalidAdminPassword() throws Exception {
        Cookie csrfCookie = fetchCsrfCookie();
        mockMvc.perform(post("/api/admin/auth/login")
                        .cookie(csrfCookie)
                        .header("X-XSRF-TOKEN", csrfCookie.getValue())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsBytes(new LoginBody(ADMIN_EMAIL, "incorrect-password"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("The email or password is incorrect."));
    }

    private Cookie fetchCsrfCookie() throws Exception {
        MvcResult csrfResult = mockMvc.perform(get("/api/admin/auth/csrf"))
                .andExpect(status().isOk())
                .andReturn();
        Cookie csrfCookie = csrfResult.getResponse().getCookie("XSRF-TOKEN");
        org.junit.jupiter.api.Assertions.assertNotNull(csrfCookie);
        return csrfCookie;
    }

    private record LoginBody(String email, String password) {
    }
}
