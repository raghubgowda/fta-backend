package com.raghu.fta.model;

import lombok.Data;

@Data
public class AuthToken {

    private String token;
    private String email;
    private String role;

    public AuthToken(String token, String email, String role){
        this.token = token;
        this.email = email;
        this.role = role;
    }
}
