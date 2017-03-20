package com.smarthouse.repository;

import com.smarthouse.pojo.Customer;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "customer", path = "customer")
public interface CustomerRepository extends PagingAndSortingRepository<Customer, String> {

    Customer findByName(@Param("name") String name);

    //Boolean exists(String email);
    //Customer save(Customer customer);
}
