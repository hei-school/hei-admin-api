package school.hei.haapi.endpoint.rest.mapper;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.ComplaintRouting;

@Component
@AllArgsConstructor
public class ComplaintRoutingMapper {
  private final UserMapper userMapper;
  private final ComplaintMapper complaintMapper;

  public ComplaintRouting toRest(school.hei.haapi.model.ComplaintRouting routing) {
    return new ComplaintRouting()
        .id(routing.getId())
        .type(complaintMapper.toRestType(routing.getType()))
        .manager(userMapper.toIdentifier(routing.getManager()))
        .creationDatetime(routing.getCreationDatetime());
  }

  public List<ComplaintRouting> toRest(List<school.hei.haapi.model.ComplaintRouting> routings) {
    return routings.stream().map(this::toRest).toList();
  }
}
