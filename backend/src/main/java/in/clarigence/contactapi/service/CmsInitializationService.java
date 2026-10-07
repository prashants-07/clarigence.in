package in.clarigence.contactapi.service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.clarigence.contactapi.dto.CmsItemRequest;
import in.clarigence.contactapi.entity.SiteDocument;
import in.clarigence.contactapi.repository.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CmsInitializationService {
    private final WebsiteServiceRepository services;
    private final SiteDocumentRepository documents;
    private final CmsService cms;
    private final ObjectMapper json;
    public CmsInitializationService(WebsiteServiceRepository services,SiteDocumentRepository documents,CmsService cms,ObjectMapper json){this.services=services;this.documents=documents;this.cms=cms;this.json=json;}
    @Transactional public void initialize() throws Exception {
        if(documents.existsById("cms-initialized"))return;
        try(var stream=new ClassPathResource("cms-seed.json").getInputStream()){
            JsonNode seed=json.readTree(stream);
            if(services.count()==0){for(JsonNode item:seed.get("services"))cms.save("services",null,json.treeToValue(item,CmsItemRequest.class));}
            for(var entry:seed.get("documents").properties()){
                if(!documents.existsById(entry.getKey()))documents.save(new SiteDocument(entry.getKey(),json.writeValueAsString(entry.getValue())));
            }
            // Marker prevents resurrecting deliberately deleted services on future restarts.
            documents.saveAndFlush(new SiteDocument("cms-initialized","{}"));
        }
    }
}
