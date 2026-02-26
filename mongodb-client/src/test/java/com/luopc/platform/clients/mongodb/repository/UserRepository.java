package com.luopc.platform.clients.mongodb.repository;


import com.luopc.platform.clients.mongodb.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByUsername(String username);

    List<User> findByAgeGreaterThan(Integer age);

    List<User> findByActiveTrue();

    List<User> findByFullNameContainingIgnoreCase(String fullName);

    @Query("{'age': {$gte: ?0, $lte: ?1}}")
    List<User> findByAgeBetween(Integer minAge, Integer maxAge);

    long countByActive(Boolean active);
}
