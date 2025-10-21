package school.hei.haapi.service;

import static school.hei.haapi.endpoint.rest.model.AttendanceStatus.MISSING;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.rest.model.AttendanceStatus;
import school.hei.haapi.endpoint.rest.model.EventType;
import school.hei.haapi.model.Event.PlaceName;
import school.hei.haapi.model.Event.RoomName;
import school.hei.haapi.model.StudentAttendanceStatus;
import school.hei.haapi.model.exception.NotImplementedException;
import school.hei.haapi.repository.EventRepository;
import school.hei.haapi.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class AttendanceService {
  private final EventRepository eventRepository;
  private final UserRepository userRepository;
  private static final int EVENT_TITLE = 0;
  private static final int EVENT_DESCRIPTION = 1;
  private static final int EVENT_TYPE = 2;
  private static final int ATTENDANCE_STATUS = 3;
  private static final int BEGIN_DATETIME = 4;
  private static final int END_DATETIME = 5;
  private static final int ROOM = 6;
  private static final int PLACE = 7;

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
            titles.stream().findFirst().map("%%%s%%"::formatted).orElse(null));
    return studentAttendanceObject.stream().map(this::toStudentAttendanceStatus).toList();
  }

  private StudentAttendanceStatus toStudentAttendanceStatus(Object[] objElement) {
    return new StudentAttendanceStatus(
        (String) objElement[EVENT_TITLE],
        (String) objElement[EVENT_DESCRIPTION],
        enumFormObject(objElement[EVENT_TYPE], EventType::valueOf),
        enumFormObject(objElement[ATTENDANCE_STATUS], AttendanceStatus::valueOf),
        (Instant) objElement[BEGIN_DATETIME],
        (Instant) objElement[END_DATETIME],
        enumFormObject(objElement[ROOM], RoomName::valueOf),
        enumFormObject(objElement[PLACE], PlaceName::valueOf));
  }

  private static <T> T enumFormObject(Object object, Function<String, T> toEnum) {
    return Optional.ofNullable((String) object).map(toEnum).orElse(null);
  }
}
