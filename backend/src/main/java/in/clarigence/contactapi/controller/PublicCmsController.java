package in.clarigence.contactapi.controller;
import in.clarigence.contactapi.dto.*;
import in.clarigence.contactapi.service.CmsService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api")
public class PublicCmsController {
    private final CmsService cms;
    public PublicCmsController(CmsService cms){this.cms=cms;}
    @GetMapping("/services") public List<ServiceSummary> services(){return cms.publicServices();}
    @GetMapping("/services/{slug}") public PublicService service(@PathVariable String slug){return cms.publicService(slug);}
    @GetMapping("/portfolio") public List<PublicProject> projects(){return cms.publicProjects();}
    @GetMapping("/portfolio/{slug}") public PublicProject project(@PathVariable String slug){return cms.publicProject(slug);}
}
