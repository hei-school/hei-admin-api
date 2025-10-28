package school.hei.haapi.service;

import static school.hei.haapi.endpoint.rest.model.AttendanceStatus.MISSING;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.rest.model.AttendanceStatus;
import school.hei.haapi.model.StudentAttendance;
import school.hei.haapi.model.exception.NotImplementedException;
import school.hei.haapi.repository.EventRepository;
import school.hei.haapi.repository.UserRepository;

@Service
@AllArgsConstructor
public class AttendanceService {
  private final EventRepository eventRepository;
  private final UserRepository userRepository;

  public List<StudentAttendance> getStudentAttendanceByStudentId(
      String studentId,
      AttendanceStatus status,
      Instant from,
      Instant to,
      @NonNull Collection<String> titles) {

    if (titles.size() > 1) throw new NotImplementedException("Titles filter can't be more than 1");

    var student = userRepository.findById(studentId).orElseThrow();
    return eventRepository.findStudentAttendance(
        student.getRef(),
        status == null ? MISSING : status,
        from,
        to,
        titles.stream().findFirst().orElse(null));
  }
}
