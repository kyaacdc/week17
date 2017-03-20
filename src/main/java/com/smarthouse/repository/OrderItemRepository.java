package com.smarthouse.repository;

import com.smarthouse.pojo.OrderItem;
import com.smarthouse.pojo.OrderMain;
import com.smarthouse.pojo.ProductCard;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(collectionResourceRel = "orderItem", path = "orderItem")
public interface OrderItemRepository extends PagingAndSortingRepository<OrderItem, Integer> {

    List<OrderItem> findByOrderMain(OrderMain orderMain);

    List<OrderItem> findByProductCard(ProductCard productCard);

}
