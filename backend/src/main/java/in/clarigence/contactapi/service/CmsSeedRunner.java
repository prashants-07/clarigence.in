package in.clarigence.contactapi.service;
import in.clarigence.contactapi.repository.SiteDocumentRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
@Component
public class CmsSeedRunner implements ApplicationRunner {
    private final CmsInitializationService initialization;
    private final SiteDocumentRepository documents;
    public CmsSeedRunner(CmsInitializationService initialization,SiteDocumentRepository documents){this.initialization=initialization;this.documents=documents;}
    public void run(ApplicationArguments args) throws Exception {
        try{initialization.initialize();}
        catch(DataIntegrityViolationException conflict){if(!documents.existsById("cms-initialized"))throw conflict;}
    }
}
