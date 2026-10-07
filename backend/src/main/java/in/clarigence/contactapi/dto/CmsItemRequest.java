package in.clarigence.contactapi.dto;
import jakarta.validation.constraints.*;
import java.util.List;

public record CmsItemRequest(
    @NotBlank @Size(max=120) String name,
    @NotBlank @Size(max=120) @Pattern(regexp="[a-z0-9]+(?:-[a-z0-9]+)*") String slug,
    @NotBlank @Size(max=600) String shortDescription,
    @NotBlank @Size(max=12000) String description,
    @NotBlank @Pattern(regexp="window|cloud|phone|trend|layout|pin|mail|shield|form|flow") String icon,
    @Size(max=2048) String imageUrl,
    @NotNull @Min(0) @Max(100000) Integer displayOrder,
    @NotNull Boolean active,
    @Size(max=20) List<@NotBlank @Size(max=300) String> capabilities,
    @Size(max=20) List<@NotBlank @Size(max=300) String> benefits,
    @Size(max=2000) String useCases,
    @Size(max=120) String category,
    @Size(max=20) List<@NotBlank @Size(max=120) String> technologies,
    @Size(max=2048) String projectUrl,
    Boolean featured, Boolean concept, @Min(0) Long version
) {}
