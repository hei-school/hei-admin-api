package school.hei.haapi.endpoint.rest.mapper;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CrupdateRemedial;
import school.hei.haapi.endpoint.rest.model.Remedial;
import school.hei.haapi.model.CourseAssignment;
import school.hei.haapi.service.CourseAssignmentService;

@Component
@AllArgsConstructor
public class RemedialMapper {
  private final CourseAssignmentMapper courseAssignmentMapper;
  private final CourseAssignmentService courseAssignmentService;

  public Remedial toRest(school.hei.haapi.model.Remedial remedial) {
    return new Remedial()
        .id(remedial.getId())
        .title(remedial.getTitle())
        .course(courseAssignmentMapper.toRest(remedial.getCourseAssignment()).getCourse())
        .remedialDate(String.valueOf(remedial.getRemedialDate()));
  }

  public school.hei.haapi.model.Remedial toDomain(CrupdateRemedial crupdateRemedial) {
    CourseAssignment courseAssignment =
        courseAssignmentService.getCourseAssignmentById(crupdateRemedial.getCourseId());
    return school.hei.haapi.model.Remedial.builder()
        .id(crupdateRemedial.getId())
        .title(crupdateRemedial.getTitle())
        .remedialDate(crupdateRemedial.getRemedialDate())
        .courseAssignment(courseAssignment)
        .build();
  }

  public List<Remedial> toRestList(List<school.hei.haapi.model.Remedial> allRemedials) {
    return allRemedials.stream().map(this::toRest).toList();
  }
}
