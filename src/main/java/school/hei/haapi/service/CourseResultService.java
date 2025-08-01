package school.hei.haapi.service;

import static school.hei.haapi.endpoint.rest.model.CourseResultStatus.INCOMPLETE;
import static school.hei.haapi.endpoint.rest.model.CourseResultStatus.NOT_STARTED;
import static school.hei.haapi.endpoint.rest.model.ResultOverviewStatus.IN_PROGRESS;
import static school.hei.haapi.endpoint.rest.model.ResultOverviewStatus.VALIDATED;
import static school.hei.haapi.model.Grade.weightedAverageOfGrades;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.endpoint.rest.model.CourseResult;
import school.hei.haapi.endpoint.rest.model.CourseResultStatus;
import school.hei.haapi.endpoint.rest.model.ResultOverviewStatus;
import school.hei.haapi.endpoint.rest.model.StudentLevel;
import school.hei.haapi.model.exception.CourseCreditsSumZero;
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
                  new CourseResult()
                      .course(courseMapper.toRest(courseAssignment.getCourse()))
                      .weightedAverage(weightedAverageOfGrades(studentGrades));
              if (studentGrades.isEmpty()) {
                return courseResult.status(NOT_STARTED);
              } else if (studentGrades.size() < examsOfTheCourse.size()) {
                return courseResult.status(INCOMPLETE);
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
        .reduce(BigDecimal.ZERO, BigDecimal::add)
        .divide(BigDecimal.valueOf(sumCredits), MathContext.DECIMAL128);
  }

  public ResultOverviewStatus courseStatusFromCourseResult(List<CourseResult> courseResults) {
    if (courseResults.parallelStream()
        .map(CourseResult::getStatus)
        .map(Optional::ofNullable)
        .allMatch(
            courseResultStatus ->
                courseResultStatus.filter(CourseResultStatus.VALIDATED::equals).isPresent())) {
      return VALIDATED;
    }
    return IN_PROGRESS;
  }

  private int getSumCredits(List<school.hei.haapi.endpoint.rest.model.Course> courses) {
    int sumCredits =
        courses.stream().mapToInt(school.hei.haapi.endpoint.rest.model.Course::getCredits).sum();
    if (sumCredits == 0) throw new CourseCreditsSumZero();
    return sumCredits;
  }

  private int getSumCredits(List<CourseResult> courses) {
    return getSumCredits(courses.stream().map(CourseResult::getCourse).toList());
  }
}
