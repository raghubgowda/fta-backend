package com.raghu.fta.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "Customer")
@Data
public class Customer {
    @Id
    private String id;
    private String name;
    private String description;
    private List<String> features;
}
