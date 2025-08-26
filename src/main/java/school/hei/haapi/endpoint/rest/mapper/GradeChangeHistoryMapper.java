package school.hei.haapi.endpoint.rest.mapper;

import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.GradeHistory;

@Component
public class GradeChangeHistoryMapper {
  public GradeHistory toRest(school.hei.haapi.model.GradeChangeHistory domain) {
    return new GradeHistory()
        .comment(domain.getComment())
        .score(domain.getScore())
        .createdAt(domain.getChangeInstant());
  }
}
