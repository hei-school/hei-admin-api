package school.hei.haapi.endpoint.rest.mapper;

import java.util.List;
import lombok.AllArgsConstructor;
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

  public List<school.hei.haapi.model.CorStatus> toDomain(List<CorStatus> rest) {
    return rest != null ? rest.stream().map(corCommentMapper::toDomain).toList() : List.of();
  }

  public Cor toRest(school.hei.haapi.model.Cor cor) {
    return new Cor()
        .id(cor.getId())
        .creationDatetime(cor.getCreationDatetime())
        .concernedStudent(userMapper.toIdentifier(cor.getStudent()))
        .description(cor.getDescription())
        .status(corCommentMapper.toRest(cor.getStatus()))
        .comments(
            cor.getComments() != null ? corCommentMapper.toRest(cor.getComments()) : List.of())
        .interviewDate(cor.getInterviewDatetime());
  }

  public school.hei.haapi.model.Cor toDomain(CrupdateCor cor, String studentId) {
    return new school.hei.haapi.model.Cor()
        .toBuilder()
            .id(cor.getId())
            .student(userService.getById(studentId))
            .description(cor.getDescription())
            .interviewDatetime(cor.getInterviewDate())
            .build();
  }

  public List<Cor> toRest(List<school.hei.haapi.model.Cor> cors) {
    return cors.stream().map(this::toRest).toList();
  }
}
