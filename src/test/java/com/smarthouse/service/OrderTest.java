package com.smarthouse.service;

import com.smarthouse.pojo.ProductCard;
import com.smarthouse.repository.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.NestedServletException;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class OrderTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductCardRepository productCardRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private OrderMainRepository orderMainRepository;
    @Autowired
    private CustomerRepository customerRepository;

    @After
    public void deleteAllAfterTests() throws Exception {
        orderItemRepository.deleteAll();
        orderMainRepository.deleteAll();
        customerRepository.deleteAll();
        productCardRepository.deleteAll();
    }

    @Test
    public void shouldCreateCorrectOrder() throws Exception {

        productCardRepository.save(new ProductCard("xxx", "prodName", 5, 1000, 8 , 10, "prodDesk", null));

        mockMvc.perform(get("/createOrder?email={email}&name={name}&phone={phone}&address={address}&amount={amount}&sku={sku}",
                "kya@bk.ru", "Yuriy", "0503337178", "Nikolaev", "4", "xxx"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.address").value("Nikolaev"))
                .andExpect(jsonPath("$.status").value("1"))
                .andExpect(jsonPath("$.customer.email").value("kya@bk.ru"))
                .andExpect(jsonPath("$.customer.phone").value("0503337178"))
                .andExpect(jsonPath("$.customer.subscribe").value("true"))
                .andExpect(jsonPath("$.customer.name").value("Yuriy"));
    }

    @Test
    public void shouldRightSubmitOrder() throws Exception {

        productCardRepository.save(new ProductCard("xxx", "prodName", 5, 1000, 8 , 10, "prodDesk", null));

        mockMvc.perform(get("/createOrder?email={email}&name={name}&phone={phone}&address={address}&amount={amount}&sku={sku}",
                "kya@bk.ru", "Yuriy", "0503337178", "Nikolaev", "4", "xxx"));

        mockMvc.perform(get("/submitOrder?email={email}", "kya@bk.ru"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("2"));

        Assert.assertThat(productCardRepository.findOne("xxx").getAmount(), is(equalTo(996)));
    }

    @Test(expected = NestedServletException.class)
    public void shouldThrowExceptionWhenAddIncorrectPhoneInCreateEntityTime() throws Exception {

        productCardRepository.save(new ProductCard("xxx", "prodName", 5, 1000, 8 , 10, "prodDesk", null));

        mockMvc.perform(get("/createOrder?email={email}&name={name}&phone={phone}&address={address}&amount={amount}&sku={sku}",
                "kya@bk.ru", "Yuriy", "0c503337178", "Nikolaev", "4", "xxx"));
    }

    @Test(expected = NestedServletException.class)
    public void shouldThrowExceptionWhenAddIncorrectNameOfCustomer() throws Exception {

        productCardRepository.save(new ProductCard("xxx", "prodName", 5, 1000, 8 , 10, "prodDesk", null));

        mockMvc.perform(get("/createOrder?email={email}&name={name}&phone={phone}&address={address}&amount={amount}&sku={sku}",
                "kya@bk.ru", "Yu1riy", "0503337178", "Nikolaev", "4", "xxx"));
    }
}