package school.hei.haapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static school.hei.haapi.model.Event.PlaceName.IVANDRY;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Event;
import school.hei.haapi.model.EventParticipant;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.Letter;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.EventParticipantRepository;
import school.hei.haapi.repository.LetterRepository;

class LetterServiceTest extends FacadeITMockedThirdParties {
  @MockBean private EventParticipantRepository eventParticipantRepository;
  @MockBean private LetterRepository letterRepository;
  @Autowired private LetterService subject;

  @Test
  void getLettersByStudentId() {
    when(eventParticipantRepository.findEventParticipantByParticipantIdAndEventId(any(), any()))
        .thenReturn(eventParticipant());
    when(letterRepository.findAllByUserIdAndEventParticipantId(any(), any(), any()))
        .thenReturn(List.of(letter()));
    var actual =
        subject.getLettersByStudentId(
            student().getId(), event().getId(), null, new PageFromOne(1), new BoundedPageSize(10));
    assertEquals(List.of(letter()), actual);
  }

  private static User student() {
    return User.builder().id("student1").email("freddy@gmail.com").ref("std25003").build();
  }

  private static Group group() {
    return Group.builder().id("group1").ref("S").build();
  }

  private static Event event() {
    return Event.builder()
        .id("event1")
        .description("math")
        .groups(List.of(group()))
        .place(IVANDRY)
        .build();
  }

  private static EventParticipant eventParticipant() {
    return EventParticipant.builder()
        .id("eventParticipantId")
        .participant(student())
        .group(group())
        .event(event())
        .build();
  }

  private static Letter letter() {
    return Letter.builder().id("letter1").filePath("filepath").ref("hei-fjkdsqj-875").build();
  }
}
