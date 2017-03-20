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
public class AttributeValueRepositoryTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AttributeValueRepository attributeValueRepository;

    @Autowired
    private AttributeNameRepository attributeNameRepository;

    @After
    public void deleteAllBeforeTests() throws Exception {
        attributeValueRepository.deleteAll();
        attributeNameRepository.deleteAll();
    }

    @Test
    public void shouldReturnRepositoryIndex() throws Exception {

        mockMvc.perform(get("/")).andDo(print()).andExpect(status().isOk()).andExpect(
                jsonPath("$._links.attributeValue").exists());
    }

    @Test
    public void shouldCreateEntity() throws Exception {

        MvcResult mvcResult = mockMvc.perform(post("/attributeName").content(
                "{\"name\": \"nameAttr\"}")).andExpect(
                status().isCreated()).andReturn();

        String location = mvcResult.getResponse().getHeader("Location");

        String postRequest = "{\"value\": \"red\", \"attributeName\":\"" +location + "\", \"productCard\":null}";

        //test
        mockMvc.perform(post("/attributeValue").content(postRequest)).andExpect(
                status().isCreated()).andExpect(
                header().string("Location", containsString("attributeValue/")));
    }

    @Test
    public void shouldRetrieveEntity() throws Exception {

        MvcResult mvcResult = mockMvc.perform(post("/attributeName").content(
                "{\"name\": \"nameAttr\"}")).andExpect(
                status().isCreated()).andReturn();

        String location = mvcResult.getResponse().getHeader("Location");

        String postRequest = "{\"value\": \"red\", \"attributeName\":\"" +location + "\", \"productCard\":null}";

        mvcResult = mockMvc.perform(post("/attributeValue").content(postRequest)).andExpect(
                status().isCreated()).andReturn();

        location = mvcResult.getResponse().getHeader("Location");

        //test
        mockMvc.perform(get(location)).andExpect(status().isOk())
                .andExpect(jsonPath("$.value").value("red"));
    }

    @Test
    public void shouldUpdateEntity() throws Exception {


        MvcResult mvcResult = mockMvc.perform(post("/attributeName").content(
                "{\"name\": \"nameAttr\"}")).andExpect(
                status().isCreated()).andReturn();

        String location = mvcResult.getResponse().getHeader("Location");

        String postRequest = "{\"value\": \"red\", \"attributeName\":\"" +location + "\", \"productCard\":null}";

        mvcResult = mockMvc.perform(post("/attributeValue").content(postRequest)).andExpect(
                status().isCreated()).andReturn();

        location = mvcResult.getResponse().getHeader("Location");

        postRequest = "{\"value\": \"white\", \"attributeName\":\"" +location + "\", \"productCard\":null}";

        //test
        mockMvc.perform(put(location).content(postRequest)).andExpect(
                status().isNoContent());

        mockMvc.perform(get(location)).andExpect(status().isOk()).andExpect(
                jsonPath("$.value").value("white"));
    }

    @Test
    public void shouldPartiallyUpdateEntity() throws Exception {

        MvcResult mvcResult = mockMvc.perform(post("/attributeName").content(
                "{\"name\": \"nameAttr\"}")).andExpect(
                status().isCreated()).andReturn();

        String location = mvcResult.getResponse().getHeader("Location");

        String postRequest = "{\"value\": \"red\", \"attributeName\":\"" +location + "\", \"productCard\":null}";

        mvcResult = mockMvc.perform(post("/attributeValue").content(postRequest)).andExpect(
                status().isCreated()).andReturn();

        location = mvcResult.getResponse().getHeader("Location");

        //test
        mockMvc.perform(
                patch(location).content("{\"value\": \"white\"}")).andExpect(
                status().isNoContent());

        mockMvc.perform(get(location)).andExpect(status().isOk()).andExpect(
                jsonPath("$.value").value("white"));
    }

    @Test
    public void shouldDeleteEntity() throws Exception {

        MvcResult mvcResult = mockMvc.perform(post("/attributeName").content(
                "{\"name\": \"nameAttr\"}")).andExpect(
                status().isCreated()).andReturn();

        String location = mvcResult.getResponse().getHeader("Location");

        String postRequest = "{\"value\": \"red\", \"attributeName\":\"" +location + "\", \"productCard\":null}";

        mvcResult = mockMvc.perform(post("/attributeValue").content(postRequest)).andExpect(
                status().isCreated()).andReturn();

        location = mvcResult.getResponse().getHeader("Location");

        //test
        mockMvc.perform(delete(location)).andExpect(status().isNoContent());

        mockMvc.perform(get(location)).andExpect(status().isNotFound());
    }

}