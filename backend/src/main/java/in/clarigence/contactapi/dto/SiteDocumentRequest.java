package in.clarigence.contactapi.dto;
import jakarta.validation.constraints.*;
import java.util.Map;
public record SiteDocumentRequest(@NotNull @Size(max=30) Map<@NotBlank @Size(max=80) String,@NotNull @Size(max=12000) String> content,@NotNull @Min(0) Long version) {}
