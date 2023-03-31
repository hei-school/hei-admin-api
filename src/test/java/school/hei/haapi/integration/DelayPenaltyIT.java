package school.hei.haapi.integration;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.SentryConf;
import school.hei.haapi.endpoint.rest.model.DelayPenalty;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;

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
        delayPenalty.setInterestTimerate(DelayPenalty.InterestTimerateEnum.valueOf("DAILY"));
        delayPenalty.setGraceDelay(10);
        delayPenalty.setApplicabilityDelayAfterGrace(5);
        delayPenalty.setCreationDatetime(Instant.parse(""));
        return delayPenalty;
    }
}
