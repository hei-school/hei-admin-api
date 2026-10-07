package school.hei.haapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static school.hei.haapi.endpoint.rest.model.FeeTypeEnum.TUITION;
import static school.hei.haapi.endpoint.rest.model.SuspensionReason.LATE_FEES;
import static school.hei.haapi.endpoint.rest.model.SuspensionReason.OTHER;
import static school.hei.haapi.model.User.Status.ENABLED;
import static school.hei.haapi.model.User.Status.SUSPENDED;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import school.hei.haapi.endpoint.rest.model.SituationLateFee;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.FeeRepository;

class StudentSituationServiceTest {
  private static final Instant DUE = Instant.parse("2026-10-15T00:00:00Z");
  private FeeRepository feeRepository;
  private StudentSituationService subject;

  @BeforeEach
  void setUp() {
    feeRepository = mock(FeeRepository.class);
    subject = new StudentSituationService(feeRepository);
  }

  @Test
  void late_fees_are_given_without_amounts() {
    when(feeRepository.findLateFeesOfStudent("student1_id"))
        .thenReturn(List.of(fee("Frais de scolarité octobre"), fee(null), fee(" ")));

    var lateFees = subject.lateFeesOf(student(ENABLED));

    assertEquals(
        List.of("Frais de scolarité octobre", "TUITION", "TUITION"),
        lateFees.stream().map(SituationLateFee::getLabel).toList());
    assertEquals(DUE, lateFees.get(0).getDueDatetime());
  }

  @Test
  void suspension_reason_only_for_a_suspended_student() {
    var lateFee = new SituationLateFee().label("Frais").dueDatetime(DUE);

    assertNull(subject.suspensionReasonOf(student(ENABLED), List.of(lateFee)));
    assertEquals(LATE_FEES, subject.suspensionReasonOf(student(SUSPENDED), List.of(lateFee)));
    assertEquals(OTHER, subject.suspensionReasonOf(student(SUSPENDED), List.of()));
  }

  private static Fee fee(String comment) {
    return Fee.builder()
        .comment(comment)
        .type(TUITION)
        .remainingAmount(10_000)
        .dueDatetime(DUE)
        .build();
  }

  private static User student(User.Status status) {
    var student = new User();
    student.setId("student1_id");
    student.setStatus(status);
    return student;
  }
}
