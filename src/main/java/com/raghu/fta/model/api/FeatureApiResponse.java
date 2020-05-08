package com.raghu.fta.model.api;

import com.raghu.fta.model.Feature;
import lombok.Data;

@Data
public class FeatureApiResponse {
    private String name;
    private boolean active;
    private boolean inverted ;

    public FeatureApiResponse(String name){
        this.name = name;
        this.active = false;
        this.inverted = false;
    }

    public FeatureApiResponse(Feature feature){
        this.name = feature.getDisplayName();
        this.active = feature.getValue();
        this.inverted = !feature.getValue();
    }

    public FeatureApiResponse(){}
}
