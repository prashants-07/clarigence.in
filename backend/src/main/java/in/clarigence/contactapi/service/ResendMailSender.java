package in.clarigence.contactapi.service;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** HTTPS delivery for hosts that block outbound SMTP. */
@Component
@ConditionalOnExpression("${clarigence.enquiry-email.enabled:false} && '${clarigence.enquiry-email.provider:smtp}' == 'resend'")
public class ResendMailSender extends JavaMailSenderImpl {
    private final RestClient client;

    @org.springframework.beans.factory.annotation.Autowired
    public ResendMailSender(@Value("${clarigence.enquiry-email.resend-api-key}") String apiKey) {
        this(apiKey, builder());
    }

    ResendMailSender(String apiKey, RestClient.Builder builder) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("Resend email requires RESEND_API_KEY on the backend host.");
        }
        client = builder.baseUrl("https://api.resend.com")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey.trim()).build();
    }

    private static RestClient.Builder builder() {
        HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3))
                .followRedirects(HttpClient.Redirect.NEVER).build();
        var factory = new JdkClientHttpRequestFactory(http);
        factory.setReadTimeout(Duration.ofSeconds(5));
        return RestClient.builder().requestFactory(factory);
    }

    @Override public void send(SimpleMailMessage message) { send(new SimpleMailMessage[]{message}); }

    @Override public void send(SimpleMailMessage... messages) {
        for (SimpleMailMessage message : messages) {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("from", message.getFrom());
            payload.put("to", Arrays.asList(message.getTo()));
            payload.put("subject", message.getSubject());
            payload.put("text", message.getText());
            if (message.getReplyTo() != null) payload.put("reply_to", message.getReplyTo());
            try {
                client.post().uri("/emails").contentType(MediaType.APPLICATION_JSON).body(payload)
                        .retrieve().toBodilessEntity();
            } catch (RuntimeException exception) {
                // Provider responses may contain private email data. Do not retain or log them.
                throw new MailSendException("Resend HTTPS delivery failed; check Resend logs, API key and verified sender domain.");
            }
        }
    }
}
