package com.raghu.fta.restcontrollers;

import com.raghu.fta.model.Feature;
import com.raghu.fta.services.FeatureService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/features")
@CrossOrigin(origins = "http://localhost:4200")
public class FeaturesController {

    private FeatureService featureService;
    private static Logger logger = LoggerFactory.getLogger(FeaturesController.class);
    
    public FeaturesController(FeatureService featureService){ this.featureService = featureService; }

    @GetMapping()
    ResponseEntity<?> getFeatures(){
        logger.debug("Fetching all the features");

        List<Feature> features = featureService.getFeaturesByFeatureNames();

        logger.debug("Fetched all the features");

        return new ResponseEntity<>(features, HttpStatus.OK);
    }

    @GetMapping("/{featureId}")
    ResponseEntity<?> getFeatureById(@PathVariable String featureId){
        logger.debug("Fetching the details for the feature with id: {}", featureId);

        Feature featureById = featureService.getFeatureById(featureId);

        logger.debug("Feature with id: {} is found", featureById);

        return new ResponseEntity<>(featureById, HttpStatus.OK);
    }

    @PostMapping()
    ResponseEntity<?> addFeature(@RequestBody Feature feature){
        logger.debug("Adding feature: {}", feature);

        if(feature.getId() != null){
            return new ResponseEntity<>("Feature id should be null", HttpStatus.BAD_REQUEST);
        }

        Feature newFeature = featureService.saveOrUpdate(feature);

        logger.debug("Feature added successfully {}", newFeature);

        return new ResponseEntity<>(newFeature, HttpStatus.OK);
    }

    @PutMapping()
    ResponseEntity<?> updateFeature(@RequestBody Feature feature){
        logger.debug("Updating the feature: {}", feature);

        Feature updatedFeature = featureService.saveOrUpdate(feature);

        logger.debug("Feature updated successfully");

        return new ResponseEntity<>(updatedFeature, HttpStatus.OK);
    }

    @DeleteMapping("/{featureId}")
    ResponseEntity<?> deleteFeature(@PathVariable String featureId){
        logger.debug("Deleting the feature with id: {}", featureId);

        featureService.deleteFeature(featureId);

        logger.debug("Feature with id: {} deleted successfully", featureId);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
