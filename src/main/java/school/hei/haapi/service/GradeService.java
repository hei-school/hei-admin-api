package school.hei.haapi.service;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.Grade;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.GradeRepository;

@Service
@AllArgsConstructor
public class GradeService {
  private final GradeRepository gradeRepository;

  public List<Grade> getAllGradesBy(String id) {
    return gradeRepository.getGradeById(id);
  }

  public List<Grade> getAllGradesByUser(User student) {
    return gradeRepository.getGradeByUser(student);
  }

  public Grade getGradeByExamId(String id) {
    return gradeRepository.getGradeByExamId(id);
  }
}

