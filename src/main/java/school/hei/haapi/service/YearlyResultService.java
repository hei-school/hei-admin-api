package school.hei.haapi.service;

import static school.hei.haapi.endpoint.rest.model.ResultOverviewStatus.NOT_STARTED;

import java.math.BigDecimal;
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
import school.hei.haapi.model.dto.YearlyResultXlsxRow;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.repository.YearlyResultRepository;
import school.hei.haapi.service.utils.StudentLevelUtils;
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

  public byte[] generateYearlyResultsXlsx(String promotionId, StudentLevel level) {
    var promotion = promotionService.getPromotionById(promotionId);
    var groupIds =
        groupFlowService.findGroupsOfPromotionAtLevel(promotion, level).stream()
            .map(Group::getId)
            .toList();
    var yearlyResults =
        yearlyResultRepository.findAllByGroupIdsAndLevelOrderByWeightedAverageDesc(groupIds, level);

    var rows = yearlyResults.stream().map(this::toRow).toList();
    XlsxCellsGenerator<YearlyResultXlsxRow> xlsxCellsGenerator = new XlsxCellsGenerator<>();
    return xlsxCellsGenerator.apply(
        rows, List.of("ref", "weightedAverage", "obtainedCredits", "totalCredits", "groupe"));
  }

  private YearlyResultXlsxRow toRow(YearlyResult yearlyResult) {
    return new YearlyResultXlsxRow(
        yearlyResult.getStudent().getRef(),
        yearlyResult.getWeightedAverage(),
        yearlyResult.getObtainedCredits(),
        yearlyResult.getTotalCredits(),
        yearlyResult.getGroup() == null ? "" : yearlyResult.getGroup().getRef());
  }

  private List<YearlyResult> buildYearlyResultsForStudent(
      User student, Map<String, YearlyResult> existingByStudentIdAndLevel) {
    var levelsUpToCurrent = StudentLevelUtils.levelsUpToCurrentOf(student);
    return levelsUpToCurrent.stream()
        .map(
            level ->
                buildYearlyResultForStudentAtLevel(student, level, existingByStudentIdAndLevel))
        .filter(Objects::nonNull)
        .toList();
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
    var existingEntity = existingByStudentIdAndLevel.get(keyOf(student.getId(), level));
    var existingId = existingEntity != null ? existingEntity.getId() : null;
    var notStarted = NOT_STARTED.equals(yearlyResult.getStatus());

    if (notStarted && group == null) {
      return toYearlyResult(
          existingId,
          student,
          null,
          level,
          EXTERNAL_LEVEL_WEIGHTED_AVERAGE,
          EXTERNAL_LEVEL_CREDITS,
          EXTERNAL_LEVEL_CREDITS,
          ResultOverviewStatus.VALIDATED);
    }

    if (notStarted) {
      return toYearlyResult(
          existingId,
          student,
          group,
          level,
          BigDecimal.ZERO,
          BigDecimal.ZERO,
          yearlyResult.getTotalCredits(),
          ResultOverviewStatus.NOT_STARTED);
    }

    return toYearlyResult(
        existingId,
        student,
        group,
        level,
        yearlyResult.getWeightedAverage(),
        yearlyResult.getObtainedCredits(),
        yearlyResult.getTotalCredits(),
        ResultOverviewStatus.valueOf(yearlyResult.getStatus().toString()));
  }

  private YearlyResult toYearlyResult(
      String existingId,
      User student,
      Group group,
      StudentLevel level,
      BigDecimal weightedAverage,
      BigDecimal obtainedCredits,
      BigDecimal totalCredits,
      ResultOverviewStatus status) {
    return YearlyResult.builder()
        .id(existingId)
        .student(student)
        .group(group)
        .level(level)
        .weightedAverage(weightedAverage)
        .obtainedCredits(obtainedCredits)
        .totalCredits(totalCredits)
        .status(status)
        .build();
  }

  private String keyOf(String studentId, StudentLevel level) {
    return studentId + "#" + level;
  }
}
