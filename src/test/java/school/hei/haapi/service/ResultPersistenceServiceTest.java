package school.hei.haapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import school.hei.haapi.model.StudentResultOverview;
import school.hei.haapi.model.YearlyResult;
import school.hei.haapi.repository.StudentResultOverviewRepository;
import school.hei.haapi.repository.YearlyResultRepository;

class ResultPersistenceServiceTest {
  private final StudentResultOverviewRepository studentResultOverviewRepository = mock();
  private final YearlyResultRepository yearlyResultRepository = mock();
  private final ResultPersistenceService subject =
      new ResultPersistenceService(studentResultOverviewRepository, yearlyResultRepository);

  @Test
  void saves_a_student_result_overview() {
    var overview = StudentResultOverview.builder().id("ov1").build();
    when(studentResultOverviewRepository.saveAndFlush(overview)).thenReturn(overview);

    assertEquals(overview, subject.saveStudentResultOverview(overview));
  }

  @Test
  void propagates_the_failure_when_a_student_result_overview_cannot_be_persisted() {
    var overview = StudentResultOverview.builder().id("ov1").build();
    when(studentResultOverviewRepository.saveAndFlush(overview))
        .thenThrow(new RuntimeException("constraint violation"));

    assertThrows(RuntimeException.class, () -> subject.saveStudentResultOverview(overview));
  }

  @Test
  void saves_a_yearly_result() {
    var yearlyResult = YearlyResult.builder().id("yr1").build();
    when(yearlyResultRepository.saveAndFlush(yearlyResult)).thenReturn(yearlyResult);

    assertEquals(yearlyResult, subject.saveYearlyResult(yearlyResult));
  }

  @Test
  void propagates_the_failure_when_a_yearly_result_cannot_be_persisted() {
    var yearlyResult = YearlyResult.builder().id("yr1").build();
    when(yearlyResultRepository.saveAndFlush(yearlyResult))
        .thenThrow(new RuntimeException("constraint violation"));

    assertThrows(RuntimeException.class, () -> subject.saveYearlyResult(yearlyResult));
  }
}
