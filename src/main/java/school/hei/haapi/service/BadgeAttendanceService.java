package school.hei.haapi.service;

import static java.util.Comparator.comparing;
import static school.hei.haapi.endpoint.rest.model.AttendanceStatus.PRESENT;
import static school.hei.haapi.endpoint.rest.model.BadgeAttendanceResult.CHECKED;
import static school.hei.haapi.endpoint.rest.model.BadgeAttendanceResult.NOT_PARTICIPANT;
import static school.hei.haapi.endpoint.rest.model.BadgeAttendanceResult.NO_COURSE_IN_PROGRESS;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.hei.haapi.endpoint.rest.model.BadgeAttendance;
import school.hei.haapi.endpoint.rest.model.BadgeAttendanceResult;
import school.hei.haapi.model.Event;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.EventParticipantRepository;
import school.hei.haapi.repository.dao.EventDao;

/** A teacher opening a scanned badge marks its student present to his course in progress. */
@Service
@AllArgsConstructor
public class BadgeAttendanceService {
  static final Duration OPENS_BEFORE_BEGIN = Duration.ofMinutes(15);
  private static final Duration LONGEST_COURSE = Duration.ofHours(12);

  private final StudentBadgeCodeService studentBadgeCodeService;
  private final EventDao eventDao;
  private final EventParticipantRepository eventParticipantRepository;

  @Transactional
  public BadgeAttendance checkAttendanceToCourseOf(User teacher, String publicId) {
    var student = studentBadgeCodeService.getValidBadge(publicId).getStudent();
    var coursesInProgress = coursesInProgressOf(teacher, Instant.now());
    if (coursesInProgress.isEmpty()) {
      return new BadgeAttendance().result(NO_COURSE_IN_PROGRESS);
    }
    for (var course : coursesInProgress) {
      var participant =
          eventParticipantRepository.findEventParticipantByParticipantIdAndEventId(
              student.getId(), course.getId());
      if (participant.isPresent()) {
        participant.get().setStatus(PRESENT);
        eventParticipantRepository.save(participant.get());
        return attendanceOf(CHECKED, course);
      }
    }
    return attendanceOf(NOT_PARTICIPANT, coursesInProgress.get(0));
  }

  /** The events of the courses of the teacher in progress, the last begun first. */
  List<Event> coursesInProgressOf(User teacher, Instant now) {
    return eventDao
        .findByCriteria(
            null,
            now.minus(LONGEST_COURSE),
            now.plus(OPENS_BEFORE_BEGIN),
            null,
            null,
            null,
            teacher.getId(),
            null)
        .stream()
        .filter(event -> event.getEndDatetime() != null && !event.getEndDatetime().isBefore(now))
        .sorted(comparing(Event::getBeginDatetime).reversed())
        .toList();
  }

  private static BadgeAttendance attendanceOf(BadgeAttendanceResult result, Event course) {
    return new BadgeAttendance()
        .result(result)
        .eventTitle(course.getTitle())
        .courseCode(course.getCourse() == null ? null : course.getCourse().getCode());
  }
}
