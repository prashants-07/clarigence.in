package in.clarigence.contactapi.entity;
import jakarta.persistence.*;
import java.util.List;

@Entity @Table(name="website_services")
public class WebsiteService extends CmsItem {
    @Convert(converter=StringListConverter.class) @Column(columnDefinition="TEXT")
    private List<String> capabilities=List.of();
    @Convert(converter=StringListConverter.class) @Column(columnDefinition="TEXT")
    private List<String> benefits=List.of();
    @Column(columnDefinition="TEXT") private String useCases;
    public List<String> getCapabilities(){return capabilities;}
    public void setCapabilities(List<String> value){capabilities=value;}
    public List<String> getBenefits(){return benefits;}
    public void setBenefits(List<String> value){benefits=value;}
    public String getUseCases(){return useCases;}
    public void setUseCases(String value){useCases=value;}
}
