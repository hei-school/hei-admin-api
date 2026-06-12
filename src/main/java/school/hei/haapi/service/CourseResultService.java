package school.hei.haapi.service;

import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static java.math.MathContext.DECIMAL128;
import static java.util.Comparator.comparing;
import static java.util.Objects.nonNull;
import static school.hei.haapi.endpoint.rest.model.ResultOverviewStatus.INVALIDATED;
import static school.hei.haapi.endpoint.rest.model.ResultOverviewStatus.IN_PROGRESS;
import static school.hei.haapi.endpoint.rest.model.ResultOverviewStatus.NOT_STARTED;
import static school.hei.haapi.endpoint.rest.model.ResultOverviewStatus.VALIDATED;
import static school.hei.haapi.model.Grade.weightedAverageOfGrades;

import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.endpoint.rest.model.CourseResult;
import school.hei.haapi.endpoint.rest.model.CourseResultStatus;
import school.hei.haapi.endpoint.rest.model.ResultOverviewStatus;
import school.hei.haapi.endpoint.rest.model.StudentLevel;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.CourseAssignment;
import school.hei.haapi.model.Exam;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.CoursesCreditSumZero;
import school.hei.haapi.model.exception.ExamsCoefficientSumZero;
import school.hei.haapi.repository.dao.GradeDao;
import school.hei.haapi.service.utils.CollectionUtils;

@Service
@AllArgsConstructor
@Slf4j
public class CourseResultService {
  private final CourseService courseService;
  private final GradeDao gradeDao;
  private final CourseMapper courseMapper;
  private final ExamService examService;
  private final UserService userService;
  private final CollectionUtils collectionUtils;

  private static final BigDecimal VALIDATED_YEAR_CREDIT = BigDecimal.valueOf(30);
  private static final BigDecimal VALIDATED_YEAR_AVERAGE = TEN;

