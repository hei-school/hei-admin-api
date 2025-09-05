package school.hei.haapi.service;

import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static java.math.MathContext.DECIMAL128;
import static java.util.Comparator.comparing;
import static java.util.Objects.nonNull;
import static school.hei.haapi.endpoint.rest.model.ResultOverviewStatus.*;
import static school.hei.haapi.endpoint.rest.model.ResultOverviewStatus.INVALIDATED;
import static school.hei.haapi.endpoint.rest.model.ResultOverviewStatus.IN_PROGRESS;
import static school.hei.haapi.endpoint.rest.model.ResultOverviewStatus.NOT_STARTED;
import static school.hei.haapi.endpoint.rest.model.ResultOverviewStatus.VALIDATED;
import static school.hei.haapi.model.Grade.weightedAverageOfGrades;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.endpoint.rest.model.CourseResult;
import school.hei.haapi.endpoint.rest.model.CourseResultStatus;
import school.hei.haapi.endpoint.rest.model.ResultOverviewStatus;
import school.hei.haapi.endpoint.rest.model.StudentLevel;
import school.hei.haapi.model.exception.CoursesCreditSumZero;
import school.hei.haapi.model.exception.ExamsCoefficientSumZero;
import school.hei.haapi.repository.dao.GradeDao;

@Component
@AllArgsConstructor
public class CourseResultService {
  private CourseService courseService;
  private GradeDao gradeDao;
  private CourseMapper courseMapper;
  private ExamService examService;
  private UserService userService;

  public List<CourseResult> getCourseResultsForLevelOfStudent(
      StudentLevel level, String studentId) {
    var coursesForSpecificLevel = courseService.getByStudentLevel(level);
    var student = userService.findById(studentId);

    return coursesForSpecificLevel.stream()
        .map(
            course -> {
              var studentGrades =
                  gradeDao.getStudentGradesByCourseId(course.getId(), student.getId());
              var examsOfTheCourse = examService.getExamsByCourseId(course.getId());

              var courseResult = new CourseResult().course(courseMapper.toRest(course));
              try {
                courseResult.weightedAverage(
                    BigDecimal.valueOf(weightedAverageOfGrades(studentGrades).doubleValue()));
              } catch (ExamsCoefficientSumZero e) {
                return courseResult.status(CourseResultStatus.NOT_STARTED);
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
        .sorted(comparing(courseResult -> courseResult.getStatus().ordinal()))
        .toList();
  }

  public BigDecimal obtainedCreditsOfCourseResults(List<CourseResult> courseResults) {
    return BigDecimal.valueOf(
        courseResults.stream()
            .filter(c -> nonNull(c.getWeightedAverage()))
            .mapToDouble(
                c -> c.getWeightedAverage().doubleValue() >= 10 ? c.getCourse().getCredits() : 0.)
            .sum());
  }

  public Optional<BigDecimal> weightedSumOfCourseResults(List<CourseResult> courseResults) {
    int sumCredits = getSumCredits(courseResults);
    var presentCourseResults =
        courseResults.stream().filter(c -> nonNull(c.getWeightedAverage())).toList();

    if (presentCourseResults.isEmpty()) return Optional.empty();

    return Optional.of(
        presentCourseResults.stream()
            .map(
                c ->
                    c.getWeightedAverage().multiply(BigDecimal.valueOf(c.getCourse().getCredits())))
            .reduce(ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(sumCredits), DECIMAL128));
  }

  public Optional<ResultOverviewStatus> courseValidationFromCourseResult(
      List<CourseResult> courseResults) {
    var coursesResultStatus = courseResults.parallelStream().map(CourseResult::getStatus).toList();
    var courseResultCount = coursesResultStatus.size();

    var notStartedCount =
        coursesResultStatus.stream().filter(CourseResultStatus.NOT_STARTED::equals).count();
    var validatedCount =
        coursesResultStatus.stream().filter(CourseResultStatus.VALIDATED::equals).count();
    var invalidatedCount =
        coursesResultStatus.stream().filter(CourseResultStatus.INCOMPLETE::equals).count();
    var inProgressCount =
        coursesResultStatus.stream().filter(CourseResultStatus.IN_PROGRESS::equals).count();

    if (courseResultCount == 0) return Optional.of(NOT_STARTED);
    if (inProgressCount > 0) return Optional.of(IN_PROGRESS);
    if (notStartedCount == courseResultCount) return Optional.of(NOT_STARTED);
    if (notStartedCount > 0) return Optional.of(IN_PROGRESS);
    if (validatedCount == courseResultCount) return Optional.of(VALIDATED);
    if (invalidatedCount > 0) return Optional.of(INVALIDATED);

    return Optional.empty();
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
