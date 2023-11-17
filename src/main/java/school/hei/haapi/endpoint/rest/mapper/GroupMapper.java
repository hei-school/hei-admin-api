package school.hei.haapi.endpoint.rest.mapper;

import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CreateGroup;
import school.hei.haapi.endpoint.rest.model.Group;

@Component
public class GroupMapper {
  //todo: to review all class
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

  public school.hei.haapi.model.notEntity.CreateGroup toDomain(CreateGroup restGroup) {
    school.hei.haapi.model.Group group = school.hei.haapi.model.Group.builder()
            .id(restGroup.getId())
            .name(restGroup.getName())
            .ref(restGroup.getRef())
            .creationDatetime(restGroup.getCreationDatetime())
            .build();
    return school.hei.haapi.model.notEntity.CreateGroup.builder()
            .group(group)
            .studentsToAdd(restGroup.getStudentsToAdd())
            .build();
  }
}
