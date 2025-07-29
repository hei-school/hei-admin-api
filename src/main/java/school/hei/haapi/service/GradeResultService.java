package school.hei.haapi.service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.rest.model.ResultSummary;
import school.hei.haapi.endpoint.rest.model.StudentLevel;
import school.hei.haapi.endpoint.rest.model.YearlyResult;
import school.hei.haapi.model.exception.CourseCreditsSumZero;
import school.hei.haapi.service.utils.CourseResultUtils;

@Service
@AllArgsConstructor
@Slf4j
public class GradeResultService {
  private CourseResultUtils courseResultUtils;

  public YearlyResult getLeveledYearlyResultByStudentId(StudentLevel level, String studentId)
      throws CourseCreditsSumZero {
    var courseResults = courseResultUtils.courseResultsForLevelOfStudent(level, studentId);

    return new YearlyResult()
        .level(level)
        .weightedAverage(courseResultUtils.weightedSumOfCourseResults(courseResults))
        .obtainedCredits(courseResultUtils.obtainedCreditsOfCourseResults(courseResults))
        .courseResults(courseResults);
  }

  private YearlyResult findLeveledYearlyResultByStudentId(StudentLevel level, String studentId) {
    try {
      return getLeveledYearlyResultByStudentId(level, studentId);
    } catch (CourseCreditsSumZero e) {
      log.error(
          "Course results for the level {} of the student id {} coefficient sum is 0",
          level,
          studentId,
          e);
      return null;
    }
  }

  public ResultSummary getStudentResultSummary(String studentId) {
    List<YearlyResult> yearlyResultList =
        Arrays.stream(StudentLevel.values())
            .map(level -> findLeveledYearlyResultByStudentId(level, studentId))
            .filter(Objects::nonNull)
            .toList();

    int obtainedCredits =
        yearlyResultList.stream()
            .map(YearlyResult::getObtainedCredits)
            .filter(Objects::nonNull)
            .mapToInt(BigDecimal::intValue)
            .sum();

    double average =
        yearlyResultList.stream()
            .map(YearlyResult::getWeightedAverage)
            .filter(Objects::nonNull)
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0.0);

    return new ResultSummary()
        .yearlyResults(yearlyResultList)
        .obtainedCredits(BigDecimal.valueOf(obtainedCredits))
        .weightedAverage(average);
  }
}
