package school.hei.haapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.L1;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.L2;
import static school.hei.haapi.model.ResultOverviewStatus.IN_PROGRESS;
import static school.hei.haapi.model.ResultOverviewStatus.NOT_STARTED;
import static school.hei.haapi.model.ResultOverviewStatus.VALIDATED;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import school.hei.haapi.model.CycleLevel;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.Promotion;
import school.hei.haapi.model.User;
import school.hei.haapi.model.YearlyResult;
import school.hei.haapi.model.dto.GroupFlowPeriod;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.repository.YearlyResultRepository;

class YearlyResultServiceTest {
  private final YearlyResultRepository yearlyResultRepository = mock();
  private final UserRepository userRepository = mock();
  private final PromotionService promotionService = mock();
  private final GroupFlowService groupFlowService = mock();
  private final GradeResultService gradeResultService = mock();
  private final YearlyResultService subject =
      new YearlyResultService(
          yearlyResultRepository,
          userRepository,
          promotionService,
          groupFlowService,
          gradeResultService);

  private static Group group(String id, String ref, Promotion promotion) {
    return Group.builder().id(id).ref(ref).promotion(promotion).build();
  }

  private static YearlyResult rankedYearlyResult(String ref, String groupRef, BigDecimal average) {
    return YearlyResult.builder()
        .student(User.builder().ref(ref).build())
        .group(Group.builder().id("g-" + ref).ref(groupRef).build())
        .level(L1)
        .status(VALIDATED)
        .weightedAverage(average)
        .obtainedCredits(BigDecimal.valueOf(60))
        .totalCredits(BigDecimal.valueOf(60))
        .build();
  }

  @Test
  void generate_ranking_xlsx_gives_tied_students_the_same_rank_and_skips_the_next_one()
      throws Exception {
    var promotion = Promotion.builder().id("promo1").build();
    var group = Group.builder().id("g1").ref("K2").build();
    when(promotionService.getPromotionById("promo1")).thenReturn(promotion);
    when(groupFlowService.findGroupsOfPromotionAtLevel(promotion, L1)).thenReturn(List.of(group));
    when(yearlyResultRepository.findAllByGroupIdsAndLevelOrderByWeightedAverageDesc(
            List.of("g1"), L1))
        .thenReturn(
            List.of(
                rankedYearlyResult("STD1", "K2", BigDecimal.valueOf(18)),
                rankedYearlyResult("STD2", "K2", BigDecimal.valueOf(15)),
                rankedYearlyResult("STD3", "K2", BigDecimal.valueOf(15)),
                rankedYearlyResult("STD4", "K2", BigDecimal.valueOf(12))));

    var xlsx = subject.generateYearlyResultsRankingXlsx("promo1", L1);

    try (var workbook = new XSSFWorkbook(new ByteArrayInputStream(xlsx))) {
      var sheet = workbook.getSheetAt(0);
      var header = sheet.getRow(0);
      assertEquals("rank", header.getCell(0).getStringCellValue());
      assertEquals("ref", header.getCell(1).getStringCellValue());
      assertEquals("weightedAverage", header.getCell(2).getStringCellValue());
      assertEquals("obtainedCredits", header.getCell(3).getStringCellValue());
      assertEquals("totalCredits", header.getCell(4).getStringCellValue());
      assertEquals("groupe", header.getCell(5).getStringCellValue());

      assertEquals("1", sheet.getRow(1).getCell(0).getStringCellValue());
      assertEquals("STD1", sheet.getRow(1).getCell(1).getStringCellValue());

      assertEquals("2", sheet.getRow(2).getCell(0).getStringCellValue());
      assertEquals("STD2", sheet.getRow(2).getCell(1).getStringCellValue());

      assertEquals("2", sheet.getRow(3).getCell(0).getStringCellValue());
      assertEquals("STD3", sheet.getRow(3).getCell(1).getStringCellValue());

      assertEquals("4", sheet.getRow(4).getCell(0).getStringCellValue());
      assertEquals("STD4", sheet.getRow(4).getCell(1).getStringCellValue());
    }
  }

  private User studentCurrentlyAt(StudentLevelSetup setup) {
    var promotion = mock(Promotion.class);
    when(promotion.getLevelAt(any(Instant.class))).thenReturn(setup.currentLevel);
    when(promotion.getCycleLevel()).thenReturn(CycleLevel.BACHELOR);
    var currentGroup = group("current-group", "K2", promotion);
    var student = mock(User.class);
    when(student.getId()).thenReturn("student1");
    when(student.findCurrentGroup()).thenReturn(Optional.of(currentGroup));
    return student;
  }

