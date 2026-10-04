package in.clarigence.contactapi.dto;

import in.clarigence.contactapi.entity.ContactStatus;
import jakarta.validation.constraints.NotNull;

public record ContactStatusRequest(@NotNull ContactStatus status) {
}
