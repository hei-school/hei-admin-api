package school.hei.haapi.service.event;

import static school.hei.haapi.endpoint.rest.model.JobHealth.FAILED;
import static school.hei.haapi.endpoint.rest.model.JobHealth.SUCCEEDED;
import static school.hei.haapi.endpoint.rest.model.JobHealth.UNKNOWN;
import static school.hei.haapi.endpoint.rest.model.JobProgression.FINISHED;
import static school.hei.haapi.endpoint.rest.model.JobProgression.PENDING;
import static school.hei.haapi.endpoint.rest.model.JobProgression.PROCESSING;

import java.util.List;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.event.model.FeeCreationJobStatusComputationTriggered;
import school.hei.haapi.endpoint.rest.model.JobHealth;
import school.hei.haapi.endpoint.rest.model.JobProgression;
import school.hei.haapi.model.FeeCreationTask;
import school.hei.haapi.model.JobStatus;
import school.hei.haapi.service.FeeCreationJobService;
import school.hei.haapi.service.FeeCreationTaskService;

@Slf4j
@Service
public class FeeCreationJobStatusComputationTriggeredService
    implements Consumer<FeeCreationJobStatusComputationTriggered> {
  private final FeeCreationJobService feeCreationJobService;
  private final FeeCreationTaskService feeCreationTaskService;
  private final int maxReceiveCount;

  public FeeCreationJobStatusComputationTriggeredService(
      FeeCreationJobService feeCreationJobService,
      FeeCreationTaskService feeCreationTaskService,
      @Value("${aws.sqs.maxReceiveCount}") int maxReceiveCount) {
    this.feeCreationJobService = feeCreationJobService;
    this.feeCreationTaskService = feeCreationTaskService;
    this.maxReceiveCount = maxReceiveCount;
  }

  @Override
  public void accept(FeeCreationJobStatusComputationTriggered event) {
    var jobId = event.getJobId();
    var tasks = feeCreationTaskService.findAllByJobId(jobId);
    var progression = progressionOf(tasks);
    var health = healthOf(tasks, progression);

    if (isLastAttemptBeforeDeadLetter(event)) {
      feeCreationJobService.updateStatus(
          jobId, JobStatus.builder().progression(FINISHED).health(FAILED).build());
      log.warn(
          "Fee creation job id={} still {}/{} after {} attempts, marked as finished and failed",
          jobId,
          progression,
          health,
          event.getAttemptNb());
      return;
    }

    feeCreationJobService.updateStatus(
        jobId, JobStatus.builder().progression(progression).health(health).build());
    log.info("Fee creation job id={} status computed as {}/{}", jobId, progression, health);

    if (!FINISHED.equals(progression)) {
      throw new IllegalStateException(
          "Fee creation job id=%s is %s/%s, its status is to be computed again"
              .formatted(jobId, progression, health));
    }
  }

  private boolean isLastAttemptBeforeDeadLetter(FeeCreationJobStatusComputationTriggered event) {
    return event.getAttemptNb() >= maxReceiveCount - 1;
  }

  private JobProgression progressionOf(List<FeeCreationTask> tasks) {
    if (tasks.isEmpty() || tasks.stream().allMatch(task -> task.getActualStatus().isEmpty())) {
      return PENDING;
    }
    return tasks.stream().allMatch(FeeCreationJobStatusComputationTriggeredService::isFinished)
        ? FINISHED
        : PROCESSING;
  }

  private JobHealth healthOf(List<FeeCreationTask> tasks, JobProgression progression) {
    if (!FINISHED.equals(progression)) {
      return UNKNOWN;
    }
    return tasks.stream().allMatch(FeeCreationJobStatusComputationTriggeredService::hasSucceeded)
        ? SUCCEEDED
        : FAILED;
  }

  private static boolean isFinished(FeeCreationTask task) {
    return task.getActualStatus()
        .map(status -> FINISHED.equals(status.getProgression()))
        .orElse(false);
  }

  private static boolean hasSucceeded(FeeCreationTask task) {
    return task.getActualStatus().map(status -> SUCCEEDED.equals(status.getHealth())).orElse(false);
  }
}
