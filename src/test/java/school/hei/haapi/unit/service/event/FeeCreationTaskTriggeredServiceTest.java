package school.hei.haapi.unit.service.event;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.inOrder;
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
import school.hei.haapi.endpoint.event.model.FeeCreationTaskTriggered;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.FeeCreationJob;
import school.hei.haapi.model.FeeCreationTask;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.service.FeeCreationTaskService;
import school.hei.haapi.service.event.FeeCreationTaskTriggeredService;

class FeeCreationTaskTriggeredServiceTest {
  private FeeCreationTaskService feeCreationTaskService;
  private FeeCreationTaskTriggeredService subject;

  @BeforeEach
  void setUp() {
    feeCreationTaskService = mock();
    subject = new FeeCreationTaskTriggeredService(feeCreationTaskService);
  }

  private FeeCreationTask givenTask() {
    var task =
        FeeCreationTask.builder()
            .id("task_1")
            .studentRef("STD21001")
            .job(FeeCreationJob.builder().id("job_1").build())
            .statuses(new ArrayList<>())
            .build();
    this.task = task;
    when(feeCreationTaskService.updateStatus(any(), any(), any(), any())).thenReturn(task);
    return task;
  }

  private FeeCreationTask task;

  private void whenTaskTriggered() {
    subject.accept(FeeCreationTaskTriggered.builder().task(task).build());
  }

  @Test
  void marks_processing_then_succeeded_when_fees_are_created() {
    var task = givenTask();
    when(feeCreationTaskService.createFees(task)).thenReturn(List.of(new Fee(), new Fee()));

    whenTaskTriggered();

    var inOrder = inOrder(feeCreationTaskService);
    inOrder.verify(feeCreationTaskService).updateStatus(task, PROCESSING, UNKNOWN, null);
    inOrder.verify(feeCreationTaskService).createFees(task);
    inOrder.verify(feeCreationTaskService).updateStatus(task, FINISHED, SUCCEEDED, null);
  }

  @Test
  void records_failure_with_its_message_when_creation_throws() {
    var task = givenTask();
    when(feeCreationTaskService.createFees(task))
        .thenThrow(new NotFoundException("Student ref=STD21001 not found"));

    whenTaskTriggered();

    verify(feeCreationTaskService).updateStatus(task, PROCESSING, UNKNOWN, null);
    verify(feeCreationTaskService)
        .updateStatus(task, FINISHED, FAILED, "Student ref=STD21001 not found");
  }

  @Test
  void a_failing_task_does_not_propagate_the_exception() {
    var task = givenTask();
    when(feeCreationTaskService.createFees(task)).thenThrow(new RuntimeException("boom"));

    whenTaskTriggered();

    verify(feeCreationTaskService).updateStatus(task, FINISHED, FAILED, "boom");
  }

  @Test
  void task_is_marked_processing_before_any_fee_is_created() {
    var task = givenTask();
    when(feeCreationTaskService.createFees(task)).thenReturn(List.of());

    whenTaskTriggered();

    var inOrder = inOrder(feeCreationTaskService);
    inOrder
        .verify(feeCreationTaskService)
        .updateStatus(eq(task), eq(PROCESSING), eq(UNKNOWN), isNull());
    inOrder.verify(feeCreationTaskService).createFees(task);
  }
}
