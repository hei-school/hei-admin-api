package school.hei.haapi.integration;

import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.SentryConf;
import school.hei.haapi.endpoint.rest.api.PayingApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.model.DelayPenalty;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.exception.ApiException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@AutoConfigureMockMvc

public class DelayPenaltyIT {
    @MockBean
    private SentryConf sentryConf;
    @MockBean
    private CognitoComponent cognitoComponentMock;

    private static ApiClient anApiClient(String token) {
        return TestUtils.anApiClient(token, FeeIT.ContextInitializer.SERVER_PORT);
    }
    public static DelayPenalty delayUpdated(){
        DelayPenalty delayPenalty = new DelayPenalty();
        delayPenalty.id("delay_penalty1_id");
        delayPenalty.interestPercent(2);
        delayPenalty.interestTimerate(DelayPenalty.InterestTimerateEnum.DAILY);
        delayPenalty.graceDelay(2);
        delayPenalty.applicabilityDelayAfterGrace(2);
        delayPenalty.creationDatetime(Instant.parse("2022-11-06T08:30:25.00Z"));
        return delayPenalty;
    }
    @Test
    void manager_update_delay_ok() throws ApiException {
        ApiClient managerClient1 = anApiClient(MANAGER1_TOKEN);
        PayingApi api = new PayingApi(managerClient1);

        DelayPenalty delayUpdated = api.changeDelayPenaltyChange(createDelayPenalty1());
        assertEquals(delayUpdated(), delayUpdated);
    }


}

