package school.hei.haapi.unit.sms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import school.hei.haapi.model.SmsContact;
import school.hei.haapi.model.SmsContactOwnerRole;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.MonitoringStudentRepository;
import school.hei.haapi.repository.SmsContactRepository;
import school.hei.haapi.repository.dao.SmsContactDao;
import school.hei.haapi.service.UserService;
import school.hei.haapi.service.sms.SmsContactService;

class SmsContactServiceTest {
  private final SmsContactRepository smsContactRepositoryMock = mock();
  private final SmsContactDao smsContactDaoMock = mock();
  private final UserService userServiceMock = mock();
  private final MonitoringStudentRepository monitoringStudentRepositoryMock = mock();

  private final SmsContactService subject =
      new SmsContactService(
          smsContactRepositoryMock,
          smsContactDaoMock,
          userServiceMock,
          monitoringStudentRepositoryMock);

  @Test
  void getByCriteria_delegates_to_the_dao() {
    var contact = SmsContact.builder().id("c1").build();
    when(smsContactDaoMock.filterByCriteria(
            eq("g1"), eq(SmsContactOwnerRole.STUDENT), eq("Ante"), any()))
        .thenReturn(List.of(contact));

    var result =
        subject.getByCriteria(
            "g1",
            SmsContactOwnerRole.STUDENT,
            "Ante",
            org.springframework.data.domain.PageRequest.of(0, 10));

    assertEquals(List.of(contact), result);
  }

  @Test
  void getByCriteria_with_no_search_does_not_expand_linked_contacts() {
    var student = User.builder().id("student1").ref("STD000001").build();
    var contact =
        SmsContact.builder().id("c1").owner(student).ownerRole(SmsContactOwnerRole.STUDENT).build();
    when(smsContactDaoMock.filterByCriteria(isNull(), isNull(), isNull(), any()))
        .thenReturn(List.of(contact));

    var result =
        subject.getByCriteria(
            null, null, null, org.springframework.data.domain.PageRequest.of(0, 10));

    assertEquals(List.of(contact), result);
  }

  @Test
  void getByCriteria_with_search_also_returns_the_matched_students_monitor_contact() {
    var student = User.builder().id("student1").ref("STD25001").build();
    var monitor = User.builder().id("monitor1").ref("MTR25001").build();
    var studentContact =
        SmsContact.builder()
            .id("c-student")
            .owner(student)
            .ownerRole(SmsContactOwnerRole.STUDENT)
            .build();
    var monitorContact =
        SmsContact.builder()
            .id("c-monitor")
            .owner(monitor)
            .ownerRole(SmsContactOwnerRole.MONITOR)
            .build();
    when(smsContactDaoMock.filterByCriteria(isNull(), isNull(), eq("STD25001"), any()))
        .thenReturn(List.of(studentContact));
    when(monitoringStudentRepositoryMock.findAllMonitorsByStudentId("student1"))
        .thenReturn(List.of(monitor));
    when(smsContactRepositoryMock.findByOwner_IdAndIsDeletedFalse("monitor1"))
        .thenReturn(Optional.of(monitorContact));

    var result =
        subject.getByCriteria(
            null, null, "STD25001", org.springframework.data.domain.PageRequest.of(0, 10));

    assertEquals(List.of(studentContact, monitorContact), result);
  }

  @Test
  void getByCriteria_with_search_also_returns_every_linked_students_contact_for_a_monitor() {
    var monitor = User.builder().id("monitor1").ref("MTR25001").build();
    var student1 = User.builder().id("student1").ref("STD25001").build();
    var student2 = User.builder().id("student2").ref("STD25002").build();
    var monitorContact =
        SmsContact.builder()
            .id("c-monitor")
            .owner(monitor)
            .ownerRole(SmsContactOwnerRole.MONITOR)
            .build();
    var student1Contact =
        SmsContact.builder()
            .id("c-student1")
            .owner(student1)
            .ownerRole(SmsContactOwnerRole.STUDENT)
            .build();
    var student2Contact =
        SmsContact.builder()
            .id("c-student2")
            .owner(student2)
            .ownerRole(SmsContactOwnerRole.STUDENT)
            .build();
    when(smsContactDaoMock.filterByCriteria(isNull(), isNull(), eq("MTR25001"), any()))
        .thenReturn(List.of(monitorContact));
    when(monitoringStudentRepositoryMock.findAllStudentsByMonitorId("monitor1", Pageable.unpaged()))
        .thenReturn(List.of(student1, student2));
    when(smsContactRepositoryMock.findByOwner_IdAndIsDeletedFalse("student1"))
        .thenReturn(Optional.of(student1Contact));
    when(smsContactRepositoryMock.findByOwner_IdAndIsDeletedFalse("student2"))
        .thenReturn(Optional.of(student2Contact));

    var result =
        subject.getByCriteria(
            null, null, "MTR25001", org.springframework.data.domain.PageRequest.of(0, 10));

    assertEquals(List.of(monitorContact, student1Contact, student2Contact), result);
  }

