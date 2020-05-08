package com.raghu.fta.dao;

import com.raghu.fta.model.Feature;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface FeaturesRepo extends MongoRepository<Feature, String> {
    @Query("{ $and:[ { 'expiresOn' : {$gte: new Date()} }, { 'displayName' : {$in: ?0} } ] }")
    List<Feature> getFeatureByNames(List<String> names);
}
