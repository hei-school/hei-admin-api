package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static school.hei.haapi.endpoint.rest.model.ResultOverviewStatus.VALIDATED;
import static school.hei.haapi.integration.conf.TestUtils.ADMIN1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.setUpCasdoor;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import static school.hei.haapi.model.CycleLevel.BACHELOR;
import static school.hei.haapi.model.GroupFlow.GroupFlowType.JOIN;
import static school.hei.haapi.model.User.Role.STUDENT;
import static school.hei.haapi.model.User.Status.ENABLED;

import java.math.BigDecimal;
import java.time.Instant;
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
import school.hei.haapi.model.Group;
import school.hei.haapi.model.GroupFlow;
import school.hei.haapi.model.Promotion;
import school.hei.haapi.model.ResultOverviewStatus;
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
  @Autowired private UserRepository userRepository;
  @Autowired private GroupRepository groupRepository;
  @Autowired private GroupFlowRepository groupFlowRepository;
  User userA;
  User userB;
  User userC;
  User userD;
  Group groupA;
  Group groupB;
  GroupFlow groupFlowA;
  GroupFlow groupFlowB;
  GroupFlow groupFlowC;
  GroupFlow groupFlowD;
  Promotion promotionA;
  Promotion promotionB;
  StudentResultOverview studentResultOverviewA;
  StudentResultOverview studentResultOverviewB;
  StudentResultOverview studentResultOverviewC;
  StudentResultOverview studentResultOverviewD;

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
    userA = userRepository.save(userA());
    userB = userRepository.save(userB());
    userD = userRepository.save(userD());
    userC = userRepository.save(userC());
    groupA = groupRepository.save(groupA());
    groupB = groupRepository.save(groupB());
    groupFlowA = groupFlowA();
    groupFlowB = groupFlowB();
    groupFlowC = groupFlowC();
    groupFlowD = groupFlowD();
    groupFlowA.setStudent(userA);
    groupFlowA.setGroup(groupA);
    groupFlowB.setStudent(userB);
    groupFlowB.setGroup(groupA);
    groupFlowD.setStudent(userD);
    groupFlowD.setGroup(groupB);
    groupFlowC.setStudent(userC);
    groupFlowC.setGroup(groupB);
    groupFlowRepository.saveAll(List.of(groupFlowA, groupFlowB, groupFlowC, groupFlowD));
    promotionA = promotionA();
    promotionB = promotionB();
    promotionA.setGroups(List.of(groupA));
    promotionB.setGroups(List.of(groupB));
    promotionA = promotionRepository.save(promotionA);
    promotionB = promotionRepository.save(promotionB);
    studentResultOverviewA = studentResultOverviewA();
    studentResultOverviewB = studentResultOverviewB();
    studentResultOverviewC = studentResultOverviewC();
    studentResultOverviewD = studentResultOverviewD();
    studentResultOverviewA.setStudent(userA);
    studentResultOverviewA.setPromotion(promotionA);
    studentResultOverviewB.setStudent(userB);
    studentResultOverviewB.setPromotion(promotionA);
    studentResultOverviewC.setStudent(userC);
    studentResultOverviewC.setPromotion(promotionB);
    studentResultOverviewD.setStudent(userD);
    studentResultOverviewD.setPromotion(promotionB);
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
    promotionRepository.deleteAll(List.of(promotionA, promotionB));
    groupRepository.deleteAll(List.of(groupA, groupB));
    userRepository.deleteAll(List.of(userA, userB, userC, userD));
  }

  @Test
  public void get_student_result_overviews_OK() throws ApiException {
    ApiClient apiClient = anApiClient(ADMIN1_TOKEN);
    UsersApi api = new UsersApi(apiClient);

    var studentsResultOverviews =
        api.getStudentsResultOverviewsByStatus(promotionA.getId(), VALIDATED, 1, 10);

    assertNotNull(studentsResultOverviews);
    assertEquals(2, studentsResultOverviews.size());
  }

  private static Group groupA() {
    return Group.builder()
        .name("group A")
        .ref("group A1")
        .creationDatetime(Instant.parse("2020-01-01T00:00:00Z"))
        .build();
  }

  private static Group groupB() {
    return Group.builder()
        .name("group B")
        .ref("group B2")
        .creationDatetime(Instant.parse("2021-01-01T00:00:00Z"))
        .build();
  }

  private static Promotion promotionA() {
    return Promotion.builder()
        .name("Promotion 2020-2021")
        .ref("Alumni 2020 2021")
        .creationDatetime(Instant.parse("2020-01-01T00:00:00Z"))
        .cycleLevel(BACHELOR)
        .build();
  }

  private static Promotion promotionB() {
    return Promotion.builder()
        .name("Promotion 2021-2022")
        .ref("Alumni 2021 2022")
        .creationDatetime(Instant.parse("2021-01-01T00:00:00Z"))
        .cycleLevel(BACHELOR)
        .build();
  }

  private static User userA() {
    return User.builder()
        .firstName("user A firstname")
        .lastName("user A lastname")
        .email("userA@gmail.com")
        .ref("STD2002")
        .status(ENABLED)
        .entranceDatetime(Instant.parse("2020-01-01T00:00:00Z"))
        .role(STUDENT)
        .build();
  }

  private static User userB() {
    return User.builder()
        .firstName("user B firstname")
        .lastName("user B lastname")
        .email("userB@gmail.com")
        .ref("STD2001")
        .status(ENABLED)
        .entranceDatetime(Instant.parse("2020-01-01T00:00:00Z"))
        .role(STUDENT)
        .build();
  }

  private static User userC() {
    return User.builder()
        .firstName("user C firstname")
        .lastName("user C lastname")
        .email("userC@gmail.com")
        .ref("STD2101")
        .status(ENABLED)
        .entranceDatetime(Instant.parse("2021-01-01T00:00:00Z"))
        .role(STUDENT)
        .build();
  }

  private static User userD() {
    return User.builder()
        .firstName("user D firstname")
        .lastName("user D lastname")
        .email("userD@gmail.com")
        .ref("STD2102")
        .status(ENABLED)
        .entranceDatetime(Instant.parse("2021-01-01T00:00:00Z"))
        .role(STUDENT)
        .build();
  }

  private static GroupFlow groupFlowA() {
    return GroupFlow.builder()
        .groupFlowType(JOIN)
        .flowDatetime(Instant.parse("2020-01-01T00:00:00Z"))
        .build();
  }

  private static GroupFlow groupFlowB() {
    return GroupFlow.builder()
        .groupFlowType(JOIN)
        .flowDatetime(Instant.parse("2020-01-01T00:00:00Z"))
        .build();
  }

  private static GroupFlow groupFlowC() {
    return GroupFlow.builder()
        .groupFlowType(JOIN)
        .flowDatetime(Instant.parse("2021-01-01T00:00:00Z"))
        .build();
  }

  private static GroupFlow groupFlowD() {
    return GroupFlow.builder()
        .groupFlowType(JOIN)
        .flowDatetime(Instant.parse("2021-01-01T00:00:00Z"))
        .build();
  }

  private static StudentResultOverview studentResultOverviewA() {
    return StudentResultOverview.builder()
        .status(ResultOverviewStatus.VALIDATED)
        .weightedAverage(BigDecimal.valueOf(17.45))
        .obtainedCredits(BigDecimal.valueOf(180.0))
        .totalCredits(BigDecimal.valueOf(180.0))
        .build();
  }

  private static StudentResultOverview studentResultOverviewB() {
    return StudentResultOverview.builder()
        .status(ResultOverviewStatus.VALIDATED)
        .weightedAverage(BigDecimal.valueOf(12.45))
        .obtainedCredits(BigDecimal.valueOf(180.0))
        .totalCredits(BigDecimal.valueOf(180.0))
        .build();
  }

  private static StudentResultOverview studentResultOverviewC() {
    return StudentResultOverview.builder()
        .status(ResultOverviewStatus.INVALIDATED)
        .weightedAverage(BigDecimal.valueOf(09.45))
        .obtainedCredits(BigDecimal.valueOf(160.0))
        .totalCredits(BigDecimal.valueOf(180.0))
        .build();
  }

  private static StudentResultOverview studentResultOverviewD() {
    return StudentResultOverview.builder()
        .status(ResultOverviewStatus.VALIDATED)
        .weightedAverage(BigDecimal.valueOf(14.15))
        .obtainedCredits(BigDecimal.valueOf(180.0))
        .totalCredits(BigDecimal.valueOf(180.0))
        .build();
  }
}
