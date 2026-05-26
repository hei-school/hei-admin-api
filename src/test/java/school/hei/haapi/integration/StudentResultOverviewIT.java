package school.hei.haapi.integration;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.endpoint.rest.api.UsersApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.GroupFlow;
import school.hei.haapi.model.Promotion;
import school.hei.haapi.model.StudentResultOverview;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.GroupFlowRepository;
import school.hei.haapi.repository.GroupRepository;
import school.hei.haapi.repository.PromotionRepository;
import school.hei.haapi.repository.StudentResultOverviewRepository;
import school.hei.haapi.repository.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static school.hei.haapi.endpoint.rest.model.ResultOverviewStatus.VALIDATED;
import static school.hei.haapi.integration.conf.TestUtils.ADMIN1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.setUpCasdoor;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import static school.hei.haapi.integration.test_data.StudentResultOverviewTestData.groupAB;
import static school.hei.haapi.integration.test_data.StudentResultOverviewTestData.groupCD;
import static school.hei.haapi.integration.test_data.StudentResultOverviewTestData.groupFlowA;
import static school.hei.haapi.integration.test_data.StudentResultOverviewTestData.groupFlowB;
import static school.hei.haapi.integration.test_data.StudentResultOverviewTestData.groupFlowC;
import static school.hei.haapi.integration.test_data.StudentResultOverviewTestData.groupFlowD;
import static school.hei.haapi.integration.test_data.StudentResultOverviewTestData.promotionAB;
import static school.hei.haapi.integration.test_data.StudentResultOverviewTestData.promotionCD;
import static school.hei.haapi.integration.test_data.StudentResultOverviewTestData.studentA;
import static school.hei.haapi.integration.test_data.StudentResultOverviewTestData.studentB;
import static school.hei.haapi.integration.test_data.StudentResultOverviewTestData.studentC;
import static school.hei.haapi.integration.test_data.StudentResultOverviewTestData.studentD;
import static school.hei.haapi.integration.test_data.StudentResultOverviewTestData.studentResultOverviewA;
import static school.hei.haapi.integration.test_data.StudentResultOverviewTestData.studentResultOverviewB;
import static school.hei.haapi.integration.test_data.StudentResultOverviewTestData.studentResultOverviewC;
import static school.hei.haapi.integration.test_data.StudentResultOverviewTestData.studentResultOverviewD;

@Slf4j
@Testcontainers
@AutoConfigureMockMvc
public class StudentResultOverviewIT extends FacadeITMockedThirdParties {
  @Autowired private StudentResultOverviewRepository studentResultOverviewRepository;
  @Autowired private PromotionRepository promotionRepository;
  @Autowired private UserRepository studentRepository;
  @Autowired private GroupRepository groupRepository;
  @Autowired private GroupFlowRepository groupFlowRepository;
  private User studentA;
  private User studentB;
  private User studentC;
  private User studentD;
  private Group groupAB;
  private Group groupCD;
  private GroupFlow groupFlowA;
  private GroupFlow groupFlowB;
  private GroupFlow groupFlowC;
  private GroupFlow groupFlowD;
  private Promotion promotionAB;
  private Promotion promotionCD;
  private StudentResultOverview studentResultOverviewA;
  private StudentResultOverview studentResultOverviewB;
  private StudentResultOverview studentResultOverviewC;
  private StudentResultOverview studentResultOverviewD;

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }

  @BeforeEach
  public void setUp() {
    setUpCasdoor(casdoorAuthServiceMock, certificateLoaderMock);
    setUpCognito(cognitoComponentMock);
  }

  @BeforeEach
  void setUpTestData() {
    studentA = studentRepository.save(studentA());
    studentB = studentRepository.save(studentB());
    studentD = studentRepository.save(studentD());
    studentC = studentRepository.save(studentC());
    groupAB = groupRepository.save(groupAB());
    groupCD = groupRepository.save(groupCD());
    groupFlowA = groupFlowA();
    groupFlowB = groupFlowB();
    groupFlowC = groupFlowC();
    groupFlowD = groupFlowD();
    groupFlowA.setStudent(studentA);
    groupFlowA.setGroup(groupAB);
    groupFlowB.setStudent(studentB);
    groupFlowB.setGroup(groupAB);
    groupFlowD.setStudent(studentD);
    groupFlowD.setGroup(groupCD);
    groupFlowC.setStudent(studentC);
    groupFlowC.setGroup(groupCD);
    groupFlowRepository.saveAll(List.of(groupFlowA, groupFlowB, groupFlowC, groupFlowD));
    promotionAB = promotionAB();
    promotionCD = promotionCD();
    promotionAB.setGroups(List.of(groupAB));
    promotionCD.setGroups(List.of(groupCD));
    promotionAB = promotionRepository.save(promotionAB);
    promotionCD = promotionRepository.save(promotionCD);
    studentResultOverviewA = studentResultOverviewA();
    studentResultOverviewB = studentResultOverviewB();
    studentResultOverviewC = studentResultOverviewC();
    studentResultOverviewD = studentResultOverviewD();
    studentResultOverviewA.setStudent(studentA);
    studentResultOverviewA.setPromotion(promotionAB);
    studentResultOverviewB.setStudent(studentB);
    studentResultOverviewB.setPromotion(promotionAB);
    studentResultOverviewC.setStudent(studentC);
    studentResultOverviewC.setPromotion(promotionCD);
    studentResultOverviewD.setStudent(studentD);
    studentResultOverviewD.setPromotion(promotionCD);
    var results =
        studentResultOverviewRepository.saveAll(
            List.of(
                studentResultOverviewA,
                studentResultOverviewB,
                studentResultOverviewC,
                studentResultOverviewD));
    log.info("results overviews : " + results);
  }

  @AfterEach
  void tearDownTestData() {
    studentResultOverviewRepository.deleteAll(
        List.of(
            studentResultOverviewA, studentResultOverviewB,
            studentResultOverviewC, studentResultOverviewD));
    groupFlowRepository.deleteAll(List.of(groupFlowA, groupFlowB, groupFlowC, groupFlowD));
    promotionRepository.deleteAll(List.of(promotionAB, promotionCD));
    groupRepository.deleteAll(List.of(groupAB, groupCD));
    studentRepository.deleteAll(List.of(studentA, studentB, studentC, studentD));
  }

  @Test
  public void get_student_result_overviews_OK() throws ApiException {
    ApiClient apiClient = anApiClient(ADMIN1_TOKEN);
    UsersApi api = new UsersApi(apiClient);

    var studentsResultOverviews =
        api.getStudentsResultOverviewsByStatus(promotionCD.getId(), VALIDATED, 1, 10);

    assertNotNull(studentsResultOverviews);
    assertEquals(1, studentsResultOverviews.size());
  }
}
