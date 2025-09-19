package school.hei.haapi.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.endpoint.rest.mapper.RetakeExamMapper;
import school.hei.haapi.endpoint.rest.model.Course;
import school.hei.haapi.endpoint.rest.model.CourseResult;
import school.hei.haapi.endpoint.rest.model.CrupdateRetakeExam;
import school.hei.haapi.endpoint.rest.model.StudentLevel;
import school.hei.haapi.endpoint.rest.model.YearlyResult;
import school.hei.haapi.model.CourseAssignment;
import school.hei.haapi.model.RetakeExam;
import school.hei.haapi.model.RetakeExamSession;
import school.hei.haapi.repository.RetakeExamRepository;

@Component
public class RetakeExamService {
  @Autowired RetakeExamRepository retakeExamRepository;
  @Autowired RetakeExamSessionService retakeExamSessionService;
  @Autowired RetakeExamMapper retakeExamMapper;
  @Autowired GradeResultService gradeResultService;
  @Autowired CourseAssignmentService courseAssignmentService;
  @Autowired private CourseMapper courseMapper;

  public List<RetakeExam> crupdateRetakeExams(
      @PathVariable String sessionId, List<CrupdateRetakeExam> crupdateRetakeExams) {
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
    List<CourseAssignment> courses = courseAssignmentService.getByStudentId(studentId);
    Set<StudentLevel> studentLevels =
        courses.stream()
            .map(course -> course.getCourse().getStudentLevel())
            .collect(Collectors.toSet());

    List<YearlyResult> yearlyResults =
        studentLevels.stream()
            .map(level -> gradeResultService.getLeveledYearlyResultByStudentId(level, studentId))
            .filter(Objects::nonNull)
            .toList();

    List<CourseResult> coursesToRetake =
        yearlyResults.stream()
            .filter(Objects::nonNull)
            .map(YearlyResult::getCourseResults)
            .filter(Objects::nonNull)
            .flatMap(List::stream)
            .filter(
                courseResult ->
                    courseResult.getWeightedAverage() != null
                        && courseResult.getWeightedAverage().compareTo(BigDecimal.TEN) <= 0)
            .toList();

    return coursesToRetake.stream()
        .map(
            courseResult -> {
              Course course = courseResult.getCourse();
              if (course == null) {
                throw new IllegalStateException(
                    "Course must not be null for CourseResult: " + courseResult);
              }
              RetakeExam retakeExam = new RetakeExam();
              retakeExam.setCourse(courseMapper.toDomain(course));
              retakeExam.setSession(session);
              return retakeExam;
            })
        .toList();
  }
}
