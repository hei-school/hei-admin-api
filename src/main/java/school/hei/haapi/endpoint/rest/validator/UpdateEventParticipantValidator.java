package school.hei.haapi.endpoint.rest.validator;

import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CrupdateEventParticipant;
import school.hei.haapi.model.EventParticipant;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.service.EventParticipantService;

@Component
@AllArgsConstructor
public class UpdateEventParticipantValidator implements Consumer<CrupdateEventParticipant> {
  private final EventParticipantService eventParticipantService;

  @Override
  public void accept(CrupdateEventParticipant toCheck) {
    EventParticipant eventParticipant = eventParticipantService.findById(toCheck.getId());
    if (toCheck.getEmail() != eventParticipant.getParticipant().getEmail()
        || toCheck.getFirstName() != eventParticipant.getParticipant().getFirstName()
        || toCheck.getLastName() != eventParticipant.getParticipant().getLastName()
        || toCheck.getNic() != eventParticipant.getParticipant().getNic()) {
      throw new BadRequestException(
          "Event participant information cannot be modified except the status");
    }
  }

  public void acceptAll(List<CrupdateEventParticipant> toCheck) {
    toCheck.forEach(this);
  }
}
