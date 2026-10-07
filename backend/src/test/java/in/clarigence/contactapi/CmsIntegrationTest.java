package in.clarigence.contactapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.clarigence.contactapi.repository.*;
import in.clarigence.contactapi.service.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
class CmsIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired WebsiteServiceRepository services;
    @Autowired PortfolioProjectRepository projects;
    @Autowired SiteDocumentRepository documents;
    @Autowired CmsInitializationService initialization;
    @Autowired SiteContentService content;

    @BeforeEach void resetCms() throws Exception {services.deleteAll();projects.deleteAll();documents.deleteAll();initialization.initialize();}
    private Map<String,Object> item(String slug,boolean active){
        Map<String,Object> r=new LinkedHashMap<>();r.put("name","Verification content");r.put("slug",slug);r.put("shortDescription","A test record in the isolated database.");r.put("description","Useful content for testing the CMS without touching MySQL.");r.put("icon","window");r.put("imageUrl","");r.put("displayOrder",1);r.put("active",active);r.put("capabilities",List.of("Planning","Development"));r.put("benefits",List.of("Clear processes"));r.put("technologies",List.of("Java"));r.put("featured",true);r.put("concept",true);return r;
    }
    private Map<String,Object> create(String kind,Map<String,Object> request) throws Exception {
        String result=mvc.perform(post("/api/admin/cms/"+kind).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(request))).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return json.readValue(result,new com.fasterxml.jackson.core.type.TypeReference<Map<String,Object>>(){});
    }
    @Test void publicApiSeedAndPrivateFields() throws Exception {
        mvc.perform(get("/api/services")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(10)).andExpect(jsonPath("$[0].slug").value("website-development")).andExpect(jsonPath("$[0].id").doesNotExist()).andExpect(jsonPath("$[0].version").doesNotExist());
        mvc.perform(get("/api/services/website-development")).andExpect(status().isOk()).andExpect(jsonPath("$.capabilities[0]").isNotEmpty());
        mvc.perform(get("/api/portfolio")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
        mvc.perform(get("/api/content/business")).andExpect(status().isOk()).andExpect(jsonPath("$.phone").doesNotExist()).andExpect(jsonPath("$.version").doesNotExist());
        mvc.perform(get("/api/content/cms-initialized")).andExpect(status().isNotFound());
    }
    @Test void seedIsIdempotentAndDoesNotResurrectDeletedServices() throws Exception {
        initialization.initialize();assertEquals(10,services.count());services.deleteAll();initialization.initialize();assertEquals(0,services.count());
    }
    @Test void authenticationAndCsrfRequired() throws Exception {
        for(String kind:List.of("services","portfolio")){
            mvc.perform(get("/api/admin/cms/"+kind)).andExpect(status().isUnauthorized());
            mvc.perform(post("/api/admin/cms/"+kind).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(item("private-test",true)))).andExpect(status().isUnauthorized());
            mvc.perform(put("/api/admin/cms/"+kind+"/1").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(item("private-test",true)))).andExpect(status().isUnauthorized());
            mvc.perform(delete("/api/admin/cms/"+kind+"/1").with(csrf())).andExpect(status().isUnauthorized());
        }
        mvc.perform(put("/api/admin/content/home").with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isUnauthorized());
    }
    @Test @WithMockUser(roles="USER") void nonAdminCannotWrite() throws Exception {
        mvc.perform(post("/api/admin/cms/services").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(item("restricted",true)))).andExpect(status().isForbidden());
    }
    @Test @WithMockUser(roles="ADMIN") void missingCsrfDenied() throws Exception {
        mvc.perform(post("/api/admin/cms/services").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(item("no-csrf",true)))).andExpect(status().isForbidden());
        mvc.perform(put("/api/admin/content/home").contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isForbidden());
    }
    @Test @WithMockUser(roles="ADMIN") void serviceCrudPublicationOrderAndConflict() throws Exception {
        Map<String,Object> saved=create("services",item("verification-service",true));Long id=((Number)saved.get("id")).longValue();
        mvc.perform(get("/api/services")).andExpect(jsonPath("$[0].slug").value("verification-service"));
        saved.put("name","Edited service");saved.put("active",false);
        mvc.perform(put("/api/admin/cms/services/"+id).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(saved))).andExpect(status().isOk()).andExpect(jsonPath("$.version").value(1));
        mvc.perform(get("/api/services/verification-service")).andExpect(status().isNotFound());
        mvc.perform(put("/api/admin/cms/services/"+id).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(saved))).andExpect(status().isConflict());
        mvc.perform(delete("/api/admin/cms/services/"+id).with(csrf())).andExpect(status().isNoContent());assertFalse(services.existsById(id));
    }
    @Test @WithMockUser(roles="ADMIN") void serviceValidationAndStableSlugs() throws Exception {
        Map<String,Object> r=item("website-development",true);
        mvc.perform(post("/api/admin/cms/services").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(r))).andExpect(status().isBadRequest()).andExpect(jsonPath("$.fieldErrors.slug").exists());
        r.put("slug","INVALID SLUG");r.put("name"," ");r.put("displayOrder",-1);
        mvc.perform(post("/api/admin/cms/services").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(r))).andExpect(status().isBadRequest());
        for(String unsafe:List.of("javascript:alert(1)","assets/../private.png","//evil.example/file.png","https://example.com/payload.svg","assets/test.php","assets/%2e%2e/private.png")){
            r=item("safe-test",true);r.put("imageUrl",unsafe);
            mvc.perform(post("/api/admin/cms/services").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(r))).andExpect(status().isBadRequest());
        }
        Map<String,Object> saved=create("services",item("stable-service",true));saved.put("slug","changed-service");
        mvc.perform(put("/api/admin/cms/services/"+saved.get("id")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(saved))).andExpect(status().isBadRequest()).andExpect(jsonPath("$.fieldErrors.slug").exists());
    }
    @Test @WithMockUser(roles="ADMIN") void portfolioCrudAndPublication() throws Exception {
        Map<String,Object> saved=create("portfolio",item("internal-verification-concept",false));String path="/api/admin/cms/portfolio/"+saved.get("id");
        mvc.perform(get("/api/portfolio")).andExpect(jsonPath("$.length()").value(0));saved.put("active",true);
        String updated=mvc.perform(put(path).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(saved))).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        mvc.perform(get("/api/portfolio/internal-verification-concept")).andExpect(status().isOk()).andExpect(jsonPath("$.concept").value(true)).andExpect(jsonPath("$.featured").value(true));
        saved=json.readValue(updated,new com.fasterxml.jackson.core.type.TypeReference<Map<String,Object>>(){});saved.put("active",false);
        mvc.perform(put(path).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(saved))).andExpect(status().isOk());
        mvc.perform(get("/api/portfolio/internal-verification-concept")).andExpect(status().isNotFound());
        mvc.perform(delete(path).with(csrf())).andExpect(status().isNoContent());assertEquals(0,projects.count());
    }
    @Test @WithMockUser(roles="ADMIN") void documentsValidationAndPublicUpdates() throws Exception {
        var document=content.get("home");Map<String,String> values=new LinkedHashMap<>(document.content());values.put("subtitle","Updated homepage content from the test database.");
        mvc.perform(put("/api/admin/content/home").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of("version",document.version(),"content",values)))).andExpect(status().isOk());
        mvc.perform(get("/api/content/home")).andExpect(jsonPath("$.subtitle").value(values.get("subtitle")));
        values.put("primaryCtaUrl","javascript:alert(1)");var latest=content.get("home");
        mvc.perform(put("/api/admin/content/home").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of("version",latest.version(),"content",values)))).andExpect(status().isBadRequest());
        values.put("primaryCtaUrl","contact.html");values.put("unsafeUnknown","x");
        mvc.perform(put("/api/admin/content/home").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of("version",latest.version(),"content",values)))).andExpect(status().isBadRequest());
    }
    @Test void publicCorsAllowsOnlyConfiguredOrigins() throws Exception {
        mvc.perform(options("/api/services").header("Origin","http://localhost:8000").header("Access-Control-Request-Method","GET")).andExpect(status().isOk()).andExpect(header().string("Access-Control-Allow-Origin","http://localhost:8000"));
        mvc.perform(options("/api/services").header("Origin","https://untrusted.example").header("Access-Control-Request-Method","GET")).andExpect(status().isForbidden());
        mvc.perform(options("/api/admin/cms/services").header("Origin","http://localhost:8000").header("Access-Control-Request-Method","PUT")).andExpect(status().isForbidden());
    }
}