  private record StudentLevelSetup(
      school.hei.haapi.endpoint.rest.model.StudentLevel currentLevel) {}

  @Test
  void crupdate_persists_the_real_computed_result_for_every_started_level_up_to_the_current_one() {
    var student = studentCurrentlyAt(new StudentLevelSetup(L2));
    var l1Group = group("l1-group", "K1", null);
    var l2Group = group("l2-group", "K2", null);
    when(userRepository.findAllStudentNotDisabledWithGroupFlow("promo1"))
        .thenReturn(List.of(student));
    when(yearlyResultRepository.findAllByStudent_IdIn(List.of("student1"))).thenReturn(List.of());
    when(gradeResultService.findLeveledYearlyResultByStudentId(L1, "student1"))
        .thenReturn(
            Optional.of(
                new school.hei.haapi.endpoint.rest.model.YearlyResult()
                    .level(L1)
                    .status(school.hei.haapi.endpoint.rest.model.ResultOverviewStatus.VALIDATED)
                    .weightedAverage(BigDecimal.valueOf(15))
                    .obtainedCredits(BigDecimal.valueOf(60))
                    .totalCredits(BigDecimal.valueOf(60))));
    when(gradeResultService.findLeveledYearlyResultByStudentId(L2, "student1"))
        .thenReturn(
            Optional.of(
                new school.hei.haapi.endpoint.rest.model.YearlyResult()
                    .level(L2)
                    .status(school.hei.haapi.endpoint.rest.model.ResultOverviewStatus.IN_PROGRESS)
                    .weightedAverage(BigDecimal.valueOf(12))
                    .obtainedCredits(BigDecimal.valueOf(30))
                    .totalCredits(BigDecimal.valueOf(60))));
    when(groupFlowService.findStudentLatestGroupFlowPeriodsAtLevel("student1", L1))
        .thenReturn(
            List.of(new GroupFlowPeriod(l1Group, Instant.parse("2023-11-05T00:00:00Z"), null)));
    when(groupFlowService.findStudentLatestGroupFlowPeriodsAtLevel("student1", L2))
        .thenReturn(
            List.of(new GroupFlowPeriod(l2Group, Instant.parse("2024-11-05T00:00:00Z"), null)));

    var toSave = subject.getYearlyResultsToCrupdate("promo1");

    assertEquals(2, toSave.size());
    var l1Result = toSave.stream().filter(r -> r.getLevel() == L1).findFirst().orElseThrow();
    assertEquals(l1Group, l1Result.getGroup());
    assertEquals(0, BigDecimal.valueOf(15).compareTo(l1Result.getWeightedAverage()));
    assertEquals(VALIDATED, l1Result.getStatus());

    var l2Result = toSave.stream().filter(r -> r.getLevel() == L2).findFirst().orElseThrow();
    assertEquals(l2Group, l2Result.getGroup());
    assertEquals(0, BigDecimal.valueOf(12).compareTo(l2Result.getWeightedAverage()));
    assertEquals(IN_PROGRESS, l2Result.getStatus());
  }

  @Test
  void crupdate_zeroes_the_average_and_credits_of_the_current_level_when_not_started_yet() {
    var student = studentCurrentlyAt(new StudentLevelSetup(L1));
    var l1Group = group("l1-group", "K1", null);
    when(userRepository.findAllStudentNotDisabledWithGroupFlow("promo1"))
        .thenReturn(List.of(student));
    when(yearlyResultRepository.findAllByStudent_IdIn(List.of("student1"))).thenReturn(List.of());
    when(gradeResultService.findLeveledYearlyResultByStudentId(L1, "student1"))
        .thenReturn(
            Optional.of(
                new school.hei.haapi.endpoint.rest.model.YearlyResult()
                    .level(L1)
                    .status(school.hei.haapi.endpoint.rest.model.ResultOverviewStatus.NOT_STARTED)
                    .totalCredits(BigDecimal.valueOf(60))));
    when(groupFlowService.findStudentLatestGroupFlowPeriodsAtLevel("student1", L1))
        .thenReturn(
            List.of(new GroupFlowPeriod(l1Group, Instant.parse("2023-11-05T00:00:00Z"), null)));

    var toSave = subject.getYearlyResultsToCrupdate("promo1");

    assertEquals(1, toSave.size());
    var result = toSave.getFirst();
    assertEquals(l1Group, result.getGroup());
    assertEquals(NOT_STARTED, result.getStatus());
    assertEquals(0, BigDecimal.ZERO.compareTo(result.getWeightedAverage()));
    assertEquals(0, BigDecimal.ZERO.compareTo(result.getObtainedCredits()));
    assertEquals(0, BigDecimal.valueOf(60).compareTo(result.getTotalCredits()));
  }

