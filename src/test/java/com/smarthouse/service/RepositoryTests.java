package com.smarthouse.service;

import com.smarthouse.pojo.Customer;
import com.smarthouse.pojo.ProductCard;
import com.smarthouse.repository.CustomerRepository;
import com.smarthouse.repository.ProductCardRepository;
import org.junit.*;
import org.junit.runner.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.*;
import org.springframework.test.context.junit4.SpringRunner;
import static org.assertj.core.api.Assertions.*;

@RunWith(SpringRunner.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace= AutoConfigureTestDatabase.Replace.NONE)
public class RepositoryTests {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductCardRepository productCardRepository;

    @Test
    public void shouldSaveRemoveCustomer() throws Exception {
        this.entityManager.persist(new Customer("kya@bk.ru", "sboot", true, "1234"));
        Customer customer = this.customerRepository.findByName("sboot").get(0);
        assertThat(customer.getName()).isEqualTo("sboot");
        assertThat(customer.getPhone()).isEqualTo("1234");
        entityManager.remove(customer);
    }

    @Test
    public void shouldSaveRemoveProductCard() throws Exception {
        this.entityManager.persist(new ProductCard("prodSku", "prodName", 1234, 5, 6, 7, "desc", null));
        ProductCard productCard = this.productCardRepository.findByNameIgnoreCase("prodName").get(0);
        assertThat(productCard.getProductDescription()).isEqualTo("desc");
        assertThat(productCard.getPrice()).isEqualTo(1234);
        entityManager.remove(productCard);
    }
}