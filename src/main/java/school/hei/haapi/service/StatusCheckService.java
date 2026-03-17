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
import school.hei.haapi.model.validator.CreateStatusCheckValidator;
import school.hei.haapi.model.validator.UpdateStatusCheckValidator;
import school.hei.haapi.repository.StatusCheckRepository;

@Service
@AllArgsConstructor
public class StatusCheckService {
  private final StatusCheckRepository repository;
  private final StatusCheckMapper statusCheckMapper;
  private final UserService userService; 
  private final CreateStatusCheckValidator createStatusCheckValidator;
  private final UpdateStatusCheckValidator updateStatusCheckValidator; 

  public List<StatusCheck> getAllByResult(StatusCheckResult result) {
    return repository.findAllByResult(Objects.requireNonNullElse(result, PENDING));
  }

  public List<StatusCheck> getByConcernedStudentId(String studentId) {
    return repository.findAllByConcernedStudentId(studentId);
  }

  public StatusCheck updateStatusCheckByStudentId(
      String studentId, String statusCheckId, UpdateStatusCheck updateStatusCheck) {
    updateStatusCheckValidator.accept(updateStatusCheck);
    userService.getById(studentId);
    var toUpdate = getStatusByIdIfPresent(statusCheckId);
    toUpdate.setDescription(updateStatusCheck.getDescription());
    toUpdate.setResult(updateStatusCheck.getResult());
    return repository.save(toUpdate);
  }

  public StatusCheck createStatusCheckByStudentId(String studentId, CreateStatusCheck toInsert) {
    createStatusCheckValidator.accept(toInsert);
    var concernedStudent = userService.getById(studentId);
    checkStudentStatus(concernedStudent);
    return repository.save(statusCheckMapper.toDomain(toInsert));
  }

  private void checkStudentStatus(User student) {
    if (DISABLED.equals(student.getStatus())){
      throw new BadRequestException(
          "Cannot create a status check: Student with ref : "
              + student.getRef()
              + " is already DISABLED");
    } 
    if(ALUMNI.equals(student.getStatus())) {
      throw new BadRequestException(
          "Cannot create a status check: Student with ref : "
              + student.getRef()
              + " is already an ALUMNI");
    }
  }

  private StatusCheck getStatusByIdIfPresent(String statusCheckId) {
    var optionalStatusCheck = repository.findById(statusCheckId);
    if (optionalStatusCheck.isEmpty()) {
      throw new NotFoundException("Status check with id " + statusCheckId + " not found");
    }
    return optionalStatusCheck.get();
  }
}
