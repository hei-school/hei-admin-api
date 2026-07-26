package school.hei.haapi.unit.service;

import static java.math.BigInteger.valueOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static school.hei.haapi.endpoint.rest.model.FeeCategory.WORK_FEES;
import static school.hei.haapi.endpoint.rest.model.FeeStatusEnum.LATE;
import static school.hei.haapi.endpoint.rest.model.FeeStatusEnum.UNPAID;
import static school.hei.haapi.endpoint.rest.model.FeeTypeEnum.TUITION;
import static school.hei.haapi.endpoint.rest.model.JobHealth.UNKNOWN;
import static school.hei.haapi.endpoint.rest.model.JobProgression.PROCESSING;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.FeeCreationJob;
import school.hei.haapi.model.FeeCreationTask;
import school.hei.haapi.model.User;
import school.hei.haapi.model.V2FeeTemplate;
import school.hei.haapi.model.V2FeeTemplateContent;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.FeeCreationTaskRepository;
import school.hei.haapi.repository.FeeRepository;
import school.hei.haapi.service.FeeCreationTaskService;
import school.hei.haapi.service.UserService;

class FeeCreationTaskServiceTest {
  private FeeCreationTaskRepository feeCreationTaskRepository;
  private FeeRepository feeRepository;
  private UserService userService;
  private FeeCreationTaskService subject;

  @BeforeEach
  void setUp() {
    feeCreationTaskRepository = mock();
    feeRepository = mock();
    userService = mock();
    subject = new FeeCreationTaskService(feeCreationTaskRepository, feeRepository, userService);
  }

  private static V2FeeTemplateContent aContent(String label, long amount, LocalDate dueDate) {
    return V2FeeTemplateContent.builder()
        .id("content_" + label)
        .label(label)
        .amount(valueOf(amount))
        .dueDate(dueDate)
        .build();
  }

  private static FeeCreationTask aTask(List<V2FeeTemplateContent> contents) {
    var feeTemplate =
        V2FeeTemplate.builder()
            .id("template_id")
            .label("Tuition")
            .type(TUITION)
            .category(WORK_FEES)
            .feeTemplateContents(contents)
            .build();
    var job = FeeCreationJob.builder().id("job_1").feeTemplate(feeTemplate).build();
    return FeeCreationTask.builder()
        .id("task_1")
        .studentRef("STD21001")
        .job(job)
        .statuses(new ArrayList<>())
        .build();
  }

  private User givenStudent() {
    var student = User.builder().id("student_id").ref("STD21001").build();
    when(userService.findByRef("STD21001")).thenReturn(student);
    return student;
  }

  @Test
  void creates_one_fee_per_template_content() {
    var student = givenStudent();
    var task =
        aTask(
            List.of(
                aContent("January", 5000, LocalDate.of(2030, 1, 31)),
                aContent("February", 6000, LocalDate.of(2030, 2, 28))));
    when(feeRepository.existsByStudentIdAndFeeTemplateId(any(), any())).thenReturn(false);
    when(feeRepository.saveAll(any())).thenAnswer(i -> i.getArgument(0));

    subject.createFees(task);

    var captor = ArgumentCaptor.forClass(List.class);
    verify(feeRepository).saveAll(captor.capture());
    List<Fee> saved = captor.getValue();
    assertEquals(2, saved.size());
    assertEquals(student, saved.get(0).getStudent());
    assertEquals(TUITION, saved.get(0).getType());
    assertEquals(WORK_FEES, saved.get(0).getCategory());
    assertEquals(5000, saved.get(0).getTotalAmount());
    assertEquals(5000, saved.get(0).getRemainingAmount());
    assertEquals("January", saved.get(0).getComment());
    assertEquals(6000, saved.get(1).getTotalAmount());
  }

  @Test
  void created_fees_carry_the_template_they_were_charged_from() {
    givenStudent();
    var task = aTask(List.of(aContent("January", 5000, LocalDate.of(2030, 1, 31))));
    when(feeRepository.existsByStudentIdAndFeeTemplateId(any(), any())).thenReturn(false);
    when(feeRepository.saveAll(any())).thenAnswer(i -> i.getArgument(0));

    subject.createFees(task);

    var captor = ArgumentCaptor.forClass(List.class);
    verify(feeRepository).saveAll(captor.capture());
    assertEquals("template_id", ((List<Fee>) captor.getValue()).get(0).getFeeTemplate().getId());
  }

