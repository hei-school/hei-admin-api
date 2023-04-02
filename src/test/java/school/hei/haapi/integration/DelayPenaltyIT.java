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

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.*;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = DelayPenaltyIT.ContextInitializer.class)
@AutoConfigureMockMvc
public class DelayPenaltyIT {

    @MockBean
    private SentryConf sentryConf;

    @MockBean
    private CognitoComponent cognitoComponentMock;

    private static ApiClient anApiClient(String token) {
        return TestUtils.anApiClient(token, ContextInitializer.SERVER_PORT);
    }

    public static school.hei.haapi.endpoint.rest.model.DelayPenalty delayPenalty1() {
        school.hei.haapi.endpoint.rest.model.DelayPenalty delayPenalty = new school.hei.haapi.endpoint.rest.model.DelayPenalty();
        delayPenalty.setId("delayPenalty1_id");
        delayPenalty.setInterestPercent(0);
        delayPenalty.setInterestTimerate(school.hei.haapi.endpoint.rest.model.DelayPenalty.InterestTimerateEnum.fromValue("Daily"));
        delayPenalty.setGraceDelay(0);
        delayPenalty.setApplicabilityDelayAfterGrace(0);
        delayPenalty.setCreationDatetime(Instant.parse("2023-03-31T06:00:45.279Z"));
        return delayPenalty;
    }



    @BeforeEach
    public void setUp() {
        setUpCognito(cognitoComponentMock);
    }

    @Test
    void student_read_ok() throws ApiException {
        ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
        PayingApi api = new PayingApi(student1Client);

        DelayPenalty actualDelay = api.getDelayPenalty();

        assertEquals(delayPenalty1(), actualDelay);

    }


    static class ContextInitializer extends AbstractContextInitializer {
        public static final int SERVER_PORT = anAvailableRandomPort();

        @Override
        public int getServerPort() {
            return SERVER_PORT;
        }
    }
}