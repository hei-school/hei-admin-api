package school.hei.haapi.service;

import static java.time.ZoneOffset.UTC;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static school.hei.haapi.endpoint.rest.model.FeeStatusEnum.LATE;
import static school.hei.haapi.endpoint.rest.model.FeeStatusEnum.UNPAID;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.hei.haapi.endpoint.rest.model.JobHealth;
import school.hei.haapi.endpoint.rest.model.JobProgression;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.FeeCreationTask;
import school.hei.haapi.model.TaskStatus;
import school.hei.haapi.model.User;
import school.hei.haapi.model.V2FeeTemplate;
import school.hei.haapi.model.V2FeeTemplateContent;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.FeeCreationTaskRepository;
import school.hei.haapi.repository.FeeRepository;

@Service
@AllArgsConstructor
public class FeeCreationTaskService {
  private final FeeCreationTaskRepository feeCreationTaskRepository;
  private final FeeRepository feeRepository;
  private final UserService userService;

  public List<FeeCreationTask> findAllByJobId(String jobId) {
    return feeCreationTaskRepository.findAllByJobId(jobId);
  }

  public Map<String, User> findStudentsByRefs(List<FeeCreationTask> tasks) {
    var refs = tasks.stream().map(FeeCreationTask::getStudentRef).distinct().toList();
    return userService.findAllByRefIn(refs).stream()
        .collect(toMap(User::getRef, identity(), (first, ignored) -> first));
  }

  public FeeCreationTask getById(String id) {
    return feeCreationTaskRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("FeeCreationTask.id=" + id + " not found"));
  }

  @Transactional
  public FeeCreationTask updateStatus(
      FeeCreationTask task, JobProgression progression, JobHealth health, String message) {
    task.addStatus(TaskStatus.builder().progression(progression).health(health).build());
    task.setMessage(message);
    return feeCreationTaskRepository.save(task);
  }

  @Transactional
  public List<Fee> createFees(FeeCreationTask task) {
    var student = userService.findByRef(task.getStudentRef());
    var feeTemplate = task.getJob().getFeeTemplate();
    if (feeRepository.existsByStudentIdAndFeeTemplateId(student.getId(), feeTemplate.getId())) {
      throw new BadRequestException(
          "Fees were already created for student ref="
              + task.getStudentRef()
              + " and fee template id="
              + feeTemplate.getId());
    }
    var contents = feeTemplate.getFeeTemplateContents();
    if (contents == null || contents.isEmpty()) {
      throw new BadRequestException(
          "Fee template id=" + feeTemplate.getId() + " holds no content to create fees from");
    }
    var fees = contents.stream().map(content -> toFee(student, feeTemplate, content)).toList();
    return feeRepository.saveAll(fees);
  }

  private Fee toFee(
      school.hei.haapi.model.User student,
      V2FeeTemplate feeTemplate,
      V2FeeTemplateContent content) {
    // TODO: must be mandatory as well not 0 by default
    var totalAmount = content.getAmount() == null ? 0 : content.getAmount().intValue();
    var dueDatetime = toDueDatetime(content);
    return Fee.builder()
        .student(student)
        .feeTemplate(feeTemplate)
        .type(feeTemplate.getType())
        .category(feeTemplate.getCategory())
        .frequency(
            school.hei.haapi.endpoint.rest.model.FeeFrequency.UNKNOWN) // TODO: create PUNCTUAL type
        .comment(content.getLabel())
        .totalAmount(totalAmount)
        .remainingAmount(totalAmount)
        .status(isLate(dueDatetime) ? LATE : UNPAID)
        .dueDatetime(dueDatetime)
        .build();
  }

  private Instant toDueDatetime(V2FeeTemplateContent content) {
    return content.getDueDate() == null ? null : content.getDueDate().atStartOfDay().toInstant(UTC);
  }

  private boolean isLate(Instant dueDatetime) {
    return dueDatetime != null && dueDatetime.isBefore(Instant.now());
  }
}
