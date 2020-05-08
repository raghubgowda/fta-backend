package com.raghu.fta.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "User")
@Data
public class User {
    @Id
    private String id;
    private String name;
    private String email;
    private String role;
    private String password;
}
