package school.hei.haapi.service;

import static school.hei.haapi.endpoint.rest.model.CourseResultStatus.VALIDATED;
import static school.hei.haapi.model.grade.GradeUtils.weightedAverageOfGrades;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.endpoint.rest.model.CourseResult;
import school.hei.haapi.endpoint.rest.model.ResultSummary;
import school.hei.haapi.endpoint.rest.model.StudentLevel;
import school.hei.haapi.endpoint.rest.model.YearlyResult;
import school.hei.haapi.model.Course;
import school.hei.haapi.repository.dao.CourseDao;
import school.hei.haapi.repository.dao.GradeDao;

@Service
@AllArgsConstructor
public class GradeResultService {
  private CourseDao courseDao;
  private GradeDao gradeDao;
  private CourseMapper courseMapper;

  public YearlyResult getLeveledYearlyResultByStudentId(StudentLevel level, String studentId) {
    var coursesForSpecificLevel =
        courseDao.findByCriteria(
            null, null, null, null, null, null, null, level, Pageable.unpaged());
    double totalCredits = 0;
    double obtainedCredits = 0;
    List<CourseResult> courseResults = new ArrayList<>();

    for (Course course : coursesForSpecificLevel) {
      double courseWeightedAverageOfGrades =
          weightedAverageOfGrades(gradeDao.getStudentGradesByCourseId(course.getId(), studentId));
      totalCredits += course.getCredits();
      if (courseWeightedAverageOfGrades >= 10) {
        obtainedCredits += course.getCredits();
      }
      courseResults.add(
          new CourseResult()
              .course(courseMapper.toRest(course))
              .weightedAverage(courseWeightedAverageOfGrades)
              .status(VALIDATED));
    }

    return new YearlyResult()
        .level(level)
        .weightedAverage(
            courseResults.stream().mapToDouble(CourseResult::getWeightedAverage).sum()
                / totalCredits)
        .obtainedCredits(BigDecimal.valueOf(obtainedCredits))
        .courseResults(courseResults);
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
