package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.Transcript;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.service.UserService;

import java.util.Objects;

@Component
@AllArgsConstructor
@Slf4j
public class TranscriptMapper {

  private final UserService userService;
  public school.hei.haapi.endpoint.rest.model.Transcript toRest(Transcript transcript) {
    var restTranscript = new school.hei.haapi.endpoint.rest.model.Transcript();
    restTranscript.setId(transcript.getId());
    restTranscript.setStudentId(transcript.getStudent().getId());
    restTranscript.setSemester(transcript.getSemester());
    restTranscript.setAcademicYear(transcript.getAcademicYear());
    restTranscript.setIsDefinitive(transcript.getIsDefinitive());
    restTranscript.setCreationDatetime(transcript.getCreationDatetime());
    return restTranscript;
  }

  public Transcript toDomain(school.hei.haapi.endpoint.rest.model.Transcript restTranscript, String studentId) {
    User student = userService.getById(studentId);
    if (Objects.equals(student, null)) {
      throw new NotFoundException("Student.id=" + studentId + " is not found");
    }
    return Transcript.builder()
        .id(restTranscript.getId())
        .student(student)
        .semester(restTranscript.getSemester())
        .academicYear(restTranscript.getAcademicYear())
        .isDefinitive(restTranscript.getIsDefinitive())
        .creationDatetime(restTranscript.getCreationDatetime())
        .build();
  }
}
