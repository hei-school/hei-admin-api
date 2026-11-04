package school.hei.haapi.service;

import static school.hei.haapi.endpoint.rest.model.ResultOverviewStatus.NOT_STARTED;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.rest.model.StudentLevel;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.ResultOverviewStatus;
import school.hei.haapi.model.User;
import school.hei.haapi.model.YearlyResult;
import school.hei.haapi.model.dto.YearlyResultRankingXlsxRow;
import school.hei.haapi.model.promotion.PromotionLevelOutOfRangeException;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.repository.YearlyResultRepository;
import school.hei.haapi.service.utils.XlsxCellsGenerator;

@Slf4j
@Service
@AllArgsConstructor
public class YearlyResultService {
  private static final BigDecimal EXTERNAL_LEVEL_WEIGHTED_AVERAGE = BigDecimal.TEN;
  private static final BigDecimal EXTERNAL_LEVEL_CREDITS = BigDecimal.valueOf(60);

  private final YearlyResultRepository yearlyResultRepository;
  private final UserRepository userRepository;
  private final PromotionService promotionService;
  private final GroupFlowService groupFlowService;
  private final GradeResultService gradeResultService;

  public List<YearlyResult> getYearlyResultsToCrupdate(String promotionId) {
    var students = userRepository.findAllStudentNotDisabledWithGroupFlow(promotionId);
    var studentIds = students.stream().map(User::getId).toList();
    var existingByStudentIdAndLevel =
        yearlyResultRepository.findAllByStudent_IdIn(studentIds).stream()
            .collect(
                Collectors.toMap(
                    yearlyResult ->
                        keyOf(yearlyResult.getStudent().getId(), yearlyResult.getLevel()),
                    Function.identity()));

    return students.stream()
        .flatMap(
            student -> buildYearlyResultsForStudent(student, existingByStudentIdAndLevel).stream())
        .toList();
  }

  public List<YearlyResult> saveAll(List<YearlyResult> yearlyResults) {
    return yearlyResultRepository.saveAll(yearlyResults);
  }

  public byte[] generateYearlyResultsRankingXlsx(String promotionId, StudentLevel level) {
    var promotion = promotionService.getPromotionById(promotionId);
    var groupIds =
        groupFlowService.findGroupsOfPromotionAtLevel(promotion, level).stream()
            .map(Group::getId)
            .toList();
    var rankedYearlyResults =
        yearlyResultRepository.findAllByGroupIdsAndLevelOrderByWeightedAverageDesc(groupIds, level);

    var rows = toRankedRows(rankedYearlyResults);
    XlsxCellsGenerator<YearlyResultRankingXlsxRow> xlsxCellsGenerator = new XlsxCellsGenerator<>();
    return xlsxCellsGenerator.apply(
        rows,
        List.of("rank", "ref", "weightedAverage", "obtainedCredits", "totalCredits", "groupe"));
  }

  private List<YearlyResultRankingXlsxRow> toRankedRows(List<YearlyResult> rankedYearlyResults) {
    var rows = new ArrayList<YearlyResultRankingXlsxRow>();
    var rank = 0;
    BigDecimal previousAverage = null;
    for (var i = 0; i < rankedYearlyResults.size(); i++) {
      var yearlyResult = rankedYearlyResults.get(i);
      var currentAverage = yearlyResult.getWeightedAverage();
      if (i == 0 || !sameAverage(previousAverage, currentAverage)) {
        rank = i + 1;
      }
      previousAverage = currentAverage;
      rows.add(
          new YearlyResultRankingXlsxRow(
              rank,
              yearlyResult.getStudent().getRef(),
              currentAverage,
              yearlyResult.getObtainedCredits(),
              yearlyResult.getTotalCredits(),
              yearlyResult.getGroup() == null ? "" : yearlyResult.getGroup().getRef()));
    }
    return rows;
  }

  private boolean sameAverage(BigDecimal a, BigDecimal b) {
    if (a == null || b == null) {
      return a == b;
    }
    return a.compareTo(b) == 0;
  }

