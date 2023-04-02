package school.hei.haapi.integration;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.SentryConf;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.DelayPenalty;

import java.time.Instant;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class DelayPenaltyIT {

    @MockBean
    private SentryConf sentryConf;

    @MockBean
    private CognitoComponent cognitoComponent;

    static DelayPenalty delayPenalty(){
        DelayPenalty delayPenalty = new DelayPenalty();
        delayPenalty.setId("DELAY_ID");
        delayPenalty.setInterestPercent(2);
        delayPenalty.setInterestTimerate(String.valueOf(5));
        delayPenalty.setGraceDelay(10);
        delayPenalty.setApplicabilityDelayAfterGrace(5);
        delayPenalty.setCreationDatetime(Instant.parse(""));
        return delayPenalty;
    }
}
