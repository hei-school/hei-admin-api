package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static school.hei.haapi.endpoint.rest.model.ResultOverviewStatus.INVALIDATED;
import static school.hei.haapi.endpoint.rest.model.ResultOverviewStatus.IN_PROGRESS;
import static school.hei.haapi.endpoint.rest.model.ResultOverviewStatus.VALIDATED;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.L1;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.L2;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.L3;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.M1;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.M2;
import static school.hei.haapi.integration.conf.TestUtils.ADMIN1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.setUpCasdoor;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import static school.hei.haapi.integration.test_data.StudentResultOverviewTestData.axelResultOverview;
import static school.hei.haapi.integration.test_data.StudentResultOverviewTestData.axelYearlyResultL1;
import static school.hei.haapi.integration.test_data.StudentResultOverviewTestData.axelYearlyResultL2;
import static school.hei.haapi.integration.test_data.StudentResultOverviewTestData.axelYearlyResultL3;
import static school.hei.haapi.integration.test_data.StudentResultOverviewTestData.freddyResultOverview;
import static school.hei.haapi.integration.test_data.StudentResultOverviewTestData.freddyYearlyResultL1;
import static school.hei.haapi.integration.test_data.StudentResultOverviewTestData.freddyYearlyResultL2;
import static school.hei.haapi.integration.test_data.StudentResultOverviewTestData.freddyYearlyResultL3;
import static school.hei.haapi.integration.test_data.StudentResultOverviewTestData.groupFlowAxel;
import static school.hei.haapi.integration.test_data.StudentResultOverviewTestData.groupFlowFreddy;
import static school.hei.haapi.integration.test_data.StudentResultOverviewTestData.groupFlowManitra;
import static school.hei.haapi.integration.test_data.StudentResultOverviewTestData.groupFlowTolojanahary;
import static school.hei.haapi.integration.test_data.StudentResultOverviewTestData.groupH;
import static school.hei.haapi.integration.test_data.StudentResultOverviewTestData.groupJ;
import static school.hei.haapi.integration.test_data.StudentResultOverviewTestData.manitraResultOverview;
import static school.hei.haapi.integration.test_data.StudentResultOverviewTestData.manitraYearlyResultL1;
import static school.hei.haapi.integration.test_data.StudentResultOverviewTestData.manitraYearlyResultL2;
import static school.hei.haapi.integration.test_data.StudentResultOverviewTestData.manitraYearlyResultL3;
import static school.hei.haapi.integration.test_data.StudentResultOverviewTestData.promotionH;
import static school.hei.haapi.integration.test_data.StudentResultOverviewTestData.promotionJ;
import static school.hei.haapi.integration.test_data.StudentResultOverviewTestData.tolojanaharyResultOverview;
import static school.hei.haapi.integration.test_data.StudentResultOverviewTestData.tolojanaharyYearlyResultL1;
import static school.hei.haapi.integration.test_data.StudentResultOverviewTestData.tolojanaharyYearlyResultL2;
import static school.hei.haapi.integration.test_data.StudentResultOverviewTestData.tolojanaharyYearlyResultL3;
import static school.hei.haapi.integration.test_data.StudentTestData.axel;
import static school.hei.haapi.integration.test_data.StudentTestData.freddy;
import static school.hei.haapi.integration.test_data.StudentTestData.manitra;
import static school.hei.haapi.integration.test_data.StudentTestData.tolojanahary;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.endpoint.event.EventProducer;
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
import school.hei.haapi.service.GradeResultService;
import school.hei.haapi.service.PromotionService;
import school.hei.haapi.service.StudentResultOverviewService;
import school.hei.haapi.service.UserService;

@Slf4j
@Testcontainers
@AutoConfigureMockMvc
public class StudentResultOverviewIT extends FacadeITMockedThirdParties {
  @Autowired private StudentResultOverviewRepository studentResultOverviewRepository;
  @Autowired private PromotionRepository promotionRepository;
  @SpyBean private UserRepository userRepository;
  @Autowired private GroupRepository groupRepository;
  @Autowired private GroupFlowRepository groupFlowRepository;
  @Autowired private StudentResultOverviewService subject;
  @SpyBean private PromotionService promotionService;
  @SpyBean private UserService userService;
  @SpyBean private GradeResultService gradeResultService;
  @SpyBean private EventProducer eventProducer;

