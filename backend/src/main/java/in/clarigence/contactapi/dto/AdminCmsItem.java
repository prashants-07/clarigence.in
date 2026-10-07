package in.clarigence.contactapi.dto;
import in.clarigence.contactapi.entity.*;
import java.time.Instant;
import java.util.List;
public record AdminCmsItem(Long id,Long version,String name,String slug,String shortDescription,String description,
    String icon,String imageUrl,int displayOrder,boolean active,List<String> capabilities,List<String> benefits,
    String useCases,String category,List<String> technologies,String projectUrl,boolean featured,boolean concept,
    Instant createdAt,Instant updatedAt) {
    public static AdminCmsItem from(CmsItem item){
        WebsiteService s=item instanceof WebsiteService value?value:null;
        PortfolioProject p=item instanceof PortfolioProject value?value:null;
        return new AdminCmsItem(item.getId(),item.getVersion(),item.getName(),item.getSlug(),item.getShortDescription(),item.getDescription(),item.getIcon(),item.getImageUrl(),item.getDisplayOrder(),item.isActive(),s==null?List.of():s.getCapabilities(),s==null?List.of():s.getBenefits(),s==null?null:s.getUseCases(),p==null?null:p.getCategory(),p==null?List.of():p.getTechnologies(),p==null?null:p.getProjectUrl(),p!=null&&p.isFeatured(),p!=null&&p.isConcept(),item.getCreatedAt(),item.getUpdatedAt());
    }
}
