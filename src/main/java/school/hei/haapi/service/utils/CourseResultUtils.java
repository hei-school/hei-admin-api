package school.hei.haapi.service.utils;

import static school.hei.haapi.endpoint.rest.model.CourseResultStatus.INCOMPLETE;
import static school.hei.haapi.endpoint.rest.model.CourseResultStatus.NOT_STARTED;
import static school.hei.haapi.endpoint.rest.model.CourseResultStatus.VALIDATED;
import static school.hei.haapi.model.Grade.weightedAverageOfGrades;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.endpoint.rest.model.CourseResult;
import school.hei.haapi.endpoint.rest.model.StudentLevel;
import school.hei.haapi.model.exception.CourseCreditsSumZero;
import school.hei.haapi.repository.dao.CourseAssignmentDao;
import school.hei.haapi.repository.dao.GradeDao;
import school.hei.haapi.service.ExamService;

@Component
@AllArgsConstructor
public class CourseResultUtils {
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
                return courseResult.status(VALIDATED);
              }
            })
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

  public BigDecimal weightedSumOfCourseResults(List<CourseResult> courseResults)
      throws CourseCreditsSumZero {
    double sumCoefficient =
        courseResults.stream()
            .mapToDouble(courseResult -> courseResult.getCourse().getCredits())
            .sum();
    if (sumCoefficient == 0.0) throw new CourseCreditsSumZero();

    return BigDecimal.valueOf(
            courseResults.stream()
                .mapToDouble(
                    courseResult ->
                        courseResult.getWeightedAverage() * courseResult.getCourse().getCredits())
                .sum())
        .divide(BigDecimal.valueOf(sumCoefficient), MathContext.DECIMAL128);
  }
}
