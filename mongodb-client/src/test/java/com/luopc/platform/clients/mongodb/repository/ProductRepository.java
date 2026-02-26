package com.luopc.platform.clients.mongodb.repository;

import com.luopc.platform.clients.mongodb.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {

    List<Product> findByCategory(String category);

    List<Product> findByPriceLessThan(BigDecimal price);

    List<Product> findByAvailableTrueOrderByPriceAsc();

    Page<Product> findByCategoryAndAvailableTrue(String category, Pageable pageable);

    @Query("{'name': {$regex: ?0, $options: 'i'}}")
    List<Product> findByNameContainingIgnoreCase(String name);

    @Query("{'$and': [{'category': ?0}, {'price': {'$gte': ?1, '$lte': ?2}}]}")
    List<Product> findByCategoryAndPriceBetween(String category, BigDecimal minPrice, BigDecimal maxPrice);

    long countByCategory(String category);
}
