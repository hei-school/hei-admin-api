package school.hei.haapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import school.hei.haapi.endpoint.rest.model.AttendanceStatus;
import school.hei.haapi.model.Event;
import school.hei.haapi.model.EventParticipant;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.EventParticipantRepository;
import school.hei.haapi.repository.dao.EventDao;

class EventParticipantGroupSyncServiceTest {
  private final EventParticipantRepository eventParticipantRepository = mock();
  private final EventDao eventDao = mock();
  private final EventParticipantGroupSyncService subject =
      new EventParticipantGroupSyncService(eventParticipantRepository, eventDao);

  @Test
  void adds_enabled_student_to_future_events_of_group_missing_them() {
    var student = User.builder().id("student1").status(User.Status.ENABLED).build();
    var group = Group.builder().id("g1").build();
    var eventAlreadyIn = Event.builder().id("e1").build();
    var eventMissing = Event.builder().id("e2").build();
    when(eventDao.findByCriteria(any(), any(), any(), any(), eq("g1"), any(), any(), any()))
        .thenReturn(List.of(eventAlreadyIn, eventMissing));
    when(eventParticipantRepository.existsByEventIdAndParticipantId("e1", "student1"))
        .thenReturn(true);
    when(eventParticipantRepository.existsByEventIdAndParticipantId("e2", "student1"))
        .thenReturn(false);

    subject.addStudentToFutureEventsOfGroup(student, group);

    @SuppressWarnings("unchecked")
    var captor = org.mockito.ArgumentCaptor.forClass(List.class);
    verify(eventParticipantRepository).saveAll(captor.capture());
    List<EventParticipant> saved = captor.getValue();
    assertEquals(1, saved.size());
    assertEquals("e2", saved.get(0).getEvent().getId());
    assertEquals(AttendanceStatus.UNCHECKED, saved.get(0).getStatus());
  }

  @Test
  void adds_suspended_student_as_missing() {
    var student = User.builder().id("student1").status(User.Status.SUSPENDED).build();
    var group = Group.builder().id("g1").build();
    var event = Event.builder().id("e1").build();
    when(eventDao.findByCriteria(any(), any(), any(), any(), eq("g1"), any(), any(), any()))
        .thenReturn(List.of(event));
    when(eventParticipantRepository.existsByEventIdAndParticipantId("e1", "student1"))
        .thenReturn(false);

    subject.addStudentToFutureEventsOfGroup(student, group);

    @SuppressWarnings("unchecked")
    var captor = org.mockito.ArgumentCaptor.forClass(List.class);
    verify(eventParticipantRepository).saveAll(captor.capture());
    List<EventParticipant> saved = captor.getValue();
    assertEquals(AttendanceStatus.MISSING, saved.get(0).getStatus());
  }

  @Test
  void does_nothing_for_a_student_who_is_neither_enabled_nor_suspended() {
    var student = User.builder().id("student1").status(User.Status.DISABLED).build();
    var group = Group.builder().id("g1").build();

    subject.addStudentToFutureEventsOfGroup(student, group);

    verify(eventDao, never())
        .findByCriteria(any(), any(), any(), any(), anyString(), any(), any(), any());
  }

  @Test
  void removes_student_from_future_events_of_the_left_group() {
    var student = User.builder().id("student1").build();
    var group = Group.builder().id("g1").build();
    var staleParticipant = EventParticipant.builder().id("ep1").build();
    when(eventParticipantRepository.findAllByParticipantIdAndGroupIdAndEventBeginDatetimeAfter(
            eq("student1"), eq("g1"), any()))
        .thenReturn(List.of(staleParticipant));

    subject.removeStudentFromFutureEventsOfGroup(student, group);

    verify(eventParticipantRepository).deleteAll(List.of(staleParticipant));
  }
}
