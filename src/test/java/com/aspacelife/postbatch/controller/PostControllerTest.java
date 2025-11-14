package com.aspacelife.postbatch.controller;

import com.aspacelife.postbatch.config.WireMockTestConfig;
import com.aspacelife.postbatch.repository.PostRepository;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(classes = { WireMockTestConfig.class })
public class PostControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostRepository postRepository;
    private static WireMockServer wireMockServer;

    @BeforeAll
    static void setupWireMock() {
        wireMockServer = new WireMockServer(9999);
        wireMockServer.start();
    }

    @AfterAll
    static void stopWireMock() {
        System.out.println("Stopping WireMock server");
        wireMockServer.stop();
    }

    @BeforeEach
    void setupMocks() {
        String mockPosts = "[{\"userId\":1,\"id\":1,\"title\":\"Test title 1\",\"body\":\"Body 1\"}," +
                "{\"userId\":1,\"id\":2,\"title\":\"Test title 2\",\"body\":\"Body 2\"}]";
        System.out.println("setup...");
        wireMockServer.stubFor(get(urlPathEqualTo("/posts/1"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"userId\":1,\"id\":1,\"title\":\"Test title 1\",\"body\":\"Body 1\"}")
                        .withStatus(200)));
        wireMockServer.stubFor(get(urlPathEqualTo("/posts/2"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"userId\":1,\"id\":2,\"title\":\"Test title 1\",\"body\":\"Body 1\"}")
                        .withStatus(200)));

        wireMockServer.stubFor(get(urlPathEqualTo("/posts/3"))
                .willReturn(aResponse().withStatus(500)));
    }

    @Test
    void testBatchInsert_Success() throws Exception {
        MvcResult mvcResult = mockMvc.perform(post("/api/v1/posts/batch_insert")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"postNumber\": 2}"))
                        .andExpect(request().asyncStarted())
                        .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Posts fetched and saved successfully"))
                .andDo(print());

    }

    @Test
    void testBatchInsert_InvalidInput() throws Exception {
        mockMvc.perform(post("/api/v1/posts/batch_insert")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"postNumber\": -1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void testBatchInsert_ApiFailure() throws Exception {
        wireMockServer.stubFor(get(urlPathEqualTo("/posts/999"))
                .willReturn(aResponse().withStatus(500)));

        MvcResult mvcResult = mockMvc.perform(post("/api/v1/posts/batch_insert")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"postNumber\": 1}"))
                .andExpect(request().asyncStarted())
                .andReturn();

        wireMockServer.stubFor(get(urlPathEqualTo("/posts/1"))
                .willReturn(aResponse().withStatus(500)));

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Failed to fetch and save posts"));
    }


    @Test
    @Sql(scripts = {"/db/test-data.sql"})
    void testFetchRecord_FirstPage() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/posts/fetch_record")
                        .param("page", "1")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("records fetched successfully"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.pageNumber").value(0))
                .andExpect(jsonPath("$.data.pageSize").value(2))
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.totalPages").value(1))
                .andExpect(jsonPath("$.data.first").value(true))
                .andExpect(jsonPath("$.data.last").value(true))
                .andExpect(jsonPath("$.data.empty").value(false))
                .andDo(print());
    }



    @Test
    void testFetchRecord_InvalidPagination() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/posts/fetch_record")
                        .param("page", "-1")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testFetchRecord_EmptyDatabase() throws Exception {
        postRepository.deleteAll();
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/posts/fetch_record")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isEmpty())
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }


}
