package com.raghu.fta.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

@Data
@Document(collection = "Feature")
public class Feature {
    @Id
    private String id;
    private String displayName;
    private String technicalName;
    private Date expiresOn;
    private String description;
    private Boolean value;
    private Date createdOn;
    private Date modifiedOn;
    private String createdBy;
    private String modifiedBy;
}
