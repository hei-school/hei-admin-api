package school.hei.haapi.service;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.User;
import school.hei.haapi.model.WorkInfo;
import school.hei.haapi.model.validator.WorkInfoValidator;
import school.hei.haapi.repository.WorkInfoRepository;

@Service
@AllArgsConstructor
public class WorkInfoService {
  private final WorkInfoRepository workInfoRepository;
  private final WorkInfoValidator workInfoValidator;
  private final UserService userService;

  public List<WorkInfo> saveAll(List<WorkInfo> toSave) {
    workInfoValidator.accept(toSave);
    return workInfoRepository.saveAll(toSave);
  }

  public List<WorkInfo> getWorkInfo(String studentId) {
    User student = userService.findById(studentId);
    return workInfoRepository.findWorkInfosByStudent(student);
  }
}
