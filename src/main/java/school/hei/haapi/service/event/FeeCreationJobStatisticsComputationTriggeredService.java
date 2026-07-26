package school.hei.haapi.service.event;

import static school.hei.haapi.endpoint.rest.model.JobHealth.FAILED;
import static school.hei.haapi.endpoint.rest.model.JobHealth.SUCCEEDED;
import static school.hei.haapi.endpoint.rest.model.JobProgression.FINISHED;

import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.event.model.FeeCreationJobStatisticsComputationTriggered;
import school.hei.haapi.endpoint.rest.model.JobHealth;
import school.hei.haapi.model.FeeCreationJobStatistics;
import school.hei.haapi.model.FeeCreationTask;
import school.hei.haapi.service.FeeCreationJobService;
import school.hei.haapi.service.FeeCreationTaskService;

@Slf4j
@Service
@AllArgsConstructor
public class FeeCreationJobStatisticsComputationTriggeredService
    implements Consumer<FeeCreationJobStatisticsComputationTriggered> {
  private final FeeCreationJobService feeCreationJobService;
  private final FeeCreationTaskService feeCreationTaskService;

  @Override
  public void accept(FeeCreationJobStatisticsComputationTriggered event) {
    var jobId = event.getJobId();
    var tasks = feeCreationTaskService.findAllByJobId(jobId);
    var statistics =
        FeeCreationJobStatistics.builder()
            .totalCount(tasks.size())
            .successCount(countHealth(tasks, SUCCEEDED))
            .failureCount(countHealth(tasks, FAILED))
            .build();
    feeCreationJobService.updateStatistics(jobId, statistics);
    log.info(
        "Fee creation job id={} statistics computed: {} total, {} succeeded, {} failed",
        jobId,
        statistics.getTotalCount(),
        statistics.getSuccessCount(),
        statistics.getFailureCount());

    // TODO: settle from the counts themselves - successCount, failureCount and an unknownCount
    //  left to add - rather than from the job status
    if (!hasSettled(jobId)) {
      throw new IllegalStateException(
          "Fee creation job id=%s has not settled yet, its statistics are to be computed again"
              .formatted(jobId));
    }
  }

  private boolean hasSettled(String jobId) {
    return feeCreationJobService
        .getActualStatusOf(jobId)
        .map(status -> FINISHED.equals(status.getProgression()))
        .orElse(false);
  }

  private int countHealth(List<FeeCreationTask> tasks, JobHealth health) {
    return (int)
        tasks.stream()
            .filter(
                task ->
                    task.getActualStatus()
                        .map(status -> health.equals(status.getHealth()))
                        .orElse(false))
            .count();
  }
}
