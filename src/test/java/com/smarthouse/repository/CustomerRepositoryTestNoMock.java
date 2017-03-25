package com.smarthouse.repository;

import com.smarthouse.pojo.Customer;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.TransactionSystemException;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CustomerRepositoryTestNoMock {

    @Autowired
    private CustomerRepository customerRepository;

    @After
    public void before() {
        customerRepository.deleteAll();
    }

    @Test
    public void shouldCorrectCreateCustomerAndAddIntoDb() throws Exception {
        Customer customer = new Customer("kya@bk.ru", "Yuriy", true, "0503337178");
        customerRepository.save(customer);
        assertThat(customerRepository.findOne("kya@bk.ru").getName(), is (equalTo("Yuriy")));
    }

    @Test(expected = TransactionSystemException.class)
    public void shouldThrowExceptionWhenAddIncorrectPhoneInCreateEntityTime() throws Exception {

        Customer customer = new Customer("kya@bk.ru", "Yuriy", true, "050t3337178");
        customerRepository.save(customer);
    }

    @Test(expected = TransactionSystemException.class)
    public void shouldThrowExceptionWhenAddIncorrectNameOfCustomer() throws Exception {

        Customer customer = new Customer("kya@bk.ru", "Yur7iy", true, "0503337178");
        customerRepository.save(customer);
    }
}