package com.smarthouse.repository;

import com.smarthouse.pojo.AttributeName;
import com.smarthouse.pojo.AttributeValue;
import com.smarthouse.pojo.ProductCard;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.Repository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(collectionResourceRel = "attributeValue", path = "attributeValue")
public interface AttributeValueRepository extends PagingAndSortingRepository<AttributeValue, Integer> {
    List<AttributeValue> findByProductCard(ProductCard productCard);
    List<AttributeValue> findByAttributeName(AttributeName attributeName);
}
