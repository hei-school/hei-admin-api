package school.hei.haapi.service;

import static school.hei.haapi.endpoint.rest.model.ResultOverviewStatus.NOT_STARTED;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.repository.YearlyResultRepository;
import school.hei.haapi.service.utils.XlsxCellsGenerator;

@Slf4j
@Service
@AllArgsConstructor
public class YearlyResultService {
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
              yearlyResult.getGroup().getRef()));
    }
    return rows;
  }

  private boolean sameAverage(BigDecimal a, BigDecimal b) {
    if (a == null || b == null) {
      return a == b;
    }
    return a.compareTo(b) == 0;
  }

  private List<YearlyResult> buildYearlyResultsForStudent(
      User student, Map<String, YearlyResult> existingByStudentIdAndLevel) {
    var resultSummary = gradeResultService.getStudentResultSummary(student.getId());
    return resultSummary.getYearlyResults().stream()
        .filter(yearlyResult -> yearlyResult.getStatus() != NOT_STARTED)
        .map(yearlyResult -> toEntity(student, yearlyResult, existingByStudentIdAndLevel))
        .filter(java.util.Objects::nonNull)
        .toList();
  }

  private YearlyResult toEntity(
      User student,
      school.hei.haapi.endpoint.rest.model.YearlyResult yearlyResult,
      Map<String, YearlyResult> existingByStudentIdAndLevel) {
    var level = yearlyResult.getLevel();
    var groupFlowPeriods =
        groupFlowService.findStudentLatestGroupFlowPeriodsAtLevel(student.getId(), level);
    if (groupFlowPeriods.isEmpty()) {
      log.warn(
          "No group found for student {} at level {}, skipping yearly result upsert",
          student.getId(),
          level);
      return null;
    }
    var group = groupFlowPeriods.getFirst().group();
    var existing = existingByStudentIdAndLevel.get(keyOf(student.getId(), level));
    return YearlyResult.builder()
        .id(existing != null ? existing.getId() : null)
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
