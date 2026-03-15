package school.hei.haapi.service;

import static school.hei.haapi.endpoint.rest.model.StatusCheckResult.PENDING;
import static school.hei.haapi.model.User.Status.ALUMNI;
import static school.hei.haapi.model.User.Status.DISABLED;

import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.rest.mapper.StatusCheckMapper;
import school.hei.haapi.endpoint.rest.model.CreateStatusCheck;
import school.hei.haapi.endpoint.rest.model.StatusCheckResult;
import school.hei.haapi.endpoint.rest.model.UpdateStatusCheck;
import school.hei.haapi.model.StatusCheck;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.StatusCheckRepository;
import school.hei.haapi.repository.UserRepository;

@Service
@AllArgsConstructor
public class StatusCheckService {
  private final StatusCheckRepository statusCheckRepository;
  private final StatusCheckMapper statusCheckMapper;
  private final UserRepository userRepository;

  public List<StatusCheck> getAllByResult(StatusCheckResult result) {
    return statusCheckRepository.findAllByResult(Objects.requireNonNullElse(result, PENDING));
  }

  public List<StatusCheck> getByConcernedStudentId(String studentId) {
    return statusCheckRepository.findAllByConcernedStudentId(studentId);
  }

  public StatusCheck updateStatusCheckByStudentId(
      String studentId, String statusCheckId, UpdateStatusCheck updateStatusCheck) {
    var optionalUser = userRepository.findById(studentId);
    if (optionalUser.isEmpty()) {
      throw new NotFoundException("The concerned student could not be not found");
    }
    var toUpdate = getStatusByIdIfPresent(statusCheckId);
    toUpdate.setDescription(updateStatusCheck.getDescription());
    toUpdate.setResult(updateStatusCheck.getResult());
    return statusCheckRepository.save(toUpdate);
  }

  public StatusCheck createStatusCheckByStudentId(String studentId, CreateStatusCheck toInsert) {
    var optionalUser = userRepository.findById(studentId);
    if (optionalUser.isEmpty()) {
      throw new NotFoundException("The concerned student could not be not found");
    }
    var student = optionalUser.get();
    checkStudentStatus(student);
    return statusCheckRepository.save(statusCheckMapper.toDomain(toInsert));
  }

  private void checkStudentStatus(User student) {
    if (ALUMNI.equals(student.getStatus()) || DISABLED.equals(student.getStatus())) {
      throw new BadRequestException(
          "Cannot create a status check: Student with ref : "
              + student.getRef()
              + " is already DISABLED or an ALUMNI");
    }
  }

  private StatusCheck getStatusByIdIfPresent(String statusCheckId) {
    var optionalStatusCheck = statusCheckRepository.findById(statusCheckId);
    if (optionalStatusCheck.isEmpty()) {
      throw new NotFoundException("Status check with id " + statusCheckId + " not found");
    }
    ;
    return optionalStatusCheck.get();
  }
}
