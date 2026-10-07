package in.clarigence.contactapi.exception;
import java.util.Map;
public class CmsValidationException extends RuntimeException {
    private final Map<String,String> fields;
    public CmsValidationException(String field,String message){super("Please check the submitted content.");fields=Map.of(field,message);}
    public Map<String,String> getFields(){return fields;}
}
