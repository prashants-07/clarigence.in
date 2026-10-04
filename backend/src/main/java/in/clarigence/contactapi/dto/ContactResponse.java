package in.clarigence.contactapi.dto;

import java.time.Instant;

public record ContactResponse(Long id, String message, Instant createdAt) {
}
