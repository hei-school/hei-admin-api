package school.hei.haapi.integration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.SentryConf;
import school.hei.haapi.endpoint.rest.api.PayingApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.CreateDelayPenaltyChange;
import school.hei.haapi.endpoint.rest.model.CreateFee;
import school.hei.haapi.endpoint.rest.model.DelayPenalty;
import school.hei.haapi.endpoint.rest.model.Fee;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.TestUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static school.hei.haapi.integration.conf.TestUtils.*;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class DelayPenaltyIT {
    public static final String DELAY_PENALTY = "delay_penalty_id";
    @MockBean
    private SentryConf sentryConf;

    @MockBean
    private CognitoComponent cognitoComponent;

    private static ApiClient anApiClient(String token) {
        return TestUtils.anApiClient(token, TeacherIT.ContextInitializer.SERVER_PORT);
    }

    public static CreateDelayPenaltyChange createDelayPenaltyChange(){
        return new CreateDelayPenaltyChange()
                .graceDelay(3)
                .interestTimerate(CreateDelayPenaltyChange.InterestTimerateEnum.DAILY)
                .interestPercent(0)
                .applicabilityDelayAfterGrace(10);
    }
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
    static CreateDelayPenaltyChange createDelayPenaltyChange1() {
        return new CreateDelayPenaltyChange()
                .graceDelay(3)
                .interestTimerate(CreateDelayPenaltyChange.InterestTimerateEnum.DAILY)
                .applicabilityDelayAfterGrace(10)
                .interestPercent(0);

    }
    @Test
    void manager_write_delay_ok() throws ApiException {
        ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
        PayingApi api = new PayingApi(manager1Client);
        DelayPenalty actual = api.createDelayPenaltyChange(createDelayPenaltyChange());

        DelayPenalty expected = api.getDelayPenalty();
        assertEquals(expected, actual);
    }
    @Test
    void manager_write_delay_with_some_bad_fields_ko() throws ApiException {
        ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
        PayingApi api = new PayingApi(manager1Client);
        CreateDelayPenaltyChange toCreate1 = createDelayPenaltyChange1().graceDelay(null);
        CreateDelayPenaltyChange toCreate2 = createDelayPenaltyChange1().applicabilityDelayAfterGrace(null);
        CreateDelayPenaltyChange toCreate3 = createDelayPenaltyChange1().interestTimerate(null);
        CreateDelayPenaltyChange toCreate4 = createDelayPenaltyChange1().interestPercent(null);
        DelayPenalty expected = api.getDelayPenalty();

        ApiException exception1 = assertThrows(ApiException.class,
                () -> api.createDelayPenaltyChange(toCreate1));
        ApiException exception2 = assertThrows(ApiException.class,
                () -> api.createDelayPenaltyChange(toCreate2));
        ApiException exception3 = assertThrows(ApiException.class,
                () -> api.createDelayPenaltyChange(toCreate3));
        ApiException exception4 = assertThrows(ApiException.class,
                () -> api.createDelayPenaltyChange(toCreate4));


        DelayPenalty actual = api.getDelayPenalty();
        assertEquals(expected, actual);
        assertTrue(expected.equals(actual));
        String exceptionMessage1 = exception1.getMessage();
        String exceptionMessage2 = exception2.getMessage();
        String exceptionMessage3 = exception3.getMessage();
        String exceptionMessage4 = exception4.getMessage();

        assertTrue(exceptionMessage1.contains("Grace delay is mandatory"));
        assertTrue(exceptionMessage2.contains("interestPercent is madatory"));
        assertTrue(exceptionMessage3.contains("applicabilityDelayAfterGrace is mandatory"));
        assertTrue(exceptionMessage4.contains("interestTimerate is mandatory"));

    }
}
