package school.hei.haapi.endpoint.rest.mapper;

import org.springframework.stereotype.Component;

import school.hei.haapi.endpoint.rest.model.MonitorStudentLink;
import school.hei.haapi.endpoint.rest.model.UpdateMonitorStudentLink;
import school.hei.haapi.model.dto.MonitorStudentLinkDto;
import school.hei.haapi.model.dto.MonitorStudentLinkDto.Status;

@Component
public class MonitorStudentLinkMapper {

  public MonitorStudentLink toRest(MonitorStudentLinkDto dto) {
    return new MonitorStudentLink()
        .id(dto.id())
        .monitorId(dto.monitorId())
        .studentId(dto.studentId());
  }

  public MonitorStudentLinkDto toDto(UpdateMonitorStudentLink monitorStudentLink) {
    return new MonitorStudentLinkDto(
        monitorStudentLink.getId(),
        monitorStudentLink.getMonitorId(),
        monitorStudentLink.getStudentId(),
        Status.valueOf(monitorStudentLink.getStatus().name()));
  }
}
