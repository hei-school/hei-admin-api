package school.hei.haapi.unit.sms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import school.hei.haapi.model.SmsContact;
import school.hei.haapi.model.SmsContactGroup;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.SmsContactGroupRepository;
import school.hei.haapi.repository.SmsContactRepository;
import school.hei.haapi.service.sms.SmsContactGroupService;

class SmsContactGroupServiceTest {
  private final SmsContactGroupRepository smsContactGroupRepositoryMock = mock();
  private final SmsContactRepository smsContactRepositoryMock = mock();

  private final SmsContactGroupService subject =
      new SmsContactGroupService(smsContactGroupRepositoryMock, smsContactRepositoryMock);

  private final User owner = User.builder().id("admin1").build();

  @Test
  void lists_groups_owned_by_the_given_user() {
    var group = SmsContactGroup.builder().id("g1").owner(owner).build();
    when(smsContactGroupRepositoryMock
            .findAllByOwner_IdAndIsDeletedFalseOrderByCreationDatetimeDesc(
                "admin1", PageRequest.of(0, 10)))
        .thenReturn(List.of(group));

    var result = subject.getByOwner(owner, PageRequest.of(0, 10));

    assertEquals(List.of(group), result);
  }

  @Test
  void getById_throws_not_found_for_a_missing_group() {
    when(smsContactGroupRepositoryMock.findById("missing")).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> subject.getById("missing"));
  }

  @Test
  void create_resolves_member_contacts_and_saves_the_group() {
    var member = SmsContact.builder().id("c1").build();
    when(smsContactRepositoryMock.findAllByIdInAndIsDeletedFalse(List.of("c1")))
        .thenReturn(List.of(member));
    when(smsContactGroupRepositoryMock.save(any())).thenAnswer(i -> i.getArgument(0));

    var created = subject.create(owner, "Promo 2026", List.of("c1"));

    assertEquals("Promo 2026", created.getName());
    assertEquals(owner, created.getOwner());
    assertEquals(List.of(member), created.getMembers());
  }

  @Test
  void update_renames_and_replaces_the_members() {
    var group = SmsContactGroup.builder().id("g1").name("Old name").owner(owner).build();
    when(smsContactGroupRepositoryMock.findById("g1")).thenReturn(Optional.of(group));
    var newMember = SmsContact.builder().id("c2").build();
    when(smsContactRepositoryMock.findAllByIdInAndIsDeletedFalse(List.of("c2")))
        .thenReturn(List.of(newMember));
    when(smsContactGroupRepositoryMock.save(any())).thenAnswer(i -> i.getArgument(0));

    var updated = subject.update("g1", "New name", List.of("c2"));

    assertEquals("New name", updated.getName());
    assertEquals(List.of(newMember), updated.getMembers());
  }

  @Test
  void delete_soft_deletes_the_group_without_touching_its_members() {
    var member = SmsContact.builder().id("c1").build();
    var group = SmsContactGroup.builder().id("g1").owner(owner).members(List.of(member)).build();
    when(smsContactGroupRepositoryMock.findById("g1")).thenReturn(Optional.of(group));
    when(smsContactGroupRepositoryMock.save(any())).thenAnswer(i -> i.getArgument(0));

    var deleted = subject.delete("g1");

    assertTrue(deleted.isDeleted());
    assertEquals(List.of(member), deleted.getMembers());
    verify(smsContactRepositoryMock, never()).save(any());
  }

  @Test
  void addMember_adds_the_contact_when_not_already_a_member() {
    var existing = SmsContact.builder().id("c1").build();
    var group =
        SmsContactGroup.builder()
            .id("g1")
            .owner(owner)
            .members(new ArrayList<>(List.of(existing)))
            .build();
    when(smsContactGroupRepositoryMock.findById("g1")).thenReturn(Optional.of(group));
    var toAdd = SmsContact.builder().id("c2").build();
    when(smsContactRepositoryMock.findById("c2")).thenReturn(Optional.of(toAdd));
    when(smsContactGroupRepositoryMock.save(any())).thenAnswer(i -> i.getArgument(0));

    var result = subject.addMember("g1", "c2");

    assertEquals(List.of(existing, toAdd), result.getMembers());
  }

  @Test
  void addMember_is_a_noop_when_contact_is_already_a_member() {
    var existing = SmsContact.builder().id("c1").build();
    var group =
        SmsContactGroup.builder()
            .id("g1")
            .owner(owner)
            .members(new ArrayList<>(List.of(existing)))
            .build();
    when(smsContactGroupRepositoryMock.findById("g1")).thenReturn(Optional.of(group));
    when(smsContactRepositoryMock.findById("c1")).thenReturn(Optional.of(existing));

    var result = subject.addMember("g1", "c1");

    assertEquals(List.of(existing), result.getMembers());
    verify(smsContactGroupRepositoryMock, never()).save(any());
  }

  @Test
  void addMember_throws_not_found_for_a_missing_contact() {
    var group = SmsContactGroup.builder().id("g1").owner(owner).members(new ArrayList<>()).build();
    when(smsContactGroupRepositoryMock.findById("g1")).thenReturn(Optional.of(group));
    when(smsContactRepositoryMock.findById("missing")).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> subject.addMember("g1", "missing"));
  }

  @Test
  void removeMember_removes_the_contact_from_the_group() {
    var member1 = SmsContact.builder().id("c1").build();
    var member2 = SmsContact.builder().id("c2").build();
    var group =
        SmsContactGroup.builder()
            .id("g1")
            .owner(owner)
            .members(new ArrayList<>(List.of(member1, member2)))
            .build();
    when(smsContactGroupRepositoryMock.findById("g1")).thenReturn(Optional.of(group));
    when(smsContactGroupRepositoryMock.save(any())).thenAnswer(i -> i.getArgument(0));

    var result = subject.removeMember("g1", "c1");

    assertEquals(List.of(member2), result.getMembers());
  }
}
