package com.amalitech.org.productservice.repository;

import com.amalitech.org.productservice.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductRepository extends MongoRepository<Product,String> {
}
