package school.hei.haapi.unit.sms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.GroupFlow;
import school.hei.haapi.model.SmsContact;
import school.hei.haapi.model.SmsContactGroup;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.SmsContactGroupRepository;
import school.hei.haapi.repository.SmsContactRepository;
import school.hei.haapi.service.sms.StudentGroupContactSyncService;

class StudentGroupContactSyncServiceTest {
  private final SmsContactGroupRepository smsContactGroupRepositoryMock = mock();
  private final SmsContactRepository smsContactRepositoryMock = mock();
  private final StudentGroupContactSyncService subject =
      new StudentGroupContactSyncService(smsContactGroupRepositoryMock, smsContactRepositoryMock);

  private static Group group() {
    return Group.builder().id("g1").name("K2").build();
  }

  private static GroupFlow flow(String studentId, Group group, GroupFlow.GroupFlowType type) {
    return GroupFlow.builder()
        .student(User.builder().id(studentId).build())
        .group(group)
        .groupFlowType(type)
        .build();
  }

  @Test
  void createContactGroupFor_names_it_contact_de_plus_the_group_name() {
    var creator = User.builder().id("creator1").build();
    when(smsContactGroupRepositoryMock.findByStudentGroup_IdAndIsDeletedFalse("g1"))
        .thenReturn(Optional.empty());
    when(smsContactGroupRepositoryMock.save(any())).thenAnswer(i -> i.getArgument(0));

    var created = subject.createContactGroupFor(group(), creator);

    assertEquals("Contact de K2", created.getName());
    assertEquals(group(), created.getStudentGroup());
    assertEquals(creator, created.getOwner());
  }

  @Test
  void createContactGroupFor_is_idempotent_when_the_group_already_has_one() {
    var existing = SmsContactGroup.builder().id("cg1").studentGroup(group()).build();
    when(smsContactGroupRepositoryMock.findByStudentGroup_IdAndIsDeletedFalse("g1"))
        .thenReturn(Optional.of(existing));

    var result = subject.createContactGroupFor(group(), User.builder().id("creator1").build());

    assertEquals(existing, result);
    verify(smsContactGroupRepositoryMock, never()).save(any());
  }

  @Test
  void syncMembership_adds_the_students_contact_on_join() {
    var group = group();
    var contactGroup =
        SmsContactGroup.builder().id("cg1").studentGroup(group).members(new ArrayList<>()).build();
    var contact = SmsContact.builder().id("c1").build();
    when(smsContactGroupRepositoryMock.findByStudentGroup_IdAndIsDeletedFalse("g1"))
        .thenReturn(Optional.of(contactGroup));
    when(smsContactRepositoryMock.findByOwner_IdAndIsDeletedFalse("student1"))
        .thenReturn(Optional.of(contact));

    subject.syncMembership(flow("student1", group, GroupFlow.GroupFlowType.JOIN));

    assertEquals(List.of(contact), contactGroup.getMembers());
    verify(smsContactGroupRepositoryMock).save(contactGroup);
  }

  @Test
  void syncMembership_is_a_noop_on_join_when_already_a_member() {
    var group = group();
    var contact = SmsContact.builder().id("c1").build();
    var contactGroup =
        SmsContactGroup.builder()
            .id("cg1")
            .studentGroup(group)
            .members(new ArrayList<>(List.of(contact)))
            .build();
    when(smsContactGroupRepositoryMock.findByStudentGroup_IdAndIsDeletedFalse("g1"))
        .thenReturn(Optional.of(contactGroup));
    when(smsContactRepositoryMock.findByOwner_IdAndIsDeletedFalse("student1"))
        .thenReturn(Optional.of(contact));

    subject.syncMembership(flow("student1", group, GroupFlow.GroupFlowType.JOIN));

    assertEquals(List.of(contact), contactGroup.getMembers());
    verify(smsContactGroupRepositoryMock, never()).save(any());
  }

  @Test
  void syncMembership_removes_the_students_contact_on_leave() {
    var group = group();
    var contact = SmsContact.builder().id("c1").build();
    var contactGroup =
        SmsContactGroup.builder()
            .id("cg1")
            .studentGroup(group)
            .members(new ArrayList<>(List.of(contact)))
            .build();
    when(smsContactGroupRepositoryMock.findByStudentGroup_IdAndIsDeletedFalse("g1"))
        .thenReturn(Optional.of(contactGroup));
    when(smsContactRepositoryMock.findByOwner_IdAndIsDeletedFalse("student1"))
        .thenReturn(Optional.of(contact));

    subject.syncMembership(flow("student1", group, GroupFlow.GroupFlowType.LEAVE));

    assertEquals(List.of(), contactGroup.getMembers());
    verify(smsContactGroupRepositoryMock).save(contactGroup);
  }

  @Test
  void syncMembership_is_a_noop_when_the_group_has_no_linked_contact_group() {
    when(smsContactGroupRepositoryMock.findByStudentGroup_IdAndIsDeletedFalse("g1"))
        .thenReturn(Optional.empty());

    subject.syncMembership(flow("student1", group(), GroupFlow.GroupFlowType.JOIN));

    verify(smsContactRepositoryMock, never()).findByOwner_IdAndIsDeletedFalse(any());
    verify(smsContactGroupRepositoryMock, never()).save(any());
  }

  @Test
  void syncMembership_is_a_noop_when_the_student_has_no_contact() {
    var contactGroup = SmsContactGroup.builder().id("cg1").studentGroup(group()).build();
    when(smsContactGroupRepositoryMock.findByStudentGroup_IdAndIsDeletedFalse("g1"))
        .thenReturn(Optional.of(contactGroup));
    when(smsContactRepositoryMock.findByOwner_IdAndIsDeletedFalse("student1"))
        .thenReturn(Optional.empty());

    subject.syncMembership(flow("student1", group(), GroupFlow.GroupFlowType.JOIN));

    verify(smsContactGroupRepositoryMock, never()).save(any());
  }

  @Test
  void syncMembership_of_a_list_syncs_each_flow() {
    var group = group();
    var contactGroup =
        SmsContactGroup.builder().id("cg1").studentGroup(group).members(new ArrayList<>()).build();
    var contact1 = SmsContact.builder().id("c1").build();
    var contact2 = SmsContact.builder().id("c2").build();
    when(smsContactGroupRepositoryMock.findByStudentGroup_IdAndIsDeletedFalse("g1"))
        .thenReturn(Optional.of(contactGroup));
    when(smsContactRepositoryMock.findByOwner_IdAndIsDeletedFalse("student1"))
        .thenReturn(Optional.of(contact1));
    when(smsContactRepositoryMock.findByOwner_IdAndIsDeletedFalse("student2"))
        .thenReturn(Optional.of(contact2));

    subject.syncMembership(
        List.of(
            flow("student1", group, GroupFlow.GroupFlowType.JOIN),
            flow("student2", group, GroupFlow.GroupFlowType.JOIN)));

    assertEquals(List.of(contact1, contact2), contactGroup.getMembers());
    verify(smsContactGroupRepositoryMock, times(2)).save(contactGroup);
  }
}
