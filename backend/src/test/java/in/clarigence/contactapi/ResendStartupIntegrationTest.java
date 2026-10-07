package in.clarigence.contactapi;

import in.clarigence.contactapi.service.ResendMailSender;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "ENQUIRY_EMAIL_ENABLED=true", "ENQUIRY_EMAIL_PROVIDER=resend",
        "RESEND_API_KEY=isolated-test-key", "ENQUIRY_EMAIL_FROM=enquiries@clarigence.in"
})
@ActiveProfiles("test")
class ResendStartupIntegrationTest {
    @Autowired JavaMailSender sender;
    @Test void selectsHttpsSenderWithoutSmtpCredentials() {
        assertThat(sender).isInstanceOf(ResendMailSender.class);
    }
}
