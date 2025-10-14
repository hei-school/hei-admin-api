package school.hei.haapi.service;

import static school.hei.haapi.endpoint.rest.model.CourseResultStatus.INCOMPLETE;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.endpoint.rest.mapper.RetakeExamMapper;
import school.hei.haapi.endpoint.rest.model.Course;
import school.hei.haapi.endpoint.rest.model.CourseResult;
import school.hei.haapi.endpoint.rest.model.CrupdateRetakeExam;
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
  private final RetakeExamMapper retakeExamMapper;
  private final GradeResultService gradeResultService;
  private final CourseMapper courseMapper;
  private final RetakeExamDao retakeExamDao;
  private final PaginationFromPageAndPageSize paginationFromPageAndPageSize;

  public List<RetakeExam> crupdateRetakeExams(
      String sessionId, List<CrupdateRetakeExam> crupdateRetakeExams) {
    RetakeExamSession retakeExamSession = retakeExamSessionService.getById(sessionId);
    if (retakeExamSession != null) {
      crupdateRetakeExams.forEach(
          crupdateRetakeExam -> crupdateRetakeExam.sessionId(retakeExamSession.getId()));
    }
    List<CrupdateRetakeExam> crupdateRetakeExamsNotExisting =
        crupdateRetakeExams.stream()
            .filter(crupdateRetakeExam -> !isExisting(crupdateRetakeExam))
            .toList();
    return retakeExamRepository.saveAll(
        retakeExamMapper.toDoMainList(crupdateRetakeExamsNotExisting));
  }

  private Boolean isExisting(CrupdateRetakeExam crupdateRetakeExam) {
    return retakeExamRepository
        .findByCourse_IdAndStudent_IdAndSession_Id(
            crupdateRetakeExam.getCourseId(),
            crupdateRetakeExam.getStudentId(),
            crupdateRetakeExam.getSessionId())
        .isPresent();
  }

  public List<RetakeExam> getStudentRetakeExams(String sessionId, String studentId) {
    var session = retakeExamSessionService.getById(sessionId);
    var coursesToRetake = getCourseResultToRetake(studentId);
    var existingRetakeExams =
        retakeExamRepository.findRetakeExamsBySession_IdAndStudent_Id(session.getId(), studentId);
    var retakeExams =
        new ArrayList<>(
            coursesToRetake.stream()
                .map(courseResult -> courseResultAndSessionToRetake(courseResult, session))
                .filter(newRetakeExam -> !isRetakeIn(newRetakeExam, existingRetakeExams, studentId))
                .toList());
    retakeExams.addAll(existingRetakeExams);
    return retakeExams;
  }

  private boolean isRetakeIn(
      RetakeExam newExam, List<RetakeExam> existingRetakeExams, String studentId) {
    return existingRetakeExams.stream()
        .anyMatch(
            existing ->
                existing.getCourse().getId().equals(newExam.getCourse().getId())
                    && existing.getSession().getId().equals(newExam.getSession().getId())
                    && existing.getStudent().getId().equals(studentId));
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

    return yearlyResults.stream()
        .filter(Objects::nonNull)
        .map(YearlyResult::getCourseResults)
        .filter(Objects::nonNull)
        .flatMap(List::stream)
        .filter(courseResult -> INCOMPLETE.equals(courseResult.getStatus()))
        .toList();
  }

  public List<RetakeExam> getAllRetakeExamBySessionId(
      String sessionId,
      List<RetakeExamStatus> statuses,
      PageFromOne page,
      BoundedPageSize pageSize) {
    Pageable pageable = PageRequest.of(page.getValue() - 1, pageSize.getValue());
    return retakeExamDao.filterByCriteria(sessionId, null, null, null, null, statuses, pageable);
  }

  public List<school.hei.haapi.model.Course> getAllRetakeExamCoursesBySessionId(
      String sessionId, String courseCode, PageFromOne page, BoundedPageSize pageSize) {
    var pageable = paginationFromPageAndPageSize.apply(page, pageSize);
    return retakeExamDao
        .filterByCriteria(sessionId, null, null, null, courseCode, null, pageable)
        .stream()
        .map(RetakeExam::getCourse)
        .distinct()
        .sorted(Comparator.comparing(school.hei.haapi.model.Course::getCode))
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
