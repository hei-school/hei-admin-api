package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.Transcript;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.service.UserService;

@Component
@AllArgsConstructor
public class TranscriptMapper {
    private UserService userService;
    public Transcript toRest(school.hei.haapi.model.Transcript transcript) {
        return new Transcript()
                .id(transcript.getId())
                .studentId(transcript.getStudent().getId())
                .semester(Transcript.SemesterEnum.fromValue(transcript.getSemester().toString()))
                .academicYear(transcript.getAcademicYear())
                .isDefinitive(transcript.getIsDefinitive())
                .creationDatetime(transcript.getCreationDatetime());
    }

    public school.hei.haapi.model.Transcript toDomain(String studentId, Transcript transcript) {
        User student = userService.getById(transcript.getStudentId());
        if (student == null) {
            throw new NotFoundException("Student.id=" + studentId + " is not found");
        }
        return school.hei.haapi.model.Transcript.builder()
                .id(transcript.getId())
                .student(student)
                .semester(school.hei.haapi.model.Transcript.Semester.valueOf(transcript.getSemester().toString()))
                .academicYear(transcript.getAcademicYear())
                .isDefinitive(transcript.getIsDefinitive())
                .creationDatetime(transcript.getCreationDatetime())
                .build();
    }
}
