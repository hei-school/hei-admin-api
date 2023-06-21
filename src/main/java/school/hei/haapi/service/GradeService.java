package school.hei.haapi.service;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.Grade;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.GradeRepository;
import school.hei.haapi.repository.UserRepository;

@Service
@AllArgsConstructor
public class GradeService {
  private final GradeRepository gradeRepository;
  private final UserRepository userRepository;
  public List<Grade> getAllGradesBy(String id) {
    return gradeRepository.getGradeById(id);
  }

  public List<Grade> getGradeByUser(User student) {
    return gradeRepository.getGradeByUserId(student);
  }

  public Grade getGradeByExamId(String id) {
    return gradeRepository.getGradeByExamId(id);
  }
}

