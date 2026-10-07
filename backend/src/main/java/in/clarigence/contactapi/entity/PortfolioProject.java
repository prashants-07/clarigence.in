package in.clarigence.contactapi.entity;
import jakarta.persistence.*;
import java.util.List;

@Entity @Table(name="portfolio_projects")
public class PortfolioProject extends CmsItem {
    @Column(length=120) private String category;
    @Column(length=2048) private String projectUrl;
    @Column(nullable=false) private boolean featured;
    @Column(nullable=false) private boolean concept=true;
    @Convert(converter=StringListConverter.class) @Column(columnDefinition="TEXT")
    private List<String> technologies=List.of();
    public String getCategory(){return category;}
    public void setCategory(String value){category=value;}
    public String getProjectUrl(){return projectUrl;}
    public void setProjectUrl(String value){projectUrl=value;}
    public boolean isFeatured(){return featured;}
    public void setFeatured(boolean value){featured=value;}
    public boolean isConcept(){return concept;}
    public void setConcept(boolean value){concept=value;}
    public List<String> getTechnologies(){return technologies;}
    public void setTechnologies(List<String> value){technologies=value;}
}
