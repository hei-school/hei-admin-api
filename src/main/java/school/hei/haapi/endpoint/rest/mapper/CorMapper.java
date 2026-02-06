package school.hei.haapi.endpoint.rest.mapper;

import static java.util.stream.Collectors.toUnmodifiableSet;
import static school.hei.haapi.endpoint.rest.model.CorStatus.CANCELED;
import static school.hei.haapi.endpoint.rest.model.CorStatus.IN_PROGRESS;
import static school.hei.haapi.endpoint.rest.model.CorStatus.LEAVE;
import static school.hei.haapi.endpoint.rest.model.CorStatus.NO_SHOW;
import static school.hei.haapi.endpoint.rest.model.CorStatus.STAY;
import static school.hei.haapi.model.User.Role.ADMIN;
import static school.hei.haapi.model.User.Role.MANAGER;
import static school.hei.haapi.model.User.Role.STAFF_MEMBER;
import static school.hei.haapi.model.User.Role.TEACHER;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.Cor;
import school.hei.haapi.endpoint.rest.model.CorStatus;
import school.hei.haapi.endpoint.rest.model.CrupdateCor;
import school.hei.haapi.endpoint.rest.model.UserIdentifier;
import school.hei.haapi.model.User;
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
        .interviewers(getRestInterviewers(cor))
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
            .interviewers(getDomainInterviewers(cor))
            .build();
  }

  public school.hei.haapi.model.Cor toDomainUpdate(
      CrupdateCor corDto, school.hei.haapi.model.Cor cor) {
    cor.setDescription(corDto.getDescription());
    cor.setInterviewDatetime(corDto.getInterviewDate());
    cor.setStatus(toDomainStatus(corDto.getStatus()));
    cor.setInterviewers(getDomainInterviewers(corDto));
    return cor;
  }

  private List<UserIdentifier> getRestInterviewers(school.hei.haapi.model.Cor cor) {
    if (cor.getInterviewers() == null) {
      return List.of();
    }
    return cor.getInterviewers().stream().map(userMapper::toIdentifier).toList();
  }

  private List<User> getDomainInterviewers(CrupdateCor cor) {
    if (cor.getInterviewerIds() == null) {
      return List.of();
    }
    return userService.getByRoleAndIds(
        List.of(TEACHER, ADMIN, MANAGER, STAFF_MEMBER),
        cor.getInterviewerIds().stream().collect(toUnmodifiableSet()));
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
