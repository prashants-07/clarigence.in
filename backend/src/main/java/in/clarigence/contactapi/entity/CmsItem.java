package in.clarigence.contactapi.entity;

import jakarta.persistence.*;
import java.time.Instant;

@MappedSuperclass
public abstract class CmsItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Version private Long version;
    @Column(nullable=false, length=120) private String name;
    @Column(nullable=false, unique=true, length=120) private String slug;
    @Column(nullable=false, length=600) private String shortDescription;
    @Column(nullable=false, columnDefinition="TEXT") private String description;
    @Column(nullable=false, length=24) private String icon = "window";
    @Column(length=2048) private String imageUrl;
    @Column(nullable=false) private int displayOrder;
    @Column(nullable=false) private boolean active;
    @Column(nullable=false, updatable=false) private Instant createdAt;
    @Column(nullable=false) private Instant updatedAt;

    @PrePersist void createAudit() { createdAt=Instant.now(); updatedAt=createdAt; }
    @PreUpdate void updateAudit() { updatedAt=Instant.now(); }
    public Long getId(){return id;}
    public Long getVersion(){return version;}
    public String getName(){return name;}
    public void setName(String value){name=value;}
    public String getSlug(){return slug;}
    public void setSlug(String value){slug=value;}
    public String getShortDescription(){return shortDescription;}
    public void setShortDescription(String value){shortDescription=value;}
    public String getDescription(){return description;}
    public void setDescription(String value){description=value;}
    public String getIcon(){return icon;}
    public void setIcon(String value){icon=value;}
    public String getImageUrl(){return imageUrl;}
    public void setImageUrl(String value){imageUrl=value;}
    public int getDisplayOrder(){return displayOrder;}
    public void setDisplayOrder(int value){displayOrder=value;}
    public boolean isActive(){return active;}
    public void setActive(boolean value){active=value;}
    public Instant getCreatedAt(){return createdAt;}
    public Instant getUpdatedAt(){return updatedAt;}
}
