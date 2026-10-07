package in.clarigence.contactapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.clarigence.contactapi.entity.AdminUser;
import in.clarigence.contactapi.repository.AdminUserRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.security.crypto.password.PasswordEncoder;
import javax.sql.DataSource;
import java.nio.file.*;
import java.security.SecureRandom;
import java.util.*;

/** Optional browser-test launcher. Never packaged in the production JAR. */
public class CmsBrowserFixture {
    public static void main(String[] args) throws Exception {
        var context=SpringApplication.run(ContactApiApplication.class,
            "--server.port=18081","--server.address=127.0.0.1",
            "--spring.datasource.url=jdbc:h2:mem:clarigence_cms_browser",
            "--spring.datasource.username=sa","--spring.datasource.password=",
            "--spring.datasource.driver-class-name=org.h2.Driver",
            "--spring.jpa.hibernate.ddl-auto=create-drop",
            "--spring.profiles.active=test","--ADMIN_BOOTSTRAP_ENABLED=false",
            "--SESSION_COOKIE_SECURE=false","--clarigence.enquiry-email.enabled=false",
            "--CORS_ALLOWED_ORIGINS=http://localhost:8000,http://127.0.0.1:8000");
        try(var connection=context.getBean(DataSource.class).getConnection()){
            if(!connection.getMetaData().getURL().startsWith("jdbc:h2:mem:clarigence_cms_browser"))throw new IllegalStateException("Browser tests require the isolated in-memory database.");
        }
        byte[] random=new byte[24];new SecureRandom().nextBytes(random);
        String password=Base64.getUrlEncoder().withoutPadding().encodeToString(random);
        String email="cms-browser@example.test";
        context.getBean(AdminUserRepository.class).saveAndFlush(new AdminUser(email,context.getBean(PasswordEncoder.class).encode(password)));
        Path directory=Path.of("..",".qa-tools").toAbsolutePath().normalize();Files.createDirectories(directory);
        context.getBean(ObjectMapper.class).writeValue(directory.resolve("cms-qa-credentials.json").toFile(),Map.of("email",email,"password",password));
        System.out.println("Isolated CMS browser fixture ready on port 18081. Test credentials are stored only in the ignored QA directory.");
    }
}
