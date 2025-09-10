package school.hei.haapi.service;

import static school.hei.haapi.endpoint.rest.model.AttendanceStatus.MISSING;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.rest.model.AttendanceStatus;
import school.hei.haapi.endpoint.rest.model.EventType;
import school.hei.haapi.model.StudentAttendanceStatus;
import school.hei.haapi.model.exception.NotImplementedException;
import school.hei.haapi.repository.EventRepository;
import school.hei.haapi.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class AttendanceService {
  private final EventRepository eventRepository;
  private final UserRepository userRepository;

  public List<StudentAttendanceStatus> getStudentAttendanceByStudentId(
      String studentId,
      AttendanceStatus actualAttendanceStatus,
      Instant from,
      Instant to,
      @NonNull Collection<String> titles) {

    if (titles.size() > 1) throw new NotImplementedException("Titles filter can't be more than 1");

    var student = userRepository.findById(studentId).orElseThrow();
    var studentReference = student.getRef();
    var attendanceStatus =
        actualAttendanceStatus == null ? MISSING.name() : actualAttendanceStatus.name();
    var studentAttendanceObject =
        eventRepository.getStudentAttendance(
            studentReference,
            attendanceStatus,
            from,
            to,
            "%" + titles.stream().findFirst().orElse("") + "%");
    return studentAttendanceObject.stream().map(this::toStudentAttendanceStatus).toList();
  }

  private StudentAttendanceStatus toStudentAttendanceStatus(Object[] objElement) {
    return new StudentAttendanceStatus(
        (String) objElement[0],
        (String) objElement[1],
        EventType.valueOf((String) objElement[2]),
        AttendanceStatus.valueOf((String) objElement[3]),
        (Instant) objElement[4],
        (Instant) objElement[5]);
  }
}
