package com.smarthouse.repository;

import com.smarthouse.pojo.Category;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(collectionResourceRel = "category", path = "category")
public interface CategoryRepository extends PagingAndSortingRepository<Category, Integer> {

    List<Category> findByNameIgnoreCase(@Param("name") String name);

    List<Category> findByDescriptionIgnoreCase(String description);
    List<Category> findByCategory(Category category);
}
