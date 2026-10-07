package school.hei.haapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
import school.hei.haapi.endpoint.rest.mapper.StatusEnumMapper;
import school.hei.haapi.endpoint.rest.model.EnableStatus;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.StudentBadge;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.repository.FeeRepository;

class StudentSituationServiceTest {
  private static final String PUBLIC_ID = "7c1e4b9a-2f3d-4e8a-9b6c-1d2e3f4a5b6c";
  private static final Instant DUE = Instant.parse("2026-10-15T00:00:00Z");
  private StudentBadgeCodeService studentBadgeCodeService;
  private FeeRepository feeRepository;
  private StudentSituationService subject;

  @BeforeEach
  void setUp() {
    studentBadgeCodeService = mock(StudentBadgeCodeService.class);
    feeRepository = mock(FeeRepository.class);
    subject =
        new StudentSituationService(studentBadgeCodeService, feeRepository, new StatusEnumMapper());
  }

  @Test
  void enabled_student_has_no_suspension_reason() {
    givenBadgeOf(student(ENABLED));

    var situation = subject.getSituationOfBadge(PUBLIC_ID);

    assertEquals(EnableStatus.ENABLED, situation.getStatus());
    assertNull(situation.getSuspensionReason());
    verify(feeRepository, never()).findLateFeesOfStudent("student1_id");
  }

  @Test
  void suspended_student_gets_its_late_fees_without_amounts() {
    givenBadgeOf(student(SUSPENDED));
    when(feeRepository.findLateFeesOfStudent("student1_id"))
        .thenReturn(
            List.of(
                fee("Frais de scolarité octobre", 50_000), fee(null, 20_000), fee(" ", 10_000)));

    var situation = subject.getSituationOfBadge(PUBLIC_ID);

    assertEquals(EnableStatus.SUSPENDED, situation.getStatus());
    assertEquals(LATE_FEES, situation.getSuspensionReason());
    assertEquals(
        List.of("Frais de scolarité octobre", "TUITION", "TUITION"),
        situation.getLateFees().stream().map(lateFee -> lateFee.getLabel()).toList());
    assertEquals(DUE, situation.getLateFees().get(0).getDueDatetime());
  }

  @Test
  void suspended_student_without_late_fee_is_suspended_for_another_reason() {
    givenBadgeOf(student(SUSPENDED));
    when(feeRepository.findLateFeesOfStudent("student1_id")).thenReturn(List.of());

    assertEquals(OTHER, subject.getSituationOfBadge(PUBLIC_ID).getSuspensionReason());
  }

  @Test
  void no_situation_for_an_invalid_badge() {
    when(studentBadgeCodeService.getValidBadge(PUBLIC_ID))
        .thenThrow(new BadRequestException("The badge has been revoked"));

    assertThrows(BadRequestException.class, () -> subject.getSituationOfBadge(PUBLIC_ID));
  }

  private void givenBadgeOf(User student) {
    when(studentBadgeCodeService.getValidBadge(PUBLIC_ID))
        .thenReturn(StudentBadge.builder().student(student).publicId(PUBLIC_ID).build());
  }

  private static Fee fee(String comment, int remainingAmount) {
    return Fee.builder()
        .comment(comment)
        .type(TUITION)
        .remainingAmount(remainingAmount)
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
