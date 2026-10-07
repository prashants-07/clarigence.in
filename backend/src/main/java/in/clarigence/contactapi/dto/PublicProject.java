package in.clarigence.contactapi.dto;
import in.clarigence.contactapi.entity.PortfolioProject;
import java.util.List;
public record PublicProject(String title,String slug,String category,String shortDescription,String description,
    String imageUrl,String projectUrl,List<String> technologies,int displayOrder,boolean featured,boolean concept) {
    public static PublicProject from(PortfolioProject p){return new PublicProject(p.getName(),p.getSlug(),p.getCategory(),p.getShortDescription(),p.getDescription(),p.getImageUrl(),p.getProjectUrl(),p.getTechnologies(),p.getDisplayOrder(),p.isFeatured(),p.isConcept());}
}
