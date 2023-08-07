package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.Transcript;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.service.UserService;

import java.util.Objects;

@Component
@AllArgsConstructor
@Slf4j
public class TranscriptMapper {

    private final UserService userService;

    public Transcript toRest(school.hei.haapi.model.Transcript transcript) {
        return new Transcript()
                .id(transcript.getId())
                .studentId(transcript.getStudent().getId())
                .semester(transcript.getSemester())
                .academicYear(transcript.getAcademicYear())
                .isDefinitive(transcript.getIsDefinitive())
                .creationDatetime(transcript.getCreationDatetime());
    }

    public school.hei.haapi.model.Transcript toDomain(Transcript restTranscript, String studentId) {
        final User student = userService.getById(studentId);
        if (Objects.equals(student, null)) {
            throw new NotFoundException("Student.id=" + studentId + " is not found");
        }
        return school.hei.haapi.model.Transcript.builder()
                .id(restTranscript.getId())
                .student(student)
                .semester(restTranscript.getSemester())
                .academicYear(restTranscript.getAcademicYear())
                .isDefinitive(restTranscript.getIsDefinitive())
                .creationDatetime(restTranscript.getCreationDatetime())
                .build();
    }
}
