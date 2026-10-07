package in.clarigence.contactapi.service;
import in.clarigence.contactapi.exception.CmsValidationException;
import java.net.URI;

public final class CmsValidation {
    private CmsValidation(){}
    public static void url(String field,String value,boolean image){
        if(value==null || value.isBlank())return;
        if(!value.equals(value.trim()) || value.matches(".*[\\s\\\\<>\"].*"))fail(field);
        try {
            URI uri=URI.create(value);
            if(uri.isAbsolute()){
                if(!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost()==null || uri.getUserInfo()!=null)fail(field);
                if(uri.getFragment()!=null && image)fail(field);
                if(image && !uri.getPath().toLowerCase().matches(".*\\.(png|jpe?g|webp|gif|avif)$"))fail(field);
            } else {
                if(value.startsWith("//") || value.contains("..") || value.contains("%") || uri.getQuery()!=null || (image && uri.getFragment()!=null))fail(field);
                if(image && !value.matches("/?assets/[a-zA-Z0-9_./-]+\\.(png|jpg|jpeg|webp|gif|avif)"))fail(field);
                if(!image && !value.matches("(?:[a-zA-Z0-9/_-]+(?:\\.html)?)?(?:#[a-zA-Z0-9_-]+)?"))fail(field);
            }
        } catch(IllegalArgumentException error){fail(field);}
    }
    private static void fail(String field){throw new CmsValidationException(field,"Use a safe local asset/page path or an absolute HTTPS URL.");}
}
