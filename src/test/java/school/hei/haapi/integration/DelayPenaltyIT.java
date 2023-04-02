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
import school.hei.haapi.endpoint.rest.model.DelayPenalty;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@ContextConfiguration(initializers = DelayPenaltyIT.ContextInitializer.class)
public class DelayPenaltyIT {
    @MockBean
    private SentryConf sentryConf;

    @MockBean
    private CognitoComponent cognitoComponentMock;

    private static ApiClient anApiClient(String token) {
        return TestUtils.anApiClient(token, FeeIT.ContextInitializer.SERVER_PORT);
    }

    @BeforeEach
    void setup() {
        setUpCognito(cognitoComponentMock);
    }

    @Test
    void read_delay_penalty_ok() throws Exception {
        ApiClient joeDoeClient = anApiClient(MANAGER1_TOKEN);
        PayingApi api = new PayingApi(joeDoeClient);

        DelayPenalty actual = api.getDelayPenalty();

        assertNotNull(actual);
    }

    @Test
    void read_delay_penalty_ko() throws Exception {

    }

    static class ContextInitializer extends AbstractContextInitializer {
        public static final int SERVER_PORT = anAvailableRandomPort();
        @Override
        public int getServerPort() {
            return SERVER_PORT;
        }
    }
}
