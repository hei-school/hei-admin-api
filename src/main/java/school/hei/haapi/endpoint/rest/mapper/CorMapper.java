package school.hei.haapi.endpoint.rest.mapper;

import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.Cor;
import school.hei.haapi.endpoint.rest.model.CrupdateCor;
import school.hei.haapi.service.UserService;

@Component
@AllArgsConstructor
public class CorMapper {
  private final UserMapper userMapper;
  private final UserService userService;

  public Cor toRest(school.hei.haapi.model.Cor cor) {
    return new Cor()
        .id(cor.getId())
        .creationDatetime(cor.getCreationDatetime())
        .concernedStudent(userMapper.toIdentifier(cor.getConcernedStudent()))
        .description(cor.getDescription());
  }

  public school.hei.haapi.model.Cor toDomain(Cor cor) {
    var concernedStudent = Optional.ofNullable(cor.getConcernedStudent());
    return new school.hei.haapi.model.Cor()
        .toBuilder()
            .concernedStudent(concernedStudent.map(userMapper::toDomain).orElse(null))
            .description(cor.getDescription())
            .creationDatetime(cor.getCreationDatetime())
            .build();
  }

  public school.hei.haapi.model.Cor toDomain(CrupdateCor cor, String studentId) {
    return new school.hei.haapi.model.Cor()
        .toBuilder()
            .id(cor.getId())
            .concernedStudent(userService.findById(studentId))
            .description(cor.getDescription())
            .interviewDateTime(cor.getInterviewDate())
            .build();
  }

  public List<Cor> toRest(List<school.hei.haapi.model.Cor> cors) {
    return cors.stream().map(this::toRest).toList();
  }

  public List<school.hei.haapi.model.Cor> toDomain(List<CrupdateCor> cors, String studentId) {
    return cors.stream().map(cor -> toDomain(cor, studentId)).toList();
  }
}
