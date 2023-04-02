package school.hei.haapi.endpoint.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import school.hei.haapi.model.DelayPenalty;
import school.hei.haapi.model.InterestTimeRate;
import school.hei.haapi.endpoint.rest.controller.DelayPenaltyController;
import school.hei.haapi.service.DelayPenaltyService;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class DelayPenaltyControllerTest {

    @Mock
    private DelayPenaltyService delayPenaltyService;

    @InjectMocks
    private DelayPenaltyController delayPenaltyController;

    @BeforeEach
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetDelayPenalty() {
        DelayPenalty delayPenalty = DelayPenalty.builder()
                .id("1")
                .interestPercent(new BigDecimal("2.5"))
                .interestTimeRate(InterestTimeRate.MONTHLY)
                .graceDelay(7)
                .applicabilityDelayAfterGrace(30)
                .creationDatetime(LocalDateTime.now())
                .build();

        when(delayPenaltyService.getDelayPenalty("1")).thenReturn(delayPenalty);

        ResponseEntity<DelayPenalty> response = delayPenaltyController.getDelayPenalty("1");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(delayPenalty, response.getBody());
    }
}

//test includes connection to the service
