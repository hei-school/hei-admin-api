package school.hei.haapi.endpoint.rest.mapper;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.*;
import school.hei.haapi.service.GroupService;

@Component
@AllArgsConstructor
public class GroupMapper {
  // todo: to review all class
  private final GroupService groupService;

  public Group toRest(school.hei.haapi.model.Group group) {
    return new Group()
        .id(group.getId())
        .name(group.getName())
        .ref(group.getRef())
        .creationDatetime(group.getCreationDatetime());
  }

  public GroupDTO toGroupDTO(List<school.hei.haapi.model.Group> groups) {
    Map<String, Integer> stats = groupService.getStudentsStat();
    return new GroupDTO()
        .groups(groups.stream().map(this::toRest).toList())
        .men(stats.get("men"))
        .totalStudents(stats.get("totalStudents"))
        .women(stats.get("women"))
        .totalGroups(stats.get("totalGroups"))
        .studentsAlternating(0);
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
    school.hei.haapi.model.Group group =
        school.hei.haapi.model.Group.builder()
            .id(restGroup.getId())
            .name(restGroup.getName())
            .ref(restGroup.getRef())
            .creationDatetime(restGroup.getCreationDatetime())
            .build();
    return school
        .hei
        .haapi
        .model
        .notEntity
        .CreateGroup
        .builder()
        .group(group)
        .students(restGroup.getStudents())
        .build();
  }

  public GroupIdentifier toRestGroupIdentifier(school.hei.haapi.model.Group domain) {
    return new GroupIdentifier().id(domain.getId()).name(domain.getName()).ref(domain.getRef());
  }
}