  @Test
  void crupdate_treats_a_past_level_never_attended_with_us_as_validated_externally() {
    var student = studentCurrentlyAt(new StudentLevelSetup(L2));
    var l2Group = group("l2-group", "K2", null);
    when(userRepository.findAllStudentNotDisabledWithGroupFlow("promo1"))
        .thenReturn(List.of(student));
    when(yearlyResultRepository.findAllByStudent_IdIn(List.of("student1"))).thenReturn(List.of());
    when(gradeResultService.findLeveledYearlyResultByStudentId(L1, "student1"))
        .thenReturn(
            Optional.of(
                new school.hei.haapi.endpoint.rest.model.YearlyResult()
                    .level(L1)
                    .status(
                        school.hei.haapi.endpoint.rest.model.ResultOverviewStatus.NOT_STARTED)));
    when(gradeResultService.findLeveledYearlyResultByStudentId(L2, "student1"))
        .thenReturn(
            Optional.of(
                new school.hei.haapi.endpoint.rest.model.YearlyResult()
                    .level(L2)
                    .status(school.hei.haapi.endpoint.rest.model.ResultOverviewStatus.IN_PROGRESS)
                    .weightedAverage(BigDecimal.valueOf(13))
                    .obtainedCredits(BigDecimal.valueOf(20))
                    .totalCredits(BigDecimal.valueOf(60))));
    when(groupFlowService.findStudentLatestGroupFlowPeriodsAtLevel("student1", L1))
        .thenReturn(List.of());
    when(groupFlowService.findStudentLatestGroupFlowPeriodsAtLevel("student1", L2))
        .thenReturn(
            List.of(new GroupFlowPeriod(l2Group, Instant.parse("2024-11-05T00:00:00Z"), null)));

    var toSave = subject.getYearlyResultsToCrupdate("promo1");

    assertEquals(2, toSave.size());
    var l1Result = toSave.stream().filter(r -> r.getLevel() == L1).findFirst().orElseThrow();
    assertNull(l1Result.getGroup());
    assertEquals(VALIDATED, l1Result.getStatus());
    assertEquals(0, BigDecimal.TEN.compareTo(l1Result.getWeightedAverage()));
    assertEquals(0, BigDecimal.valueOf(60).compareTo(l1Result.getObtainedCredits()));
    assertEquals(0, BigDecimal.valueOf(60).compareTo(l1Result.getTotalCredits()));
  }

  @Test
  void crupdate_returns_nothing_for_a_student_without_a_resolvable_current_level() {
    var student = mock(User.class);
    when(student.getId()).thenReturn("student1");
    when(student.findCurrentGroup()).thenReturn(Optional.empty());
    when(userRepository.findAllStudentNotDisabledWithGroupFlow("promo1"))
        .thenReturn(List.of(student));
    when(yearlyResultRepository.findAllByStudent_IdIn(List.of("student1"))).thenReturn(List.of());

    var toSave = subject.getYearlyResultsToCrupdate("promo1");

    assertTrue(toSave.isEmpty());
  }

  @Test
  void crupdate_reuses_the_existing_row_id_for_the_same_student_and_level() {
    var student = studentCurrentlyAt(new StudentLevelSetup(L1));
    var l1Group = group("l1-group", "K1", null);
    var existing = YearlyResult.builder().id("existing-id").student(student).level(L1).build();
    when(userRepository.findAllStudentNotDisabledWithGroupFlow("promo1"))
        .thenReturn(List.of(student));
    when(yearlyResultRepository.findAllByStudent_IdIn(List.of("student1")))
        .thenReturn(List.of(existing));
    when(gradeResultService.findLeveledYearlyResultByStudentId(L1, "student1"))
        .thenReturn(
            Optional.of(
                new school.hei.haapi.endpoint.rest.model.YearlyResult()
                    .level(L1)
                    .status(school.hei.haapi.endpoint.rest.model.ResultOverviewStatus.VALIDATED)
                    .weightedAverage(BigDecimal.valueOf(16))
                    .obtainedCredits(BigDecimal.valueOf(60))
                    .totalCredits(BigDecimal.valueOf(60))));
    when(groupFlowService.findStudentLatestGroupFlowPeriodsAtLevel("student1", L1))
        .thenReturn(
            List.of(new GroupFlowPeriod(l1Group, Instant.parse("2023-11-05T00:00:00Z"), null)));

    var toSave = subject.getYearlyResultsToCrupdate("promo1");

    assertEquals(1, toSave.size());
    assertEquals("existing-id", toSave.getFirst().getId());
  }
}
