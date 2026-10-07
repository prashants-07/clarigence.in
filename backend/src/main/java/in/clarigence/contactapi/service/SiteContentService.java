package in.clarigence.contactapi.service;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.clarigence.contactapi.dto.SiteDocumentRequest;
import in.clarigence.contactapi.entity.SiteDocument;
import in.clarigence.contactapi.exception.*;
import in.clarigence.contactapi.repository.SiteDocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service @Transactional(readOnly=true)
public class SiteContentService {
    public record Field(String name,String label,String type,int max,boolean required) {}
    public record DocumentView(String key,Map<String,String> content,Long version) {}
    private final SiteDocumentRepository documents;
    private final ObjectMapper json;
    private static final Map<String,List<Field>> SCHEMAS=schemas();
    public SiteContentService(SiteDocumentRepository documents,ObjectMapper json){this.documents=documents;this.json=json;}
    private static Field field(String name,String label,String type,int max,boolean required){return new Field(name,label,type,max,required);}
    private static Map<String,List<Field>> schemas(){
        Map<String,List<Field>> s=new LinkedHashMap<>();
        s.put("home",List.of(field("headline","Hero headline (one line per title line)","textarea",180,true),field("subtitle","Hero subtitle","textarea",1000,true),field("primaryCtaText","Primary CTA label","text",80,true),field("primaryCtaUrl","Primary CTA URL","url",2048,true),field("secondaryCtaText","Secondary CTA label","text",80,true),field("secondaryCtaUrl","Secondary CTA URL","url",2048,true),field("aboutHeading","About preview heading","textarea",200,true),field("aboutText","About preview text","textarea",3000,true),field("ctaHeading","Main CTA heading","textarea",200,true),field("ctaDescription","Main CTA description","textarea",1000,true)));
        s.put("about",List.of(field("heading","Page heading","textarea",200,true),field("intro","Introduction","textarea",1500,true),field("companyDescription","Company description","textarea",5000,true),field("mission","Mission (optional)","textarea",2000,false),field("vision","Vision (optional)","textarea",2000,false),field("valuesIntro","Values introduction","textarea",1500,true)));
        s.put("business",List.of(field("name","Business name","text",120,true),field("tagline","Tagline","text",200,true),field("email","Contact email","email",254,true),field("phone","Phone (not displayed unless enabled)","text",30,false),field("showPhone","Display direct phone number","boolean",5,true),field("whatsapp","WhatsApp number (international digits, no +)","text",20,false),field("address","Address","textarea",1000,false),field("instagram","Instagram URL","url",2048,false),field("linkedin","LinkedIn URL","url",2048,false),field("facebook","Facebook URL","url",2048,false),field("hours","Business hours","text",300,false)));
        List<Field> seo=List.of(field("title","SEO title","text",160,true),field("description","Meta description","textarea",500,true),field("ogTitle","Open Graph title","text",160,false),field("ogDescription","Open Graph description","textarea",500,false),field("ogImage","Open Graph image URL/path","image",2048,false));
        for(String page:List.of("home","about","services","solutions","portfolio","contact"))s.put("seo-"+page,seo);
        return Collections.unmodifiableMap(s);
    }
    public Map<String,List<Field>> schema(){return SCHEMAS;}
    private SiteDocument find(String key){if(!SCHEMAS.containsKey(key))throw new ResourceNotFoundException("Content page not found.");return documents.findById(key).orElseThrow(()->new ResourceNotFoundException("Content page not found."));}
    public Map<String,String> publicContent(String key){Map<String,String> result=new LinkedHashMap<>(read(find(key).getContentJson()));if(key.equals("business")&&!"true".equals(result.get("showPhone")))result.remove("phone");return result;}
    public DocumentView get(String key){SiteDocument d=find(key);return new DocumentView(key,read(d.getContentJson()),d.getVersion());}
    @Transactional public DocumentView save(String key,SiteDocumentRequest request){
        SiteDocument doc=find(key);
        if(!Objects.equals(doc.getVersion(),request.version()))throw new CmsConflictException();
        Map<String,String> values=new LinkedHashMap<>();
        Set<String> allowed=new HashSet<>();
        for(Field f:SCHEMAS.get(key)){
            allowed.add(f.name());String value=request.content().getOrDefault(f.name(),"").trim();
            if((f.required() && value.isBlank())||value.length()>f.max())throw new CmsValidationException(f.name(),"This field is required or exceeds its allowed length.");
            if(f.type().equals("url")||f.type().equals("image"))CmsValidation.url(f.name(),value,f.type().equals("image"));
            if(f.type().equals("email")&&!value.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"))throw new CmsValidationException(f.name(),"Enter a valid email address.");
            if(f.type().equals("boolean")&&!Set.of("true","false").contains(value))throw new CmsValidationException(f.name(),"Choose enabled or disabled.");
            if(f.name().equals("phone")&&!value.isEmpty()&&!value.matches("[+0-9() .-]{7,30}"))throw new CmsValidationException(f.name(),"Enter a valid phone number.");
            if(f.name().equals("whatsapp")&&!value.isEmpty()&&!value.matches("[0-9]{7,20}"))throw new CmsValidationException(f.name(),"Use the international number with digits only.");
            if(f.name().equals("headline")&&(value.split("\\R",-1).length>3 || Arrays.stream(value.split("\\R",-1)).anyMatch(line->line.isBlank()||line.length()>60)))throw new CmsValidationException(f.name(),"Use one to three nonempty lines of up to 60 characters each.");
            values.put(f.name(),value);
        }
        if(!allowed.containsAll(request.content().keySet()))throw new CmsValidationException("content","Unknown content field.");
        doc.setContentJson(write(values));documents.saveAndFlush(doc);return get(key);
    }
    public Map<String,String> read(String value){try{return json.readValue(value,new TypeReference<LinkedHashMap<String,String>>(){});}catch(Exception e){throw new IllegalStateException("Invalid stored content",e);}}
    public String write(Map<String,String> value){try{return json.writeValueAsString(value);}catch(Exception e){throw new IllegalArgumentException("Invalid content",e);}}
}
