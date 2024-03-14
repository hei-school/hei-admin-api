package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CreateEvent;
import school.hei.haapi.endpoint.rest.model.GroupIdentifier;
import school.hei.haapi.model.Event;
import school.hei.haapi.service.CourseService;
import school.hei.haapi.service.GroupService;
import school.hei.haapi.service.UserService;

import java.util.Objects;

@Component
@AllArgsConstructor
public class EventMapper {
  private final CourseService courseService;
  private final GroupService groupService;
  private final UserService userService;
  private final CourseMapper courseMapper;
  private final UserMapper userMapper;

  public school.hei.haapi.model.Event toDomain(CreateEvent createEvent) {
    return Event.builder()
        .id(createEvent.getId())
        .begin(createEvent.getBegin())
        .end(createEvent.getEnd())
        .description(createEvent.getDescription())
        .course(courseService.getById(createEvent.getCourseId()))
        .groups(
            groupService.getAllById(
                Objects.requireNonNull(createEvent.getGroups()).stream().map(GroupIdentifier::getId).toList()))
        .planner(userService.findById(createEvent.getPlannerId()))
        .type(createEvent.getEventType())
        .build();
  }

  public school.hei.haapi.endpoint.rest.model.Event toRest(Event domain) {
    return new school.hei.haapi.endpoint.rest.model.Event()
        .id(domain.getId())
        .end(domain.getEnd())
        .begin(domain.getBegin())
        .description(domain.getDescription())
        .type(domain.getType())
        .course(Objects.isNull(domain.getCourse()) ? null : courseMapper.toRest(domain.getCourse()))
        .planner(userMapper.toIdentifier(domain.getPlanner()));
  }
}
