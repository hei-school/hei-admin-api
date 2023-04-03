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
import school.hei.haapi.endpoint.rest.model.Fee;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;

import java.time.Instant;
import java.util.List;

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

    static CreateDelayPenaltyChange delayPenalty1() {
        return new CreateDelayPenaltyChange()
                .interestPercent(3)
                .interestTimerate(CreateDelayPenaltyChange.InterestTimerateEnum.DAILY)
                .graceDelay(5)
                .applicabilityDelayAfterGrace(15);
    }

    static DelayPenalty delayPenalty2() {
        DelayPenalty delayPenalty = new DelayPenalty();
        delayPenalty.setId("delay_penalty1_id");
        delayPenalty.setInterestPercent(3);
        delayPenalty.setInterestTimerate(DelayPenalty.InterestTimerateEnum.DAILY);
        delayPenalty.setGraceDelay(5);
        delayPenalty.setApplicabilityDelayAfterGrace(15);
        delayPenalty.setCreationDatetime(Instant.parse("2022-11-08T08:25:24.00Z"));
        return delayPenalty;
    }

    static Fee fee3() {
        Fee fee = new Fee();
        fee.setId(FEE3_ID);
        fee.setStudentId(STUDENT1_ID);
        fee.setStatus(Fee.StatusEnum.LATE);
        fee.setType(Fee.TypeEnum.TUITION);
        fee.setTotalAmount(5202);
        fee.setRemainingAmount(5202);
        fee.setComment("Comment");
        fee.setUpdatedAt(Instant.now());
        fee.creationDatetime(Instant.parse("2022-12-08T08:25:24.00Z"));
        fee.setDueDatetime(Instant.parse("2021-12-09T08:25:24.00Z"));
        return fee;
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
        assertEquals(delayPenalty2(), actualPenalty);
    }

    @Test
    void student_update_ko() {
        ApiClient student1Client = anApiClient(STUDENT1_TOKEN);

        PayingApi api = new PayingApi(student1Client);
        assertThrowsForbiddenException(() -> api.createDelayPenaltyChange(delayPenalty1()));
    }

    @Test
    void teacher_write_ko() {
        ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);

        PayingApi api = new PayingApi(teacher1Client);
        assertThrowsForbiddenException(() -> api.createDelayPenaltyChange(delayPenalty1()));
    }

    @Test
    void manager_write_ok() throws ApiException {
        ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
        PayingApi api = new PayingApi(manager1Client);

        DelayPenalty actualPenalty = api.createDelayPenaltyChange(delayPenalty1());
        List<Fee> actual = api.getStudentFees(STUDENT1_ID, 1, 5, null);
        assertEquals(delayPenalty2(), actualPenalty);
        assertEquals(fee3().getTotalAmount(),actual.get(1).getTotalAmount());
    }

    static class ContextInitializer extends AbstractContextInitializer {
        public static final int SERVER_PORT = anAvailableRandomPort();

        @Override
        public int getServerPort() {
            return SERVER_PORT;
        }
    }
}
