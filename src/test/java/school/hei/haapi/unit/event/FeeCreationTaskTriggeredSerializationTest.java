package school.hei.haapi.unit.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static school.hei.haapi.endpoint.rest.model.FeeCategory.WORK_FEES;
import static school.hei.haapi.endpoint.rest.model.FeeTypeEnum.TUITION;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import school.hei.haapi.endpoint.event.model.FeeCreationTaskTriggered;
import school.hei.haapi.model.FeeCreationJob;
import school.hei.haapi.model.FeeCreationTask;
import school.hei.haapi.model.TaskStatus;
import school.hei.haapi.model.V2FeeTemplate;
import school.hei.haapi.model.V2FeeTemplateContent;

/**
 * The task now rides inside the event, so it has to survive a round trip through the queue with
 * everything the consumer needs, and without cycling through its siblings.
 */
class FeeCreationTaskTriggeredSerializationTest {
  // mirrors the application mapper, see EndpointConf
  private final ObjectMapper om =
      new ObjectMapper()
          .registerModule(new JavaTimeModule())
          .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  private static FeeCreationTask aTask() {
    var content =
        V2FeeTemplateContent.builder()
            .id("content_id")
            .label("January")
            .amount(BigInteger.valueOf(5000))
            .dueDate(LocalDate.of(2026, 1, 31))
            .build();
    var feeTemplate =
        V2FeeTemplate.builder()
            .id("template_id")
            .label("Tuition")
            .type(TUITION)
            .category(WORK_FEES)
            .feeTemplateContents(new ArrayList<>(List.of(content)))
            .build();
    var job =
        FeeCreationJob.builder()
            .id("job_1")
            .feeTemplate(feeTemplate)
            .tasks(new ArrayList<>())
            .build();
    var task = FeeCreationTask.builder().id("task_1").studentRef("STD21001").build();
    job.addTask(task);
    return task;
  }

  private FeeCreationTaskTriggered roundTrip(FeeCreationTaskTriggered event) throws Exception {
    return om.readValue(om.writeValueAsString(event), FeeCreationTaskTriggered.class);
  }

  @Test
  void carries_everything_the_consumer_needs() throws Exception {
    var event = FeeCreationTaskTriggered.builder().task(aTask()).build();

    var actual = roundTrip(event).getTask();

    assertEquals("task_1", actual.getId());
    assertEquals("STD21001", actual.getStudentRef());
    assertNotNull(actual.getJob());
    assertEquals("job_1", actual.getJob().getId());
    var feeTemplate = actual.getJob().getFeeTemplate();
    assertEquals("template_id", feeTemplate.getId());
    assertEquals(TUITION, feeTemplate.getType());
    assertEquals(WORK_FEES, feeTemplate.getCategory());
    assertEquals(1, feeTemplate.getFeeTemplateContents().size());
    assertEquals(
        BigInteger.valueOf(5000), feeTemplate.getFeeTemplateContents().getFirst().getAmount());
    assertEquals(
        LocalDate.of(2026, 1, 31), feeTemplate.getFeeTemplateContents().getFirst().getDueDate());
  }

  @Test
  void does_not_drag_the_sibling_tasks_of_the_job_along() throws Exception {
    var task = aTask();
    task.getJob().addTask(FeeCreationTask.builder().id("task_2").studentRef("STD21002").build());

    var json = om.writeValueAsString(FeeCreationTaskTriggered.builder().task(task).build());

    assertEquals(-1, json.indexOf("STD21002"), "sibling task leaked into the payload: " + json);
    assertNotNull(roundTrip(FeeCreationTaskTriggered.builder().task(task).build()).getTask());
  }

  @Test
  void statuses_do_not_travel_so_that_none_can_be_stale() throws Exception {
    var task = aTask();
    task.addStatus(
        TaskStatus.builder()
            .progression(school.hei.haapi.endpoint.rest.model.JobProgression.FINISHED)
            .health(school.hei.haapi.endpoint.rest.model.JobHealth.SUCCEEDED)
            .creationDatetime(java.time.Instant.now())
            .build());

    var actual = roundTrip(FeeCreationTaskTriggered.builder().task(task).build()).getTask();

    assertNull(actual.getStatuses());
  }

  @Test
  void event_is_dispatched_on_the_first_stack() {
    assertEquals(
        school.hei.haapi.endpoint.event.EventStack.EVENT_STACK_1,
        FeeCreationTaskTriggered.builder().task(aTask()).build().getEventStack());
  }
}
