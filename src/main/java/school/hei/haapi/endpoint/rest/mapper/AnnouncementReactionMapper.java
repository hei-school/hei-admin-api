package school.hei.haapi.endpoint.rest.mapper;

import static school.hei.haapi.model.AnnouncementReaction.ReactionEnum.*;

import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.ReactionEnum;
import school.hei.haapi.model.AnnouncementReaction;
import school.hei.haapi.model.exception.BadRequestException;

@Component
public class AnnouncementReactionMapper {
  public AnnouncementReaction.ReactionEnum toDomain(ReactionEnum reactionEnum) {
    return switch (reactionEnum) {
      case UNCHECK -> UNCHECK;
      case CHECK -> CHECK;
      case null, default ->
          throw new BadRequestException(
              "Unexpected value during the conversion in AnnouncementReaction, the value: "
                  + reactionEnum);
    };
  }
}
