package com.greentrack.carbon_tracker_api.repositories;

import com.greentrack.carbon_tracker_api.entities.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    // Find by email (for login)
    Optional<User> findByEmail(String email);

    // check if email exists (for registration)
    boolean existsByEmail(String email);

    // Find active users
    List<User> findByIsActive(boolean isActive);

    // Find by region
    List<User> findByRegion(String region);

    // Custom query to find users by partial name match
//    List<User> findByNameContaining(String name);

    // Count active users
    @Query("{$or: [" +
            "{ 'firstName': {$regex: ?0, $options: 'i'} }, " +
            "{ 'lastName': {$regex: ?0, $options: 'i'} }, " +
            "]}")
    long countByIsActive(boolean isActive);

}
