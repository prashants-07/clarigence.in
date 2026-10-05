package in.clarigence.contactapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import in.clarigence.contactapi.repository.ContactRepository;
import in.clarigence.contactapi.dto.ContactRequest;
import in.clarigence.contactapi.service.ContactService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest(properties = {
        "clarigence.enquiry-email.enabled=true",
        "clarigence.enquiry-email.to=clarigence@gmail.com",
        "clarigence.enquiry-email.from=notifications@example.com"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EnquiryEmailIntegrationTest {

    @Autowired private MockMvc mvc;
    @Autowired private ContactRepository contacts;
    @Autowired private ContactService service;
    @Autowired private PlatformTransactionManager transactions;
    @MockitoBean private JavaMailSender sender;

    private static final String REQUEST = """
            {"name":"Taylor Example", "email":"taylor@example.com", "phone":"",
             "company":"Example Studio", "service":"Web development",
             "message":"We need a new company website."}
            """;

    @Test
    void sendsEnquiryDetailsOnlyAfterSaving() throws Exception {
        long before = contacts.count();
        org.mockito.Mockito.doAnswer(invocation -> {
            assertThat(contacts.count()).isEqualTo(before + 1);
            return null;
        }).when(sender).send(any(SimpleMailMessage.class));

        mvc.perform(post("/api/contact").contentType(MediaType.APPLICATION_JSON).content(REQUEST))
                .andExpect(status().isCreated());

        ArgumentCaptor<SimpleMailMessage> mail = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(sender).send(mail.capture());
        assertThat(mail.getValue().getTo()).containsExactly("clarigence@gmail.com");
        assertThat(mail.getValue().getFrom()).isEqualTo("notifications@example.com");
        assertThat(mail.getValue().getReplyTo()).isEqualTo("taylor@example.com");
        assertThat(mail.getValue().getText()).contains("Taylor Example", "Example Studio",
                "Web development", "We need a new company website.", "Phone: Not provided");
    }

    @Test
    void mailFailureStillReturnsSuccessAndKeepsEnquiry() throws Exception {
        long before = contacts.count();
        doThrow(new MailSendException("SMTP unavailable")).when(sender).send(any(SimpleMailMessage.class));
        mvc.perform(post("/api/contact").contentType(MediaType.APPLICATION_JSON).content(REQUEST))
                .andExpect(status().isCreated());
        assertThat(contacts.count()).isEqualTo(before + 1);
    }

    @Test
    void rolledBackEnquiryDoesNotSendEmail() {
        long before = contacts.count();
        new TransactionTemplate(transactions).executeWithoutResult(transaction -> {
            service.create(new ContactRequest("Taylor Example", "taylor@example.com", "", "",
                    "Web development", "We need a new company website."));
            verifyNoInteractions(sender);
            transaction.setRollbackOnly();
        });
        assertThat(contacts.count()).isEqualTo(before);
        verifyNoInteractions(sender);
    }

    @Test
    void invalidEnquiryDoesNotSendEmail() throws Exception {
        mvc.perform(post("/api/contact").contentType(MediaType.APPLICATION_JSON)
                        .content(REQUEST.replace("taylor@example.com", "invalid")))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(sender);
    }
}
