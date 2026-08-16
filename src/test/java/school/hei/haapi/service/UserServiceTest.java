package school.hei.haapi.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static school.hei.haapi.integration.conf.TestMocks.setUpEventBridge;
import static school.hei.haapi.integration.conf.TestMocks.setUpS3Service;
import static school.hei.haapi.integration.testData.MonitorTestData.monitorOfAxel;
import static school.hei.haapi.integration.testData.StudentTestData.axel;
import static school.hei.haapi.model.User.Role.STUDENT;
import static school.hei.haapi.model.dto.MonitorStudentLinkDto.Status.LINKED;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.MonitoringStudentRepository;
import school.hei.haapi.repository.UserRepository;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

class UserServiceTest extends FacadeITMockedThirdParties {
  @Autowired private UserService subject;
  @Autowired private UserRepository userRepository;
  @Autowired private MonitoringStudentRepository monitoringStudentRepository;
  @Autowired private JdbcTemplate jdbcTemplate;
  @MockBean private EventBridgeClient eventBridgeClientMock;

  private User student;
  private User monitor;

  @BeforeEach
  void setUp() {
    setUpEventBridge(eventBridgeClientMock);

    monitor = userRepository.save(monitorOfAxel());
    student = userRepository.save(axel());
    monitoringStudentRepository.saveMonitorFollowingStudents(
        monitor.getId(), List.of(student.getId()), LINKED.toString());

    setUpS3Service(fileService, student);
  }

  @AfterEach
  void tearDown() {
    // the repository only knows how to create the link, so the sweep goes through the table
    jdbcTemplate.update(
        "DELETE FROM monitor_following_student WHERE monitor_id = ?", monitor.getId());
    userRepository.deleteAll(List.of(student, monitor));
  }

  @Test
  void monitors_is_retrieve_by_student_id_ok() {
    var actualMonitorsId =
        subject.findMonitorsByStudentId(student.getId()).stream().map(User::getId).toList();

    assertEquals(1, actualMonitorsId.size());
    assertTrue(actualMonitorsId.contains(monitor.getId()));
  }

  @Test
  void dao_can_handle_null_value_in_params() {
    var students =
        assertDoesNotThrow(
            () ->
                subject.getByCriteria(
                    STUDENT,
                    null,
                    null,
                    null,
                    new PageFromOne(1),
                    new BoundedPageSize(15),
                    null,
                    null));

    assertTrue(students.size() <= 15);
    assertTrue(students.stream().allMatch(user -> STUDENT.equals(user.getRole())));
  }

  @Test
  void disabled_user_enable_ko() {
    var disabled = subject.getById(student.getId());
    disabled.setStatus(User.Status.DISABLED);

    assertThrows(
        IllegalArgumentException.class,
        () -> {
          disabled.setStatus(User.Status.ENABLED);
        });
  }
}
