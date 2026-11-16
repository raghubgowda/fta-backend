package com.raghu.fta.restcontrollers.api;

import com.raghu.fta.model.Customer;
import com.raghu.fta.model.Feature;
import com.raghu.fta.model.api.FeatureApiRequest;
import com.raghu.fta.model.api.FeatureApiResponse;
import com.raghu.fta.model.api.FeatureName;
import com.raghu.fta.model.api.FeatureRequest;
import com.raghu.fta.services.CustomerService;
import com.raghu.fta.services.FeatureService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeaturesAPITest {
    @Mock
    private FeatureService featureService;

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private FeaturesAPI api;

    @Test
    void getFeaturesReturnsFeatureStatesForCustomer() {
        Customer customer = new Customer();
        customer.setId("c-1");
        customer.setFeatures(Arrays.asList("checkout", "search"));

        Feature feature = new Feature();
        feature.setDisplayName("Checkout");
        feature.setValue(true);

        FeatureApiRequest request = new FeatureApiRequest();
        FeatureRequest featureRequest = new FeatureRequest();
        featureRequest.setCustomerId("c-1");
        FeatureName[] names = new FeatureName[]{featureName("checkout"), featureName("search")};
        featureRequest.setFeatures(names);
        request.setFeatureRequest(featureRequest);

        when(customerService.getCustomerById("c-1")).thenReturn(customer);
        when(featureService.getFeaturesByFeatureNames(customer.getFeatures())).thenReturn(Collections.singletonList(feature));

        ResponseEntity<?> response = api.getFeatures(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<FeatureApiResponse> body = (List<FeatureApiResponse>) response.getBody();
        assertNotNull(body);
        assertEquals(2, body.size());
        assertEquals("Checkout", body.get(0).getName());
        assertTrue(body.get(0).isActive());
        assertFalse(body.get(0).isInverted());
    }

    @Test
    void getFeaturesReturnsDefaultResponseForUnknownFeature() {
        Customer customer = new Customer();
        customer.setId("c-1");
        customer.setFeatures(Collections.singletonList("checkout"));

        FeatureApiRequest request = new FeatureApiRequest();
        FeatureRequest featureRequest = new FeatureRequest();
        featureRequest.setCustomerId("c-1");
        featureRequest.setFeatures(new FeatureName[]{featureName("missing")});
        request.setFeatureRequest(featureRequest);

        when(customerService.getCustomerById("c-1")).thenReturn(customer);
        when(featureService.getFeaturesByFeatureNames(customer.getFeatures())).thenReturn(Collections.emptyList());

        ResponseEntity<?> response = api.getFeatures(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<FeatureApiResponse> body = (List<FeatureApiResponse>) response.getBody();
        assertEquals("missing", body.get(0).getName());
        assertFalse(body.get(0).isActive());
        assertFalse(body.get(0).isInverted());
    }

    private FeatureName featureName(String value) {
        FeatureName featureName = new FeatureName();
        featureName.setName(value);
        return featureName;
    }
}
