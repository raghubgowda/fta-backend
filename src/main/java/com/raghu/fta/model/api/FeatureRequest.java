package com.raghu.fta.model.api;

import lombok.Data;

@Data
public class FeatureRequest {
    private String customerId;
    private FeatureName[] features;
}
