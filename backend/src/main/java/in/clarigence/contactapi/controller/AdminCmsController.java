package in.clarigence.contactapi.controller;
import in.clarigence.contactapi.dto.*;
import in.clarigence.contactapi.service.CmsService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
@RestController @RequestMapping("/api/admin/cms") @Validated
public class AdminCmsController {
    private final CmsService cms;
    public AdminCmsController(CmsService cms){this.cms=cms;}
    @GetMapping("/summary") public Map<String,Long> summary(){return cms.summary();}
    @GetMapping("/{kind}") public List<AdminCmsItem> list(@PathVariable String kind){return cms.list(kind);}
    @GetMapping("/{kind}/{id}") public AdminCmsItem get(@PathVariable String kind,@PathVariable @Min(1) Long id){return cms.get(kind,id);}
    @PostMapping("/{kind}") @ResponseStatus(HttpStatus.CREATED)
    public AdminCmsItem create(@PathVariable String kind,@Valid @RequestBody CmsItemRequest request){return cms.save(kind,null,request);}
    @PutMapping("/{kind}/{id}") public AdminCmsItem update(@PathVariable String kind,@PathVariable @Min(1) Long id,@Valid @RequestBody CmsItemRequest request){return cms.save(kind,id,request);}
    @DeleteMapping("/{kind}/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String kind,@PathVariable @Min(1) Long id){cms.delete(kind,id);}
}
