package school.hei.haapi.model.validator;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.EventParticipant;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.repository.EventParticipantRepository;

import java.util.List;
import java.util.function.Consumer;

@Component
@AllArgsConstructor
public class EventParticipantValidator implements Consumer<EventParticipant> {
    private EventParticipantRepository eventParticipantRepository;

    @Override
    public void accept(EventParticipant eventParticipant) {
        if (eventParticipantRepository.getByUserParticipant_IdAndEvent_Id(
                eventParticipant.getUserParticipant().getId(),
                eventParticipant.getEvent().getId()
        ) != null) {
            throw new BadRequestException(
                    "Event participant id:" + eventParticipant.getUserParticipant().getId() +
                            " in Event:" + eventParticipant.getEvent().getId() +
                            " already exists.");
        }
    }

    public void accept(List<EventParticipant> eventParticipantList) {
        eventParticipantList.forEach(this::accept);
    }
}