  @Test
  void getByCriteria_does_not_duplicate_a_linked_contact_already_in_the_matches() {
    var student = User.builder().id("student1").ref("STD25001").build();
    var monitor = User.builder().id("monitor1").ref("MTR25001").build();
    var studentContact =
        SmsContact.builder()
            .id("c-student")
            .owner(student)
            .ownerRole(SmsContactOwnerRole.STUDENT)
            .build();
    var monitorContact =
        SmsContact.builder()
            .id("c-monitor")
            .owner(monitor)
            .ownerRole(SmsContactOwnerRole.MONITOR)
            .build();
    when(smsContactDaoMock.filterByCriteria(isNull(), isNull(), eq("25001"), any()))
        .thenReturn(List.of(studentContact, monitorContact));
    when(monitoringStudentRepositoryMock.findAllMonitorsByStudentId("student1"))
        .thenReturn(List.of(monitor));
    when(smsContactRepositoryMock.findByOwner_IdAndIsDeletedFalse("monitor1"))
        .thenReturn(Optional.of(monitorContact));
    when(monitoringStudentRepositoryMock.findAllStudentsByMonitorId("monitor1", Pageable.unpaged()))
        .thenReturn(List.of(student));
    when(smsContactRepositoryMock.findByOwner_IdAndIsDeletedFalse("student1"))
        .thenReturn(Optional.of(studentContact));

    var result =
        subject.getByCriteria(
            null, null, "25001", org.springframework.data.domain.PageRequest.of(0, 10));

    assertEquals(List.of(studentContact, monitorContact), result);
  }

  @Test
  void getById_throws_not_found_for_a_missing_contact() {
    when(smsContactRepositoryMock.findById("missing")).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> subject.getById("missing"));
  }

  @Test
  void delete_soft_deletes_the_contact() {
    var contact = SmsContact.builder().id("c1").build();
    when(smsContactRepositoryMock.findById("c1")).thenReturn(Optional.of(contact));
    when(smsContactRepositoryMock.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    var deleted = subject.delete("c1");

    assertTrue(deleted.isDeleted());
  }

  private User.UserBuilder enabledUserBuilder() {
    return User.builder()
        .id("u1")
        .ref("STD000001")
        .firstName("Antenaina")
        .lastName("Jaonina")
        .phone("0321111111")
        .status(User.Status.ENABLED)
        .role(User.Role.STUDENT);
  }

  @Test
  void createContactIfMissing_is_a_noop_for_a_disabled_user() {
    var user = enabledUserBuilder().status(User.Status.DISABLED).build();

    var result = subject.createContactIfMissing(user);

    assertTrue(result.isEmpty());
    verify(smsContactRepositoryMock, never()).save(any());
  }

  @Test
  void createContactIfMissing_is_a_noop_when_the_user_has_no_phone() {
    var user = enabledUserBuilder().phone(null).build();

    var result = subject.createContactIfMissing(user);

    assertTrue(result.isEmpty());
    verify(smsContactRepositoryMock, never()).save(any());
  }

  @Test
  void createContactIfMissing_is_a_noop_when_the_phone_is_too_short_once_normalized() {
    var user = enabledUserBuilder().phone("12").build();

    var result = subject.createContactIfMissing(user);

    assertTrue(result.isEmpty());
    verify(smsContactRepositoryMock, never()).save(any());
  }

  @Test
  void createContactIfMissing_is_a_noop_when_a_contact_already_exists() {
    var user = enabledUserBuilder().build();
    when(smsContactRepositoryMock.existsByOwner_Id("u1")).thenReturn(true);

    var result = subject.createContactIfMissing(user);

    assertTrue(result.isEmpty());
    verify(smsContactRepositoryMock, never()).save(any());
  }

  @Test
  void createContactIfMissing_creates_a_contact_with_the_normalized_phone_and_mapped_role() {
    var user =
        enabledUserBuilder().ref("TCH000001").phone("0321111111").role(User.Role.TEACHER).build();
    when(smsContactRepositoryMock.existsByOwner_Id("u1")).thenReturn(false);
    when(smsContactRepositoryMock.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    var result = subject.createContactIfMissing(user);

    assertTrue(result.isPresent());
    var contact = result.get();
    assertEquals("321111111", contact.getPhoneNumber());
    assertEquals("TCH000001", contact.getName());
    assertEquals(user, contact.getOwner());
    assertEquals(SmsContactOwnerRole.TEACHER, contact.getOwnerRole());
    assertFalse(contact.isDeleted());
  }

  @Test
  void createContactIfMissing_swallows_a_race_with_the_unique_owner_constraint() {
    var user = enabledUserBuilder().build();
    when(smsContactRepositoryMock.existsByOwner_Id("u1")).thenReturn(false);
    when(smsContactRepositoryMock.save(any()))
        .thenThrow(new DataIntegrityViolationException("duplicate owner_id"));

    var result = subject.createContactIfMissing(user);

    assertTrue(result.isEmpty());
  }

  @Test
  void backfillMissingContacts_only_asks_the_repository_for_users_already_without_one() {
    var withoutContact1 = enabledUserBuilder().id("u1").build();
    var withoutContact2 =
        enabledUserBuilder().id("u2").ref("STD000002").phone("0321111112").build();
    when(userServiceMock.getAllEnabledUsersWithoutContact())
        .thenReturn(List.of(withoutContact1, withoutContact2));
    when(smsContactRepositoryMock.existsByOwner_Id("u1")).thenReturn(false);
    when(smsContactRepositoryMock.existsByOwner_Id("u2")).thenReturn(false);
    when(smsContactRepositoryMock.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    var createdCount = subject.backfillMissingContacts();

    assertEquals(2, createdCount);
  }

  @Test
  void backfillMissingContacts_still_skips_a_user_that_raced_a_contact_in_before_the_save() {
    var user = enabledUserBuilder().id("u1").build();
    when(userServiceMock.getAllEnabledUsersWithoutContact()).thenReturn(List.of(user));
    when(smsContactRepositoryMock.existsByOwner_Id("u1")).thenReturn(true);

    var createdCount = subject.backfillMissingContacts();

    assertEquals(0, createdCount);
    verify(smsContactRepositoryMock, never()).save(any());
  }

  @Test
  void backfillMissingContacts_returns_zero_when_no_user_is_enabled() {
    when(userServiceMock.getAllEnabledUsersWithoutContact()).thenReturn(List.of());

    assertEquals(0, subject.backfillMissingContacts());
  }
}
