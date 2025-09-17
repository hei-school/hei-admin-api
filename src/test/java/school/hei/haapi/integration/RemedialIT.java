package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static school.hei.haapi.integration.StudentIT.student1;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.remedial1;
import static school.hei.haapi.integration.conf.TestUtils.remedial2;
import static school.hei.haapi.integration.conf.TestUtils.setUpCasdoor;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import static school.hei.haapi.integration.conf.TestUtils.setUpS3Service;
import static school.hei.haapi.integration.test_data.CourseAssignmentTestData.createCourseAssignment;
import static school.hei.haapi.integration.test_data.CourseTestData.prog1;
import static school.hei.haapi.integration.test_data.CourseTestData.prog2;
import static school.hei.haapi.integration.test_data.GroupTestData.g1;
import static school.hei.haapi.integration.test_data.GroupTestData.g2;
import static school.hei.haapi.integration.test_data.RemedialTestData.createRemedial;
import static school.hei.haapi.integration.test_data.StudentTestData.axel;
import static school.hei.haapi.integration.test_data.StudentTestData.tolojanahary;
import static school.hei.haapi.integration.test_data.TeacherTestData.toky;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import school.hei.haapi.endpoint.rest.api.RemedialsApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.CrupdateRemedial;
import school.hei.haapi.endpoint.rest.model.Remedial;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.CourseAssignment;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.CourseAssignmentRepository;
import school.hei.haapi.repository.CourseRepository;
import school.hei.haapi.repository.GroupRepository;
import school.hei.haapi.repository.RemedialRepository;
import school.hei.haapi.repository.UserRepository;

public class RemedialIT extends FacadeITMockedThirdParties {
  @Autowired RemedialRepository remedialRepository;
  @Autowired UserRepository userRepository;
  @Autowired CourseRepository courseRepository;
  @Autowired GroupRepository groupRepository;
  @Autowired CourseAssignmentRepository courseAssignmentRepository;

  private User studentAxel;
  private User studentTolojanahary;
  private Course courseProg1;
  private Course courseProg2;
  private User teacherToky;
  private CourseAssignment assign_prog1_toToky_forGroup1;
  private CourseAssignment assign_prog2_toToky_forGroup2;
  private Group groupG1;
  private Group groupG2;
  private school.hei.haapi.model.Remedial remedial_prog1;
  private school.hei.haapi.model.Remedial remedial_prog2;

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }

  private void setUpTestData() {
    groupG1 = g1();
    groupG2 = g2();
    studentAxel = axel();
    studentTolojanahary = tolojanahary();
    courseProg1 = prog1();
    courseProg2 = prog2();
    teacherToky = toky();
    assign_prog1_toToky_forGroup1 =
        createCourseAssignment(courseProg1, teacherToky, List.of(groupG1));
    assign_prog2_toToky_forGroup2 =
        createCourseAssignment(courseProg2, teacherToky, List.of(groupG2));
    remedial_prog1 = createRemedial(assign_prog1_toToky_forGroup1);
    remedial_prog2 = createRemedial(assign_prog1_toToky_forGroup1);
    remedial_prog1.setTitle(remedial1().getTitle());
    remedial_prog2.setTitle(remedial2().getTitle());
    remedial_prog1.setStudents(List.of(studentAxel, studentTolojanahary));
    remedial_prog2.setStudents(List.of(studentAxel, studentTolojanahary));
    userRepository.saveAll(List.of(studentAxel, studentTolojanahary));
    groupRepository.saveAll(List.of(groupG1, groupG2));
    userRepository.saveAll(List.of(teacherToky));
    courseRepository.saveAll(List.of(courseProg1, courseProg2));
    courseAssignmentRepository.saveAll(
        List.of(assign_prog1_toToky_forGroup1, assign_prog2_toToky_forGroup2));
    remedialRepository.saveAll(List.of(remedial_prog1, remedial_prog2));
  }

  @BeforeEach
  void setUp() {
    setUpTestData();
    setUpCasdoor(casdoorAuthServiceMock, certificateLoaderMock);
    setUpCognito(cognitoComponentMock);
    setUpS3Service(fileService, student1());
  }

  @Test
  public void manager_read_remedial_details_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    RemedialsApi api = new RemedialsApi(manager1Client);
    List<Remedial> remedials =
        api.getAllRemedials(
            null, null, null, null, Instant.parse("2024-07-22T10:15:30Z"), Instant.now(), 1, 15);
    Remedial remedial = remedials.getFirst();
    assertNotNull(remedial.getCourse());
    assertEquals(prog1().getName(), remedial.getCourse().getName());
  }

  @Test
  public void manager_get_all_remedials_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    RemedialsApi api = new RemedialsApi(manager1Client);
    List<Remedial> remedials =
        api.getAllRemedials(
            null, null, null, null, Instant.parse("2024-07-22T10:15:30Z"), Instant.now(), 1, 10);
    assertEquals(2, remedials.size());
    assertEquals("Remedial 2", remedials.getFirst().getTitle());
  }

  @Test
  public void manager_read_remedial_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    RemedialsApi api = new RemedialsApi(manager1Client);
    String remedial1Id = remedial_prog1.getId();
    Remedial actual = api.getRemedialById(remedial1Id);
    assertDoesNotThrow(() -> api.getRemedialById(remedial1Id));
    assertEquals(remedial1().getTitle(), actual.getTitle());
  }

  @Test
  public void teacher_create_or_update_remedial_ok() throws ApiException {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
    RemedialsApi api = new RemedialsApi(teacher1Client);
    var remedial = remedial_prog1;
    CrupdateRemedial crupdateRemedial = new CrupdateRemedial();
    crupdateRemedial.setId(remedial_prog1.getId());
    crupdateRemedial.setTitle(remedial_prog1.getTitle());
    crupdateRemedial.setCourseId(remedial_prog1.getCourseAssignment().getId());
    crupdateRemedial.setRemedialDate(remedial_prog1.getRemedialDate());
    var actualCreate = api.createOrUpdateRemedialInfos(crupdateRemedial);
    assertEquals(remedial_prog1.getTitle(), actualCreate.getTitle());
    assertNotNull(actualCreate.getCourse());
    assertEquals("Algorithmique", actualCreate.getCourse().getName());
  }
}
