package in.clarigence.contactapi.repository;

import in.clarigence.contactapi.entity.AdminUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminUserRepository extends JpaRepository<AdminUser, Long> {
    Optional<AdminUser> findByEmailIgnoreCase(String email);
}