  /**
   * Every level up to the student's current one in their current promotion's cycle (e.g. current L2
   * -> [L1, L2]), so a level never started with us still gets a row instead of being silently
   * missing.
   */
  private List<YearlyResult> buildYearlyResultsForStudent(
      User student, Map<String, YearlyResult> existingByStudentIdAndLevel) {
    var levelsUpToCurrent = levelsUpToCurrentOf(student);
    return levelsUpToCurrent.stream()
        .map(
            level ->
                buildYearlyResultForStudentAtLevel(student, level, existingByStudentIdAndLevel))
        .filter(Objects::nonNull)
        .toList();
  }

  private List<StudentLevel> levelsUpToCurrentOf(User student) {
    var currentPromotion = student.findCurrentGroup().map(Group::getPromotion).orElse(null);
    if (currentPromotion == null) {
      return List.of();
    }
    StudentLevel currentLevel;
    try {
      currentLevel = currentPromotion.getLevelAt(Instant.now());
    } catch (PromotionLevelOutOfRangeException e) {
      return List.of();
    }
    var cycleLevels = currentPromotion.getCycleLevel().getLevels();
    var currentIndex = cycleLevels.indexOf(currentLevel);
    if (currentIndex < 0) {
      return List.of();
    }
    return cycleLevels.subList(0, currentIndex + 1);
  }

  private YearlyResult buildYearlyResultForStudentAtLevel(
      User student, StudentLevel level, Map<String, YearlyResult> existingByStudentIdAndLevel) {
    var computedYearlyResult =
        gradeResultService.findLeveledYearlyResultByStudentId(level, student.getId());
    if (computedYearlyResult.isEmpty()) {
      return null;
    }
    var yearlyResult = computedYearlyResult.get();
    var groupFlowPeriods =
        groupFlowService.findStudentLatestGroupFlowPeriodsAtLevel(student.getId(), level);
    var group = groupFlowPeriods.isEmpty() ? null : groupFlowPeriods.getFirst().group();
    var existingId = existingByStudentIdAndLevel.get(keyOf(student.getId(), level));
    var existingEntityId = existingId != null ? existingId.getId() : null;

    if (NOT_STARTED.equals(yearlyResult.getStatus()) && group == null) {
      // The student never attended this level with us (e.g. did their L1 elsewhere before
      // joining us in L2): the level is assumed validated elsewhere, not a gap in our data.
      return YearlyResult.builder()
          .id(existingEntityId)
          .student(student)
          .group(null)
          .level(level)
          .weightedAverage(EXTERNAL_LEVEL_WEIGHTED_AVERAGE)
          .obtainedCredits(EXTERNAL_LEVEL_CREDITS)
          .totalCredits(EXTERNAL_LEVEL_CREDITS)
          .status(ResultOverviewStatus.VALIDATED)
          .build();
    }

    if (NOT_STARTED.equals(yearlyResult.getStatus())) {
      // The student does have a group at this level with us, it's genuinely not started yet
      // (typically their current level): keep the real status and total credits, zero the rest.
      return YearlyResult.builder()
          .id(existingEntityId)
          .student(student)
          .group(group)
          .level(level)
          .weightedAverage(BigDecimal.ZERO)
          .obtainedCredits(BigDecimal.ZERO)
          .totalCredits(yearlyResult.getTotalCredits())
          .status(ResultOverviewStatus.NOT_STARTED)
          .build();
    }

    return YearlyResult.builder()
        .id(existingEntityId)
        .student(student)
        .group(group)
        .level(level)
        .weightedAverage(yearlyResult.getWeightedAverage())
        .obtainedCredits(yearlyResult.getObtainedCredits())
        .totalCredits(yearlyResult.getTotalCredits())
        .status(ResultOverviewStatus.valueOf(yearlyResult.getStatus().toString()))
        .build();
  }

  private String keyOf(String studentId, StudentLevel level) {
    return studentId + "#" + level;
  }
}
