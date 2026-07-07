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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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
import school.hei.haapi.model.User;
import school.hei.haapi.model.dto.GroupFlowPeriod;
import school.hei.haapi.model.exception.CoursesCreditSumZero;
import school.hei.haapi.model.exception.ExamsCoefficientSumZero;
import school.hei.haapi.repository.dao.GradeDao;
import school.hei.haapi.service.utils.CollectionUtils;

@Service
@AllArgsConstructor
@Slf4j
public class CourseResultService {
  private final GradeDao gradeDao;
  private final CourseMapper courseMapper;
  private final ExamService examService;
  private final UserService userService;
  private final CollectionUtils collectionUtils;
  private final CourseAssignmentService courseAssignmentService;
  private final GroupFlowService groupFlowService;

  private static final BigDecimal VALIDATED_YEAR_CREDIT = BigDecimal.valueOf(30);
  private static final BigDecimal VALIDATED_YEAR_AVERAGE = TEN;

  @Transactional
  public List<CourseResult> getCourseResultsByStudentIdAndLevel(
      String studentId, StudentLevel level) {
    var student = userService.getById(studentId);
    var studentGroupsAtLevel = groupFlowService.getStudentGroupFlowAtLevel(student, level);
    var studentLatestGroupFlows = findLatestGroups(studentGroupsAtLevel);
    var studentCoursesAtLevel =
        getCourseAssignmentsWithGroupByGroupsAtLevel(studentLatestGroupFlows, level);
    return studentCoursesAtLevel.stream()
        .map(courseWithGroup -> computeCourseResult(courseWithGroup, student))
        .sorted(comparing(courseResult -> courseResult.getStatus().ordinal()))
        .toList();
  }

  public List<GroupFlowPeriod> findLatestGroups(List<GroupFlowPeriod> groupFlowPeriods) {
    if (groupFlowPeriods.isEmpty()) {
      return List.of();
    }
    var groupPromotions =
        groupFlowPeriods.stream()
            .map(groupFlowPeriod -> extractGroupPromotion(groupFlowPeriod.group().getName()))
            .collect(Collectors.toSet());

    if (sameGroupPromotion(groupPromotions)) {
      return groupFlowPeriods;
    }

    return Collections.singletonList(
        groupFlowPeriods.stream().max(comparing(GroupFlowPeriod::start)).orElseThrow());
  }

  private boolean sameGroupPromotion(Set<String> promotionNames) {
    return promotionNames.size() == 1;
  }

  private String extractGroupPromotion(String groupName) {
    return groupName.replaceAll("[0-9]+$", "");
  }

  private List<Map<GroupFlowPeriod, List<CourseAssignment>>>
      getCourseAssignmentsWithGroupByGroupsAtLevel(
          List<GroupFlowPeriod> studentLatestGroupFlows, StudentLevel level) {
    return studentLatestGroupFlows.stream()
        .map(groupFlowPeriod -> getCourseAssignmentByGroupIdAndStudentLevel(groupFlowPeriod, level))
        .collect(Collectors.toList());
  }

  private Map<GroupFlowPeriod, List<CourseAssignment>> getCourseAssignmentByGroupIdAndStudentLevel(
      GroupFlowPeriod groupFlowPeriod, StudentLevel level) {
    var courseAssignments =
        courseAssignmentService.getByGroupId(groupFlowPeriod.group().getId()).stream()
            .filter(
                courseAssignment -> level.equals(courseAssignment.getCourse().getStudentLevel()))
            .filter(
                courseAssignment ->
                    haveExamBetweenGroupFlowPeriod(courseAssignment, groupFlowPeriod))
            .toList();
    return Map.of(groupFlowPeriod, courseAssignments);
  }

  private boolean haveExamBetweenGroupFlowPeriod(
      CourseAssignment courseAssignment, GroupFlowPeriod groupFlowPeriod) {
    var courseExams = courseAssignment.getExams();
    if (courseExams.isEmpty() && groupFlowPeriod.end() == null) {
      return true;
    }
    return courseExams.stream()
        .anyMatch(courseExam -> groupFlowPeriod.contains(courseExam.getExaminationDate()));
  }

  private CourseResult computeCourseResult(
      Map<GroupFlowPeriod, List<CourseAssignment>> courseWithGroup, User student) {
    var entry = courseWithGroup.entrySet().stream().findFirst().orElseThrow();
    var groupFlowPeriod = entry.getKey();
    var courseAssignment = entry.getValue().getFirst();
    var courseExams = courseAssignment.getExams();
    var studentGrades =
        gradeDao.getStudentGradesByCourseId(courseAssignment.getId(), student.getId());
    var courseResult = new CourseResult().course(courseMapper.toRest(courseAssignment.getCourse()));

    if (groupFlowPeriod.end() != null) {
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
