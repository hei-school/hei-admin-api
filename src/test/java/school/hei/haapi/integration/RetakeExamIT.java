package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static school.hei.haapi.integration.StudentIT.student1;
import static school.hei.haapi.integration.conf.TestUtils.course1;
import static school.hei.haapi.integration.conf.TestUtils.setUpCasdoor;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import static school.hei.haapi.integration.conf.TestUtils.setUpS3Service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.repository.RetakeExamRepository;

@Testcontainers
@AutoConfigureMockMvc
public class RetakeExamIT extends FacadeITMockedThirdParties {

  @Autowired RetakeExamRepository retakeExamRepository;

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }

  @BeforeEach
  void setup() {
    setUpCasdoor(casdoorAuthServiceMock, certificateLoaderMock);
    setUpCognito(cognitoComponentMock);
    setUpS3Service(fileService, student1());
  }

  @Test
  public void read_all_retake_exams_by_student_ok() {
    var retakeExams =
        retakeExamRepository.findByRetakeExamSessionIdAndStudentId("session1_id", "student1_id");
    assertNotNull(retakeExams);
    assertEquals(retakeExams.getFirst().getStudent().getId(), student1().getId());
    assertEquals(retakeExams.getFirst().getCourse().getName(), course1().getName());
  }
}
