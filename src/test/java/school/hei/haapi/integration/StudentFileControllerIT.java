package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_ID;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import school.hei.haapi.SentryConf;
import school.hei.haapi.conf.FacadeIT;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.controller.StudentFileController;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.TestUtils;

public class StudentFileControllerIT extends FacadeIT {
    @LocalServerPort
    private int serverPort;
    @MockBean
    private SentryConf sentryConf;

    @MockBean
    private CognitoComponent cognitoComponentMock;
    @Autowired
    StudentFileController subject;

    private ApiClient anApiClient(String token) {
        return TestUtils.anApiClient(token, serverPort);
    }

    @BeforeEach
    void setUp() {
        setUpCognito(cognitoComponentMock);
    }

    @Test
    void student_read_own_ok() {
        assertDoesNotThrow(() -> subject.getStudentScholarshipCertificate(STUDENT1_ID));
    }

    @Test
    void manager_read_ok() {
        assertDoesNotThrow(() -> subject.getStudentScholarshipCertificate(STUDENT1_ID));
    }
}
