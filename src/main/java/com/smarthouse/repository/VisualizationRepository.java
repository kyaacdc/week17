package com.smarthouse.repository;

import com.smarthouse.pojo.ProductCard;
import com.smarthouse.pojo.Visualization;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(collectionResourceRel = "visualization", path = "visualization")
public interface VisualizationRepository extends PagingAndSortingRepository<Visualization, Integer> {

    List<Visualization> findByProductCard(ProductCard productCard);
}
