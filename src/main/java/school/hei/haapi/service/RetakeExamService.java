package school.hei.haapi.service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.endpoint.rest.mapper.RetakeExamMapper;
import school.hei.haapi.endpoint.rest.model.CourseResult;
import school.hei.haapi.endpoint.rest.model.CrupdateRetakeExam;
import school.hei.haapi.endpoint.rest.model.StudentLevel;
import school.hei.haapi.endpoint.rest.model.YearlyResult;
import school.hei.haapi.model.CourseAssignment;
import school.hei.haapi.model.RetakeExam;
import school.hei.haapi.model.RetakeExamSession;
import school.hei.haapi.repository.RetakeExamRepository;
import school.hei.haapi.repository.RetakeExamSessionRepository;

@Component
public class RetakeExamService {
  @Autowired RetakeExamRepository retakeExamRepository;
  @Autowired RetakeExamSessionRepository retakeExamSessionRepository;
  @Autowired RetakeExamMapper retakeExamMapper;
  @Autowired GradeResultService gradeResultService;
  @Autowired CourseAssignmentService courseAssignmentService;
  @Autowired private CourseMapper courseMapper;

  public List<RetakeExam> crupdateRetakeExams(
      @PathVariable String sessionId, List<CrupdateRetakeExam> crupdateRetakeExams) {
    Optional<RetakeExamSession> retakeExamSession = retakeExamSessionRepository.findById(sessionId);
    if (retakeExamSession.isPresent()) {
      crupdateRetakeExams.forEach(
          crupdateRetakeExam -> crupdateRetakeExam.sessionId(String.valueOf(retakeExamSession)));
    }
    List<CrupdateRetakeExam> crupdateRetakeExamsNotExisted =
        crupdateRetakeExams.stream()
            .filter(
                crupdateRetakeExam -> {
                  assert crupdateRetakeExam.getStudentId() != null;
                  return retakeExamRepository.findById(crupdateRetakeExam.getStudentId()).isEmpty();
                })
            .toList();
    return retakeExamRepository.saveAll(
        retakeExamMapper.toDoMainList(crupdateRetakeExamsNotExisted));
  }

  public List<RetakeExam> getStudentRetakeExams(String sessionId, String studentId) {
    return retakeExamSessionRepository
        .findById(sessionId)
        .map(
            session -> {
              List<CourseAssignment> courses = courseAssignmentService.getByStudentId(studentId);
              Set<StudentLevel> studentLevels =
                  courses.stream()
                      .map(course -> course.getCourse().getStudentLevel())
                      .collect(Collectors.toSet());

              List<YearlyResult> yearlyResults =
                  studentLevels.stream()
                      .map(
                          level ->
                              gradeResultService.getLeveledYearlyResultByStudentId(
                                  level, studentId))
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
                                  && courseResult.getWeightedAverage().compareTo(BigDecimal.TEN)
                                      <= 0)
                      .toList();

              return coursesToRetake.stream()
                  .map(
                      courseResult -> {
                        assert courseResult.getCourse() != null;
                        RetakeExam retakeExam = new RetakeExam();
                        retakeExam.setCourse(courseMapper.toDomain(courseResult.getCourse()));
                        retakeExam.setRetakeExamSession(session);
                        return retakeExam;
                      })
                  .toList();
            })
        .orElse(Collections.emptyList());
  }
}
