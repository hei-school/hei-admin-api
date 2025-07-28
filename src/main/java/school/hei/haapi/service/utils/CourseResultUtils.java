package school.hei.haapi.service.utils;

import static school.hei.haapi.endpoint.rest.model.CourseResultStatus.VALIDATED;
import static school.hei.haapi.model.grade.GradeUtils.weightedAverageOfGrades;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.endpoint.rest.model.CourseResult;
import school.hei.haapi.endpoint.rest.model.StudentLevel;
import school.hei.haapi.repository.dao.CourseDao;
import school.hei.haapi.repository.dao.GradeDao;

@Component
@AllArgsConstructor
public class CourseResultUtils {
  private CourseDao courseDao;
  private GradeDao gradeDao;
  private CourseMapper courseMapper;

  public List<CourseResult> courseResultsForLevelOfStudent(StudentLevel level, String studentId) {
    var coursesForSpecificLevel =
        courseDao.findByCriteria(
            null, null, null, null, null, null, null, level, Pageable.unpaged());

    return coursesForSpecificLevel.stream()
        .map(
            course ->
                new CourseResult()
                    .course(courseMapper.toRest(course))
                    .weightedAverage(
                        weightedAverageOfGrades(
                            gradeDao.getStudentGradesByCourseId(course.getId(), studentId)))
                    .status(VALIDATED))
        .toList();
  }

  public BigDecimal obtainedCreditsOfCourseResults(List<CourseResult> courseResults) {
    return BigDecimal.valueOf(
        courseResults.stream()
            .mapToDouble(
                courseResult ->
                    courseResult.getWeightedAverage() >= 10
                        ? courseResult.getCourse().getCredits()
                        : 0.)
            .sum());
  }

  public double weightedSumOfCourseResults(List<CourseResult> courseResults) {
    return courseResults.stream()
            .mapToDouble(
                courseResult ->
                    courseResult.getWeightedAverage() * courseResult.getCourse().getCredits())
            .sum()
        / courseResults.stream()
            .mapToDouble(courseResult -> courseResult.getCourse().getCredits())
            .sum();
  }
}
