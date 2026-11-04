package school.hei.haapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static school.hei.haapi.model.ResultOverviewStatus.VALIDATED;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.List;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import school.hei.haapi.endpoint.rest.mapper.StudentResultOverviewMapper;
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
  private final StudentResultOverviewService subject =
      new StudentResultOverviewService(
          paginationFromPageAndPageSize,
          studentResultOverviewDao,
          studentResultOverviewMapper,
          studentResultOverviewRepository,
          promotionService,
          userRepository,
          gradeResultService);

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
}
