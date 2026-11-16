package com.raghu.fta.restcontrollers;

import com.raghu.fta.model.Feature;
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
class FeaturesControllerTest {
    @Mock
    private FeatureService featureService;

    @InjectMocks
    private FeaturesController controller;

    @Test
    void getFeaturesReturnsOk() {
        List<Feature> features = Arrays.asList(new Feature(), new Feature());
        when(featureService.getAllFeatures()).thenReturn(features);

        ResponseEntity<?> response = controller.getFeatures();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(features, response.getBody());
    }

    @Test
    void getFeatureByIdReturnsOk() {
        Feature feature = new Feature();
        feature.setId("f-1");
        when(featureService.getFeatureById("f-1")).thenReturn(feature);

        ResponseEntity<?> response = controller.getFeatureById("f-1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(feature, response.getBody());
    }

    @Test
    void addFeatureRejectsExplicitId() {
        Feature feature = new Feature();
        feature.setId("f-1");

        ResponseEntity<?> response = controller.addFeature(feature);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Feature id should be null", response.getBody());
        verifyNoInteractions(featureService);
    }

    @Test
    void addFeatureReturnsCreatedFeature() {
        Feature feature = new Feature();
        when(featureService.saveOrUpdate(feature)).thenReturn(feature);

        ResponseEntity<?> response = controller.addFeature(feature);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(feature, response.getBody());
    }

    @Test
    void updateFeatureReturnsUpdatedFeature() {
        Feature feature = new Feature();
        when(featureService.saveOrUpdate(feature)).thenReturn(feature);

        ResponseEntity<?> response = controller.updateFeature(feature);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(feature, response.getBody());
    }

    @Test
    void deleteFeatureReturnsNoContent() {
        ResponseEntity<?> response = controller.deleteFeature("f-1");

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(featureService).deleteFeature("f-1");
    }
}
