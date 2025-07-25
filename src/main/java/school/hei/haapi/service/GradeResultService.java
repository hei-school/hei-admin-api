package school.hei.haapi.service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.rest.model.ResultSummary;
import school.hei.haapi.endpoint.rest.model.StudentLevel;
import school.hei.haapi.endpoint.rest.model.YearlyResult;
import school.hei.haapi.model.exception.NotImplementedException;

@Service
@AllArgsConstructor
public class GradeResultService {
  public YearlyResult getLeveledYearlyResultByStudentId(StudentLevel level, String studentId) {
    throw new NotImplementedException("Need to be implemented");
  }

  public ResultSummary getStudentResultSummary(String studentId) {
    List<YearlyResult> yearlyResultList =
        Arrays.stream(StudentLevel.values())
            .map(level -> getLeveledYearlyResultByStudentId(level, studentId))
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
            .mapToDouble(YearlyResult::getWeightedAverage)
            .average()
            .orElse(0.0);

    return new ResultSummary()
        .yearlyResults(yearlyResultList)
        .obtainedCredits(BigDecimal.valueOf(obtainedCredits))
        .weightedAverage(average);
  }
}
