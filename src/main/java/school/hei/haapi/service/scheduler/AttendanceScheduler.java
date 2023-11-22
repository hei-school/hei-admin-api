package school.hei.haapi.service.scheduler;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import school.hei.haapi.endpoint.rest.model.AttendanceMovementType;
import school.hei.haapi.model.CourseSession;
import school.hei.haapi.model.StudentAttendance;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.AttendanceRepository;
import school.hei.haapi.service.UserService;
import school.hei.haapi.service.scheduler.schedulerUtils.AttendanceSchedulerUtils;

import static java.util.stream.Collectors.*;

@Component
@AllArgsConstructor
@EnableAsync
public class AttendanceScheduler {
  private final AttendanceRepository attendanceRepository;
  private final AttendanceSchedulerUtils attendanceSchedulerUtils;
  private final UserService userService;
  /*
   * Take all the lessons of the day, look at the pupils concerned by group.
   * Take all the day's timetables, filter the timetables by lesson time.
   * Now look to see if the pupils in each group have punched in, if not absent.
   */

  @Async
  @Scheduled(fixedDelay = 60000)
  @Transactional
  public synchronized void checkAttendancesEachDays() {
    System.out.println("scheduler running at " + LocalDateTime.now().getHour() + ":" +
        LocalDateTime.now().getMinute() + " ...");
    List<CourseSession> courseSessions = attendanceSchedulerUtils.findCourseSessionOfTheDay();

    courseSessions.forEach(courseSession -> {
      List<User> expectedParticipant = userService
          .getByGroupId(courseSession.getAwardedCourse().getGroup().getId());
      expectedParticipant.forEach(participant -> {
        checkPresentAndAbsent(participant, courseSession);
      });
    });
  }

  @Transactional
  public void checkPresentAndAbsent(User participant, CourseSession courseSession) {
    List<User> hisStudentAttends = attendanceSchedulerUtils
        .findStudentAttendanceOfCourseSession(courseSession)
        .stream().map(attendanceSchedulerUtils::toUserUtils)
        .collect(toList());

    List<User> studentEscapePredicate = attendanceSchedulerUtils
        .findStudentEscapeCoursePredicate(courseSession)
        .stream().map(attendanceSchedulerUtils::toUserUtils)
        .collect(toList());

    if (
        hisStudentAttends.contains(participant) &&
            !studentEscapePredicate.contains(participant)
    ) {
      insertPresentParticipant(participant, courseSession);
    }
    if (!attendanceRepository.findStudentAttendanceByCourseSessionAndStudent(courseSession, participant).isPresent()) {
      insertAbsentParticipant(participant, courseSession);
    }
  }

  @Transactional
  public void insertAbsentParticipant(User participant, CourseSession courseSession) {
    StudentAttendance absent = StudentAttendance.builder()
        .courseSession(courseSession)
        .student(participant)
        .attendanceMovementType(AttendanceMovementType.IN)
        .build();
    attendanceRepository.save(absent);
  }

  @Transactional
  public void insertPresentParticipant(User participant, CourseSession courseSession) {
    Instant begin = LocalDateTime.ofInstant(courseSession.getBegin(), ZoneId.of("UTC+3"))
        .minusHours(1)
        .atZone(ZoneId.of("UTC+3"))
        .toInstant();
    Instant end = LocalDateTime.ofInstant(courseSession.getBegin(), ZoneId.of("UTC+3"))
        .plusMinutes(45)
        .atZone(ZoneId.of("UTC+3"))
        .toInstant();
    Optional<StudentAttendance> predicate  = attendanceRepository
        .findStudentAttendanceFromAndToAndStudent(
            begin,
            end,
            participant.getId());

    if(predicate.isPresent()) {
      StudentAttendance attendance = predicate.get();
      attendance.setLate(attendance.isLateFrom(courseSession.getBegin()));
      attendance.setCourseSession(courseSession);
      attendanceRepository.save(attendance);
    }
  }
}