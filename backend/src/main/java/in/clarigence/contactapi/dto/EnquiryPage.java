package in.clarigence.contactapi.dto;

import java.util.List;

public record EnquiryPage(
        List<EnquirySummary> items,
        int page,
        int size,
        long totalItems,
        int totalPages
) {
}
