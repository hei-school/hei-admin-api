package school.hei.haapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static school.hei.haapi.model.ResultOverviewStatus.VALIDATED;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import school.hei.haapi.endpoint.rest.mapper.StudentResultOverviewMapper;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.Promotion;
import school.hei.haapi.model.StudentResultOverview;
import school.hei.haapi.model.User;
import school.hei.haapi.model.pagination.PaginationFromPageAndPageSize;
import school.hei.haapi.repository.StudentResultOverviewRepository;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.repository.dao.StudentResultOverviewDao;

class StudentResultOverviewServiceTest {
  private final PaginationFromPageAndPageSize paginationFromPageAndPageSize =
      new PaginationFromPageAndPageSize();
  private final StudentResultOverviewDao studentResultOverviewDao = mock();
  private final StudentResultOverviewMapper studentResultOverviewMapper = mock();
  private final StudentResultOverviewRepository studentResultOverviewRepository = mock();
  private final PromotionService promotionService = mock();
  private final UserRepository userRepository = mock();
  private final GradeResultService gradeResultService = mock();
  private final ResultPersistenceService resultPersistenceService = mock();
  private final StudentResultOverviewService subject =
      new StudentResultOverviewService(
          paginationFromPageAndPageSize,
          studentResultOverviewDao,
          studentResultOverviewMapper,
          studentResultOverviewRepository,
          promotionService,
          userRepository,
          gradeResultService,
          resultPersistenceService);

  @Test
  void generates_an_xlsx_with_one_row_per_overview_matching_the_criteria() throws Exception {
    var promotion = Promotion.builder().id("promo1").name("Promo 2025").build();
    var student = User.builder().ref("STD1").firstName("Jean").lastName("Rakoto").build();
    var overview =
        StudentResultOverview.builder()
            .student(student)
            .graduationPromotion(promotion)
            .weightedAverage(BigDecimal.valueOf(15))
            .obtainedCredits(BigDecimal.valueOf(60))
            .totalCredits(BigDecimal.valueOf(60))
            .status(VALIDATED)
            .build();
    when(studentResultOverviewDao.filterByCriteria("promo1", VALIDATED, Pageable.unpaged()))
        .thenReturn(List.of(overview));

    var xlsx = subject.generateStudentResultOverviewsXlsx("promo1", VALIDATED);

    try (var workbook = new XSSFWorkbook(new ByteArrayInputStream(xlsx))) {
      var sheet = workbook.getSheetAt(0);
      var header = sheet.getRow(0);
      assertEquals("ref", header.getCell(0).getStringCellValue());
      assertEquals("firstName", header.getCell(1).getStringCellValue());
      assertEquals("lastName", header.getCell(2).getStringCellValue());
      assertEquals("weightedAverage", header.getCell(3).getStringCellValue());
      assertEquals("obtainedCredits", header.getCell(4).getStringCellValue());
      assertEquals("totalCredits", header.getCell(5).getStringCellValue());
      assertEquals("status", header.getCell(6).getStringCellValue());
      assertEquals("graduationPromotion", header.getCell(7).getStringCellValue());

      var row = sheet.getRow(1);
      assertEquals("STD1", row.getCell(0).getStringCellValue());
      assertEquals("Jean", row.getCell(1).getStringCellValue());
      assertEquals("Rakoto", row.getCell(2).getStringCellValue());
      assertEquals(VALIDATED.toString(), row.getCell(6).getStringCellValue());
      assertEquals("Promo 2025", row.getCell(7).getStringCellValue());
    }
  }

  @Test
  void crupdate_skips_a_student_whose_computation_fails_but_keeps_the_others() {
    var promotion = mock(Promotion.class);
    when(promotion.getLevelAt(any(Instant.class)))
        .thenReturn(school.hei.haapi.endpoint.rest.model.StudentLevel.L1);
    var group = Group.builder().id("g1").promotion(promotion).build();

    var goodStudent = mock(User.class);
    when(goodStudent.getId()).thenReturn("good");
    when(goodStudent.findCurrentGroup()).thenReturn(Optional.of(group));

    var badStudent = mock(User.class);
    when(badStudent.getId()).thenReturn("bad");

    when(userRepository.findAllStudentNotDisabledWithGroupFlow("promo1"))
        .thenReturn(List.of(goodStudent, badStudent));
    when(studentResultOverviewRepository.findAllByStudentIdIn(List.of("good", "bad")))
        .thenReturn(List.of());
    when(promotionService.getPromotions(null, null, null, null, null))
        .thenReturn(List.of(promotion));

    when(gradeResultService.getStudentResultSummaryOf(goodStudent))
        .thenReturn(
            new school.hei.haapi.endpoint.rest.model.ResultSummary()
                .status(school.hei.haapi.endpoint.rest.model.ResultOverviewStatus.NOT_STARTED)
                .obtainedCredits(BigDecimal.ZERO)
                .totalCredits(BigDecimal.ZERO));
    when(gradeResultService.getStudentResultSummaryOf(badStudent))
        .thenThrow(new RuntimeException("boom"));

    var toCrupdate = subject.getStudentResultOverviewsToCrupdate("promo1");

    assertEquals(1, toCrupdate.size());
    var overview = toCrupdate.getFirst();
    assertEquals(goodStudent, overview.getStudent());
    assertEquals(0, BigDecimal.ZERO.compareTo(overview.getWeightedAverage()));
  }

