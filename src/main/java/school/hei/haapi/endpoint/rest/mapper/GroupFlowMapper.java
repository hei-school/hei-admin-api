package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.GroupFlow;

@Component
@AllArgsConstructor
public class GroupFlowMapper {

  public GroupFlow toRest(school.hei.haapi.model.GroupFlow domain) {
    return new GroupFlow()
            .id(domain.getId())
            .flowdDateTime(domain.getFlowDatetime())
            .groupId(domain.getGroup().getId())
            .studentId(domain.getStudent().getId())
            .moveType(GroupFlow.MoveTypeEnum.valueOf(domain.getGroupFlowType().toString()));
  }

}
