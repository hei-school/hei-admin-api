package school.hei.haapi.endpoint.rest.mapper;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CreateGroupFlow;
import school.hei.haapi.endpoint.rest.model.GroupFlow;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.User;

@Component
public class GroupFlowMapper {

  public GroupFlow toRest(school.hei.haapi.model.GroupFlow domain) {
    return new GroupFlow()
        .id(domain.getId())
        .flowDatetime(domain.getFlowDatetime())
        .groupId(domain.getGroup().getId())
        .studentId(domain.getStudent().getId())
        .moveType(GroupFlow.MoveTypeEnum.fromValue(domain.getGroupFlowType().toString()));
  }

  public List<CreateGroupFlow> toDomain(
      Group group, List<User> students, CreateGroupFlow.MoveTypeEnum type) {
    List<CreateGroupFlow> createGroupFlows = new ArrayList<>();
    for (User student : students) {
      createGroupFlows.add(
          new CreateGroupFlow().studentId(student.getId()).groupId(group.getId()).moveType(type));
    }
    return createGroupFlows;
  }
}
