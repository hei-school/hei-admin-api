package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.rest.model.StudentLevel;
import school.hei.haapi.endpoint.rest.model.YearlyResult;
import school.hei.haapi.model.exception.NotImplementedException;

@Service
@AllArgsConstructor
public class YearlyResultService {
  public YearlyResult getLevelYearlyResultByStudentId(StudentLevel level, String studentId) {
    throw new NotImplementedException("Need to be implemented");
  }
}
