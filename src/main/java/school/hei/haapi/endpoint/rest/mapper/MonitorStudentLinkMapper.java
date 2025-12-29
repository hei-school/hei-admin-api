package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.MonitorStudentLink;
import school.hei.haapi.endpoint.rest.model.UpdateMonitorStudentLink;
import school.hei.haapi.model.dto.MonitorStudentLinkDto;
import school.hei.haapi.model.dto.MonitorStudentLinkDto.Status;
import school.hei.haapi.service.UserService;

@Component
@AllArgsConstructor
public class MonitorStudentLinkMapper {

  private final UserService userService;
  private final UserMapper userMapper;

  public MonitorStudentLink toRest(MonitorStudentLinkDto dto) {
    return new MonitorStudentLink()
        .id(dto.id())
        .monitor(userMapper.toRestMonitor(userService.getById(dto.monitorId())))
        .student(userMapper.toRestStudent(userService.getById(dto.studentId())));
  }

  public MonitorStudentLinkDto toDto(UpdateMonitorStudentLink monitorStudentLink) {
    return new MonitorStudentLinkDto(
        monitorStudentLink.getId(),
        monitorStudentLink.getMonitorId(),
        monitorStudentLink.getStudentId(),
        Status.valueOf(monitorStudentLink.getStatus().name()));
  }
}
