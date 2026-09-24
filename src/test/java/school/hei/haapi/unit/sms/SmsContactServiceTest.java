package school.hei.haapi.unit.sms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import school.hei.haapi.model.SmsContact;
import school.hei.haapi.model.SmsContactOwnerRole;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.SmsContactRepository;
import school.hei.haapi.repository.dao.SmsContactDao;
import school.hei.haapi.service.sms.SmsContactService;

class SmsContactServiceTest {
  private final SmsContactRepository smsContactRepositoryMock = mock();
  private final SmsContactDao smsContactDaoMock = mock();

  private final SmsContactService subject =
      new SmsContactService(smsContactRepositoryMock, smsContactDaoMock);

  @Test
  void getByCriteria_delegates_to_the_dao() {
    var contact = SmsContact.builder().id("c1").build();
    when(smsContactDaoMock.filterByCriteria(eq("g1"), eq(SmsContactOwnerRole.STUDENT), any()))
        .thenReturn(List.of(contact));

    var result =
        subject.getByCriteria(
            "g1",
            SmsContactOwnerRole.STUDENT,
            org.springframework.data.domain.PageRequest.of(0, 10));

    assertEquals(List.of(contact), result);
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
    when(smsContactRepositoryMock.save(any())).thenAnswer(i -> i.getArgument(0));

    var deleted = subject.delete("c1");

    assertTrue(deleted.isDeleted());
  }

  private User.UserBuilder enabledUserBuilder() {
    return User.builder()
        .id("u1")
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
    var user = enabledUserBuilder().phone("0321111111").role(User.Role.TEACHER).build();
    when(smsContactRepositoryMock.existsByOwner_Id("u1")).thenReturn(false);
    when(smsContactRepositoryMock.save(any())).thenAnswer(i -> i.getArgument(0));

    var result = subject.createContactIfMissing(user);

    assertTrue(result.isPresent());
    var contact = result.get();
    assertEquals("321111111", contact.getPhoneNumber());
    assertEquals("Antenaina Jaonina", contact.getName());
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
}
