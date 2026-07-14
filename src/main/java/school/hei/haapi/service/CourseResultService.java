package school.hei.haapi.service;

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

import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.endpoint.rest.model.CourseResult;
import school.hei.haapi.endpoint.rest.model.CourseResultStatus;
import school.hei.haapi.endpoint.rest.model.ResultOverviewStatus;
import school.hei.haapi.endpoint.rest.model.StudentLevel;
import school.hei.haapi.model.CourseAssignment;
import school.hei.haapi.model.Exam;
import school.hei.haapi.model.User;
import school.hei.haapi.model.dto.CourseDto;
import school.hei.haapi.model.dto.GroupFlowPeriod;
import school.hei.haapi.model.exception.CoursesCreditSumZero;
import school.hei.haapi.model.exception.ExamsCoefficientSumZero;
import school.hei.haapi.repository.GradeRepository;

@Service
@AllArgsConstructor
@Slf4j
public class CourseResultService {
  private final CourseMapper courseMapper;
  private final UserService userService;
  private final CourseAssignmentService courseAssignmentService;
  private final GroupFlowService groupFlowService;
  private final GradeRepository gradeRepository;

  private static final BigDecimal VALIDATED_YEAR_CREDIT = BigDecimal.valueOf(30);
  private static final BigDecimal VALIDATED_YEAR_AVERAGE = TEN;
  private static final Pattern GROUP_TRAILING_DIGITS = compile("\\d+$");

  @Transactional
  public List<CourseResult> getCourseResultsByStudentIdAndLevel(
      String studentId, StudentLevel level) {
    var student = userService.getById(studentId);
    var studentGroupFlowsAtLevel = groupFlowService.getStudentGroupFlowAtLevel(student, level);
    var studentLatestGroupFlowsAtLevel = findLatestGroupFlowPeriods(studentGroupFlowsAtLevel);
    var studentGroupCourseAssignmentsAtLevel =
        getGroupsCourseAssignmentsByGroupFlowsAtLevel(studentLatestGroupFlowsAtLevel, level);
    return studentGroupCourseAssignmentsAtLevel.stream()
        .map(courseDto -> computeStudentCourseResult(courseDto, student))
        .sorted(comparing(courseResult -> courseResult.getStatus().ordinal()))
        .toList();
  }

  private List<GroupFlowPeriod> findLatestGroupFlowPeriods(List<GroupFlowPeriod> groupFlowPeriods) {
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
    return GROUP_TRAILING_DIGITS.matcher(groupName).replaceAll("");
  }

  private List<CourseDto> getGroupsCourseAssignmentsByGroupFlowsAtLevel(
      List<GroupFlowPeriod> studentLatestGroupFlows, StudentLevel level) {

    var courseAssignments =
        studentLatestGroupFlows.stream()
            .flatMap(
                groupFlowPeriod ->
                    getGroupCourseAssignmentsByLevelBetweenPeriod(groupFlowPeriod, level).stream())
            .collect(Collectors.groupingBy(CourseAssignment::getCourse));

    return courseAssignments.entrySet().stream()
        .map(entry -> new CourseDto(entry.getKey(), entry.getValue()))
        .toList();
  }

  private List<CourseAssignment> getGroupCourseAssignmentsByLevelBetweenPeriod(
      GroupFlowPeriod groupFlowPeriod, StudentLevel level) {
    return courseAssignmentService.getByGroupId(groupFlowPeriod.group().getId()).stream()
        .filter(courseAssignment -> level.equals(courseAssignment.getCourse().getStudentLevel()))
        .filter(courseAssignment -> hasExamOrAssignedBeforeLeave(courseAssignment, groupFlowPeriod))
        .toList();
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

  private CourseResult computeStudentCourseResult(CourseDto courseDto, User student) {
    var courseExams = getExamsByCourseDto(courseDto);
    var courseAssignmentIds =
        courseDto.courseAssigments().stream().map(CourseAssignment::getId).toList();
    var studentGrades =
        gradeRepository.findGradesByCourseAssignmentIdsAndStudentId(
            courseAssignmentIds, student.getId());
    var courseResult = new CourseResult().course(courseMapper.toRest(courseDto.course()));

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
    } else if (studentGrades.size() < courseExams.size()) {
      return courseResult.status(CourseResultStatus.IN_PROGRESS);
    } else if (TEN.compareTo(courseResult.getWeightedAverage()) > 0) {
      return courseResult.status(CourseResultStatus.INCOMPLETE);
    } else {
      return courseResult.status(CourseResultStatus.VALIDATED);
    }
  }

  private List<Exam> getExamsByCourseDto(CourseDto courseDto) {
    return courseDto.courseAssigments().stream()
        .flatMap(courseAssignment -> courseAssignment.getExams().stream())
        .toList();
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