  @Test
  void findStudentsByRefs_indexes_known_students_and_skips_unknown_ones() {
    var known = User.builder().id("student_id").ref("STD21001").build();
    var tasks =
        List.of(
            FeeCreationTask.builder().studentRef("STD21001").build(),
            FeeCreationTask.builder().studentRef("STD21099").build());
    when(userService.findAllByRefIn(List.of("STD21001", "STD21099"))).thenReturn(List.of(known));

    var actual = subject.findStudentsByRefs(tasks);

    assertEquals(1, actual.size());
    assertEquals(known, actual.get("STD21001"));
    assertNull(actual.get("STD21099"));
  }

  @Test
  void a_future_due_date_yields_an_unpaid_fee() {
    givenStudent();
    var task = aTask(List.of(aContent("January", 5000, LocalDate.of(2030, 1, 31))));
    when(feeRepository.existsByStudentIdAndFeeTemplateId(any(), any())).thenReturn(false);
    when(feeRepository.saveAll(any())).thenAnswer(i -> i.getArgument(0));

    subject.createFees(task);

    var captor = ArgumentCaptor.forClass(List.class);
    verify(feeRepository).saveAll(captor.capture());
    assertEquals(UNPAID, ((List<Fee>) captor.getValue()).get(0).getStatus());
  }

  @Test
  void a_past_due_date_yields_a_late_fee() {
    givenStudent();
    var task = aTask(List.of(aContent("January", 5000, LocalDate.of(2020, 1, 31))));
    when(feeRepository.existsByStudentIdAndFeeTemplateId(any(), any())).thenReturn(false);
    when(feeRepository.saveAll(any())).thenAnswer(i -> i.getArgument(0));

    subject.createFees(task);

    var captor = ArgumentCaptor.forClass(List.class);
    verify(feeRepository).saveAll(captor.capture());
    assertEquals(LATE, ((List<Fee>) captor.getValue()).get(0).getStatus());
  }

  @Test
  void refuses_to_charge_twice_the_same_template_to_the_same_student() {
    givenStudent();
    var task = aTask(List.of(aContent("January", 5000, LocalDate.of(2030, 1, 31))));
    when(feeRepository.existsByStudentIdAndFeeTemplateId("student_id", "template_id"))
        .thenReturn(true);

    var exception = assertThrows(BadRequestException.class, () -> subject.createFees(task));

    assertTrue(exception.getMessage().contains("already created"));
    assertTrue(exception.getMessage().contains("STD21001"));
    verify(feeRepository, never()).saveAll(any());
  }

  @Test
  void an_unknown_student_ref_propagates_a_not_found() {
    var task = aTask(List.of(aContent("January", 5000, LocalDate.of(2030, 1, 31))));
    when(userService.findByRef("STD21001"))
        .thenThrow(new NotFoundException("User ref=STD21001 not found"));

    assertThrows(NotFoundException.class, () -> subject.createFees(task));
    verify(feeRepository, never()).saveAll(any());
  }

  @Test
  void a_template_without_content_cannot_create_any_fee() {
    givenStudent();
    var task = aTask(List.of());
    when(feeRepository.existsByStudentIdAndFeeTemplateId(any(), any())).thenReturn(false);

    var exception = assertThrows(BadRequestException.class, () -> subject.createFees(task));

    assertTrue(exception.getMessage().contains("no content"));
    verify(feeRepository, never()).saveAll(any());
  }

  @Test
  void updateStatus_appends_the_status_and_keeps_the_message() {
    var task = aTask(List.of());
    when(feeCreationTaskRepository.save(task)).thenReturn(task);

    subject.updateStatus(task, PROCESSING, UNKNOWN, "some message");

    assertEquals(1, task.getStatuses().size());
    assertEquals(PROCESSING, task.getStatuses().get(0).getProgression());
    assertEquals(UNKNOWN, task.getStatuses().get(0).getHealth());
    assertEquals(task, task.getStatuses().get(0).getTask());
    assertEquals("some message", task.getMessage());
    verify(feeCreationTaskRepository).save(task);
  }
}
