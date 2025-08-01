package school.hei.haapi.service;

import static java.math.BigDecimal.ZERO;
import static java.math.MathContext.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.rest.model.ResultSummary;
import school.hei.haapi.endpoint.rest.model.StudentLevel;
import school.hei.haapi.endpoint.rest.model.YearlyResult;
import school.hei.haapi.model.exception.CourseCreditsSumZero;

@Service
@AllArgsConstructor
@Slf4j
public class GradeResultService {
  private CourseResultService courseResultService;

  public YearlyResult getLeveledYearlyResultByStudentId(StudentLevel level, String studentId) {
    var courseResults = courseResultService.courseResultsForLevelOfStudent(level, studentId);

    return new YearlyResult()
        .level(level)
        .weightedAverage(courseResultService.weightedSumOfCourseResults(courseResults))
        .obtainedCredits(courseResultService.obtainedCreditsOfCourseResults(courseResults))
        .courseResults(courseResults)
        .status(courseResultService.courseStatusFromCourseResult(courseResults));
  }

  private Optional<YearlyResult> findLeveledYearlyResultByStudentId(
      StudentLevel level, String studentId) {
    try {
      return Optional.of(getLeveledYearlyResultByStudentId(level, studentId));
    } catch (CourseCreditsSumZero e) {
      log.error(
          "Course results for the level {} of the student id {} coefficient sum is 0",
          level,
          studentId,
          e);
      return Optional.empty();
    }
  }

  public ResultSummary getStudentResultSummary(String studentId) {
    List<YearlyResult> yearlyResultList =
        Arrays.stream(StudentLevel.values())
            .map(level -> findLeveledYearlyResultByStudentId(level, studentId))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .toList();

    BigDecimal obtainedCredits =
        yearlyResultList.stream()
            .map(YearlyResult::getObtainedCredits)
            .filter(Objects::nonNull)
            .reduce(BigDecimal::add)
            .orElse(ZERO);

    List<BigDecimal> yearlyResults =
        yearlyResultList.stream()
            .map(YearlyResult::getWeightedAverage)
            .filter(Objects::nonNull)
            .toList();

    BigDecimal yearlyResultsWeightedAverageSum =
        yearlyResults.stream().reduce(BigDecimal::add).orElse(ZERO);

    BigDecimal weightedAverage =
        yearlyResultsWeightedAverageSum.divide(BigDecimal.valueOf(yearlyResults.size()), UNLIMITED);

    return new ResultSummary()
        .yearlyResults(yearlyResultList)
        .obtainedCredits(obtainedCredits)
        .weightedAverage(weightedAverage);
  }
}
