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
import school.hei.haapi.endpoint.rest.model.CreateDelayPenaltyChange;
import school.hei.haapi.endpoint.rest.model.DelayPenalty;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsApiException;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;

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
        return TestUtils.anApiClient(token, DelayPenaltyIT.ContextInitializer.SERVER_PORT);
    }

    @BeforeEach
    void setup() {
        setUpCognito(cognitoComponentMock);
    }

    CreateDelayPenaltyChange creatableDelayPenalty() {
        CreateDelayPenaltyChange delayPenalty = new CreateDelayPenaltyChange();
        delayPenalty.setInterestPercent(2);
        delayPenalty.setInterestTimerate(CreateDelayPenaltyChange.InterestTimerateEnum.DAILY);
        delayPenalty.setGraceDelay(3);
        delayPenalty.setApplicabilityDelayAfterGrace(2);
        return delayPenalty;
    }

    DelayPenalty delayPenalty() {
        return new DelayPenalty()
            .id("delay_penalty1_id")
            .interestPercent(creatableDelayPenalty().getInterestPercent())
            .graceDelay(creatableDelayPenalty().getGraceDelay())
            .interestTimerate(DelayPenalty.InterestTimerateEnum.DAILY)
            .applicabilityDelayAfterGrace(creatableDelayPenalty()
                .getApplicabilityDelayAfterGrace())
            .creationDatetime(null);
    }

    @Test
    void manager_update_delayPenalty_ok() throws Exception {
        ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
        PayingApi api = new PayingApi(manager1Client);

        DelayPenalty actual = api.createDelayPenaltyChange(creatableDelayPenalty());

        assertEquals(delayPenalty(), actual.creationDatetime(null));
    }

    @Test
    void teacher_write_ko() {
        ApiClient teacherClient = anApiClient(TEACHER1_TOKEN);
        PayingApi api = new PayingApi(teacherClient);

        assertThrowsApiException(
            "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
            () -> api.createDelayPenaltyChange(new CreateDelayPenaltyChange()));
    }

    @Test
    void student_write_ko() {
        ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
        PayingApi api = new PayingApi(student1Client);

        assertThrowsApiException(
            "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
            () -> api.createDelayPenaltyChange(new CreateDelayPenaltyChange()));
    }

    static class ContextInitializer extends AbstractContextInitializer {
        public static final int SERVER_PORT = anAvailableRandomPort();

        @Override
        public int getServerPort() {
            return SERVER_PORT;
        }
    }
}
