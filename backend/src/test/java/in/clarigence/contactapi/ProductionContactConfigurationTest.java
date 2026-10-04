package in.clarigence.contactapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "PORT=9090")
@AutoConfigureMockMvc
@ActiveProfiles({"production", "test"})
class ProductionContactConfigurationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private Environment environment;

    @Test
    void allowsBothLiveOriginsForJsonSubmissions() throws Exception {
        for (String origin : new String[]{"https://www.clarigence.in", "https://clarigence.in"}) {
            mockMvc.perform(options("/api/contact")
                            .header("Origin", origin)
                            .header("Access-Control-Request-Method", "POST")
                            .header("Access-Control-Request-Headers", "content-type,accept"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Access-Control-Allow-Origin", origin));
            mockMvc.perform(post("/api/contact")
                            .header("Origin", origin)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(header().string("Access-Control-Allow-Origin", origin));
        }
    }

    @Test
    void rejectsUnconfiguredOrigins() throws Exception {
        for (String origin : new String[]{"http://www.clarigence.in", "http://localhost:8000", "https://example.com"}) {
            mockMvc.perform(options("/api/contact")
                            .header("Origin", origin)
                            .header("Access-Control-Request-Method", "POST"))
                    .andExpect(status().isForbidden())
                    .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
        }
    }

    @Test
    void usesHostPortAndSecureCookies() {
        assertEquals("9090", environment.getProperty("server.port"));
        assertEquals("true", environment.getProperty("server.servlet.session.cookie.secure"));
        assertEquals("true", environment.getProperty("SESSION_COOKIE_SECURE"));
    }
}
