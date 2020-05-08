package com.raghu.fta.restcontrollers.api;

import com.raghu.fta.model.Customer;
import com.raghu.fta.model.api.FeatureApiRequest;
import com.raghu.fta.model.api.FeatureApiResponse;
import com.raghu.fta.model.api.FeatureRequest;
import com.raghu.fta.services.CustomerService;
import com.raghu.fta.services.FeatureService;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/public/api/v1/features")
public class FeaturesAPI {

    private FeatureService featureService;
    private CustomerService customerService;
    private static Logger logger = LoggerFactory.getLogger(FeaturesAPI.class);
    
    public FeaturesAPI(FeatureService featureService, CustomerService customerService){
        this.featureService = featureService;
        this.customerService = customerService;
    }

    @PostMapping()
    ResponseEntity<?> getFeatures(@RequestBody FeatureApiRequest request){
        logger.debug("Fetching all the features for the request: {}", request);
        FeatureRequest featureRequest = request.getFeatureRequest();
        List<String> featureNames = new ArrayList<>();
        List<FeatureApiResponse> featureApiResponseList;
        Customer customerById = customerService.getCustomerById(featureRequest.getCustomerId());

        List<String> customerFeatures = customerById.getFeatures();

        Arrays.asList(featureRequest.getFeatures()).forEach(featureName -> {
            if(!Strings.isEmpty(featureName.getName()))
                featureNames.add(featureName.getName());
        });

        Map<String, FeatureApiResponse> featureMap = featureService.getFeaturesByFeatureNames(customerFeatures)
                .stream()
                .collect(Collectors.toMap(feature
                        -> feature.getDisplayName().toLowerCase(), FeatureApiResponse::new));

        featureApiResponseList = new ArrayList<>();
        featureNames.forEach(featureName -> featureApiResponseList.add(featureMap.getOrDefault(featureName.toLowerCase(), new FeatureApiResponse(featureName))));

        logger.debug("Fetched all the features");

        return new ResponseEntity<>(featureApiResponseList, HttpStatus.OK);
    }
}
