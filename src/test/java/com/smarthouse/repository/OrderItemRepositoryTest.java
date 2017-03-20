package com.smarthouse.repository;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class OrderItemRepositoryTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderMainRepository orderMainRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @After
    public void deleteAllBeforeTests() throws Exception {
        orderItemRepository.deleteAll();
        orderMainRepository.deleteAll();
        customerRepository.deleteAll();
    }

    @Test
    public void shouldReturnRepositoryIndex() throws Exception {

        mockMvc.perform(get("/")).andDo(print()).andExpect(status().isOk()).andExpect(
                jsonPath("$._links.orderItem").exists());
    }

    @Test
    public void shouldCreateEntity() throws Exception {

        MvcResult mvcResult = mockMvc.perform(post("/customer").content(
                "{\"email\": \"kya@bk.ru\", \"name\":\"Yuriy\", \"phone\":\"0503337178\", \"subscribe\":\"true\"}")).andExpect(
                status().isCreated()).andExpect(header().string("Location", containsString("customer/")))
                .andReturn();

        String locationOfCustomer = mvcResult.getResponse().getHeader("Location");
        String postRequest = "{\"address\": \"Nikolaev\", \"status\":\"1\", \"customer\":\""+ locationOfCustomer +"\"}";

        mvcResult = mockMvc.perform(post("/orderMain").content(postRequest)).andExpect(
                status().isCreated()).andReturn();

        String locationOfOrderMain = mvcResult.getResponse().getHeader("Location");


        String postRequestProduct = "{\"sku\": \"qwerty\", \"amount\":\"10\", \"dislikes\":\"5\", " +
                "\"likes\":\"6\", \"name\":\"prodName\", \"price\":\"50\", " +
                "\"productDescription\":\"prodDesc\", \"category\": null}";

        mvcResult = mockMvc.perform(post("/productCard").content(postRequestProduct)).andExpect(
                status().isCreated()).andReturn();

        String locationProduct = mvcResult.getResponse().getHeader("Location");

        postRequest = "{\"amount\": \"1000\", \"totalprice\":\"10\", \"orderMain\":\"" + locationOfOrderMain + "\", \"productCard\":\"" + locationProduct + "\"}";

        //test
        mockMvc.perform(post("/orderItem").content(postRequest)).andExpect(
                status().isCreated()).andExpect(
                header().string("Location", containsString("orderItem/")));
    }

    @Test
    public void shouldRetrieveEntity() throws Exception {

        MvcResult mvcResult = mockMvc.perform(post("/customer").content(
                "{\"email\": \"kya@bk.ru\", \"name\":\"Yuriy\", \"phone\":\"0503337178\", \"subscribe\":\"true\"}")).andExpect(
                status().isCreated()).andExpect(header().string("Location", containsString("customer/")))
                .andReturn();

        String locationOfCustomer = mvcResult.getResponse().getHeader("Location");
        String postRequest = "{\"address\": \"Nikolaev\", \"status\":\"1\", \"customer\":\""+ locationOfCustomer +"\"}";

        mvcResult = mockMvc.perform(post("/orderMain").content(postRequest)).andExpect(
                status().isCreated()).andReturn();

        String locationOfOrderMain = mvcResult.getResponse().getHeader("Location");


        String postRequestProduct = "{\"sku\": \"qwerty\", \"amount\":\"10\", \"dislikes\":\"5\", " +
                "\"likes\":\"6\", \"name\":\"prodName\", \"price\":\"50\", " +
                "\"productDescription\":\"prodDesc\", \"category\": null}";

        mvcResult = mockMvc.perform(post("/productCard").content(postRequestProduct)).andExpect(
                status().isCreated()).andReturn();

        String locationProduct = mvcResult.getResponse().getHeader("Location");

        postRequest = "{\"amount\": \"1000\", \"totalprice\":\"10\", \"orderMain\":\"" + locationOfOrderMain + "\", \"productCard\":\"" + locationProduct + "\"}";

        MvcResult result = mockMvc.perform(post("/orderItem").content(postRequest)).andExpect(
                status().isCreated()).andReturn();

        String location = result.getResponse().getHeader("Location");

        //test
        mockMvc.perform(get(location)).andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value("1000"))
                .andExpect(jsonPath("$.totalprice").value("10"));
    }

    @Test
    public void shouldUpdateEntity() throws Exception {

        MvcResult mvcResult = mockMvc.perform(post("/customer").content(
                "{\"email\": \"kya@bk.ru\", \"name\":\"Yuriy\", \"phone\":\"0503337178\", \"subscribe\":\"true\"}")).andExpect(
                status().isCreated()).andExpect(header().string("Location", containsString("customer/")))
                .andReturn();

        String locationOfCustomer = mvcResult.getResponse().getHeader("Location");
        String postRequest = "{\"address\": \"Nikolaev\", \"status\":\"1\", \"customer\":\""+ locationOfCustomer +"\"}";

        mvcResult = mockMvc.perform(post("/orderMain").content(postRequest)).andExpect(
                status().isCreated()).andReturn();

        String locationOfOrderMain = mvcResult.getResponse().getHeader("Location");


        String postRequestProduct = "{\"sku\": \"qwerty\", \"amount\":\"10\", \"dislikes\":\"5\", " +
                "\"likes\":\"6\", \"name\":\"prodName\", \"price\":\"50\", " +
                "\"productDescription\":\"prodDesc\", \"category\": null}";

        mvcResult = mockMvc.perform(post("/productCard").content(postRequestProduct)).andExpect(
                status().isCreated()).andReturn();

        String locationProduct = mvcResult.getResponse().getHeader("Location");

        postRequest = "{\"amount\": \"1000\", \"totalprice\":\"10\", \"orderMain\":\"" + locationOfOrderMain + "\", \"productCard\":\"" + locationProduct + "\"}";

        MvcResult result = mockMvc.perform(post("/orderItem").content(postRequest)).andExpect(
                status().isCreated()).andReturn();

        String location = result.getResponse().getHeader("Location");

        //test
        postRequest = "{\"amount\": \"2000\", \"totalprice\":\"20\", \"orderMain\":\"" + locationOfOrderMain + "\", \"productCard\":\"" + locationProduct + "\"}";

        mockMvc.perform(put(location).content(postRequest)).andExpect(
                status().isNoContent());

        mockMvc.perform(get(location)).andExpect(status().isOk()).andExpect(
                jsonPath("$.amount").value("2000"));
    }

    @Test
    public void shouldPartiallyUpdateEntity() throws Exception {

        MvcResult mvcResult = mockMvc.perform(post("/customer").content(
                "{\"email\": \"kya@bk.ru\", \"name\":\"Yuriy\", \"phone\":\"0503337178\", \"subscribe\":\"true\"}")).andExpect(
                status().isCreated()).andExpect(header().string("Location", containsString("customer/")))
                .andReturn();

        String locationOfCustomer = mvcResult.getResponse().getHeader("Location");
        String postRequest = "{\"address\": \"Nikolaev\", \"status\":\"1\", \"customer\":\""+ locationOfCustomer +"\"}";

        mvcResult = mockMvc.perform(post("/orderMain").content(postRequest)).andExpect(
                status().isCreated()).andReturn();

        String locationOfOrderMain = mvcResult.getResponse().getHeader("Location");


        String postRequestProduct = "{\"sku\": \"qwerty\", \"amount\":\"10\", \"dislikes\":\"5\", " +
                "\"likes\":\"6\", \"name\":\"prodName\", \"price\":\"50\", " +
                "\"productDescription\":\"prodDesc\", \"category\": null}";

        mvcResult = mockMvc.perform(post("/productCard").content(postRequestProduct)).andExpect(
                status().isCreated()).andReturn();

        String locationProduct = mvcResult.getResponse().getHeader("Location");

        postRequest = "{\"amount\": \"1000\", \"totalprice\":\"10\", \"orderMain\":\"" + locationOfOrderMain + "\", \"productCard\":\"" + locationProduct + "\"}";

        MvcResult result = mockMvc.perform(post("/orderItem").content(postRequest)).andExpect(
                status().isCreated()).andReturn();

        String location = result.getResponse().getHeader("Location");

        //test
        mockMvc.perform(
                patch(location).content("{\"totalprice\": \"100\"}")).andExpect(
                status().isNoContent());

        mockMvc.perform(get(location)).andExpect(status().isOk()).andExpect(
                jsonPath("$.totalprice").value("100"));
    }

    @Test
    public void shouldDeleteEntity() throws Exception {

        MvcResult mvcResult = mockMvc.perform(post("/customer").content(
                "{\"email\": \"kya@bk.ru\", \"name\":\"Yuriy\", \"phone\":\"0503337178\", \"subscribe\":\"true\"}")).andExpect(
                status().isCreated()).andExpect(header().string("Location", containsString("customer/")))
                .andReturn();

        String locationOfCustomer = mvcResult.getResponse().getHeader("Location");
        String postRequest = "{\"address\": \"Nikolaev\", \"status\":\"1\", \"customer\":\""+ locationOfCustomer +"\"}";

        mvcResult = mockMvc.perform(post("/orderMain").content(postRequest)).andExpect(
                status().isCreated()).andReturn();

        String locationOfOrderMain = mvcResult.getResponse().getHeader("Location");


        String postRequestProduct = "{\"sku\": \"qwerty\", \"amount\":\"10\", \"dislikes\":\"5\", " +
                "\"likes\":\"6\", \"name\":\"prodName\", \"price\":\"50\", " +
                "\"productDescription\":\"prodDesc\", \"category\": null}";

        mvcResult = mockMvc.perform(post("/productCard").content(postRequestProduct)).andExpect(
                status().isCreated()).andReturn();

        String locationProduct = mvcResult.getResponse().getHeader("Location");

        postRequest = "{\"amount\": \"1000\", \"totalprice\":\"10\", \"orderMain\":\"" + locationOfOrderMain + "\", \"productCard\":\"" + locationProduct + "\"}";

        MvcResult result = mockMvc.perform(post("/orderItem").content(postRequest)).andExpect(
                status().isCreated()).andReturn();

        String location = result.getResponse().getHeader("Location");

        //test
        mockMvc.perform(delete(location)).andExpect(status().isNoContent());

        mockMvc.perform(get(location)).andExpect(status().isNotFound());
    }

}