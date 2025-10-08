package school.hei.haapi.endpoint.rest.mapper;

import static school.hei.haapi.endpoint.rest.model.CorStatus.CANCELED;
import static school.hei.haapi.endpoint.rest.model.CorStatus.IN_PROGRESS;
import static school.hei.haapi.endpoint.rest.model.CorStatus.LEAVE;
import static school.hei.haapi.endpoint.rest.model.CorStatus.NO_SHOW;
import static school.hei.haapi.endpoint.rest.model.CorStatus.STAY;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.Cor;
import school.hei.haapi.endpoint.rest.model.CorStatus;
import school.hei.haapi.endpoint.rest.model.CrupdateCor;
import school.hei.haapi.service.UserService;

@Component
@AllArgsConstructor
public class CorMapper {
  private final UserMapper userMapper;
  private final UserService userService;
  private final CorCommentMapper corCommentMapper;

  public List<school.hei.haapi.model.CorStatus> toDomainStatus(@NonNull List<CorStatus> rest) {
    return rest.stream().map(this::toDomainStatus).toList();
  }

  public Cor toRest(school.hei.haapi.model.Cor cor) {
    return new Cor()
        .id(cor.getId())
        .creationDatetime(cor.getCreationDatetime())
        .concernedStudent(userMapper.toIdentifier(cor.getStudent()))
        .description(cor.getDescription())
        .status(toRestStatus(cor.getStatus()))
        .comments(
            cor.getComments() != null ? corCommentMapper.toRest(cor.getComments()) : List.of())
        .interviewDate(cor.getInterviewDatetime());
  }

  public school.hei.haapi.model.Cor toDomain(CrupdateCor cor) {
    return new school.hei.haapi.model.Cor()
        .toBuilder()
            .id(cor.getId())
            .student(userService.getById(cor.getConcernedStudentId()))
            .description(cor.getDescription())
            .interviewDatetime(cor.getInterviewDate())
            .status(toDomainStatus(cor.getStatus()))
            .build();
  }

  public List<Cor> toRest(List<school.hei.haapi.model.Cor> cors) {
    return cors.stream().map(this::toRest).toList();
  }

  private CorStatus toRestStatus(school.hei.haapi.model.CorStatus domain) {
    if (domain == null) return null;
    return switch (domain) {
      case IN_PROGRESS -> IN_PROGRESS;
      case STAY -> STAY;
      case CANCELED -> CANCELED;
      case LEAVE -> LEAVE;
      case NO_SHOW -> NO_SHOW;
    };
  }

  private school.hei.haapi.model.CorStatus toDomainStatus(CorStatus rest) {
    if (rest == null) return null;
    return switch (rest) {
      case IN_PROGRESS -> school.hei.haapi.model.CorStatus.IN_PROGRESS;
      case STAY -> school.hei.haapi.model.CorStatus.STAY;
      case CANCELED -> school.hei.haapi.model.CorStatus.CANCELED;
      case LEAVE -> school.hei.haapi.model.CorStatus.LEAVE;
      case NO_SHOW -> school.hei.haapi.model.CorStatus.NO_SHOW;
    };
  }
}
