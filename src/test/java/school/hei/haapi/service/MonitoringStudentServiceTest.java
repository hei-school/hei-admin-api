package school.hei.haapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.repository.MonitoringStudentRepository;
import school.hei.haapi.repository.UserRepository;

public class MonitoringStudentServiceTest {
  private final UserRepository userRepository = mock();
  private final MonitoringStudentRepository monitoringStudentRepository = mock();
  private final MonitoringStudentService subject = new MonitoringStudentService(userRepository, mock(), mock(), monitoringStudentRepository);

  @Test
  void getStudentByIdAndMonitorId_with_unexistingStudentId_ko() {
    when(userRepository.findById("non-existing-student-id")).thenReturn(Optional.empty());

    assertBadRequestException(
        "Student with id: non-existing-student-id does not exist",
        () -> subject.getStudentByIdAndMonitorId("non-existing-student-id", "good-monitor-id"));
  }

  @Test
  void getStudentByIdAndMonitorId_with_nonStudentId_ko() {
    var teacherId = "some-teacher-id";
    when(userRepository.findById(teacherId))
        .thenReturn(
            Optional.of(
                User.builder()
                    .id(teacherId)
                    .email("teacher-email@mail.hei.school")
                    .entranceDatetime(Instant.parse("2021-01-01T00:00:00Z"))
                    .role(User.Role.TEACHER)
                    .build()));

    assertBadRequestException(
        "Student with id: %s does not exist".formatted(teacherId),
        () -> subject.getStudentByIdAndMonitorId(teacherId, "good-monitor-id"));
  }

  @Test
  void getStudentByIdAndMonitorId_with_unlinkedMonitorAndStudent_ko() {
    var studentId = "good-student-id";
    var monitorId = "good-monitor-id";
    when(userRepository.findById(studentId))
        .thenReturn(
            Optional.of(
                User.builder()
                    .id(studentId)
                    .email("some-unique-email@gmail.com")
                    .entranceDatetime(Instant.parse("2021-01-01T00:00:00Z"))
                    .role(User.Role.STUDENT)
                    .build()));
    when(monitoringStudentRepository.getAllMonitorsIdsByStudentId(studentId)).thenReturn(List.of());

    assertBadRequestException(
        "Monitor with id: " + monitorId + " does not have a student with id: " + studentId,
        () -> subject.getStudentByIdAndMonitorId(studentId, "good-monitor-id"));
  }

  private static void assertBadRequestException(String expectedMessage, Executable executable) {
    var exception = assertThrows(BadRequestException.class, executable);
    assertEquals(expectedMessage, exception.getMessage());
  }
}
