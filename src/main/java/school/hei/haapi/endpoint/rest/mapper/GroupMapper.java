package school.hei.haapi.endpoint.rest.mapper;

import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CreateGroup;
import school.hei.haapi.model.Group;

@Component
public class GroupMapper {

  public school.hei.haapi.endpoint.rest.model.Group toRest(Group group) {
    var restGroup = new school.hei.haapi.endpoint.rest.model.Group();
    restGroup.setId(group.getId());
    restGroup.setName(group.getName());
    restGroup.setRef(group.getRef());
    restGroup.setCreationDatetime(group.getCreationDatetime());
    return restGroup;
  }

  public Group toDomain(CreateGroup createGroup) {
    return Group.builder()
        .name(createGroup.getName())
        .ref(createGroup.getRef())
        .build();
  }
}
