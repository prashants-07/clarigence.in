package in.clarigence.contactapi.service;

import in.clarigence.contactapi.entity.Contact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@ConditionalOnProperty(name = "clarigence.enquiry-email.enabled", havingValue = "true")
public class EnquiryEmailNotification {

    private static final Logger log = LoggerFactory.getLogger(EnquiryEmailNotification.class);
    private final JavaMailSender sender;
    private final String recipient;
    private final String from;

    public EnquiryEmailNotification(JavaMailSender sender,
            @Value("${clarigence.enquiry-email.to}") String recipient,
            @Value("${clarigence.enquiry-email.from}") String from) {
        this.sender = sender;
        this.recipient = recipient;
        this.from = from;
        if (recipient.isBlank() || from.isBlank()) {
            throw new IllegalArgumentException("Enquiry email requires a recipient and sender address.");
        }
    }

    // Send synchronously after commit so serverless hosts finish the attempt before returning.
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void send(EnquiryCreated event) {
        Contact contact = event.contact();
        log.info("Email sending started for saved enquiry {}.", contact.getId());
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(recipient);
            mail.setFrom(from);
            mail.setReplyTo(contact.getEmail());
            mail.setSubject("New Clarigence enquiry #" + contact.getId());
            mail.setText("""
                    A new website enquiry has been saved in the admin portal.

                    Enquiry ID: %s
                    Received (UTC): %s
                    Name: %s
                    Email: %s
                    Phone: %s
                    Company: %s
                    Service: %s

                    Message:
                    %s
                    """.formatted(contact.getId(), contact.getCreatedAt(), contact.getName(),
                    contact.getEmail(), optional(contact.getPhone()), optional(contact.getCompany()),
                    contact.getService(), contact.getMessage()));
            sender.send(mail);
            log.info("Email sent successfully for enquiry {} (SMTP server accepted the message).", contact.getId());
        } catch (RuntimeException exception) {
            // Avoid logging enquiry contents or SMTP credentials.
            log.error("Email sending failed for saved enquiry {}: type={}, diagnostic={}. Enquiry remains saved.",
                    contact.getId(), exception.getClass().getSimpleName(), MailFailureDiagnostic.describe(exception));
        }
    }

    private static String optional(String value) {
        return value == null || value.isBlank() ? "Not provided" : value;
    }
}
