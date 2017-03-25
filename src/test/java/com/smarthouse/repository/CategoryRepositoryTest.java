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
public class CategoryRepositoryTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

    @After
    public void deleteAllBeforeTests() throws Exception {
        categoryRepository.deleteAll();
    }

    @Test
    public void shouldReturnRepositoryIndex() throws Exception {

        mockMvc.perform(get("/")).andDo(print()).andExpect(status().isOk()).andExpect(
                jsonPath("$._links.category").exists());
    }

    @Test
    public void shouldCreateEntity() throws Exception {

        mockMvc.perform(post("/category").content(
                "{\"category\":null, \"description\": \"categoryDescr\", \"name\":\"categoryName\"}")).andExpect(
                status().isCreated()).andExpect(
                header().string("Location", containsString("category/")));
    }

    @Test
    public void shouldRetrieveEntity() throws Exception {

        MvcResult mvcResult = mockMvc.perform(post("/category").content(
                "{\"category\":null, \"description\": \"categoryDescr\", \"name\":\"categoryName\"}")).andExpect(
                status().isCreated()).andReturn();

        String location = mvcResult.getResponse().getHeader("Location");
        mockMvc.perform(get(location)).andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("categoryDescr"))
                .andExpect(jsonPath("$.name").value("categoryName"));
    }

    @Test
    public void shouldQueryEntity() throws Exception {

        mockMvc.perform(post("/category").content(
                "{\"category\":null, \"description\": \"categoryDescr\", \"name\":\"categoryName\"}")).andExpect(
                status().isCreated());

        mockMvc.perform(
                get("/category/search/findByNameIgnoreCase?name={name}", "categoryName")).andExpect(
                status().isOk())
                .andExpect(jsonPath("$._embedded.category[0].name").value("categoryName"))
                .andExpect(jsonPath("$._embedded.category[0].description").value("categoryDescr"));
    }

    @Test
    public void shouldUpdateEntity() throws Exception {

        MvcResult mvcResult = mockMvc.perform(post("/category").content(
                "{\"category\":null, \"description\": \"categoryDescr\", \"name\":\"categoryName\"}")).andExpect(
                status().isCreated()).andReturn();

        String location = mvcResult.getResponse().getHeader("Location");

        mockMvc.perform(put(location).content(
                "{\"category\":null, \"description\": \"NewCategoryDescr\", \"name\":\"NewCategoryName\"}")).andExpect(
                status().isNoContent());

        mockMvc.perform(get(location)).andExpect(status().isOk()).andExpect(
                jsonPath("$.description").value("NewCategoryDescr")).andExpect(
                jsonPath("$.name").value("NewCategoryName"));
    }

    @Test
    public void shouldPartiallyUpdateEntity() throws Exception {

        MvcResult mvcResult = mockMvc.perform(post("/category").content(
                "{\"category\":null, \"description\": \"categoryDescr\", \"name\":\"categoryName\"}")).andExpect(
                status().isCreated()).andReturn();

        String location = mvcResult.getResponse().getHeader("Location");

        mockMvc.perform(
                patch(location).content("{\"name\": \"NewCategoryName\"}")).andExpect(
                status().isNoContent());

        mockMvc.perform(get(location)).andExpect(status().isOk()).andExpect(
                jsonPath("$.name").value("NewCategoryName")).andExpect(
                jsonPath("$.description").value("categoryDescr"));
    }

    @Test
    public void shouldDeleteEntity() throws Exception {


        MvcResult mvcResult = mockMvc.perform(post("/category").content(
                "{\"category\":null, \"description\": \"categoryDescr\", \"name\":\"categoryName\"}")).andExpect(
                status().isCreated()).andReturn();

        String location = mvcResult.getResponse().getHeader("Location");

        mockMvc.perform(delete(location)).andExpect(status().isNoContent());

        mockMvc.perform(get(location)).andExpect(status().isNotFound());
    }
}