package school.hei.haapi.endpoint.rest.mapper;

import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.Group;

@Component
public class GroupMapper {

  public Group toRest(school.hei.haapi.model.Group group) {
    return new Group()
        .id(group.getId())
        .name(group.getName())
        .ref(group.getRef())
        .creationDatetime(group.getCreationDatetime());
  }

  public school.hei.haapi.model.Group toDomain(Group restGroup) {
    return school.hei.haapi.model.Group.builder()
        .id(restGroup.getId())
        .name(restGroup.getName())
        .ref(restGroup.getRef())
        .creationDatetime(restGroup.getCreationDatetime())
        .build();
  }
}