  @Test
  void saveAll_skips_an_overview_that_fails_to_persist_but_saves_the_rest() {
    var overview1 =
        StudentResultOverview.builder()
            .id("ov1")
            .student(User.builder().id("std1").build())
            .build();
    var overview2 =
        StudentResultOverview.builder()
            .id("ov2")
            .student(User.builder().id("std2").build())
            .build();
    when(resultPersistenceService.saveStudentResultOverview(overview1)).thenReturn(overview1);
    when(resultPersistenceService.saveStudentResultOverview(overview2))
        .thenThrow(new RuntimeException("db fail"));

    var saved = subject.saveAll(List.of(overview1, overview2));

    assertEquals(List.of(overview1), saved);
  }

  @Test
  void crupdate_handles_more_than_200_students_skipping_only_the_ones_that_fail() {
    var promotion = mock(Promotion.class);
    when(promotion.getLevelAt(any(Instant.class)))
        .thenReturn(school.hei.haapi.endpoint.rest.model.StudentLevel.L1);
    var group = Group.builder().id("g1").promotion(promotion).build();

    var totalStudents = 250;
    var students = new ArrayList<User>();
    var failingIds = new HashSet<String>();
    for (int i = 0; i < totalStudents; i++) {
      var id = "std" + i;
      var student = mock(User.class);
      when(student.getId()).thenReturn(id);
      students.add(student);
      if (i % 10 == 0) {
        failingIds.add(id);
        when(gradeResultService.getStudentResultSummaryOf(student))
            .thenThrow(new RuntimeException("boom-" + id));
      } else {
        when(student.findCurrentGroup()).thenReturn(Optional.of(group));
        when(gradeResultService.getStudentResultSummaryOf(student))
            .thenReturn(
                new school.hei.haapi.endpoint.rest.model.ResultSummary()
                    .status(school.hei.haapi.endpoint.rest.model.ResultOverviewStatus.NOT_STARTED)
                    .obtainedCredits(BigDecimal.ZERO)
                    .totalCredits(BigDecimal.ZERO));
      }
    }

    when(userRepository.findAllStudentNotDisabledWithGroupFlow("promo1")).thenReturn(students);
    when(studentResultOverviewRepository.findAllByStudentIdIn(any())).thenReturn(List.of());
    when(promotionService.getPromotions(null, null, null, null, null))
        .thenReturn(List.of(promotion));

    var toCrupdate = subject.getStudentResultOverviewsToCrupdate("promo1");

    assertEquals(totalStudents - failingIds.size(), toCrupdate.size());
    assertTrue(
        toCrupdate.stream().map(o -> o.getStudent().getId()).noneMatch(failingIds::contains));
  }

  @Test
  void saveAll_handles_more_than_200_overviews_skipping_only_the_ones_that_fail_to_persist() {
    var total = 250;
    var overviews = new ArrayList<StudentResultOverview>();
    var failingIds = new HashSet<String>();
    for (int i = 0; i < total; i++) {
      var id = "ov" + i;
      var overview =
          StudentResultOverview.builder()
              .id(id)
              .student(User.builder().id("std" + i).build())
              .build();
      overviews.add(overview);
      if (i % 7 == 0) {
        failingIds.add(id);
        when(resultPersistenceService.saveStudentResultOverview(overview))
            .thenThrow(new RuntimeException("db fail " + id));
      } else {
        when(resultPersistenceService.saveStudentResultOverview(overview)).thenReturn(overview);
      }
    }

    var saved = subject.saveAll(overviews);

    assertEquals(total - failingIds.size(), saved.size());
    assertTrue(saved.stream().map(StudentResultOverview::getId).noneMatch(failingIds::contains));
  }
}
