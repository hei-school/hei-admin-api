package school.hei.haapi.endpoint.rest.mapper;

import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.Group;

@Component
public class GroupMapper {

  public Group toRest(school.hei.haapi.model.Group group) {
    Group restGroup = new Group();
    restGroup.setId(group.getId());
    restGroup.setName(group.getName());
    restGroup.setRef(group.getRef());
    restGroup.setCreationDatetime(group.getCreationDatetime());
    return restGroup;
  }
}
