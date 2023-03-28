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
import school.hei.haapi.endpoint.rest.model.DelayPenalty;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.endpoint.rest.model.DelayPenalty.InterestTimerateEnum.DAILY;
import static school.hei.haapi.integration.conf.TestUtils.*;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = PenaltyIT.ContextInitializer.class)
@AutoConfigureMockMvc
public class PenaltyIT {

    @MockBean
    private SentryConf sentryConf;

    @MockBean
    private CognitoComponent cognitoComponentMock;

    @MockBean
    private EventBridgeClient eventBridgeClientMock;


    private static ApiClient anApiClient(String token) {
        return TestUtils.anApiClient(token, PenaltyIT.ContextInitializer.SERVER_PORT);
    }

    static DelayPenalty penalty1(){
        DelayPenalty penalty1 = new DelayPenalty();
        penalty1.setId("1");
        penalty1.setInterestPercent(2);
        penalty1.setInterestTimerate(DAILY);
        penalty1.setGraceDelay(7);
        penalty1.setApplicabilityDelayAfterGrace(25);
        penalty1.setCreationDatetime(Instant.parse("2022-02-01 10:00:00"));
        return penalty1;
    }

    @BeforeEach
    public void setUp() {
        setUpCognito(cognitoComponentMock);
        setUpEventBridge(eventBridgeClientMock);
    }

    @Test
    void get_all_penality_without_any_params() throws ApiException{

        ApiClient student1Client = anApiClient(MANAGER1_TOKEN);
        PayingApi api = new PayingApi(student1Client);

        DelayPenalty actual1 = api.getDelayPenalty();

        assertEquals(penalty1(), actual1);
    }

    static class ContextInitializer extends AbstractContextInitializer {
        public static final int SERVER_PORT = anAvailableRandomPort();

        @Override
        public int getServerPort() {
            return SERVER_PORT;
        }
    }
}
