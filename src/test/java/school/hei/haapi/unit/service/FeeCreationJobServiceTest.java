package school.hei.haapi.unit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static school.hei.haapi.endpoint.event.EventStack.EVENT_STACK_1;
import static school.hei.haapi.endpoint.event.EventStack.EVENT_STACK_2;
import static school.hei.haapi.endpoint.rest.model.FeeCategory.WORK_FEES;
import static school.hei.haapi.endpoint.rest.model.FeeTypeEnum.TUITION;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import school.hei.haapi.endpoint.event.EventProducer;
import school.hei.haapi.endpoint.event.model.FeeCreationJobStatisticsComputationTriggered;
import school.hei.haapi.endpoint.event.model.FeeCreationJobStatusComputationTriggered;
import school.hei.haapi.endpoint.event.model.FeeCreationTaskTriggered;
import school.hei.haapi.model.FeeCreationJob;
import school.hei.haapi.model.FeeCreationTask;
import school.hei.haapi.model.V2FeeTemplate;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.FeeCreationJobRepository;
import school.hei.haapi.repository.V2FeeTemplateRepository;
import school.hei.haapi.service.FeeCreationJobService;

class FeeCreationJobServiceTest {
  private FeeCreationJobRepository feeCreationJobRepository;
  private V2FeeTemplateRepository v2FeeTemplateRepository;
  private EventProducer<FeeCreationTaskTriggered> eventProducer;
  private FeeCreationJobService subject;

  @BeforeEach
  void setUp() {
    feeCreationJobRepository = mock();
    v2FeeTemplateRepository = mock();
    eventProducer = mock();
    subject =
        new FeeCreationJobService(feeCreationJobRepository, v2FeeTemplateRepository, eventProducer);
  }

  private static V2FeeTemplate aFeeTemplate() {
    return V2FeeTemplate.builder()
        .id("template_id")
        .label("Tuition")
        .type(TUITION)
        .category(WORK_FEES)
        .build();
  }

  private static FeeCreationJob aJob(String id, String... studentRefs) {
    var job =
        FeeCreationJob.builder()
            .id(id)
            .feeTemplate(aFeeTemplate())
            .tasks(new ArrayList<>())
            .build();
    for (var studentRef : studentRefs) {
      job.addTask(
          FeeCreationTask.builder().id("task_" + studentRef).studentRef(studentRef).build());
    }
    return job;
  }

  /** Every event the producer was handed, flattened, filtered to the given type. */
  @SuppressWarnings("unchecked")
  private <T> List<T> dispatchedOfType(Class<T> type) {
    var captor = ArgumentCaptor.forClass(List.class);
    verify(eventProducer, atLeastOnce()).accept(captor.capture());
    return (List<T>)
        captor.getAllValues().stream()
            .flatMap(events -> ((List<Object>) events).stream())
            .filter(type::isInstance)
            .toList();
  }

  @Test
  void crupdate_persists_job_and_triggers_one_event_per_task() {
    var job = aJob("job_1", "STD21001", "STD21002");
    when(feeCreationJobRepository.findById("job_1")).thenReturn(Optional.empty());
    when(feeCreationJobRepository.save(job)).thenReturn(job);

    var actual = subject.crupdate(job);

    assertEquals(job, actual);
    verify(feeCreationJobRepository).save(job);
    assertEquals(2, dispatchedOfType(FeeCreationTaskTriggered.class).size());
  }

  @Test
  void crupdate_triggers_the_job_status_and_statistics_computations_once() {
    var job = aJob("job_1", "STD21001", "STD21002");
    when(feeCreationJobRepository.findById("job_1")).thenReturn(Optional.empty());
    when(feeCreationJobRepository.save(job)).thenReturn(job);

    subject.crupdate(job);

    // computed aside from the tasks, each on its own event, both on the second stack
    var statuses = dispatchedOfType(FeeCreationJobStatusComputationTriggered.class);
    var statistics = dispatchedOfType(FeeCreationJobStatisticsComputationTriggered.class);
    assertEquals(1, statuses.size());
    assertEquals(1, statistics.size());
    assertEquals("job_1", statuses.getFirst().getJobId());
    assertEquals("job_1", statistics.getFirst().getJobId());
    assertEquals(EVENT_STACK_2, statuses.getFirst().getEventStack());
    assertEquals(EVENT_STACK_2, statistics.getFirst().getEventStack());
  }

  @Test
  void crupdate_triggers_one_singleton_event_carrying_the_task_id() {
    var job = aJob("job_1", "STD21001", "STD21002");
    when(feeCreationJobRepository.findById("job_1")).thenReturn(Optional.empty());
    when(feeCreationJobRepository.save(job)).thenReturn(job);

    subject.crupdate(job);

    assertEquals(
        List.of("task_STD21001", "task_STD21002"),
        dispatchedOfType(FeeCreationTaskTriggered.class).stream()
            .map(event -> event.getTask().getId())
            .toList());
  }

  @Test
  void crupdate_dispatches_tasks_on_first_event_stack() {
    var job = aJob("job_1", "STD21001");
    when(feeCreationJobRepository.findById("job_1")).thenReturn(Optional.empty());
    when(feeCreationJobRepository.save(job)).thenReturn(job);

    subject.crupdate(job);

    var event = dispatchedOfType(FeeCreationTaskTriggered.class).getFirst();
    assertEquals(EVENT_STACK_1, event.getEventStack());
  }

  @Test
  void crupdate_of_a_known_job_returns_current_state_without_saving_nor_dispatching() {
    var known = aJob("job_1", "STD21001", "STD21002");
    var submittedAgain = aJob("job_1", "STD21001", "STD21002");
    when(feeCreationJobRepository.findById("job_1")).thenReturn(Optional.of(known));

    var actual = subject.crupdate(submittedAgain);

    assertEquals(known, actual);
    verify(feeCreationJobRepository, never()).save(any());
    verifyNoInteractions(eventProducer);
  }

  @Test
  void crupdateAll_processes_every_submitted_job() {
    var first = aJob("job_1", "STD21001");
    var second = aJob("job_2", "STD21002");
    when(feeCreationJobRepository.findById(any())).thenReturn(Optional.empty());
    when(feeCreationJobRepository.save(any())).thenAnswer(i -> i.getArgument(0));

    var actual = subject.crupdateAll(List.of(first, second));

    assertEquals(List.of(first, second), actual);
    assertEquals(2, dispatchedOfType(FeeCreationTaskTriggered.class).size());
  }

  @Test
  void getFeeTemplateById_throws_when_template_not_found() {
    when(v2FeeTemplateRepository.findById("unknown")).thenReturn(Optional.empty());

    var exception =
        assertThrows(NotFoundException.class, () -> subject.getFeeTemplateById("unknown"));
    assertEquals("FeeTemplate.id=unknown not found", exception.getMessage());
  }

  @Test
  void getById_throws_when_job_not_found() {
    when(feeCreationJobRepository.findById("unknown")).thenReturn(Optional.empty());

    var exception = assertThrows(NotFoundException.class, () -> subject.getById("unknown"));
    assertEquals("FeeCreationJob.id=unknown not found", exception.getMessage());
  }
}
