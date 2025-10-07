package school.hei.haapi.endpoint.rest.mapper;


import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.CorComment;

@Component
@AllArgsConstructor
public class CorCommentMapper {
  public CorComment toDomain(school.hei.haapi.endpoint.rest.model.CorCommentInfo rest) {
    return CorComment.builder().comment(rest.getComment()).build();
  }

  public school.hei.haapi.endpoint.rest.model.CorComment toRest(CorComment domain) {
    return new school.hei.haapi.endpoint.rest.model.CorComment()
        .comment(domain.getComment())
        .creationDate(domain.getCreationDatetime());
  }

  public List<school.hei.haapi.endpoint.rest.model.CorComment> toRest(List<CorComment> domain) {
    return domain.stream().map(this::toRest).toList();
  }
}
