package com.raghu.fta.services;

import com.raghu.fta.dao.FeaturesRepo;
import com.raghu.fta.exceptions.EntityNotFoundException;
import com.raghu.fta.exceptions.ServiceException;
import com.raghu.fta.model.Feature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class FeatureService {
    private final FeaturesRepo featuresRepo;
    private static Logger logger = LoggerFactory.getLogger(FeatureService.class);

    public FeatureService(FeaturesRepo featuresRepo) {
        this.featuresRepo = featuresRepo;
    }
    
    public List<Feature> getFeaturesByFeatureNames() {
        List<Feature> features;
        try{
            features = featuresRepo.findAll();
        }
        catch (Exception e){
            logger.error(e.getMessage());
            throw new ServiceException("Exception while fetching the features from the database. Error: "+e.getMessage());
        }
        return features;
    }

    public List<Feature> getFeaturesByFeatureNames(List<String> featureNames) {
        List<Feature> features;
        try{
            features = featuresRepo.getFeatureByNames(featureNames);
        }
        catch (Exception e){
            logger.error(e.getMessage());
            throw new ServiceException("Exception while fetching the features from the database. Error: "+e.getMessage());
        }
        return features;
    }

    public Feature getFeatureById(String featureId) {
        Feature feature;
        Optional<Feature> featureOptional;

        try{
            featureOptional = featuresRepo.findById(featureId);
        }
        catch (Exception e){
            logger.error(e.getMessage());
            throw new ServiceException("Exception while fetching the feature with id "+featureId+". Error: "+e.getMessage());
        }

        if(featureOptional.isPresent()){
            feature = featureOptional.get();
        }
        else{
            throw new EntityNotFoundException("Feature with id "+featureId+" not found");
        }
        return feature;
    }

    public Feature saveOrUpdate(Feature feature) {
        Feature updatedFeature;
        try{
            updatedFeature = featuresRepo.save(feature);
        }
        catch (Exception e){
            logger.error(e.getMessage());
            throw new ServiceException("Exception while updating the feature details. Error: "+e.getMessage());
        }
        return updatedFeature;
    }

    public void deleteFeature(String featureId) {
        try{
            featuresRepo.deleteById(featureId);
        }
        catch (Exception e){
            logger.error(e.getMessage());
            throw new ServiceException("Exception while deleting the feature with id "+featureId+". Error: "+e.getMessage());
        }
    }
}
