package school.hei.haapi.endpoint.rest.mapper;

import static school.hei.haapi.endpoint.rest.mapper.FileInfoMapper.ONE_DAY_DURATION_AS_LONG;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.EventParticipantLetter;
import school.hei.haapi.model.EventParticipant;
import school.hei.haapi.model.Letter;
import school.hei.haapi.model.User;
import school.hei.haapi.service.EventParticipantService;
import school.hei.haapi.service.LetterService;
import school.hei.haapi.service.aws.FileService;

@Component
@AllArgsConstructor
public class EventParticipantMapper {

  private final EventParticipantService eventParticipantService;
  private final FileService fileService;
  private final LetterService letterService;

  public EventParticipant toDomain(
      school.hei.haapi.endpoint.rest.model.UpdateEventParticipant updateEventParticipant) {

    EventParticipant eventParticipant =
        eventParticipantService.findById(updateEventParticipant.getId());
    eventParticipant.setStatus(updateEventParticipant.getEventStatus());
    return eventParticipant;
  }

  public school.hei.haapi.endpoint.rest.model.EventParticipant toRest(EventParticipant domain) {
    User participant = domain.getParticipant();

    List<EventParticipantLetter> letters =
        letterService.getLettersByEventParticipantId(domain.getId()).stream()
            .map(this::toRest)
            .toList();

    return new school.hei.haapi.endpoint.rest.model.EventParticipant()
        .email(participant.getEmail())
        .eventStatus(domain.getStatus())
        .id(domain.getId())
        .nic(participant.getNic())
        .ref(participant.getRef())
        .firstName(participant.getFirstName())
        .lastName(participant.getLastName())
        .groupName(domain.getGroup().getName())
        .letter(letters);
  }

  public EventParticipantLetter toRest(Letter letter) {
    String fileUrl = fileService.getPresignedUrl(letter.getFilePath(), ONE_DAY_DURATION_AS_LONG);
    return new EventParticipantLetter()
        .ref(letter.getRef())
        .status(letter.getStatus())
        .creationDatetime(letter.getCreationDatetime())
        .description(letter.getDescription())
        .fileUrl(fileUrl);
  }
}
