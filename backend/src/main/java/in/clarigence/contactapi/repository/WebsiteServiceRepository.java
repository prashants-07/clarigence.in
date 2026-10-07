package in.clarigence.contactapi.repository;
import in.clarigence.contactapi.entity.WebsiteService;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface WebsiteServiceRepository extends JpaRepository<WebsiteService,Long> {
    List<WebsiteService> findAllByOrderByDisplayOrderAscIdAsc();
    List<WebsiteService> findByActiveTrueOrderByDisplayOrderAscIdAsc();
    Optional<WebsiteService> findBySlugAndActiveTrue(String slug);
    boolean existsBySlug(String slug);
    boolean existsBySlugAndIdNot(String slug,Long id);
    long countByActiveTrue();
}
