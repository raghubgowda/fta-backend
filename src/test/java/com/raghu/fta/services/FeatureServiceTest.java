package com.raghu.fta.services;

import com.raghu.fta.dao.FeaturesRepo;
import com.raghu.fta.exceptions.EntityNotFoundException;
import com.raghu.fta.exceptions.ServiceException;
import com.raghu.fta.model.Feature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeatureServiceTest {
    @Mock
    private FeaturesRepo featuresRepo;

    @InjectMocks
    private FeatureService featureService;

    @Test
    void getAllFeaturesReturnsRepositoryResults() {
        List<Feature> features = Collections.singletonList(new Feature());
        when(featuresRepo.findAll()).thenReturn(features);

        assertSame(features, featureService.getAllFeatures());
    }

    @Test
    void getAllFeaturesWrapsRepositoryFailure() {
        when(featuresRepo.findAll()).thenThrow(new RuntimeException("db error"));

        assertThrows(ServiceException.class, () -> featureService.getAllFeatures());
    }

    @Test
    void getFeaturesByFeatureNamesReturnsRepositoryResults() {
        List<String> names = Arrays.asList("Checkout", "Search");
        List<Feature> features = Collections.singletonList(new Feature());
        when(featuresRepo.getFeatureByNames(names)).thenReturn(features);

        assertSame(features, featureService.getFeaturesByFeatureNames(names));
    }

    @Test
    void getFeaturesByFeatureNamesWrapsRepositoryFailure() {
        List<String> names = Collections.singletonList("Checkout");
        when(featuresRepo.getFeatureByNames(names)).thenThrow(new RuntimeException("db error"));

        assertThrows(ServiceException.class,
                () -> featureService.getFeaturesByFeatureNames(names));
    }

    @Test
    void getFeatureByIdReturnsFeatureWhenFound() {
        Feature feature = new Feature();
        when(featuresRepo.findById("feature-1")).thenReturn(Optional.of(feature));

        assertSame(feature, featureService.getFeatureById("feature-1"));
    }

    @Test
    void getFeatureByIdThrowsWhenNotFound() {
        when(featuresRepo.findById("missing")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> featureService.getFeatureById("missing"));
    }

    @Test
    void getFeatureByIdWrapsRepositoryFailure() {
        when(featuresRepo.findById("feature-1"))
                .thenThrow(new RuntimeException("db error"));

        assertThrows(ServiceException.class,
                () -> featureService.getFeatureById("feature-1"));
    }

    @Test
    void saveOrUpdateReturnsSavedFeature() {
        Feature feature = new Feature();
        when(featuresRepo.save(feature)).thenReturn(feature);

        assertSame(feature, featureService.saveOrUpdate(feature));
    }

    @Test
    void saveOrUpdateWrapsRepositoryFailure() {
        Feature feature = new Feature();
        when(featuresRepo.save(feature)).thenThrow(new RuntimeException("db error"));

        assertThrows(ServiceException.class, () -> featureService.saveOrUpdate(feature));
    }

    @Test
    void deleteFeatureDelegatesToRepository() {
        featureService.deleteFeature("feature-1");

        verify(featuresRepo).deleteById("feature-1");
    }

    @Test
    void deleteFeatureWrapsRepositoryFailure() {
        doThrow(new RuntimeException("db error")).when(featuresRepo).deleteById("feature-1");

        assertThrows(ServiceException.class, () -> featureService.deleteFeature("feature-1"));
    }
}
