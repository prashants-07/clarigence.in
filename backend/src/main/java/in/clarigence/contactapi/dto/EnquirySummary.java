package in.clarigence.contactapi.dto;

import in.clarigence.contactapi.entity.Contact;
import in.clarigence.contactapi.entity.ContactStatus;
import java.time.Instant;

public record EnquirySummary(
        Long id,
        String name,
        String email,
        String company,
        String service,
        ContactStatus status,
        Instant createdAt
) {
    public static EnquirySummary from(Contact contact) {
        return new EnquirySummary(contact.getId(), contact.getName(), contact.getEmail(), contact.getCompany(),
                contact.getService(), contact.getStatus(), contact.getCreatedAt());
    }
}
