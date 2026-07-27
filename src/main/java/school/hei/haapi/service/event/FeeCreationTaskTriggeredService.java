package school.hei.haapi.service.event;

import static school.hei.haapi.endpoint.rest.model.JobHealth.FAILED;
import static school.hei.haapi.endpoint.rest.model.JobHealth.SUCCEEDED;
import static school.hei.haapi.endpoint.rest.model.JobHealth.UNKNOWN;
import static school.hei.haapi.endpoint.rest.model.JobProgression.FINISHED;
import static school.hei.haapi.endpoint.rest.model.JobProgression.PROCESSING;

import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.event.model.FeeCreationTaskTriggered;
import school.hei.haapi.service.FeeCreationTaskService;

@Slf4j
@Service
@AllArgsConstructor
public class FeeCreationTaskTriggeredService implements Consumer<FeeCreationTaskTriggered> {
  private final FeeCreationTaskService feeCreationTaskService;

  @Override
  public void accept(FeeCreationTaskTriggered event) {
    var task = event.getTask();
    var processing = feeCreationTaskService.updateStatus(task, PROCESSING, UNKNOWN, null);

    try {
      var fees = feeCreationTaskService.createFees(task);

      feeCreationTaskService.updateStatus(processing, FINISHED, SUCCEEDED, null);
      log.info(
          "Fee creation task id={} created {} fees for student ref={}",
          task.getId(),
          fees.size(),
          task.getStudentRef());
    } catch (Exception e) {
      feeCreationTaskService.updateStatus(processing, FINISHED, FAILED, e.getMessage());
      log.warn(
          "Fee creation task id={} failed for student ref={}: {}",
          task.getId(),
          task.getStudentRef(),
          e.getMessage());
    }
  }
}
