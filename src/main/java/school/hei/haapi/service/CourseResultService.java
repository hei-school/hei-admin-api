package school.hei.haapi.service;

import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static school.hei.haapi.endpoint.rest.model.ResultOverviewStatus.INVALIDATED;
import static school.hei.haapi.endpoint.rest.model.ResultOverviewStatus.IN_PROGRESS;
import static school.hei.haapi.endpoint.rest.model.ResultOverviewStatus.VALIDATED;
import static school.hei.haapi.model.Grade.weightedAverageOfGrades;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.endpoint.rest.model.CourseResult;
import school.hei.haapi.endpoint.rest.model.CourseResultStatus;
import school.hei.haapi.endpoint.rest.model.ResultOverviewStatus;
import school.hei.haapi.endpoint.rest.model.StudentLevel;
import school.hei.haapi.model.exception.CoursesCreditSumZero;
import school.hei.haapi.model.exception.ExamsCoefficientSumZero;
import school.hei.haapi.repository.dao.CourseAssignmentDao;
import school.hei.haapi.repository.dao.GradeDao;

@Component
@AllArgsConstructor
public class CourseResultService {
  private CourseAssignmentDao courseAssignmentDao;
  private GradeDao gradeDao;
  private CourseMapper courseMapper;
  private ExamService examService;

  public List<CourseResult> courseResultsForLevelOfStudent(StudentLevel level, String studentId) {
    var coursesForSpecificLevel =
        courseAssignmentDao.findByCriteria(null, null, level, Pageable.unpaged());

    return coursesForSpecificLevel.stream()
        .map(
            courseAssignment -> {
              var studentGrades =
                  gradeDao.getStudentGradesByCourseId(
                      courseAssignment.getCourse().getId(), studentId);
              var examsOfTheCourse =
                  examService.getExamsByCourseAssignmentId(courseAssignment.getId());

              var courseResult =
                  new CourseResult().course(courseMapper.toRest(courseAssignment.getCourse()));
              try {
                courseResult.weightedAverage(weightedAverageOfGrades(studentGrades));
              } catch (ExamsCoefficientSumZero e) {
                return courseResult.weightedAverage(ZERO).status(CourseResultStatus.IN_PROGRESS);
              }

              if (studentGrades.isEmpty()) {
                return courseResult.status(CourseResultStatus.NOT_STARTED);
              } else if (studentGrades.size() < examsOfTheCourse.size()) {
                return courseResult.status(CourseResultStatus.INCOMPLETE);
              } else if (TEN.compareTo(courseResult.getWeightedAverage()) > 0) {
                return courseResult.status(CourseResultStatus.INCOMPLETE);
              } else {
                return courseResult.status(CourseResultStatus.VALIDATED);
              }
            })
        .toList();
  }

  public BigDecimal obtainedCreditsOfCourseResults(List<CourseResult> courseResults) {
    return BigDecimal.valueOf(
        courseResults.stream()
            .mapToDouble(
                courseResult ->
                    courseResult.getWeightedAverage().doubleValue() >= 10
                        ? courseResult.getCourse().getCredits()
                        : 0.)
            .sum());
  }

  public BigDecimal weightedSumOfCourseResults(List<CourseResult> courseResults) {
    int sumCredits = getSumCredits(courseResults);

    return courseResults.stream()
        .map(
            courseResult ->
                courseResult
                    .getWeightedAverage()
                    .multiply(BigDecimal.valueOf(courseResult.getCourse().getCredits())))
        .reduce(ZERO, BigDecimal::add)
        .divide(BigDecimal.valueOf(sumCredits), MathContext.DECIMAL128);
  }

  public ResultOverviewStatus courseValidationFromCourseResult(List<CourseResult> courseResults) {
    var coursesResultStatus = courseResults.parallelStream().map(CourseResult::getStatus).toList();
    if (coursesResultStatus.stream()
        .map(Optional::ofNullable)
        .allMatch(
            courseResultStatus ->
                courseResultStatus.filter(CourseResultStatus.VALIDATED::equals).isPresent())) {
      return VALIDATED;
    }
    if (coursesResultStatus.stream().anyMatch(CourseResultStatus.IN_PROGRESS::equals)) {
      return IN_PROGRESS;
    }
    return INVALIDATED;
  }

  public int getSumCredits(List<CourseResult> courses) {
    int sumCredits =
        courses.parallelStream()
            .map(CourseResult::getCourse)
            .filter(Objects::nonNull)
            .map(school.hei.haapi.endpoint.rest.model.Course::getCredits)
            .map(Optional::ofNullable)
            .filter(Optional::isPresent)
            .mapToInt(Optional::get)
            .sum();
    if (sumCredits == 0) throw new CoursesCreditSumZero();
    return sumCredits;
  }
}
