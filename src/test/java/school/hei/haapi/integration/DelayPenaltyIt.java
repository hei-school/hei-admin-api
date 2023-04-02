package school.hei.haapi.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.SentryConf;
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
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = GroupIT.ContextInitializer.class)
@AutoConfigureMockMvc
public class DelayPenaltyIt {
    @MockBean
    private SentryConf sentryConf;

    @MockBean
    private CognitoComponent cognitoComponentMock;

    private static ApiClient anApiClient(String token) {
        return TestUtils.anApiClient(token, DelayPenaltyIt.ContextInitializer.SERVER_PORT);
    }

    public static DelayPenalty delayPenalty1(){
        DelayPenalty delayPenalty = new DelayPenalty();
        delayPenalty.setId("delay_penalty1_id");
        delayPenalty.setGraceDelay(10);
        delayPenalty.setApplicabilityDelayAfterGrace(10);
        delayPenalty.setInterestTimerate(DelayPenalty.InterestTimerateEnum.DAILY);
        delayPenalty.setInterestPercent(2);
        delayPenalty.setCreationDatetime(Instant.parse("2023-01-15T08:25:25.00Z"));
        return delayPenalty;
    }

    private static CreateDelayPenaltyChange delayPenaltyChange() {
        CreateDelayPenaltyChange penaltyToUpdate = new CreateDelayPenaltyChange();
        penaltyToUpdate.setGraceDelay(10);
        penaltyToUpdate.setInterestPercent(2);
        penaltyToUpdate.setApplicabilityDelayAfterGrace(10);
        penaltyToUpdate.setInterestTimerate(CreateDelayPenaltyChange.InterestTimerateEnum.DAILY);

        return penaltyToUpdate;

    }

    @BeforeEach
    void setUp() {
        setUpCognito(cognitoComponentMock);
    }

    @Test
    void manager_read_delay_penalty_ok() throws ApiException {
        ApiClient managerClient = anApiClient(MANAGER1_TOKEN);
        PayingApi api = new PayingApi(managerClient);

        DelayPenalty delayPenalty = api.getDelayPenalty();

        assertEquals(delayPenalty1(), delayPenalty);
    }

    @Test
    void manager_delay_penalty_change_ok() throws ApiException {
        ApiClient managerClient = anApiClient(MANAGER1_TOKEN);
        PayingApi api = new PayingApi(managerClient);

        DelayPenalty delayPenalty = api.createDelayPenaltyChange(delayPenaltyChange());
        assertEquals(delayPenalty,delayPenaltyChange());
    }

    static class ContextInitializer extends AbstractContextInitializer {
        public static final int SERVER_PORT = anAvailableRandomPort();

        @Override
        public int getServerPort() {
            return SERVER_PORT;
        }
    }
}
