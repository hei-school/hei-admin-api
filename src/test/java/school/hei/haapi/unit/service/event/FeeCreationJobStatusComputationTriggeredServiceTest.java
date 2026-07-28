package school.hei.haapi.unit.service.event;

import static java.time.Instant.now;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static school.hei.haapi.endpoint.rest.model.JobHealth.FAILED;
import static school.hei.haapi.endpoint.rest.model.JobHealth.SUCCEEDED;
import static school.hei.haapi.endpoint.rest.model.JobHealth.UNKNOWN;
import static school.hei.haapi.endpoint.rest.model.JobProgression.FINISHED;
import static school.hei.haapi.endpoint.rest.model.JobProgression.PENDING;
import static school.hei.haapi.endpoint.rest.model.JobProgression.PROCESSING;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import school.hei.haapi.endpoint.event.model.FeeCreationJobStatusComputationTriggered;
import school.hei.haapi.endpoint.rest.model.JobHealth;
import school.hei.haapi.endpoint.rest.model.JobProgression;
import school.hei.haapi.model.FeeCreationTask;
import school.hei.haapi.model.JobStatus;
import school.hei.haapi.model.TaskStatus;
import school.hei.haapi.service.FeeCreationJobService;
import school.hei.haapi.service.FeeCreationTaskService;
import school.hei.haapi.service.event.FeeCreationJobStatusComputationTriggeredService;

class FeeCreationJobStatusComputationTriggeredServiceTest {
  private static final int MAX_RECEIVE_COUNT = 5;

  private FeeCreationJobService feeCreationJobService;
  private FeeCreationTaskService feeCreationTaskService;
  private FeeCreationJobStatusComputationTriggeredService subject;

  @BeforeEach
  void setUp() {
    feeCreationJobService = mock();
    feeCreationTaskService = mock();
    subject =
        new FeeCreationJobStatusComputationTriggeredService(
            feeCreationJobService, feeCreationTaskService, MAX_RECEIVE_COUNT);
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

  private JobStatus computedStatusOf(List<FeeCreationTask> tasks) {
    return computedStatusOf(tasks, 1);
  }

  private JobStatus computedStatusOf(List<FeeCreationTask> tasks, int attemptNb) {
    when(feeCreationTaskService.findAllByJobId("job_1")).thenReturn(tasks);
    var event = FeeCreationJobStatusComputationTriggered.builder().jobId("job_1").build();
    event.setAttemptNb(attemptNb);

    // an unsettled job fails the event on purpose, so that the queue brings it back later
    try {
      subject.accept(event);
    } catch (IllegalStateException retriedLater) {
      // expected while the job has not settled
    }

    var captor = ArgumentCaptor.forClass(JobStatus.class);
    verify(feeCreationJobService)
        .updateStatus(org.mockito.ArgumentMatchers.eq("job_1"), captor.capture());
    return captor.getValue();
  }

  @Test
  void no_task_processed_yet_is_pending() {
    var status = computedStatusOf(List.of(aTask(null, null), aTask(null, null)));

    assertEquals(PENDING, status.getProgression());
    assertEquals(UNKNOWN, status.getHealth());
  }

  @Test
  void job_without_task_is_pending() {
    var status = computedStatusOf(List.of());

    assertEquals(PENDING, status.getProgression());
    assertEquals(UNKNOWN, status.getHealth());
  }

  @Test
  void partially_processed_job_is_processing() {
    var status = computedStatusOf(List.of(aTask(FINISHED, SUCCEEDED), aTask(PROCESSING, UNKNOWN)));

    assertEquals(PROCESSING, status.getProgression());
    assertEquals(UNKNOWN, status.getHealth());
  }

  @Test
  void job_with_a_pending_task_is_processing() {
    var status = computedStatusOf(List.of(aTask(FINISHED, SUCCEEDED), aTask(null, null)));

    assertEquals(PROCESSING, status.getProgression());
    assertEquals(UNKNOWN, status.getHealth());
  }

  @Test
  void all_tasks_succeeded_is_finished_and_succeeded() {
    var status = computedStatusOf(List.of(aTask(FINISHED, SUCCEEDED), aTask(FINISHED, SUCCEEDED)));

    assertEquals(FINISHED, status.getProgression());
    assertEquals(SUCCEEDED, status.getHealth());
  }

  @Test
  void a_single_failed_task_makes_the_whole_job_failed() {
    var status = computedStatusOf(List.of(aTask(FINISHED, SUCCEEDED), aTask(FINISHED, FAILED)));

    assertEquals(FINISHED, status.getProgression());
    assertEquals(FAILED, status.getHealth());
  }

  @Test
  void an_unsettled_job_fails_the_event_so_that_it_comes_back_later() {
    when(feeCreationTaskService.findAllByJobId("job_1"))
        .thenReturn(List.of(aTask(PROCESSING, UNKNOWN)));
    var event = FeeCreationJobStatusComputationTriggered.builder().jobId("job_1").build();
    event.setAttemptNb(1);

    assertThrows(IllegalStateException.class, () -> subject.accept(event));
  }

  @Test
  void a_settled_job_lets_the_event_through() {
    when(feeCreationTaskService.findAllByJobId("job_1"))
        .thenReturn(List.of(aTask(FINISHED, SUCCEEDED)));
    var event = FeeCreationJobStatusComputationTriggered.builder().jobId("job_1").build();
    event.setAttemptNb(1);

    assertDoesNotThrow(() -> subject.accept(event));
  }

  @Test
  void a_finished_but_failed_job_is_terminal_too() {
    when(feeCreationTaskService.findAllByJobId("job_1"))
        .thenReturn(List.of(aTask(FINISHED, SUCCEEDED), aTask(FINISHED, FAILED)));
    var event = FeeCreationJobStatusComputationTriggered.builder().jobId("job_1").build();
    event.setAttemptNb(1);

    assertDoesNotThrow(() -> subject.accept(event));
  }

  @Test
  void a_job_still_running_one_attempt_before_the_dead_letter_queue_is_called_off() {
    var status = computedStatusOf(List.of(aTask(PROCESSING, UNKNOWN)), MAX_RECEIVE_COUNT - 1);

    // taking that long is not something the job is expected to recover from
    assertEquals(FINISHED, status.getProgression());
    assertEquals(FAILED, status.getHealth());
  }

  @Test
  void the_last_attempt_does_not_fail_the_event_any_more() {
    when(feeCreationTaskService.findAllByJobId("job_1"))
        .thenReturn(List.of(aTask(PROCESSING, UNKNOWN)));
    var event = FeeCreationJobStatusComputationTriggered.builder().jobId("job_1").build();
    event.setAttemptNb(MAX_RECEIVE_COUNT - 1);

    // it is called off rather than sent to the dead letter queue
    assertDoesNotThrow(() -> subject.accept(event));
  }
}
