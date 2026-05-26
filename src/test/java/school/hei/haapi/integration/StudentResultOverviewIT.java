package school.hei.haapi.integration;

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

import java.util.List;
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
import school.hei.haapi.model.Course;
import school.hei.haapi.model.CourseAssignment;
import school.hei.haapi.model.Exam;
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
  private Course web1;
  private Course web2;
  private Course api;
  private Course lv1;
  private Course theorie1;
  private Course prog1;
  private Course prog2;
  private Course prog2Poo;
  private Course donnees1;
  private Course sys1;
  private Course sys2;
  private Course mgt1;
  private Course donnees2;
  private Course lv2;
  private Course web3;
  private Course prog3;
  private Course prog4Sys3;
  private Course ia1;
  private Course mgt2;
  private Course projet1;
  private Course pro1;
  private Course secu2;
  private Course prog5;
  private Course secu1;
  private Course pro4;
  private Course pro3;

  private CourseAssignment web1AssignToTeacher;
  private CourseAssignment web2AssignToTeacher;
  private CourseAssignment apiAssignToTeacher;
  private CourseAssignment lv1AssignToTeacher;
  private CourseAssignment theorie1AssignToTeacher;
  private CourseAssignment prog1AssignToTeacher;
  private CourseAssignment prog2AssignToTeacher;
  private CourseAssignment prog2PooAssignToTeacher;
  private CourseAssignment donnees1AssignToTeacher;
  private CourseAssignment sys1AssignToTeacher;
  private CourseAssignment sys2AssignToTeacher;
  private CourseAssignment mgt1AssignToTeacher;
  private CourseAssignment donnees2AssignToTeacher;
  private CourseAssignment lv2AssignToTeacher;
  private CourseAssignment web3AssignToTeacher;
  private CourseAssignment prog3AssignToTeacher;
  private CourseAssignment prog4Sys3AssignToTeacher;
  private CourseAssignment ia1AssignToTeacher;
  private CourseAssignment mgt2AssignToTeacher;
  private CourseAssignment projet1AssignToTeacher;
  private CourseAssignment pro1AssignToTeacher;
  private CourseAssignment secu2AssignToTeacher;
  private CourseAssignment prog5AssignToTeacher;
  private CourseAssignment secu1AssignToTeacher;
  private CourseAssignment pro4AssignToTeacher;
  private CourseAssignment pro3AssignToTeacher;
  private CourseAssignment mob1AssignToTeacher;

  private Exam examWeb1;
  private Exam examWeb2;
  private Exam examApi;
  private Exam examLv1;
  private Exam examTheorie1;
  private Exam examProg1;
  private Exam examProg2;
  private Exam examProg2Poo;
  private Exam examDonnees1;
  private Exam examSys1;
  private Exam examSys2;
  private Exam examMgt1;
  private Exam examDonnees2;
  private Exam examLv2;
  private Exam examWeb3;
  private Exam examProg3;
  private Exam examProg4Sys3;
  private Exam examIa1;
  private Exam examMgt2;
  private Exam examProjet1;
  private Exam examPro1;
  private Exam examSecu2;
  private Exam examProg5;
  private Exam examSecu1;
  private Exam examPro4;
  private Exam examPro3;
  private Exam examMob1;

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }

  @BeforeEach
  public void setUp() {
    setUpCasdoor(casdoorAuthServiceMock, certificateLoaderMock);
    setUpCognito(cognitoComponentMock);
  }

  //            | WEB1 | WEB1_ASSIGN_TO_TEACHER | EXAM_WEB1 |
  //            | WEB2 | WEB2_ASSIGN_TO_TEACHER | EXAM_WEB2 |
  //            | API | API_ASSIGN_TO_TEACHER | EXAM_API |
  //            | LV1 | LV1_ASSIGN_TO_TEACHER | EXAM_LV1 |
  //            | THEORIE1 | THEORIE1_ASSIGN_TO_TEACHER | EXAM_THEORIE1 |
  //            | PROG1 | PROG1_ASSIGN_TO_TEACHER | EXAM_PROG1 |
  //            | PROG2 | PROG2_ASSIGN_TO_TEACHER | EXAM_PROG2 |
  //            | PROG2_POO | PROG2_POO_ASSIGN_TO_TEACHER | EXAM_PROG2_POO |
  //            | DONNEES1 | DONNEES1_ASSIGN_TO_TEACHER | EXAM_DONNEES1 |
  //            | SYS1 | SYS1_ASSIGN_TO_TEACHER | EXAM_SYS1 |
  //            | SYS2 | SYS2_ASSIGN_TO_TEACHER | EXAM_SYS2 |
  //            | MGT1 | MGT1_ASSIGN_TO_TEACHER | EXAM_MGT1 |
  //            | DONNEES2 | DONNEES2_ASSIGN_TO_TEACHER | EXAM_DONNEES2 |
  //            | LV2 | LV2_ASSIGN_TO_TEACHER | EXAM_LV2 |
  //            | WEB3 | WEB3_ASSIGN_TO_TEACHER | EXAM_WEB3 |
  //            | PROG3 | PROG3_ASSIGN_TO_TEACHER | EXAM_PROG3 |
  //            | PROG4_SYS3 | PROG4_SYS3_ASSIGN_TO_TEACHER | EXAM_PROG4_SYS3 |
  //            | IA1 | IA1_ASSIGN_TO_TEACHER | EXAM_IA1 |
  //            | MGT2 | MGT2_ASSIGN_TO_TEACHER | EXAM_MGT2 |
  //            | PROJET1 | PROJET1_ASSIGN_TO_TEACHER | EXAM_PROJET1 |
  //            | PRO1 | PRO1_ASSIGN_TO_TEACHER | EXAM_PRO1 |
  //            | SECU2 | SECU2_ASSIGN_TO_TEACHER | EXAM_SECU2 |
  //            | PROG5 | PROG5_ASSIGN_TO_TEACHER | EXAM_PROG5 |
  //            | SECU1 | SECU1_ASSIGN_TO_TEACHER | EXAM_SECU1 |
  //            | PRO4 | PRO4_ASSIGN_TO_TEACHER | EXAM_PRO4 |
  //            | PRO3 | PRO3_ASSIGN_TO_TEACHER | EXAM_PRO3 |
  //            | MOB1 | MOB1_ASSIGN_TO_TEACHER | EXAM_MOB1 |

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
    assertEquals(2, studentsResultOverviews.size());
  }
}