  private List<Group> findStudentGroupByExams(User student, List<Exam> exams) {
    return collectionUtils
        .filterDistinctByField(
            exams.stream()
                .map(exam -> student.findGroupAt(exam.getExaminationDate()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList(),
            Group::getId)
        .stream()
        .toList();
  }

  @Transactional
  public List<CourseResult> getCourseResultsForLevelOfStudent(
      StudentLevel level, String studentId) {
    var student = userService.getById(studentId);
    var coursesForSpecificLevel = courseService.getByStudentLevel(level);
    log.info("Course for specific level : {}", coursesForSpecificLevel);
    var coursesForSpecificStudent =
        collectionUtils.filterDistinctByField(
            getCourseInStudentGroup(coursesForSpecificLevel, student), Course::getId);
    return coursesForSpecificStudent.stream()
        .map(course -> computeCourseResult(course, student))
        .sorted(comparing(courseResult -> courseResult.getStatus().ordinal()))
        .toList();
  }

  @NotNull
  private List<Course> getCourseInStudentGroup(List<Course> coursesForSpecificLevel, User student) {
    return coursesForSpecificLevel.stream()
        .map(Course::getCourseAssignments)
        .filter(courseAssignments -> isAssignedToTheGroup(courseAssignments, student))
        .flatMap(courseAssignments -> courseAssignments.stream().map(CourseAssignment::getCourse))
        .toList();
  }

  private boolean isAssignedToTheGroup(List<CourseAssignment> courseAssignments, User student) {
    var courseAssignmentGroupIds =
        courseAssignments.stream()
            .map(CourseAssignment::getGroups)
            .flatMap(groups -> groups.stream().map(Group::getId))
            .toList();
    var courseAssignmentIds = courseAssignments.stream().map(CourseAssignment::getId).toList();
    log.info("All student group flows : {}", student.getGroupFlows());
    var studentGroupIds =
        student.getGroupFlows().stream().map(groupFlow -> groupFlow.getGroup().getId()).toList();
    var exams = examService.getExamsByCourseAssignmentIds(courseAssignmentIds);
    if (exams.isEmpty()) {
      return courseAssignmentGroupIds.stream().anyMatch(studentGroupIds::contains);
    }

    var studentExamGroupIds =
        findStudentGroupByExams(student, exams).stream().map(Group::getId).toList();
    return courseAssignmentGroupIds.stream().anyMatch(studentExamGroupIds::contains);
  }

  private CourseResult computeCourseResult(Course course, User student) {
    var courseExams = examService.getExamsByCourseId(course.getId());
    var studentGrades = gradeDao.getStudentGradesByCourseId(course.getId(), student.getId());
    var examsOfTheCourse = examService.getExamsByCourseId(course.getId());
    var studentGroupInExam = findStudentGroupByExams(student, examsOfTheCourse);
    var examOfTheStudent =
        examsOfTheCourse.stream()
            .filter(
                e ->
                    e.getCourseAssignment().getGroups().stream()
                        .anyMatch(studentGroupInExam::contains))
            .toList();
    var courseResult = new CourseResult().course(courseMapper.toRest(course));

    if (courseExams.isEmpty()) {
      return courseResult.status(CourseResultStatus.NOT_STARTED);
    }

    try {
      courseResult.weightedAverage(
          BigDecimal.valueOf(weightedAverageOfGrades(studentGrades).doubleValue()));
    } catch (ExamsCoefficientSumZero e) {
      return courseResult.status(CourseResultStatus.NOT_STARTED);
    }

    if (studentGrades.isEmpty()) {
      return courseResult.status(CourseResultStatus.IN_PROGRESS);
    } else if (studentGrades.size() < examOfTheStudent.size()) {
      return courseResult.status(CourseResultStatus.IN_PROGRESS);
    } else if (TEN.compareTo(courseResult.getWeightedAverage()) > 0) {
      return courseResult.status(CourseResultStatus.INCOMPLETE);
    } else {
      return courseResult.status(CourseResultStatus.VALIDATED);
    }
  }

  public BigDecimal obtainedCreditsOfCourseResults(List<CourseResult> courseResults) {
    return BigDecimal.valueOf(
        courseResults.stream()
            .filter(courseResult -> nonNull(courseResult.getWeightedAverage()))
            .mapToDouble(
                courseResult ->
                    courseResult.getWeightedAverage().doubleValue() >= 10
                        ? courseResult.getCourse().getCredits()
                        : 0.)
            .sum());
  }

  public Optional<BigDecimal> weightedSumOfCourseResults(List<CourseResult> courseResults) {
    int sumCredits = getSumCredits(courseResults);
    var presentCourseResults =
        courseResults.stream()
            .filter(courseResult -> nonNull(courseResult.getWeightedAverage()))
            .toList();

    if (presentCourseResults.isEmpty()) return Optional.empty();

    return Optional.of(
        presentCourseResults.stream()
            .map(
                courseResult ->
                    courseResult
                        .getWeightedAverage()
                        .multiply(BigDecimal.valueOf(courseResult.getCourse().getCredits())))
            .reduce(ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(sumCredits), DECIMAL128));
  }

  public ResultOverviewStatus courseValidationFromCourseResult(List<CourseResult> courseResults) {
    var coursesResultStatus = courseResults.parallelStream().map(CourseResult::getStatus).toList();
    var courseResultCount = coursesResultStatus.size();

    var notStartedCount =
        coursesResultStatus.stream().filter(CourseResultStatus.NOT_STARTED::equals).count();
    var inProgressCount =
        coursesResultStatus.stream().filter(CourseResultStatus.IN_PROGRESS::equals).count();

    var validated = isValidated(courseResults);

    if (courseResultCount == 0) return NOT_STARTED;
    if (inProgressCount > 0) return IN_PROGRESS;
    if (notStartedCount == courseResultCount) return NOT_STARTED;
    if (notStartedCount > 0) return IN_PROGRESS;
    if (validated) return VALIDATED;
    return INVALIDATED;
  }

  public boolean isValidated(List<CourseResult> courseResults) {
    var obtainedCredits = obtainedCreditsOfCourseResults(courseResults);
    var courseResultsWeightedAverage = weightedSumOfCourseResults(courseResults);

    return obtainedCredits.compareTo(VALIDATED_YEAR_CREDIT) >= 0
        && courseResultsWeightedAverage
            .map(average -> average.compareTo(VALIDATED_YEAR_AVERAGE) >= 0)
            .orElse(false);
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
