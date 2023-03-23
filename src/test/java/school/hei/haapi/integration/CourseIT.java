package school.hei.haapi.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.SentryConf;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.CourseRepository;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.ses.endpoints.internal.Value;


import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.*;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = CourseIT.ContextInitializer.class)
@AutoConfigureMockMvc
public class CourseIT {
    @MockBean
    private SentryConf sentryConf;
    @MockBean
    private CognitoComponent cognitoComponentMock;
    @MockBean
    private EventBridgeClient eventBridgeClientMock;

    private static ApiClient anApiClient(String token) {
        return TestUtils.anApiClient(token, CourseIT.ContextInitializer.SERVER_PORT);
    }
    public static Course course1(){
        return Course.builder()
                .id("course1_id")
                .code("PROG1")
                .name("Algo")
                .credits(4)
                .total_hours(8)
                .main_teacher(User.builder()
                            .id("teacher1_id")
                            .firstName("One")
                            .lastName("teacher")
                            .email("test+teacher1@hei.school")
                            .ref("TCR21001")
                            .phone("0322411125")
                            .sex(User.Sex.F)
                            .status(User.Status.ENABLED)
                            .role(User.Role.TEACHER)
                            .birthDate(LocalDate.parse("1990-01-01"))
                            .entranceDatetime(Instant.parse("2021-10-08T08:27:24.00Z"))
                            .address("Adr 3")
                            .build())
                .build();

    }



    @BeforeEach
    public void setUp() {
        setUpCognito(cognitoComponentMock);
        setUpEventBridge(eventBridgeClientMock);
    }

    @Test
    void course_get_ok() throws ApiException{

    }
    static class ContextInitializer extends AbstractContextInitializer {
        public static final int SERVER_PORT = anAvailableRandomPort();

        @Override
        public int getServerPort() {
            return SERVER_PORT;
        }
    }
}
