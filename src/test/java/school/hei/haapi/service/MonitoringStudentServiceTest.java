package school.hei.haapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.MonitoringStudentRepository;
import school.hei.haapi.repository.UserRepository;

public class MonitoringStudentServiceTest {
  private final UserRepository userRepository = mock();
  private final MonitoringStudentRepository monitoringStudentRepository = mock();
  private final MonitoringStudentService subject =
      new MonitoringStudentService(userRepository, mock(), mock(), monitoringStudentRepository);

  @Test
  void getStudentByIdAndMonitorId_with_unexistingStudentId_ko() {
    when(userRepository.findById("non-existing-student-id")).thenReturn(Optional.empty());

    var notFoundException =
        assertThrows(
            NotFoundException.class,
            () -> subject.getStudentByIdAndMonitorId("non-existing-student-id", "good-monitor-id"));
    assertEquals(
        "Student with id: non-existing-student-id does not exist", notFoundException.getMessage());
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

    var notFoundException =
        assertThrows(
            NotFoundException.class,
            () -> subject.getStudentByIdAndMonitorId(teacherId, "good-monitor-id"));
    assertEquals(
        "Student with id: %s does not exist".formatted(teacherId), notFoundException.getMessage());
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

    var notFoundException =
        assertThrows(
            NotFoundException.class,
            () -> subject.getStudentByIdAndMonitorId(studentId, monitorId));
    assertEquals(
        "Monitor with id: " + monitorId + " does not have a student with id: " + studentId,
        notFoundException.getMessage());
  }
}
