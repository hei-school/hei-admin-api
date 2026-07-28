package school.hei.haapi.unit.mapper;

import static java.time.Instant.now;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static school.hei.haapi.endpoint.rest.model.JobHealth.FAILED;
import static school.hei.haapi.endpoint.rest.model.JobHealth.UNKNOWN;
import static school.hei.haapi.endpoint.rest.model.JobProgression.FINISHED;
import static school.hei.haapi.endpoint.rest.model.JobProgression.PENDING;

import java.util.ArrayList;
import java.util.Map;
import org.junit.jupiter.api.Test;
import school.hei.haapi.endpoint.rest.mapper.FeeCreationJobMapper;
import school.hei.haapi.endpoint.rest.mapper.FeeTemplateMapper;
import school.hei.haapi.endpoint.rest.mapper.UserMapper;
import school.hei.haapi.endpoint.rest.model.UserIdentifier;
import school.hei.haapi.model.FeeCreationTask;
import school.hei.haapi.model.TaskStatus;
import school.hei.haapi.model.User;

class FeeCreationJobMapperTest {
  private final FeeTemplateMapper feeTemplateMapper = mock();
  private final UserMapper userMapper = mock();
  private final FeeCreationJobMapper subject =
      new FeeCreationJobMapper(feeTemplateMapper, userMapper);

  private static FeeCreationTask aTask(String studentRef) {
    return FeeCreationTask.builder().studentRef(studentRef).statuses(new ArrayList<>()).build();
  }

  @Test
  void a_known_student_is_fully_described() {
    var student = User.builder().id("student_id").ref("STD21001").build();
    var task = aTask("STD21001");
    when(userMapper.toIdentifier(student))
        .thenReturn(new UserIdentifier().id("student_id").ref("STD21001").firstName("One"));

    var actual = subject.toRestStudentCreation(task, Map.of("STD21001", student));

    assertEquals("student_id", actual.getStudent().getId());
    assertEquals("One", actual.getStudent().getFirstName());
  }

  @Test
  void an_unknown_student_ref_is_carried_by_a_reference_only_identifier() {
    var task = aTask("STD21099");
    task.setMessage("User with ref: STD21099 not found");
    task.addStatus(
        TaskStatus.builder().progression(FINISHED).health(FAILED).creationDatetime(now()).build());

    var actual = subject.toRestStudentCreation(task, Map.of());

    assertEquals("STD21099", actual.getStudent().getRef());
    assertNull(actual.getStudent().getId());
    assertNull(actual.getStudent().getFirstName());
    assertEquals(FINISHED, actual.getFeesCreationStatus().getProgression());
    assertEquals(FAILED, actual.getFeesCreationStatus().getHealth());
    assertEquals("User with ref: STD21099 not found", actual.getMessage());
  }

  @Test
  void a_task_not_picked_up_yet_is_reported_as_pending() {
    var actual = subject.toRestStudentCreation(aTask("STD21001"), Map.of());

    assertEquals(PENDING, actual.getFeesCreationStatus().getProgression());
    assertEquals(UNKNOWN, actual.getFeesCreationStatus().getHealth());
    assertNull(actual.getMessage());
  }
}