  private User axel;
  private User tolojanahary;
  private User manitra;
  private User freddy;
  private Group groupJ;
  private Group groupH;
  private GroupFlow groupFlowAxel;
  private GroupFlow groupFlowTolojanahary;
  private GroupFlow groupFlowManitra;
  private GroupFlow groupFlowFreddy;
  private Promotion promotionH;
  private Promotion promotionJ;
  private StudentResultOverview axelResultOverview;
  private StudentResultOverview tolojanaharyResultOverview;
  private StudentResultOverview manitraResultOverview;
  private StudentResultOverview freddyResultOverview;

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }

  @BeforeEach
  void setUp() {
    setUpCasdoor(casdoorAuthServiceMock, certificateLoaderMock);
    setUpCognito(cognitoComponentMock);
  }

  @BeforeEach
  void setUpTestData() {
    axel = userRepository.save(axel());
    tolojanahary = userRepository.save(tolojanahary());
    manitra = userRepository.save(manitra());
    freddy = userRepository.save(freddy());
    promotionH = promotionRepository.save(promotionH());
    promotionJ = promotionRepository.save(promotionJ());
    groupH = groupRepository.save(groupH());
    groupH.setPromotion(promotionH);
    groupJ = groupJ();
    groupJ.setPromotion(promotionJ);
    groupH = groupRepository.save(groupH);
    groupJ = groupRepository.save(groupJ);
    groupFlowAxel = groupFlowAxel();
    groupFlowTolojanahary = groupFlowTolojanahary();
    groupFlowManitra = groupFlowManitra();
    groupFlowFreddy = groupFlowFreddy();
    groupFlowAxel.setStudent(axel);
    groupFlowAxel.setGroup(groupH);
    groupFlowTolojanahary.setStudent(tolojanahary);
    groupFlowTolojanahary.setGroup(groupH);
    groupFlowManitra.setStudent(manitra);
    groupFlowManitra.setGroup(groupJ);
    groupFlowFreddy.setStudent(freddy);
    groupFlowFreddy.setGroup(groupJ);
    groupFlowRepository.saveAll(
        List.of(groupFlowAxel, groupFlowTolojanahary, groupFlowManitra, groupFlowFreddy));
    tolojanaharyResultOverview = tolojanaharyResultOverview();
    axelResultOverview = axelResultOverview();
    manitraResultOverview = manitraResultOverview();
    freddyResultOverview = freddyResultOverview();
    tolojanaharyResultOverview.setStudent(tolojanahary);
    tolojanaharyResultOverview.setGraduationPromotion(promotionH);
    axelResultOverview.setStudent(axel);
    axelResultOverview.setGraduationPromotion(promotionH);
    manitraResultOverview.setStudent(manitra);
    manitraResultOverview.setGraduationPromotion(promotionJ);
    freddyResultOverview.setStudent(freddy);
    freddyResultOverview.setGraduationPromotion(promotionJ);
    studentResultOverviewRepository.saveAll(
        List.of(
            tolojanaharyResultOverview,
            axelResultOverview,
            manitraResultOverview,
            freddyResultOverview));
  }

  @AfterEach
  void tearDownTestData() {
    studentResultOverviewRepository.deleteAllInBatch();
    groupFlowRepository.deleteAllInBatch(
        List.of(groupFlowTolojanahary, groupFlowAxel, groupFlowManitra, groupFlowFreddy));
    groupRepository.deleteAllInBatch(List.of(groupH, groupJ));
    promotionRepository.deleteAllInBatch(List.of(promotionH, promotionJ));
    userRepository.deleteAllInBatch(List.of(axel, tolojanahary, manitra, freddy));
  }

  @Test
  void read_result_overviews_with_student_in_the_other_promotion_ok() throws ApiException {
    doReturn(Optional.ofNullable(tolojanaharyYearlyResultL1()))
        .when(gradeResultService)
        .findLeveledYearlyResultByStudentId(L1, tolojanahary.getId());
    doReturn(Optional.ofNullable(tolojanaharyYearlyResultL2()))
        .when(gradeResultService)
        .findLeveledYearlyResultByStudentId(L2, tolojanahary.getId());
    doReturn(Optional.ofNullable(tolojanaharyYearlyResultL3()))
        .when(gradeResultService)
        .findLeveledYearlyResultByStudentId(L3, tolojanahary.getId());
    doReturn(Optional.empty())
        .when(gradeResultService)
        .findLeveledYearlyResultByStudentId(eq(M1), any());
    doReturn(Optional.empty())
        .when(gradeResultService)
        .findLeveledYearlyResultByStudentId(eq(M2), any());

    doReturn(Optional.ofNullable(axelYearlyResultL1()))
        .when(gradeResultService)
        .findLeveledYearlyResultByStudentId(L1, axel.getId());
    doReturn(Optional.ofNullable(axelYearlyResultL2()))
        .when(gradeResultService)
        .findLeveledYearlyResultByStudentId(L2, axel.getId());
    doReturn(Optional.ofNullable(axelYearlyResultL3()))
        .when(gradeResultService)
        .findLeveledYearlyResultByStudentId(L3, axel.getId());

    doReturn(Optional.ofNullable(manitraYearlyResultL1()))
        .when(gradeResultService)
        .findLeveledYearlyResultByStudentId(L1, manitra.getId());
    doReturn(Optional.ofNullable(manitraYearlyResultL2()))
        .when(gradeResultService)
        .findLeveledYearlyResultByStudentId(L2, manitra.getId());
    doReturn(Optional.ofNullable(manitraYearlyResultL3()))
        .when(gradeResultService)
        .findLeveledYearlyResultByStudentId(L3, manitra.getId());

    doReturn(Optional.ofNullable(freddyYearlyResultL1()))
        .when(gradeResultService)
        .findLeveledYearlyResultByStudentId(L1, freddy.getId());
    doReturn(Optional.ofNullable(freddyYearlyResultL2()))
        .when(gradeResultService)
        .findLeveledYearlyResultByStudentId(L2, freddy.getId());
    doReturn(Optional.ofNullable(freddyYearlyResultL3()))
        .when(gradeResultService)
        .findLeveledYearlyResultByStudentId(L3, freddy.getId());

    promotionH.setGroups(List.of(groupH));
    promotionJ.setGroups(List.of(groupJ));
    doReturn(List.of(promotionH, promotionJ))
        .when(promotionService)
        .getPromotions(null, null, null, null, null);
    tolojanahary.setGroupFlows(List.of(groupFlowTolojanahary));
    axel.setGroupFlows(List.of(groupFlowAxel));
    manitra.setGroupFlows(List.of(groupFlowManitra));
    freddy.setGroupFlows(List.of(groupFlowFreddy));
    doReturn(List.of(tolojanahary, axel))
        .when(userRepository)
        .findAllStudentNotDisabledWithGroupFlow(promotionH.getId());

    doReturn(List.of(manitra, freddy))
        .when(userRepository)
        .findAllStudentNotDisabledWithGroupFlow(promotionJ.getId());

    var studentsResultOverviewsH = subject.getStudentResultOverviewsToCrupdate(promotionH.getId());
    var studentsResultOverviewsJ = subject.getStudentResultOverviewsToCrupdate(promotionJ.getId());

    subject.saveAll(
        Stream.of(studentsResultOverviewsH, studentsResultOverviewsJ)
            .flatMap(Collection::stream)
            .toList());

    var apiClient = anApiClient(ADMIN1_TOKEN);
    var api = new UsersApi(apiClient);
    var actual =
        api.getStudentsResultOverviewsByStatus(promotionJ.getId(), IN_PROGRESS, null, null);
    var axelResultOverview =
        api.getStudentsResultOverviewsByStatus(promotionJ.getId(), INVALIDATED, null, null);
    assertNotNull(axelResultOverview);
    assertEquals(1, axelResultOverview.size());
    assertNotNull(actual);
    assertEquals(2, actual.size());
  }

  @Test
  void get_student_result_overviews_OK() throws ApiException {
    var apiClient = anApiClient(ADMIN1_TOKEN);
    var api = new UsersApi(apiClient);

    var studentsResultOverviews =
        api.getStudentsResultOverviewsByStatus(promotionH.getId(), VALIDATED, 1, 10);

    assertNotNull(studentsResultOverviews);
    assertEquals(1, studentsResultOverviews.size());
  }
}
