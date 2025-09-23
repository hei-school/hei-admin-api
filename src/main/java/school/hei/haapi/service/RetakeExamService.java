package school.hei.haapi.service;

import static school.hei.haapi.endpoint.rest.model.CourseResultStatus.INCOMPLETE;

import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
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
import school.hei.haapi.repository.RetakeExamRepository;
import school.hei.haapi.repository.dao.RetakeExamDao;

@Component
@AllArgsConstructor
public class RetakeExamService {
  private final RetakeExamRepository retakeExamRepository;
  private final RetakeExamSessionService retakeExamSessionService;
  private final RetakeExamMapper retakeExamMapper;
  private final GradeResultService gradeResultService;
  private final CourseMapper courseMapper;
  private final RetakeExamDao retakeExamDao;

  public List<RetakeExam> crupdateRetakeExams(
      String sessionId, List<CrupdateRetakeExam> crupdateRetakeExams) {
    RetakeExamSession retakeExamSession = retakeExamSessionService.getById(sessionId);
    if (retakeExamSession != null) {
      crupdateRetakeExams.forEach(
          crupdateRetakeExam -> crupdateRetakeExam.sessionId(retakeExamSession.getId()));
    }
    List<CrupdateRetakeExam> crupdateRetakeExamsNotExisted =
        crupdateRetakeExams.stream()
            .filter(crupdateRetakeExam -> !isExisted(crupdateRetakeExam))
            .toList();
    return retakeExamRepository.saveAll(
        retakeExamMapper.toDoMainList(crupdateRetakeExamsNotExisted));
  }

  private Boolean isExisted(CrupdateRetakeExam crupdateRetakeExam) {
    return retakeExamRepository
        .findByCourse_IdAndStudent_IdAndSession_Id(
            crupdateRetakeExam.getStudentId(),
            crupdateRetakeExam.getCourseId(),
            crupdateRetakeExam.getSessionId())
        .isPresent();
  }

  public List<RetakeExam> getStudentRetakeExams(String sessionId, String studentId) {
    var session = retakeExamSessionService.getById(sessionId);
    var coursesToRetake = getCourseResultToRetake(studentId, session);

    return coursesToRetake.stream()
        .map(courseResult -> courseResultAndSessionToRetake(courseResult, session))
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

  private List<CourseResult> getCourseResultToRetake(String studentId, RetakeExamSession session) {
    List<YearlyResult> yearlyResults =
        gradeResultService.getStudentResultSummary(studentId).getYearlyResults();
    List<CourseResult> toRetakeCourse =
        yearlyResults.stream()
            .filter(Objects::nonNull)
            .map(YearlyResult::getCourseResults)
            .filter(Objects::nonNull)
            .flatMap(List::stream)
            .filter(courseResult -> INCOMPLETE.equals(courseResult.getStatus()))
            .toList();
    List<String> alreadyRetakenCourseIds =
        retakeExamRepository
            .findRetakeExamsBySession_IdAndStudent_Id(session.getId(), studentId)
            .stream()
            .map(r -> r.getCourse().getId())
            .toList();

    return toRetakeCourse.stream()
        .filter(courseResult -> !alreadyRetakenCourseIds.contains(courseResult.getCourse().getId()))
        .toList();
  }

  public List<RetakeExam> getAllRetakeExamBySessionId(
      String sessionId, PageFromOne page, BoundedPageSize pageSize) {
    Pageable pageable = PageRequest.of(page.getValue() - 1, pageSize.getValue());
    return retakeExamDao.filterByCriteria(sessionId, null, pageable);
  }
}
