package school.hei.haapi.endpoint.rest.mapper;

import java.time.Instant;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.Transcript;
import school.hei.haapi.model.User;
import school.hei.haapi.service.UserService;

@Component
@AllArgsConstructor
public class TranscriptMapper {
  private final UserService userService;

  public Transcript toRest(school.hei.haapi.model.Transcript domain) {
    return new Transcript()
      .id(domain.getId())
      .studentId(domain.getStudent().getId())
      .academicYear(domain.getAcademicYear())
      .semester(domain.getSemester())
      .isDefinitive(domain.isDefinitive())
      .creationDatetime(domain.getCreationDatetime());
  }

  public school.hei.haapi.model.Transcript toDomain(Transcript rest) {
    User student = userService.getById(rest.getStudentId());
    return school.hei.haapi.model.Transcript.builder()
      .id(rest.getId())
      .student(student)
      .academicYear(rest.getAcademicYear())
      .semester(rest.getSemester())
      .isDefinitive(rest.getIsDefinitive())
      .creationDatetime(rest.getCreationDatetime())
      .build();
  }
}
