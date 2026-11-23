package school.hei.haapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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
  void saveStudentResultOverview_delegates_to_the_repository() {
    var overview = StudentResultOverview.builder().id("ov1").build();
    when(studentResultOverviewRepository.save(overview)).thenReturn(overview);

    var saved = subject.saveStudentResultOverview(overview);

    assertEquals(overview, saved);
    verify(studentResultOverviewRepository).save(overview);
  }

  @Test
  void saveYearlyResult_delegates_to_the_repository() {
    var yearlyResult = YearlyResult.builder().id("yr1").build();
    when(yearlyResultRepository.save(yearlyResult)).thenReturn(yearlyResult);

    var saved = subject.saveYearlyResult(yearlyResult);

    assertEquals(yearlyResult, saved);
    verify(yearlyResultRepository).save(yearlyResult);
  }
}
