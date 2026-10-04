package in.clarigence.contactapi.dto;

import in.clarigence.contactapi.entity.ContactStatus;
import java.util.Map;

public record DashboardSummary(long totalEnquiries, Map<ContactStatus, Long> enquiriesByStatus) {
}
