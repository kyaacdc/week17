package com.smarthouse.repository;

import org.junit.Before;
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
public class OrderMainRepositoryTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderMainRepository orderMainRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Before
    public void deleteAllBeforeTests() throws Exception {
        orderItemRepository.deleteAll();
        orderMainRepository.deleteAll();
        customerRepository.deleteAll();
    }

    @Test
    public void shouldReturnRepositoryIndex() throws Exception {

        mockMvc.perform(get("/")).andDo(print()).andExpect(status().isOk()).andExpect(
                jsonPath("$._links.orderMain").exists());
    }

    @Test
    public void shouldCreateEntity() throws Exception {

        MvcResult mvcResult = mockMvc.perform(post("/customer").content(
                "{\"email\": \"kya@bk.ru\", \"name\":\"Yuriy\", \"phone\":\"0503337178\", \"subscribe\":\"true\"}")).andExpect(
                status().isCreated()).andExpect(header().string("Location", containsString("customer/")))
                .andReturn();

        String location = mvcResult.getResponse().getHeader("Location");
        String postRequest = "{\"address\": \"Nikolaev\", \"status\":\"1\", \"customer\":\""+ location +"\"}";

        mockMvc.perform(post("/orderMain").content(postRequest)).andExpect(
                status().isCreated()).andExpect(
                header().string("Location", containsString("orderMain/")));
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

        mockMvc.perform(get(locationOfOrderMain)).andExpect(status().isOk())
                .andExpect(jsonPath("$.address").value("Nikolaev"))
                .andExpect(jsonPath("$.status").value("1"));
    }

    @Test
    public void shouldQueryEntity() throws Exception {

        MvcResult mvcResult = mockMvc.perform(post("/customer").content(
                "{\"email\": \"kya@bk.ru\", \"name\":\"Yuriy\", \"phone\":\"0503337178\", \"subscribe\":\"true\"}")).andExpect(
                status().isCreated()).andExpect(header().string("Location", containsString("customer/")))
                .andReturn();

        String locationOfCustomer = mvcResult.getResponse().getHeader("Location");
        String postRequest = "{\"address\": \"Nikolaev\", \"status\":\"1\", \"customer\":\""+ locationOfCustomer +"\"}";

        mockMvc.perform(post("/orderMain").content(postRequest)).andExpect(
                status().isCreated()).andReturn();

        mockMvc.perform(
                get("/orderMain/search/findByAddressIgnoreCase?address={address}", "Nikolaev")).andExpect(
                status().isOk()).andExpect(
                jsonPath("$._embedded.orderMain[0].address").value("Nikolaev"));
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

        postRequest = "{\"address\": \"Kiev\", \"status\":\"2\", \"customer\":\""+ locationOfCustomer +"\"}";

        mockMvc.perform(put(locationOfOrderMain).content(postRequest)).andExpect(
                status().isNoContent());

        mockMvc.perform(get(locationOfOrderMain)).andExpect(status().isOk()).andExpect(
                jsonPath("$.address").value("Kiev"));
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

        mockMvc.perform(
                patch(locationOfOrderMain).content("{\"address\": \"Odessa\"}")).andExpect(
                status().isNoContent());

        mockMvc.perform(get(locationOfOrderMain)).andExpect(status().isOk()).andExpect(
                jsonPath("$.address").value("Odessa"));
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

        mockMvc.perform(delete(locationOfOrderMain)).andExpect(status().isNoContent());

        mockMvc.perform(get(locationOfOrderMain)).andExpect(status().isNotFound());
    }
}