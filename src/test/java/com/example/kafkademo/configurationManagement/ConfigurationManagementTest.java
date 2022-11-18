package com.example.kafkademo.configurationManagement;

import com.example.kafkademo.common.BaseIntergrationTest;
import com.example.kafkademo.common.dto.MaxSpeedResponse;
import com.example.kafkademo.configurationManagement.dao.ConfigurationDao;
import com.example.kafkademo.configurationManagement.dto.MaxSpeedRequest;
import com.example.kafkademo.configurationManagement.kafka.MaxSpeedProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
// NOTE: BEFORE_EACH_TEST_METHOD doesn't work nicely with @MockBean/@SpyBean
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ConfigurationManagementTest extends BaseIntergrationTest {
    @Autowired
    private ConfigurationDao configurationDao;

    @SpyBean
    private MaxSpeedProducer maxSpeedProducer;

    @BeforeEach
    void setup() {
        // important part - mock sending to Kafka, necessary if no embeded Kafka is used in tests
        Mockito.doNothing().when(maxSpeedProducer).send(any());
    }

    @Test
    void changeMaxSpeed() {
        MaxSpeedRequest maxSpeedRequest = new MaxSpeedRequest();
        maxSpeedRequest.setMaxSpeed(60);
        maxSpeedRequest.setBusId(10);

        ResponseEntity<MaxSpeedResponse> response = testRestTemplate.postForEntity(
                "/api/v1/configuration/changeMaxSpeed",
                new HttpEntity<>(maxSpeedRequest, makeCommonHeaders()),
                MaxSpeedResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getMaxSpeed()).isEqualTo(60);
        assertThat(response.getBody().getBusId()).isEqualTo(10);
        assertThat(configurationDao.getMaxSpeed(10)).isEqualTo(60);

        // important part - verification that event was sent to Kafka
        verify(maxSpeedProducer, times(1)).send(any());
    }

}
