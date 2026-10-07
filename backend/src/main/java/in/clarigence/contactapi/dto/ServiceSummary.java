package in.clarigence.contactapi.dto;
import in.clarigence.contactapi.entity.WebsiteService;
public record ServiceSummary(String name,String slug,String shortDescription,String icon,String imageUrl,int displayOrder) {
    public static ServiceSummary from(WebsiteService s){return new ServiceSummary(s.getName(),s.getSlug(),s.getShortDescription(),s.getIcon(),s.getImageUrl(),s.getDisplayOrder());}
}
