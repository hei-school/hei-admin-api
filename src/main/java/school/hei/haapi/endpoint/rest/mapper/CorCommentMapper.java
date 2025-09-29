package school.hei.haapi.endpoint.rest.mapper;

import static school.hei.haapi.endpoint.rest.model.CorStatus.CANCELED;
import static school.hei.haapi.endpoint.rest.model.CorStatus.IN_PROGRESS;
import static school.hei.haapi.endpoint.rest.model.CorStatus.LEAVE;
import static school.hei.haapi.endpoint.rest.model.CorStatus.NO_SHOW;
import static school.hei.haapi.endpoint.rest.model.CorStatus.STAY;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CorStatus;
import school.hei.haapi.model.CorComment;

@Component
@AllArgsConstructor
public class CorCommentMapper {
  public CorComment toDomain(school.hei.haapi.endpoint.rest.model.CorCommentInfo rest) {
    return CorComment.builder()
        .comment(rest.getComment())
        .status(toDomain(rest.getStatus()))
        .build();
  }

  private school.hei.haapi.endpoint.rest.model.CorComment toRest(CorComment domain) {
    return new school.hei.haapi.endpoint.rest.model.CorComment()
        .comment(domain.getComment())
        .status(toRest(domain.getStatus()))
        .creationDate(domain.getCreationDatetime());
  }

  public List<school.hei.haapi.endpoint.rest.model.CorComment> toRest(List<CorComment> domain) {
    return domain.stream().map(this::toRest).toList();
  }

  public CorStatus toRest(CorComment.CorStatus domain) {
    if (domain == null) return null;
    return switch (domain) {
      case IN_PROGRESS -> IN_PROGRESS;
      case STAY -> STAY;
      case CANCELED -> CANCELED;
      case LEAVE -> LEAVE;
      case NO_SHOW -> NO_SHOW;
    };
  }

  public CorComment.CorStatus toDomain(CorStatus rest) {
    if (rest == null) return null;
    return switch (rest) {
      case IN_PROGRESS -> CorComment.CorStatus.IN_PROGRESS;
      case STAY -> CorComment.CorStatus.STAY;
      case CANCELED -> CorComment.CorStatus.CANCELED;
      case LEAVE -> CorComment.CorStatus.LEAVE;
      case NO_SHOW -> CorComment.CorStatus.NO_SHOW;
    };
  }
}
