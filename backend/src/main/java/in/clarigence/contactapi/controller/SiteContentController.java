package in.clarigence.contactapi.controller;
import in.clarigence.contactapi.dto.SiteDocumentRequest;
import in.clarigence.contactapi.service.SiteContentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
@RestController
public class SiteContentController {
    private final SiteContentService content;
    public SiteContentController(SiteContentService content){this.content=content;}
    @GetMapping("/api/content/{key}") public Map<String,String> publicContent(@PathVariable String key){return content.publicContent(key);}
    @GetMapping("/api/admin/content/schema") public Map<String,List<SiteContentService.Field>> schema(){return content.schema();}
    @GetMapping("/api/admin/content/{key}") public SiteContentService.DocumentView get(@PathVariable String key){return content.get(key);}
    @PutMapping("/api/admin/content/{key}") public SiteContentService.DocumentView save(@PathVariable String key,@Valid @RequestBody SiteDocumentRequest request){return content.save(key,request);}
}
