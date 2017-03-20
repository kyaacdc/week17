package com.smarthouse.repository;

import com.smarthouse.pojo.AttributeName;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "attributeName", path = "attributeName")
public interface AttributeNameRepository extends PagingAndSortingRepository<AttributeName, String> {
}
