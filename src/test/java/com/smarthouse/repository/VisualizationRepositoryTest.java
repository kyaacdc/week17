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
public class VisualizationRepositoryTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private VisualizationRepository visualizationRepository;

    @Before
    public void deleteAllBeforeTests() throws Exception {
        visualizationRepository.deleteAll();
    }

    @Test
    public void shouldReturnRepositoryIndex() throws Exception {

        mockMvc.perform(get("/")).andDo(print()).andExpect(status().isOk()).andExpect(
                jsonPath("$._links.visualization").exists());
    }

    @Test
    public void shouldCreateEntity() throws Exception {

        mockMvc.perform(post("/visualization").content(
                "{\"type\": \"1\", \"url\":\"visUrl\", \"productCard\":null}")).andExpect(
                status().isCreated()).andExpect(
                header().string("Location", containsString("visualization/")));
    }

    @Test
    public void shouldRetrieveEntity() throws Exception {

        MvcResult mvcResult =  mockMvc.perform(post("/visualization").content(
                "{\"type\": \"1\", \"url\":\"visUrl\", \"productCard\":null}")).andExpect(
                status().isCreated()).andReturn();

        String location = mvcResult.getResponse().getHeader("Location");

        mockMvc.perform(get(location)).andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("1"))
                .andExpect(jsonPath("$.url").value("visUrl"));
    }

    @Test
    public void shouldUpdateEntity() throws Exception {
        MvcResult mvcResult =  mockMvc.perform(post("/visualization").content(
                "{\"type\": \"1\", \"url\":\"visUrl\", \"productCard\":null}")).andExpect(
                status().isCreated()).andReturn();

        String location = mvcResult.getResponse().getHeader("Location");

        mockMvc.perform(put(location).content(
                "{\"type\": \"2\", \"url\":\"NewVisUrl\", \"productCard\":null}")).andExpect(
                status().isNoContent());

        mockMvc.perform(get(location)).andExpect(status().isOk()).andExpect(
                jsonPath("$.type").value("2")).andExpect(
                jsonPath("$.url").value("NewVisUrl"));
    }

    @Test
    public void shouldPartiallyUpdateEntity() throws Exception {

        MvcResult mvcResult =  mockMvc.perform(post("/visualization").content(
                "{\"type\": \"1\", \"url\":\"visUrl\", \"productCard\":null}")).andExpect(
                status().isCreated()).andReturn();

        String location = mvcResult.getResponse().getHeader("Location");

        mockMvc.perform(
                patch(location).content("{\"url\": \"NewUrl\"}")).andExpect(
                status().isNoContent());

        mockMvc.perform(get(location)).andExpect(status().isOk()).andExpect(
                jsonPath("$.url").value("NewUrl"));
    }

    @Test
    public void shouldDeleteEntity() throws Exception {
        MvcResult mvcResult =  mockMvc.perform(post("/visualization").content(
                "{\"type\": \"1\", \"url\":\"visUrl\", \"productCard\":null}")).andExpect(
                status().isCreated()).andReturn();

        String location = mvcResult.getResponse().getHeader("Location");

        mockMvc.perform(delete(location)).andExpect(status().isNoContent());

        mockMvc.perform(get(location)).andExpect(status().isNotFound());
    }
}