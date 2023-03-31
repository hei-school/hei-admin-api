package school.hei.haapi.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.endpoint.rest.api.PayingApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.CreateDelayPenaltyChange;
import school.hei.haapi.endpoint.rest.model.DelayPenalty;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.*;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = DelayPenaltyIT.ContextInitializer.class)
@AutoConfigureMockMvc
public class DelayPenaltyIT {
    @MockBean
    private CognitoComponent cognitoComponentMock;


    private static ApiClient anApiClient(String token) {
        return TestUtils.anApiClient(token, DelayPenaltyIT.ContextInitializer.SERVER_PORT);
    }

    static DelayPenalty delayPenalty1() {
        DelayPenalty delayPenalty = new DelayPenalty();
        delayPenalty.setId("delay_penalty1_id");
        delayPenalty.setInterestPercent(2);
        delayPenalty.setInterestTimerate(DelayPenalty.InterestTimerateEnum.DAILY);
        delayPenalty.setGraceDelay(7);
        delayPenalty.setApplicabilityDelayAfterGrace(30);
        delayPenalty.setCreationDatetime(Instant.parse("2022-11-08T08:25:24.00Z"));
        return delayPenalty;
    }

    @BeforeEach
    public void setUp() {
        setUpCognito(cognitoComponentMock);
    }

    @Test
    void manager_read_ok() throws ApiException {
        ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
        PayingApi api = new PayingApi(manager1Client);

        DelayPenalty actualPenalty = api.getDelayPenalty();
        assertEquals(delayPenalty1(), actualPenalty);
    }
    static class ContextInitializer extends AbstractContextInitializer {
        public static final int SERVER_PORT = anAvailableRandomPort();

        @Override
        public int getServerPort() {
            return SERVER_PORT;
        }
    }
}
