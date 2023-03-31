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

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
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

    private static ApiClient anApiClient(String token) {
        return TestUtils.anApiClient(token, ContextInitializer.SERVER_PORT);
    }

    public static CreateDelayPenaltyChange penalty1() {
        CreateDelayPenaltyChange penalty = new CreateDelayPenaltyChange();
        penalty.setInterestPercent(3);
        penalty.setInterestTimerate(CreateDelayPenaltyChange.InterestTimerateEnum.valueOf(DelayPenalty.InterestTimerateEnum.DAILY.toString()));
        penalty.setGraceDelay(10);
        penalty.setApplicabilityDelayAfterGrace(10);
        return penalty;
    }

    static CreateDelayPenaltyChange delayPenalty2() {
        return new CreateDelayPenaltyChange()
                .interestPercent(10)
                .interestTimerate(CreateDelayPenaltyChange.InterestTimerateEnum.DAILY)
                .graceDelay(7)
                .applicabilityDelayAfterGrace(30);
    }

    static DelayPenalty delayPenalty3() {
        DelayPenalty delayPenalty = new DelayPenalty();
        delayPenalty.setId("delay_penalty1_id");
        delayPenalty.setInterestPercent(10);
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
    void student_update_ko() {
        ApiClient student1Client = anApiClient(STUDENT1_TOKEN);

        PayingApi api = new PayingApi(student1Client);
        assertThrowsForbiddenException(() -> api.createDelayPenaltyChange(penalty1()));
    }

    @Test
    void teacher_write_ko() {
        ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);

        PayingApi api = new PayingApi(teacher1Client);
        assertThrowsForbiddenException(() -> api.createDelayPenaltyChange(penalty1()));
    }

    @Test
    void manager_write_ok() throws ApiException {
        ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
        PayingApi api = new PayingApi(manager1Client);

        DelayPenalty actualPenalty = api.createDelayPenaltyChange(delayPenalty2());

        assertEquals(delayPenalty3(), actualPenalty);
    }

    static class ContextInitializer extends AbstractContextInitializer {
        public static final int SERVER_PORT = anAvailableRandomPort();

        @Override
        public int getServerPort() {
            return SERVER_PORT;
        }
    }
}
