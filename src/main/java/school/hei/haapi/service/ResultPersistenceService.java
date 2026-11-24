package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import school.hei.haapi.model.StudentResultOverview;
import school.hei.haapi.model.YearlyResult;
import school.hei.haapi.repository.StudentResultOverviewRepository;
import school.hei.haapi.repository.YearlyResultRepository;

@Service
@AllArgsConstructor
public class ResultPersistenceService {
  private final StudentResultOverviewRepository studentResultOverviewRepository;
  private final YearlyResultRepository yearlyResultRepository;

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public StudentResultOverview saveStudentResultOverview(StudentResultOverview overview) {
    return studentResultOverviewRepository.saveAndFlush(overview);
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public YearlyResult saveYearlyResult(YearlyResult yearlyResult) {
    return yearlyResultRepository.saveAndFlush(yearlyResult);
  }
}
