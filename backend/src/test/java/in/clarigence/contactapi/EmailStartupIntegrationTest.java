package in.clarigence.contactapi;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.context.ActiveProfiles;
import in.clarigence.contactapi.service.EmailConfigurationLogging;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "ENQUIRY_EMAIL_ENABLED=true", "SMTP_HOST=smtp.example.invalid", "SMTP_PORT=587",
        "SMTP_USERNAME=startup@example.invalid", "SMTP_PASSWORD=isolated-test-secret",
        "ENQUIRY_EMAIL_TO=recipient@example.invalid"
})
@ActiveProfiles({"production", "test"})
@ExtendWith(OutputCaptureExtension.class)
class EmailStartupIntegrationTest {
    @LocalServerPort private int port;
    @Autowired private JavaMailSenderImpl sender;
    @Autowired private EmailConfigurationLogging configuration;

    @Test
    void startsWithProductionEmailSettingsWithoutConnectingToSmtpOrLoggingCredentials(CapturedOutput output) {
        assertThat(port).isPositive();
        assertThat(sender.getHost()).isEqualTo("smtp.example.invalid");
        assertThat(sender.getPort()).isEqualTo(587);
        assertThat(sender.getJavaMailProperties()).containsEntry("mail.smtp.starttls.required", "true")
                .containsEntry("mail.smtp.connectiontimeout", "3000")
                .containsEntry("mail.smtp.timeout", "3000").containsEntry("mail.smtp.writetimeout", "3000");
        configuration.logConfiguration();
        assertThat(output.getAll()).contains("SMTP configuration loaded", "usernameConfigured=true", "passwordConfigured=true",
                "senderConfigured=true", "recipientConfigured=true").doesNotContain("isolated-test-secret", "startup@example.invalid");
    }
}
