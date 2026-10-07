package in.clarigence.contactapi.service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import in.clarigence.contactapi.dto.CmsItemRequest;
import in.clarigence.contactapi.entity.SiteDocument;
import in.clarigence.contactapi.repository.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CmsInitializationService {
    private static final String BUSINESS_EMAIL_MIGRATION="business-email-migrated";
    private final WebsiteServiceRepository services;
    private final SiteDocumentRepository documents;
    private final CmsService cms;
    private final ObjectMapper json;
    public CmsInitializationService(WebsiteServiceRepository services,SiteDocumentRepository documents,CmsService cms,ObjectMapper json){this.services=services;this.documents=documents;this.cms=cms;this.json=json;}
    @Transactional public void initialize() throws Exception {
        if(!documents.existsById("cms-initialized")){
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
        migrateLegacyBusinessEmail();
    }
    private void migrateLegacyBusinessEmail() throws Exception {
        if(documents.existsById(BUSINESS_EMAIL_MIGRATION))return;
        var business=documents.findById("business").orElse(null);
        if(business==null)return;
        // Claim this migration before updating business. The unique key serializes concurrent startup attempts.
        documents.saveAndFlush(new SiteDocument(BUSINESS_EMAIL_MIGRATION,"{}"));
        JsonNode content=json.readTree(business.getContentJson());
        if(!(content instanceof ObjectNode values))throw new IllegalStateException("Invalid stored business content");
        JsonNode email=values.get("email");
        if(email==null||!email.isTextual()||!"clarigence@gmail.com".equals(email.textValue()))return;
        values.put("email","hello@clargience.in");
        business.setContentJson(json.writeValueAsString(values));
        documents.saveAndFlush(business);
    }
}
