package in.clarigence.contactapi.service;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class ResendMailSenderTest {
    private SimpleMailMessage message() {
        var message = new SimpleMailMessage();
        message.setFrom("enquiries@clarigence.in");
        message.setTo("hello@clarigence.in");
        message.setReplyTo("visitor@example.com");
        message.setSubject("New Clarigence enquiry #7");
        message.setText("Name: Visitor\nMessage: Website enquiry");
        return message;
    }

    @Test void deliversViaHttpsWithVisitorReplyAddress() {
        var builder = RestClient.builder();
        var server = MockRestServiceServer.bindTo(builder).build();
        var sender = new ResendMailSender("isolated-key", builder);
        server.expect(requestTo("https://api.resend.com/emails"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer isolated-key"))
                .andExpect(jsonPath("$.to[0]").value("hello@clarigence.in"))
                .andExpect(jsonPath("$.from").value("enquiries@clarigence.in"))
                .andExpect(jsonPath("$.reply_to").value("visitor@example.com"))
                .andExpect(jsonPath("$.text").value("Name: Visitor\nMessage: Website enquiry"))
                .andRespond(withSuccess("{\"id\":\"test-email-id\"}", MediaType.APPLICATION_JSON));
        sender.send(message());
        server.verify();
    }

    @Test void providerFailureOmitsPrivateResponse() {
        var builder = RestClient.builder();
        var server = MockRestServiceServer.bindTo(builder).build();
        var sender = new ResendMailSender("isolated-key", builder);
        server.expect(requestTo("https://api.resend.com/emails"))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED).body("private-provider-response"));
        assertThatThrownBy(() -> sender.send(message())).isInstanceOf(MailSendException.class)
                .hasMessageContaining("Resend HTTPS delivery failed")
                .hasMessageNotContaining("private-provider-response").hasNoCause();
        server.verify();
    }

    @Test void missingKeyFailsBeforeSending() {
        assertThatThrownBy(() -> new ResendMailSender("", RestClient.builder()))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("RESEND_API_KEY");
    }
}
