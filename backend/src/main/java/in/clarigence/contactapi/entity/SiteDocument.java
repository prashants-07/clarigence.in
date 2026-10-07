package in.clarigence.contactapi.entity;
import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name="site_documents")
public class SiteDocument {
    @Id @Column(length=32) private String documentKey;
    @Column(nullable=false, columnDefinition="LONGTEXT") private String contentJson;
    @Version private Long version;
    @Column(nullable=false, updatable=false) private Instant createdAt;
    @Column(nullable=false) private Instant updatedAt;
    protected SiteDocument(){}
    public SiteDocument(String key,String json){documentKey=key;contentJson=json;}
    @PrePersist void createAudit(){createdAt=Instant.now();updatedAt=createdAt;}
    @PreUpdate void updateAudit(){updatedAt=Instant.now();}
    public String getDocumentKey(){return documentKey;}
    public String getContentJson(){return contentJson;}
    public void setContentJson(String value){contentJson=value;}
    public Long getVersion(){return version;}
    public Instant getCreatedAt(){return createdAt;}
    public Instant getUpdatedAt(){return updatedAt;}
}
