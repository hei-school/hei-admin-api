package school.hei.haapi.integration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
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

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = DelayPenaltyIT.ContextInitializer.class)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DelayPenaltyIT {
    @MockBean
    private SentryConf sentryConf;
    @MockBean
    private CognitoComponent cognitoComponentMock;

    private static ApiClient anApiClient(String token) {
        return TestUtils.anApiClient(token, DelayPenaltyIT.ContextInitializer.SERVER_PORT);
    }

    @BeforeEach
    void setUp() {
        setUpCognito(cognitoComponentMock);
    }


    private static int DEFAULT_GRACE_DELAY = 0;
    private static int EXPECTED_GRACE_DELAY = 10;
    private static int DEFAULT_APPLICABILITY_DELAY_AFTER_GRACE = 0;
    private static int EXPECTED_APPLICABILITY_DELAY_AFTER_GRACE = 5;
    private static int DEFAULT_INTEREST_PERCENT = 0;
    private static int EXPECTED_INTEREST_PERCENT = 2;

    private static CreateDelayPenaltyChange delayPenaltyChange() {
        return new CreateDelayPenaltyChange()
                .graceDelay(10)
                .interestPercent(2)
                .applicabilityDelayAfterGrace(5);
    }

    @Test
    @Order(1)
    void manager_read_ok() throws ApiException {
        ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
        PayingApi api = new PayingApi(manager1Client);

        DelayPenalty actualDelayPenalty = api.getDelayPenalty();

        Assertions.assertEquals(actualDelayPenalty.getGraceDelay(), DEFAULT_GRACE_DELAY);
        Assertions.assertEquals(actualDelayPenalty.getApplicabilityDelayAfterGrace(), DEFAULT_APPLICABILITY_DELAY_AFTER_GRACE);
        Assertions.assertEquals(actualDelayPenalty.getInterestPercent(), DEFAULT_INTEREST_PERCENT);
        Assertions.assertEquals(actualDelayPenalty.getInterestTimeRate(), DelayPenalty.InterestTimeRateEnum.DAILY);
    }


    @Test
    void student_read_ko() {
        ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
        PayingApi api = new PayingApi(student1Client);
        assertThrowsForbiddenException(api::getDelayPenalty);
    }

    @Test
    void teacher_read_ko() {
        ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
        PayingApi api = new PayingApi(teacher1Client);
        assertThrowsForbiddenException(api::getDelayPenalty);
    }

    @Test
    void student_write_ko() {
        ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
        PayingApi api = new PayingApi(student1Client);
        assertThrowsForbiddenException(() -> api.createDelayPenaltyChange(new CreateDelayPenaltyChange()));
    }

    @Test
    void teacher_write_ko() {
        ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
        PayingApi api = new PayingApi(teacher1Client);
        assertThrowsForbiddenException(() -> api.createDelayPenaltyChange(new CreateDelayPenaltyChange()));
    }


    @Test
    @Transactional
    @Rollback
    void manager_write_ok() throws ApiException {
        ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
        PayingApi api = new PayingApi(manager1Client);

        DelayPenalty actualDelayPenalty = api.createDelayPenaltyChange(delayPenaltyChange());

        Assertions.assertEquals(actualDelayPenalty.getGraceDelay(), EXPECTED_GRACE_DELAY);
        Assertions.assertEquals(actualDelayPenalty.getApplicabilityDelayAfterGrace(), EXPECTED_APPLICABILITY_DELAY_AFTER_GRACE);
        Assertions.assertEquals(actualDelayPenalty.getInterestPercent(), EXPECTED_INTEREST_PERCENT);

        actualDelayPenalty = api.getDelayPenalty();
        Assertions.assertEquals(actualDelayPenalty.getGraceDelay(), EXPECTED_GRACE_DELAY);
        Assertions.assertEquals(actualDelayPenalty.getApplicabilityDelayAfterGrace(), EXPECTED_APPLICABILITY_DELAY_AFTER_GRACE);
        Assertions.assertEquals(actualDelayPenalty.getInterestPercent(), EXPECTED_INTEREST_PERCENT);
    }

    static class ContextInitializer extends AbstractContextInitializer {
        public static final int SERVER_PORT = anAvailableRandomPort();

        @Override
        public int getServerPort() {
            return SERVER_PORT;
        }
    }
}
