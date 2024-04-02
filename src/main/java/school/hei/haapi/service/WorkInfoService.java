package school.hei.haapi.service;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.User;
import school.hei.haapi.model.WorkInfo;
import school.hei.haapi.model.validator.WorkInfoValidator;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.repository.WorkInfoRepository;

@Service
@AllArgsConstructor
public class WorkInfoService {
  private final WorkInfoRepository workInfoRepository;
  private final WorkInfoValidator workInfoValidator;
  private final UserService userService;
  private final UserRepository userRepository;

  public List<WorkInfo> saveAll(List<WorkInfo> toSave) {
    workInfoValidator.accept(toSave);
    User student = toSave.get(0).getStudent();
    Instant now = Instant.now();

    toSave.forEach(
        workInfo -> {
          if (workInfo.getCommitmentEndDate() != null
              && workInfo.getCommitmentEndDate().isBefore(now)) {
            student.setTakenWorkingStudy(true);
            student.setWorkingStudy(false);
          }
          student.setWorkingStudy(true);
          student.setTakenWorkingStudy(false);

          userRepository.save(student);
        });

    return workInfoRepository.saveAll(toSave);
  }

  public List<WorkInfo> getWorkInfo(String studentId) {
    User student = userService.findById(studentId);

    return workInfoRepository.findWorkInfosByStudent(student);
  }
}
