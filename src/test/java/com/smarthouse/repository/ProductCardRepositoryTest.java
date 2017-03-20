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
public class ProductCardRepositoryTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductCardRepository productCardRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Before
    public void deleteAllBeforeTests() throws Exception {
        orderItemRepository.deleteAll();
        productCardRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Test
    public void shouldReturnRepositoryIndex() throws Exception {

        mockMvc.perform(get("/")).andDo(print()).andExpect(status().isOk()).andExpect(
                jsonPath("$._links.productCard").exists());
    }

    @Test
    public void shouldCreateEntity() throws Exception {

        String postRequest = "{\"sku\": \"qwerty\", \"amount\":\"10\", \"dislikes\":\"5\", " +
                "\"likes\":\"6\", \"name\":\"prodName\", \"price\":\"50\", " +
                "\"productDescription\":\"prodDesc\", \"category\": null}";

        mockMvc.perform(post("/productCard").content(postRequest)).andExpect(
                status().isCreated()).andExpect(
                header().string("Location", containsString("productCard/")));
    }

    @Test
    public void shouldRetrieveEntity() throws Exception {

        String postRequest = "{\"sku\": \"qwerty\", \"amount\":\"10\", \"dislikes\":\"5\", " +
                "\"likes\":\"6\", \"name\":\"prodName\", \"price\":\"50\", " +
                "\"productDescription\":\"prodDesc\", \"category\": null}";

        MvcResult mvcResult = mockMvc.perform(post("/productCard").content(postRequest)).andExpect(
                status().isCreated()).andReturn();

        String location = mvcResult.getResponse().getHeader("Location");

        mockMvc.perform(get(location)).andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("prodName"))
                .andExpect(jsonPath("$.dislikes").value("5"))
                .andExpect(jsonPath("$.likes").value("6"))
                .andExpect(jsonPath("$.price").value("50"))
                .andExpect(jsonPath("$.productDescription").value("prodDesc"))
                .andExpect(jsonPath("$.amount").value("10"));
    }

    @Test
    public void shouldQueryEntity() throws Exception {
        String postRequest = "{\"sku\": \"qwerty\", \"amount\":\"10\", \"dislikes\":\"5\", " +
                "\"likes\":\"6\", \"name\":\"prodName\", \"price\":\"50\", " +
                "\"productDescription\":\"prodDesc\", \"category\": null}";

        mockMvc.perform(post("/productCard").content(postRequest)).andExpect(
                status().isCreated()).andReturn();

        mockMvc.perform(
                get("/productCard/search/findByNameIgnoreCase?name={name}", "prodName")).andExpect(
                status().isOk())
                .andExpect(jsonPath("$._embedded.productCard[0].productDescription").value("prodDesc"))
                .andExpect(jsonPath("$._embedded.productCard[0].name").value("prodName"));
    }

    @Test
    public void shouldUpdateEntity() throws Exception {

        String postRequest = "{\"sku\": \"qwerty\", \"amount\":\"10\", \"dislikes\":\"5\", " +
                "\"likes\":\"6\", \"name\":\"prodName\", \"price\":\"50\", " +
                "\"productDescription\":\"prodDesc\", \"category\": null}";

        MvcResult mvcResult = mockMvc.perform(post("/productCard").content(postRequest)).andExpect(
                status().isCreated()).andReturn();

        postRequest = "{\"sku\": \"qwerty\", \"amount\":\"10\", \"dislikes\":\"5\", " +
                "\"likes\":\"6\", \"name\":\"NewProdName\", \"price\":\"50\", " +
                "\"productDescription\":\"prodDesc\", \"category\": null}";

        String location = mvcResult.getResponse().getHeader("Location");

        mockMvc.perform(put(location).content(postRequest)).andExpect(
                status().isNoContent());

        mockMvc.perform(get(location)).andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("NewProdName"))
                .andExpect(jsonPath("$.dislikes").value("5"))
                .andExpect(jsonPath("$.likes").value("6"))
                .andExpect(jsonPath("$.price").value("50"))
                .andExpect(jsonPath("$.productDescription").value("prodDesc"))
                .andExpect(jsonPath("$.amount").value("10"));
    }

    @Test
    public void shouldPartiallyUpdateEntity() throws Exception {

        String postRequest = "{\"sku\": \"qwerty\", \"amount\":\"10\", \"dislikes\":\"5\", " +
                "\"likes\":\"6\", \"name\":\"prodName\", \"price\":\"50\", " +
                "\"productDescription\":\"prodDesc\", \"category\": null}";

        MvcResult mvcResult = mockMvc.perform(post("/productCard").content(postRequest)).andExpect(
                status().isCreated()).andReturn();

        String location = mvcResult.getResponse().getHeader("Location");

        mockMvc.perform(
                patch(location).content("{\"name\": \"NewProdName\"}")).andExpect(
                status().isNoContent());

        mockMvc.perform(get(location)).andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("NewProdName"))
                .andExpect(jsonPath("$.dislikes").value("5"))
                .andExpect(jsonPath("$.likes").value("6"))
                .andExpect(jsonPath("$.price").value("50"))
                .andExpect(jsonPath("$.productDescription").value("prodDesc"))
                .andExpect(jsonPath("$.amount").value("10"));
    }

    @Test
    public void shouldDeleteEntity() throws Exception {

        String postRequest = "{\"sku\": \"qwerty\", \"amount\":\"10\", \"dislikes\":\"5\", " +
                "\"likes\":\"6\", \"name\":\"prodName\", \"price\":\"50\", " +
                "\"productDescription\":\"prodDesc\", \"category\": null}";

        MvcResult mvcResult = mockMvc.perform(post("/productCard").content(postRequest)).andExpect(
                status().isCreated()).andReturn();

        String location = mvcResult.getResponse().getHeader("Location");

        mockMvc.perform(delete(location)).andExpect(status().isNoContent());

        mockMvc.perform(get(location)).andExpect(status().isNotFound());
    }
}