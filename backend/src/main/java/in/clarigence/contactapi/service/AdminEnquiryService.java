package in.clarigence.contactapi.service;

import in.clarigence.contactapi.dto.DashboardSummary;
import in.clarigence.contactapi.dto.EnquiryDetails;
import in.clarigence.contactapi.dto.EnquiryPage;
import in.clarigence.contactapi.dto.EnquirySummary;
import in.clarigence.contactapi.entity.Contact;
import in.clarigence.contactapi.entity.ContactStatus;
import in.clarigence.contactapi.exception.ResourceNotFoundException;
import in.clarigence.contactapi.repository.ContactRepository;
import java.util.EnumMap;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminEnquiryService {

    private final ContactRepository contactRepository;

    public AdminEnquiryService(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    @Transactional(readOnly = true)
    public DashboardSummary dashboard() {
        Map<ContactStatus, Long> counts = new EnumMap<>(ContactStatus.class);
        for (ContactStatus status : ContactStatus.values()) counts.put(status, 0L);
        contactRepository.countByStatus().forEach(row -> counts.put(row.getStatus(), row.getTotal()));
        return new DashboardSummary(contactRepository.count(), counts);
    }

    @Transactional(readOnly = true)
    public EnquiryPage search(String query, ContactStatus status, int page, int size) {
        String normalizedQuery = query == null || query.isBlank() ? null : query.trim();
        Page<Contact> results = contactRepository.search(normalizedQuery, status,
                PageRequest.of(page, size, Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))));
        return new EnquiryPage(results.getContent().stream().map(EnquirySummary::from).toList(),
                results.getNumber(), results.getSize(), results.getTotalElements(), results.getTotalPages());
    }

    @Transactional(readOnly = true)
    public EnquiryDetails get(Long id) {
        return EnquiryDetails.from(findContact(id));
    }

    @Transactional
    public EnquiryDetails updateStatus(Long id, ContactStatus status) {
        Contact contact = findContact(id);
        contact.setStatus(status);
        return EnquiryDetails.from(contactRepository.save(contact));
    }

    @Transactional
    public void delete(Long id) {
        Contact contact = findContact(id);
        contactRepository.delete(contact);
    }

    private Contact findContact(Long id) {
        return contactRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enquiry not found."));
    }
}
