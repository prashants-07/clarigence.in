package in.clarigence.contactapi.dto;
import in.clarigence.contactapi.entity.WebsiteService;
import java.util.List;
public record PublicService(String name,String slug,String shortDescription,String description,String icon,
    String imageUrl,int displayOrder,List<String> capabilities,List<String> benefits,String useCases) {
    public static PublicService from(WebsiteService s){return new PublicService(s.getName(),s.getSlug(),s.getShortDescription(),s.getDescription(),s.getIcon(),s.getImageUrl(),s.getDisplayOrder(),s.getCapabilities(),s.getBenefits(),s.getUseCases());}
}
