package com.smarthouse.repository;

import com.smarthouse.pojo.Customer;
import com.smarthouse.pojo.OrderMain;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(collectionResourceRel = "orderMain", path = "orderMain")
public interface OrderMainRepository extends PagingAndSortingRepository<OrderMain, Integer> {

    List<OrderMain> findByAddressIgnoreCase(@Param("address") String address);

    List<OrderMain> findByCustomer(Customer customer);
}
