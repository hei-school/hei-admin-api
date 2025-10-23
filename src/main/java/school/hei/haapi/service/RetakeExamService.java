package school.hei.haapi.service;

import static java.time.Instant.now;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static school.hei.haapi.endpoint.rest.model.CourseResultStatus.INCOMPLETE;
import static school.hei.haapi.model.RetakeExamStatus.CANCELED;
import static school.hei.haapi.model.RetakeExamStatus.INVALIDATE;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.endpoint.rest.model.Course;
import school.hei.haapi.endpoint.rest.model.CourseResult;
import school.hei.haapi.endpoint.rest.model.YearlyResult;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.RetakeExam;
import school.hei.haapi.model.RetakeExamSession;
import school.hei.haapi.model.RetakeExamStatus;
import school.hei.haapi.model.pagination.PaginationFromPageAndPageSize;
import school.hei.haapi.repository.RetakeExamRepository;
import school.hei.haapi.repository.dao.RetakeExamDao;

@Service
@AllArgsConstructor
public class RetakeExamService {
  private final RetakeExamRepository retakeExamRepository;
  private final RetakeExamSessionService retakeExamSessionService;
  private final GradeResultService gradeResultService;
  private final CourseMapper courseMapper;
  private final RetakeExamDao retakeExamDao;
  private final PaginationFromPageAndPageSize paginationFromPageAndPageSize;

  public List<RetakeExam> crupdateRetakeExams(List<RetakeExam> crupdateRetakeExams) {
    return retakeExamRepository.saveAll(crupdateRetakeExams);
  }

  public List<RetakeExam> getStudentRetakeExams(String sessionId, String studentId) {
    var session = retakeExamSessionService.getById(sessionId);
    var coursesToRetake = getCourseResultToRetake(studentId);

    if (session.getDateTo().isBefore(now())) {
      return retakeExamRepository.findRetakeExamsBySession_IdAndStudent_Id(
          session.getId(), studentId);
    }

    var futureSessionRetakeExams =
        retakeExamRepository.findRetakeExamByStudent_IdAndStatusIsNotInAndSession_DateToGreaterThan(
            studentId, List.of(INVALIDATE, CANCELED), now());

    var existingCourseIds =
        futureSessionRetakeExams.stream()
            .map(exam -> exam.getCourse().getId())
            .collect(toUnmodifiableSet());

    var existingRetakeExamsInCurrentSession =
        futureSessionRetakeExams.stream()
            .filter(exam -> exam.getSession().getId().equals(sessionId))
            .toList();

    var newRetakeExams =
        coursesToRetake.stream()
            .filter(courseResult -> !existingCourseIds.contains(courseResult.getCourse().getId()))
            .map(courseResult -> courseResultAndSessionToRetake(courseResult, session))
            .toList();

    // TODO: separate the responsibility of the endpoint
    //  - one for reading what's in the database
    //  - one for determining what needs retake
    return Stream.concat(newRetakeExams.stream(), existingRetakeExamsInCurrentSession.stream())
        .toList();
  }

  private RetakeExam courseResultAndSessionToRetake(
      CourseResult courseResult, RetakeExamSession session) {
    Course course = courseResult.getCourse();
    if (course == null) {
      throw new IllegalStateException("Course must not be null for CourseResult: " + courseResult);
    }
    RetakeExam retakeExam = new RetakeExam();
    retakeExam.setCourse(courseMapper.toDomain(course));
    retakeExam.setSession(session);
    return retakeExam;
  }

  private List<CourseResult> getCourseResultToRetake(String studentId) {
    List<YearlyResult> yearlyResults =
        gradeResultService.getStudentResultSummary(studentId).getYearlyResults();

    if (yearlyResults == null) {
      throw new IllegalStateException(
          "No yearly result generated for this student with id : %s".formatted(studentId));
    }

    return yearlyResults.stream()
        .filter(Objects::nonNull)
        .map(YearlyResult::getCourseResults)
        .filter(Objects::nonNull)
        .flatMap(List::stream)
        .filter(courseResult -> INCOMPLETE.equals(courseResult.getStatus()))
        .toList();
  }

  public List<RetakeExam> getAllRetakeExams(
      List<RetakeExamStatus> statuses, PageFromOne page, BoundedPageSize pageSize) {
    Pageable pageable = PageRequest.of(page.getValue() - 1, pageSize.getValue());
    return retakeExamDao.filterByCriteria(null, null, null, null, null, statuses, pageable);
  }

  public List<school.hei.haapi.model.Course> getAllRetakeExamCoursesBySessionId(
      String sessionId, String courseCode, PageFromOne page, BoundedPageSize pageSize) {
    var pageable = paginationFromPageAndPageSize.apply(page, pageSize);
    return retakeExamDao
        .filterByCriteria(sessionId, null, null, null, courseCode, null, pageable)
        .stream()
        .map(RetakeExam::getCourse)
        .distinct()
        .sorted(comparing(school.hei.haapi.model.Course::getCode))
        .toList();
  }

  public List<school.hei.haapi.model.User> getAllRetakeExamParticipantByCourseAndBySessionId(
      String sessionId,
      String courseId,
      String studentRef,
      PageFromOne page,
      BoundedPageSize pageSize) {
    var pageable = paginationFromPageAndPageSize.apply(page, pageSize);
    var retakeExams =
        retakeExamDao.filterByCriteria(sessionId, null, studentRef, courseId, null, null, pageable);
    return retakeExams.stream().map(RetakeExam::getStudent).toList();
  }
}
