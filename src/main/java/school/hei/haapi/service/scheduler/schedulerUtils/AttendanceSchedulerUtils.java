package school.hei.haapi.service.scheduler.schedulerUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.CourseSession;
import school.hei.haapi.model.GroupFlow;
import school.hei.haapi.model.StudentAttendance;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.AttendanceRepository;
import school.hei.haapi.repository.CourseSessionRepository;

@Component
@AllArgsConstructor
public class AttendanceSchedulerUtils {
  private final CourseSessionRepository courseSessionRepository;
  private final AttendanceRepository attendanceRepository;

  public User toUserUtils(StudentAttendance studentAttendance) {
    return User.builder()
        .id(studentAttendance.getStudent().getId())
        .sex(studentAttendance.getStudent().getSex())
        .firstName(studentAttendance.getStudent().getFirstName())
        .lastName(studentAttendance.getStudent().getLastName())
        .email(studentAttendance.getStudent().getEmail())
        .phone(studentAttendance.getStudent().getPhone())
        .birthDate(studentAttendance.getStudent().getBirthDate())
        .entranceDatetime(studentAttendance.getStudent().getEntranceDatetime())
        .status(studentAttendance.getStudent().getStatus())
        .role(studentAttendance.getStudent().getRole())
        .build();
  }

  public User toUserUtils(GroupFlow groupFlow) {
    return User.builder()
        .id(groupFlow.getStudent().getId())
        .sex(groupFlow.getStudent().getSex())
        .firstName(groupFlow.getStudent().getFirstName())
        .lastName(groupFlow.getStudent().getLastName())
        .email(groupFlow.getStudent().getEmail())
        .phone(groupFlow.getStudent().getPhone())
        .birthDate(groupFlow.getStudent().getBirthDate())
        .entranceDatetime(groupFlow.getStudent().getEntranceDatetime())
        .status(groupFlow.getStudent().getStatus())
        .role(groupFlow.getStudent().getRole())
        .build();
  }

  public List<StudentAttendance> findStudentEscapeCoursePredicate(CourseSession talkingAbout) {
    Instant endDelay =
        LocalDateTime.ofInstant(talkingAbout.getEnd(), ZoneId.of("UTC+3"))
            .minusHours(1)
            .atZone(ZoneId.of("UTC+3"))
            .toInstant();
    return attendanceRepository.findStudentEscape(talkingAbout.getBegin(), endDelay);
  }

  public List<CourseSession> findCourseSessionOfTheDay() {
    Instant beginDate = LocalDate.now().atStartOfDay().atZone(ZoneId.of("UTC+3")).toInstant();
    Instant endDate =
        LocalDate.now().plusDays(1).atStartOfDay().atZone(ZoneId.of("UTC+3")).toInstant();

    return courseSessionRepository.findCoursesSessionsOfTheDay(beginDate, endDate);
  }

  public List<StudentAttendance> findStudentAttendanceOfCourseSession(CourseSession courseSession) {
    Instant begin =
        LocalDateTime.ofInstant(courseSession.getBegin(), ZoneId.of("UTC+3"))
            .minusHours(1)
            .minusMinutes(15)
            .atZone(ZoneId.of("UTC+3"))
            .toInstant();
    Instant end =
        LocalDateTime.ofInstant(courseSession.getEnd(), ZoneId.of("UTC+3"))
            .minusMinutes(45)
            .atZone(ZoneId.of("UTC+3"))
            .toInstant();
    return attendanceRepository.findStudentAttendancesBetween(begin, end);
  }
}
