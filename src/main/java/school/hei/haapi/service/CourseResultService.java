package school.hei.haapi.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.endpoint.rest.model.CourseResult;
import school.hei.haapi.endpoint.rest.model.CourseResultStatus;
import school.hei.haapi.endpoint.rest.model.ResultOverviewStatus;
import school.hei.haapi.endpoint.rest.model.StudentLevel;
import school.hei.haapi.model.CourseAssignment;
import school.hei.haapi.model.User;
import school.hei.haapi.model.dto.GroupFlowPeriod;
import school.hei.haapi.model.dto.GroupFlowPeriodCourseAssignment;
import school.hei.haapi.model.exception.CoursesCreditSumZero;
import school.hei.haapi.model.exception.ExamsCoefficientSumZero;
import school.hei.haapi.repository.dao.GradeDao;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static java.math.MathContext.DECIMAL128;
import static java.util.Comparator.comparing;
import static java.util.Objects.nonNull;
import static java.util.regex.Pattern.compile;
import static school.hei.haapi.endpoint.rest.model.ResultOverviewStatus.INVALIDATED;
import static school.hei.haapi.endpoint.rest.model.ResultOverviewStatus.IN_PROGRESS;
import static school.hei.haapi.endpoint.rest.model.ResultOverviewStatus.NOT_STARTED;
import static school.hei.haapi.endpoint.rest.model.ResultOverviewStatus.VALIDATED;
import static school.hei.haapi.model.Grade.weightedAverageOfGrades;

@Service
@AllArgsConstructor
@Slf4j
public class CourseResultService {
  private final GradeDao gradeDao;
  private final CourseMapper courseMapper;
  private final UserService userService;
  private final CourseAssignmentService courseAssignmentService;
  private final GroupFlowService groupFlowService;

  private static final BigDecimal VALIDATED_YEAR_CREDIT = BigDecimal.valueOf(30);
  private static final BigDecimal VALIDATED_YEAR_AVERAGE = TEN;
  private static final Pattern TRAILING_DIGITS = compile("\\d+$");

  @Transactional
  public List<CourseResult> getCourseResultsByStudentIdAndLevel(
      String studentId, StudentLevel level) {
    var student = userService.getById(studentId);
    var studentGroupFlowsAtLevel = groupFlowService.getStudentGroupFlowAtLevel(student, level);
    var studentLatestGroupFlowsAtLevel = findLatestGroupFlowPeriods(studentGroupFlowsAtLevel);
    var studentGroupCourseAssignmentsAtLevel =
        getGroupsCourseAssignmentsByGroupFlowsAtLevel(studentLatestGroupFlowsAtLevel, level);
    return studentGroupCourseAssignmentsAtLevel.stream()
        .flatMap(
            groupCourseAssignments ->
                computeStudentCourseResults(groupCourseAssignments, student).stream())
        .sorted(comparing(courseResult -> courseResult.getStatus().ordinal()))
        .toList();
  }

  public List<GroupFlowPeriod> findLatestGroupFlowPeriods(List<GroupFlowPeriod> groupFlowPeriods) {
    if (groupFlowPeriods.isEmpty()) {
      return List.of();
    }

    var latestPromotion = latestPromotion(groupFlowPeriods);

    return groupFlowPeriods.stream()
        .filter(
            groupFlowPeriod ->
                extractGroupPromotion(groupFlowPeriod.group().getName()).equals(latestPromotion))
        .toList();
  }

  private String latestPromotion(List<GroupFlowPeriod> groupFlowPeriods) {
    return extractGroupPromotion(
        groupFlowPeriods.stream()
            .max(comparing(GroupFlowPeriod::start))
            .orElseThrow()
            .group()
            .getName());
  }

  private String extractGroupPromotion(String groupName) {
    return TRAILING_DIGITS.matcher(groupName).replaceAll("");
  }

  private List<GroupFlowPeriodCourseAssignment> getGroupsCourseAssignmentsByGroupFlowsAtLevel(
      List<GroupFlowPeriod> studentLatestGroupFlows, StudentLevel level) {
    return studentLatestGroupFlows.stream()
        .map(
            groupFlowPeriod ->
                getGroupCourseAssignmentsBetweenPeriodAndStudentLevel(groupFlowPeriod, level))
        .toList();
  }

  private GroupFlowPeriodCourseAssignment getGroupCourseAssignmentsBetweenPeriodAndStudentLevel(
      GroupFlowPeriod groupFlowPeriod, StudentLevel level) {
    var courseAssignments =
        courseAssignmentService.getByGroupId(groupFlowPeriod.group().getId()).stream()
            .filter(
                courseAssignment -> level.equals(courseAssignment.getCourse().getStudentLevel()))
            .filter(
                courseAssignment -> hasExamOrAssignedBeforeLeave(courseAssignment, groupFlowPeriod))
            .toList();
    return new GroupFlowPeriodCourseAssignment(groupFlowPeriod, courseAssignments);
  }

  private boolean hasExamOrAssignedBeforeLeave(
      CourseAssignment courseAssignment, GroupFlowPeriod groupFlowPeriod) {
    var courseExams = courseAssignment.getExams();
    if (courseExams.isEmpty() && groupFlowPeriod.end() == null) {
      return true;
    }
    return courseExams.stream()
        .anyMatch(courseExam -> groupFlowPeriod.contains(courseExam.getExaminationDate()));
  }

  private List<CourseResult> computeStudentCourseResults(
      GroupFlowPeriodCourseAssignment groupCourseAssignments, User student) {
    var groupFlowPeriod = groupCourseAssignments.groupFlowPeriod();
    var courseAssignments = groupCourseAssignments.courseAssignments();
    return courseAssignments.stream()
        .map(
            courseAssignment ->
                getStudentCourseResultByCourseAssignmentWithGroupFlowPeriod(
                    groupFlowPeriod, courseAssignment, student))
        .toList();
  }

  private CourseResult getStudentCourseResultByCourseAssignmentWithGroupFlowPeriod(
      GroupFlowPeriod groupFlowPeriod, CourseAssignment courseAssignment, User student) {
    var courseExams = courseAssignment.getExams();
    var studentGrades =
        gradeDao.getStudentGradesByCourseId(courseAssignment.getCourse().getId(), student.getId());
    var courseResult = new CourseResult().course(courseMapper.toRest(courseAssignment.getCourse()));

    if (groupFlowPeriod.end() != null && courseExams.isEmpty()) {
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
    } else if (studentGrades.size() < courseExams.size()) {
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
