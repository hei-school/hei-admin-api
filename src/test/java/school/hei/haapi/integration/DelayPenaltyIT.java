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

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    public static DelayPenalty penalty1() {
        DelayPenalty penalty = new DelayPenalty();
        penalty.setId("id1");
        penalty.setInterestPercent(1);
        penalty.setInterestTimerate(DelayPenalty.InterestTimerateEnum.DAILY);
        penalty.setGraceDelay(0);
        penalty.setApplicabilityDelayAfterGrace(0);
        penalty.setCreationDatetime(Instant.parse("2021-12-08T08:25:24.00Z"));
        return penalty;
    }

    @BeforeEach
    public void setUp() {
        setUpCognito(cognitoComponentMock);
    }

    @Test
    void badtoken_read_ko() {
        ApiClient anonymousClient = anApiClient(BAD_TOKEN);

        PayingApi api = new PayingApi(anonymousClient);
        assertThrowsForbiddenException(api::getDelayPenalty);
    }

    @Test
    void student_read_ko() {
        ApiClient student1Client = anApiClient(STUDENT1_TOKEN);

        PayingApi api = new PayingApi(student1Client);
        assertThrowsForbiddenException(
                () -> api.getDelayPenalty());
        assertThrowsForbiddenException(
                () -> api.getDelayPenalty());
    }

    @Test
    void teacher_read_ko() {
        ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);

        PayingApi api = new PayingApi(teacher1Client);
        assertThrowsForbiddenException(
                () -> api.getDelayPenalty());
        assertThrowsForbiddenException(
                () -> api.getDelayPenalty());
    }

    @Test
    void manager_read_ok() throws ApiException {
        ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
        PayingApi api = new PayingApi(manager1Client);

        DelayPenalty actual = api.getDelayPenalty();

        assertEquals(penalty1(), actual);
    }

    static class ContextInitializer extends AbstractContextInitializer {
        public static final int SERVER_PORT = anAvailableRandomPort();

        @Override
        public int getServerPort() {
            return SERVER_PORT;
        }
    }
}
