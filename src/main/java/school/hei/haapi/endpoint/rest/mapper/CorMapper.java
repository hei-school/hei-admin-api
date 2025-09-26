package school.hei.haapi.endpoint.rest.mapper;

import static school.hei.haapi.endpoint.rest.model.CorStatus.CANCELED;
import static school.hei.haapi.endpoint.rest.model.CorStatus.IN_PROGRESS;
import static school.hei.haapi.endpoint.rest.model.CorStatus.LEAVE;
import static school.hei.haapi.endpoint.rest.model.CorStatus.NO_SHOW;
import static school.hei.haapi.endpoint.rest.model.CorStatus.STAY;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.Cor;
import school.hei.haapi.endpoint.rest.model.CorStatus;
import school.hei.haapi.endpoint.rest.model.CrupdateCor;
import school.hei.haapi.model.CorComment;
import school.hei.haapi.service.UserService;

@Component
@AllArgsConstructor
public class CorMapper {
  private final UserMapper userMapper;
  private final UserService userService;

  private CorStatus toRest(CorComment.CorStatus domain) {
    return switch (domain) {
      case IN_PROGRESS -> IN_PROGRESS;
      case STAY -> STAY;
      case CANCELED -> CANCELED;
      case LEAVE -> LEAVE;
      case NO_SHOW -> NO_SHOW;
      case null -> IN_PROGRESS;
    };
  }

  public CorComment.CorStatus toDomain(CorStatus rest) {
    return switch (rest) {
      case IN_PROGRESS -> CorComment.CorStatus.IN_PROGRESS;
      case STAY -> CorComment.CorStatus.STAY;
      case CANCELED -> CorComment.CorStatus.CANCELED;
      case LEAVE -> CorComment.CorStatus.LEAVE;
      case NO_SHOW -> CorComment.CorStatus.NO_SHOW;
      case null -> CorComment.CorStatus.IN_PROGRESS;
    };
  }

  public CorComment toDomain(school.hei.haapi.endpoint.rest.model.CorCommentInfo rest) {
    return CorComment.builder()
        .comment(rest.getComment())
        .status(toDomain(rest.getStatus()))
        .build();
  }

  public Cor toRest(school.hei.haapi.model.Cor cor) {
    return new Cor()
        .id(cor.getId())
        .creationDatetime(cor.getCreationDatetime())
        .concernedStudent(userMapper.toIdentifier(cor.getConcernedStudent()))
        .description(cor.getDescription())
        .status(toRest(cor.getStatus()))
        .interviewDate(cor.getInterviewDatetime());
  }

  public school.hei.haapi.model.Cor toDomain(CrupdateCor cor, String studentId) {
    return new school.hei.haapi.model.Cor()
        .toBuilder()
            .id(cor.getId())
            .concernedStudent(userService.findById(studentId))
            .description(cor.getDescription())
            .interviewDatetime(cor.getInterviewDate())
            .build();
  }

  public List<Cor> toRest(List<school.hei.haapi.model.Cor> cors) {
    return cors.stream().map(this::toRest).toList();
  }
}
