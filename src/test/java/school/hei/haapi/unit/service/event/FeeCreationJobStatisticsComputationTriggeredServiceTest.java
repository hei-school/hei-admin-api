package school.hei.haapi.unit.service.event;

import static java.time.Instant.now;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static school.hei.haapi.endpoint.rest.model.JobHealth.FAILED;
import static school.hei.haapi.endpoint.rest.model.JobHealth.SUCCEEDED;
import static school.hei.haapi.endpoint.rest.model.JobHealth.UNKNOWN;
import static school.hei.haapi.endpoint.rest.model.JobProgression.FINISHED;
import static school.hei.haapi.endpoint.rest.model.JobProgression.PROCESSING;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import school.hei.haapi.endpoint.event.model.FeeCreationJobStatisticsComputationTriggered;
import school.hei.haapi.endpoint.rest.model.JobHealth;
import school.hei.haapi.endpoint.rest.model.JobProgression;
import school.hei.haapi.model.FeeCreationJobStatistics;
import school.hei.haapi.model.FeeCreationTask;
import school.hei.haapi.model.JobStatus;
import school.hei.haapi.model.TaskStatus;
import school.hei.haapi.service.FeeCreationJobService;
import school.hei.haapi.service.FeeCreationTaskService;
import school.hei.haapi.service.event.FeeCreationJobStatisticsComputationTriggeredService;

class FeeCreationJobStatisticsComputationTriggeredServiceTest {
  private FeeCreationJobService feeCreationJobService;
  private FeeCreationTaskService feeCreationTaskService;
  private FeeCreationJobStatisticsComputationTriggeredService subject;

  @BeforeEach
  void setUp() {
    feeCreationJobService = mock();
    feeCreationTaskService = mock();
    subject =
        new FeeCreationJobStatisticsComputationTriggeredService(
            feeCreationJobService, feeCreationTaskService);
  }

  private static FeeCreationTask aTask(JobProgression progression, JobHealth health) {
    var task = FeeCreationTask.builder().statuses(new ArrayList<>()).build();
    if (progression != null) {
      task.addStatus(
          TaskStatus.builder()
              .progression(progression)
              .health(health)
              .creationDatetime(now())
              .build());
    }
    return task;
  }

  /** The job is left unsettled, so the event is failed on purpose and comes back later. */
  private FeeCreationJobStatistics computedStatisticsOf(List<FeeCreationTask> tasks) {
    when(feeCreationTaskService.findAllByJobId("job_1")).thenReturn(tasks);
    when(feeCreationJobService.getActualStatusOf("job_1")).thenReturn(java.util.Optional.empty());

    try {
      subject.accept(FeeCreationJobStatisticsComputationTriggered.builder().jobId("job_1").build());
    } catch (IllegalStateException retriedLater) {
      // expected while the job has not settled
    }

    var captor = ArgumentCaptor.forClass(FeeCreationJobStatistics.class);
    verify(feeCreationJobService).updateStatistics(eq("job_1"), captor.capture());
    return captor.getValue();
  }

  @Test
  void counts_succeeded_and_failed_tasks() {
    var statistics =
        computedStatisticsOf(
            List.of(
                aTask(FINISHED, SUCCEEDED),
                aTask(FINISHED, SUCCEEDED),
                aTask(FINISHED, FAILED),
                aTask(PROCESSING, UNKNOWN)));

    assertEquals(4, statistics.getTotalCount());
    assertEquals(2, statistics.getSuccessCount());
    assertEquals(1, statistics.getFailureCount());
  }

  @Test
  void unprocessed_tasks_count_in_total_only() {
    var statistics = computedStatisticsOf(List.of(aTask(null, null), aTask(null, null)));

    assertEquals(2, statistics.getTotalCount());
    assertEquals(0, statistics.getSuccessCount());
    assertEquals(0, statistics.getFailureCount());
  }

  @Test
  void job_without_task_has_empty_statistics() {
    var statistics = computedStatisticsOf(List.of());

    assertEquals(0, statistics.getTotalCount());
    assertEquals(0, statistics.getSuccessCount());
    assertEquals(0, statistics.getFailureCount());
  }

  private void givenJobStatus(JobProgression progression, JobHealth health) {
    when(feeCreationJobService.getActualStatusOf("job_1"))
        .thenReturn(
            java.util.Optional.of(
                JobStatus.builder()
                    .progression(progression)
                    .health(health)
                    .creationDatetime(now())
                    .build()));
    when(feeCreationTaskService.findAllByJobId("job_1"))
        .thenReturn(List.of(aTask(FINISHED, SUCCEEDED)));
  }

  @Test
  void an_unsettled_job_fails_the_event_so_that_it_comes_back_later() {
    givenJobStatus(PROCESSING, UNKNOWN);

    assertThrows(
        IllegalStateException.class,
        () ->
            subject.accept(
                FeeCreationJobStatisticsComputationTriggered.builder().jobId("job_1").build()));
  }

  @Test
  void a_finished_but_failed_job_is_terminal_too() {
    givenJobStatus(FINISHED, FAILED);

    assertDoesNotThrow(
        () ->
            subject.accept(
                FeeCreationJobStatisticsComputationTriggered.builder().jobId("job_1").build()));
  }

  @Test
  void a_settled_job_lets_the_event_through() {
    givenJobStatus(FINISHED, SUCCEEDED);

    assertDoesNotThrow(
        () ->
            subject.accept(
                FeeCreationJobStatisticsComputationTriggered.builder().jobId("job_1").build()));
  }

  @Test
  void statistics_are_recomputed_even_before_the_job_settles() {
    givenJobStatus(PROCESSING, UNKNOWN);

    try {
      subject.accept(FeeCreationJobStatisticsComputationTriggered.builder().jobId("job_1").build());
    } catch (IllegalStateException retriedLater) {
      // expected
    }

    // the counts are persisted on every attempt, so a polling front sees them move
    verify(feeCreationJobService).updateStatistics(eq("job_1"), any());
  }
}
