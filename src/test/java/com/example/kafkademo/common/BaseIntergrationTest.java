package com.example.kafkademo.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import javax.annotation.PostConstruct;
import java.util.List;

public abstract class BaseIntergrationTest {
    @LocalServerPort
    int localPort;

    protected TestRestTemplate testRestTemplate;

    @Autowired
    protected RestTemplateBuilder restTemplateBuilder;

    @Autowired
    protected ObjectMapper mapper;

    @PostConstruct
    public void initialize() {
        this.testRestTemplate = new TestRestTemplate(
                restTemplateBuilder
                        .rootUri("http://localhost:" + localPort)
        );
    }

    protected HttpHeaders makeCommonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }
}
